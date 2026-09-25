package goblinbob.mobends.tools

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.zip.ZipFile
import kotlin.system.exitProcess

/*
 * Generates the accessors model definitions use to reach vanilla fields, whose names are obfuscated
 * in production:
 *
 *  * goblinbob/mobends/core/vanilla/VanillaModelParts.java   every ModelRenderer / ModelRenderer[]
 *    field of every vanilla model,
 *  * goblinbob/mobends/core/vanilla/VanillaEntityFields.java every numeric field of Entity,
 *    EntityLivingBase and the living entities,
 *  * the generated section of META-INF/accesstransformer.cfg, making the non-public ones readable.
 *
 * The accessors read the fields directly, so the compiler checks every name and reobfuscation renames
 * them for production; no SRG name is looked up at runtime. Fields added by mods are not obfuscated
 * and are found by reflection instead (see DefinedFields).
 *
 * Reads the SRG-named Forge jar (the access flags before this mod's access transformer) and the
 * SRG-to-MCP mappings from ForgeGradle's cache in build/fg_cache, so run the Gradle setup first.
 * Every output names the versions it was generated for (see stamp), which checkVanillaFields compares
 * with gradle.properties before compiling. Run with `gradle generateVanillaFields`.
 */

private const val MODEL_BASE = "net/minecraft/client/model/ModelBase"
private const val MODEL_RENDERER = "Lnet/minecraft/client/model/ModelRenderer;"
private const val ENTITY = "net/minecraft/entity/Entity"
private const val LIVING = "net/minecraft/entity/EntityLivingBase"
private val NUMERIC = setOf("F", "D", "I", "J", "S", "B")

private class ClassInfo(val superName: String?, val fields: List<FieldInfo>)

private class FieldInfo(val access: Int, val name: String, val descriptor: String)

private class Found(val owner: String, val mcp: String, val srg: String, val public: Boolean)
{
    val ownerName get() = owner.replace('/', '.')
    val javaName get() = ownerName.replace('$', '.')
}

/** Arguments: project directory, Minecraft version, Forge version, mappings (`<channel>_<version>`). */
fun main(args: Array<String>)
{
    if (args.size != 4)
    {
        System.err.println("Usage: GenVanillaFields <project dir> <minecraft version> <forge version> <mappings channel_version>")
        exitProcess(2)
    }
    val (root, mc, forge, mappingsName) = args.toList()
    // Keep in sync with checkVanillaFields in build.gradle.
    val stamp = "for $mc-$forge with $mappingsName"
    val project = File(root)
    val cache = File(project, "build/fg_cache")
    val jar = File(cache, "net/minecraftforge/forge/$mc-$forge/forge-$mc-$forge-srg.jar")
    val mappingsFile = File(cache, "de/oceanlabs/mcp/mcp_config").listFiles { f -> f.name.startsWith("$mc-") }
        ?.map { File(it, "srg_to_$mappingsName.tsrg") }
        ?.firstOrNull { it.isFile }
    if (!jar.isFile || mappingsFile == null)
    {
        System.err.println("Missing $jar or the SRG-to-MCP mappings under $cache: run the ForgeGradle setup (e.g. `gradle compileJava`) first.")
        exitProcess(1)
    }

    val mappings = readMappings(mappingsFile)
    val classes = readClasses(jar)

    fun extends(name: String, root: String): Boolean
    {
        var current: String? = name
        while (current != null)
        {
            if (current == root) return true
            current = classes[current]?.superName
        }
        return false
    }

    fun fieldsOf(owner: String, accept: (String) -> Boolean) = classes.getValue(owner).fields
        .filter { it.access and (Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC) == 0 && accept(it.descriptor) }
        .map { Found(owner, mappings[owner]?.get(it.name) ?: it.name, it.name, it.access and Opcodes.ACC_PUBLIC != 0) }

    val parts = mutableListOf<Found>()
    val numbers = mutableListOf<Found>()
    for (owner in classes.keys.sorted())
    {
        if (owner != MODEL_BASE && extends(owner, MODEL_BASE))
            parts += fieldsOf(owner) { it == MODEL_RENDERER || it == "[$MODEL_RENDERER" }
        if (owner == ENTITY || owner == LIVING || extends(owner, LIVING))
            numbers += fieldsOf(owner) { it in NUMERIC }
    }

    val java = File(project, "src/main/java/goblinbob/mobends/core/vanilla").apply { mkdirs() }
    writeJava(File(java, "VanillaModelParts.java"), stamp, "Function<Object, Object>", "m", parts,
        "Every model part (ModelRenderer or ModelRenderer[]) field of the vanilla models, read directly so the names are\n" +
        " * checked at compile time and reobfuscated for production.",
        "java.util.function.Function")
    writeJava(File(java, "VanillaEntityFields.java"), stamp, "ToDoubleFunction<Object>", "e", numbers,
        "Every numeric field of the vanilla living entities (and Entity), read directly so the names are checked at compile\n" +
        " * time and reobfuscated for production.",
        "java.util.function.ToDoubleFunction")
    writeAccessTransformer(File(project, "src/main/resources/META-INF/accesstransformer.cfg"), stamp, parts + numbers)
    println("${parts.size} model parts, ${numbers.size} entity fields, " +
            "${(parts + numbers).count { !it.public }} made public by the access transformer")
}

/** `{class: {srg field: mcp field}}` from a TSRG file (methods carry a descriptor and are skipped). */
private fun readMappings(file: File): Map<String, Map<String, String>>
{
    val fields = mutableMapOf<String, MutableMap<String, String>>()
    var owner: MutableMap<String, String>? = null
    file.forEachLine { line ->
        if (line.isBlank()) return@forEachLine
        val parts = line.trim().split(Regex("\\s+"))
        if (!line.startsWith("\t")) owner = fields.getOrPut(parts[0]) { mutableMapOf() }
        else if (parts.size == 2) owner?.put(parts[0], parts[1])
    }
    return fields
}

private fun readClasses(jar: File): Map<String, ClassInfo>
{
    val classes = mutableMapOf<String, ClassInfo>()
    ZipFile(jar).use { zip ->
        for (entry in zip.entries())
        {
            if (!entry.name.startsWith("net/minecraft/") || !entry.name.endsWith(".class")) continue
            var name = ""
            var superName: String? = null
            val fields = mutableListOf<FieldInfo>()
            ClassReader(zip.getInputStream(entry)).accept(object : ClassVisitor(Opcodes.ASM5)
            {
                override fun visit(version: Int, access: Int, className: String, signature: String?, superClass: String?, interfaces: Array<out String>?)
                {
                    name = className
                    superName = superClass
                }

                override fun visitField(access: Int, fieldName: String, descriptor: String, signature: String?, value: Any?): FieldVisitor?
                {
                    fields += FieldInfo(access, fieldName, descriptor)
                    return null
                }
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            classes[name] = ClassInfo(superName, fields)
        }
    }
    return classes
}

private fun writeJava(file: File, stamp: String, accessor: String, argument: String, fields: List<Found>, doc: String, import: String)
{
    val className = file.nameWithoutExtension
    val cases = fields.joinToString("\n") {
        "            case \"${it.ownerName}#${it.mcp}\": return $argument -> ((${it.javaName}) $argument).${it.mcp};"
    }
    file.writeText("""
        |// Generated by generateVanillaFields (src/tools) $stamp. Do not edit; regenerate instead.
        |package goblinbob.mobends.core.vanilla;
        |
        |import $import;
        |
        |/**
        | * $doc
        | */
        |public final class $className
        |{
        |
        |    private $className()
        |    {
        |    }
        |
        |    /**
        |     * The accessor of the field {@code name} declared by {@code owner} (a binary class name), or null when
        |     * that class declares no such field.
        |     */
        |    public static $accessor get(String owner, String name)
        |    {
        |        switch (owner + '#' + name)
        |        {
        |$cases
        |            default: return null;
        |        }
        |    }
        |
        |}
        |""".trimMargin())
}

private fun writeAccessTransformer(file: File, stamp: String, fields: List<Found>)
{
    val manual = file.readText().split(Regex("# --- generated by .*, do not edit below ---"))[0].trimEnd('\n')
    val already = manual.lines()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .map { it.trim().split(Regex("\\s+")) }
        .filter { it.size >= 3 }
        .map { it[1] to it[2] }
        .toSet()
    val generated = fields
        .filter { !it.public && (it.ownerName to it.srg) !in already }
        .map { "public ${it.ownerName} ${it.srg} # ${it.mcp}" }
    val begin = "# --- generated by generateVanillaFields (src/tools) $stamp, do not edit below ---"
    file.writeText((listOf(manual, "", begin) + generated).joinToString("\n") + "\n")
}

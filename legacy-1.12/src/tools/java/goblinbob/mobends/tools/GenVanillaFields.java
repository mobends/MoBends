package goblinbob.mobends.tools;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Generates the accessors model definitions use to reach vanilla fields, whose names are obfuscated
 * in production:
 *
 * <ul>
 *     <li>goblinbob/mobends/core/vanilla/VanillaModelParts.java: every ModelRenderer / ModelRenderer[]
 *     field of every vanilla model,</li>
 *     <li>goblinbob/mobends/core/vanilla/VanillaEntityFields.java: every numeric field of Entity,
 *     EntityLivingBase and the living entities, and its declared type (a double or a long is a
 *     double to an animator, the others a number),</li>
 *     <li>the generated section of META-INF/accesstransformer.cfg, making the non-public ones readable.</li>
 * </ul>
 *
 * The accessors read the fields directly, so the compiler checks every name and reobfuscation renames
 * them for production; no SRG name is looked up at runtime. Fields added by mods are not obfuscated
 * and are found by reflection instead (see DefinedFields).
 *
 * <p>Reads the SRG-named Forge jar (the access flags before this mod's access transformer) and the
 * SRG-to-MCP mappings from ForgeGradle's cache in build/fg_cache, so run the Gradle setup first.
 * Every output names the versions it was generated for (see the stamp), which checkVanillaFields
 * compares with gradle.properties before compiling. Run with {@code gradle generateVanillaFields}.
 */
public final class GenVanillaFields
{

    private static final String MODEL_BASE = "net/minecraft/client/model/ModelBase";
    private static final String MODEL_RENDERER = "Lnet/minecraft/client/model/ModelRenderer;";
    private static final String ENTITY = "net/minecraft/entity/Entity";
    private static final String LIVING = "net/minecraft/entity/EntityLivingBase";
    private static final Set<String> NUMERIC = new HashSet<>(Arrays.asList("F", "D", "I", "J", "S", "B"));
    /** The Java type each numeric descriptor names. */
    private static final Map<String, String> PRIMITIVES = new HashMap<>();

    static
    {
        PRIMITIVES.put("F", "float");
        PRIMITIVES.put("D", "double");
        PRIMITIVES.put("I", "int");
        PRIMITIVES.put("J", "long");
        PRIMITIVES.put("S", "short");
        PRIMITIVES.put("B", "byte");
    }

    private static final class ClassInfo
    {
        final String superName;
        final List<FieldInfo> fields;

        ClassInfo(String superName, List<FieldInfo> fields)
        {
            this.superName = superName;
            this.fields = fields;
        }
    }

    private static final class FieldInfo
    {
        final int access;
        final String name;
        final String descriptor;

        FieldInfo(int access, String name, String descriptor)
        {
            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
        }
    }

    private static final class Found
    {
        final String owner;
        final String mcp;
        final String srg;
        final String descriptor;
        final boolean isPublic;

        Found(String owner, String mcp, String srg, String descriptor, boolean isPublic)
        {
            this.owner = owner;
            this.mcp = mcp;
            this.srg = srg;
            this.descriptor = descriptor;
            this.isPublic = isPublic;
        }

        String ownerName()
        {
            return owner.replace('/', '.');
        }

        String javaName()
        {
            return ownerName().replace('$', '.');
        }
    }

    private GenVanillaFields()
    {
    }

    /** Arguments: project directory, Minecraft version, Forge version, mappings ({@code <channel>_<version>}). */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 4)
        {
            System.err.println("Usage: GenVanillaFields <project dir> <minecraft version> <forge version> <mappings channel_version>");
            System.exit(2);
        }
        String root = args[0], mc = args[1], forge = args[2], mappingsName = args[3];
        // Keep in sync with checkVanillaFields in build.gradle.
        String stamp = "for " + mc + "-" + forge + " with " + mappingsName;
        File project = new File(root);
        File cache = new File(project, "build/fg_cache");
        File jar = new File(cache, "net/minecraftforge/forge/" + mc + "-" + forge + "/forge-" + mc + "-" + forge + "-srg.jar");
        File mappingsFile = null;
        File[] configs = new File(cache, "de/oceanlabs/mcp/mcp_config").listFiles(f -> f.getName().startsWith(mc + "-"));
        if (configs != null)
        {
            for (File config : configs)
            {
                File candidate = new File(config, "srg_to_" + mappingsName + ".tsrg");
                if (candidate.isFile())
                {
                    mappingsFile = candidate;
                    break;
                }
            }
        }
        if (!jar.isFile() || mappingsFile == null)
        {
            System.err.println("Missing " + jar + " or the SRG-to-MCP mappings under " + cache + ": run the ForgeGradle setup (e.g. `gradle compileJava`) first.");
            System.exit(1);
        }

        Map<String, Map<String, String>> mappings = readMappings(mappingsFile);
        Map<String, ClassInfo> classes = readClasses(jar);

        List<Found> parts = new ArrayList<>();
        List<Found> numbers = new ArrayList<>();
        List<String> owners = new ArrayList<>(classes.keySet());
        Collections.sort(owners);
        for (String owner : owners)
        {
            if (!owner.equals(MODEL_BASE) && extendsClass(classes, owner, MODEL_BASE))
                parts.addAll(fieldsOf(classes, mappings, owner, d -> d.equals(MODEL_RENDERER) || d.equals("[" + MODEL_RENDERER)));
            if (owner.equals(ENTITY) || owner.equals(LIVING) || extendsClass(classes, owner, LIVING))
                numbers.addAll(fieldsOf(classes, mappings, owner, NUMERIC::contains));
        }

        File java = new File(project, "src/main/java/goblinbob/mobends/core/vanilla");
        java.mkdirs();
        writeJava(new File(java, "VanillaModelParts.java"), stamp, "Function<Object, Object>", "m", parts,
                "Every model part (ModelRenderer or ModelRenderer[]) field of the vanilla models, read directly so the names are\n" +
                " * checked at compile time and reobfuscated for production.",
                "java.util.function.Function", false);
        writeJava(new File(java, "VanillaEntityFields.java"), stamp, "ToDoubleFunction<Object>", "e", numbers,
                "Every numeric field of the vanilla living entities (and Entity), read directly so the names are checked at compile\n" +
                " * time and reobfuscated for production.",
                "java.util.function.ToDoubleFunction", true);
        List<Found> all = new ArrayList<>(parts);
        all.addAll(numbers);
        writeAccessTransformer(new File(project, "src/main/resources/META-INF/accesstransformer.cfg"), stamp, all);
        System.out.println(parts.size() + " model parts, " + numbers.size() + " entity fields, "
                + all.stream().filter(f -> !f.isPublic).count() + " made public by the access transformer");
    }

    private static boolean extendsClass(Map<String, ClassInfo> classes, String name, String root)
    {
        String current = name;
        while (current != null)
        {
            if (current.equals(root)) return true;
            ClassInfo info = classes.get(current);
            current = info == null ? null : info.superName;
        }
        return false;
    }

    private static List<Found> fieldsOf(Map<String, ClassInfo> classes, Map<String, Map<String, String>> mappings, String owner, Predicate<String> accept)
    {
        List<Found> found = new ArrayList<>();
        Map<String, String> ownerMappings = mappings.get(owner);
        for (FieldInfo field : classes.get(owner).fields)
        {
            if ((field.access & (Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC)) != 0 || !accept.test(field.descriptor))
                continue;
            String mcp = ownerMappings != null && ownerMappings.containsKey(field.name) ? ownerMappings.get(field.name) : field.name;
            found.add(new Found(owner, mcp, field.name, field.descriptor, (field.access & Opcodes.ACC_PUBLIC) != 0));
        }
        return found;
    }

    /** {@code {class: {srg field: mcp field}}} from a TSRG file (methods carry a descriptor and are skipped). */
    private static Map<String, Map<String, String>> readMappings(File file) throws IOException
    {
        Map<String, Map<String, String>> fields = new HashMap<>();
        Map<String, String> owner = null;
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8))
        {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (!line.startsWith("\t")) owner = fields.computeIfAbsent(parts[0], k -> new HashMap<>());
            else if (parts.length == 2 && owner != null) owner.put(parts[0], parts[1]);
        }
        return fields;
    }

    private static Map<String, ClassInfo> readClasses(File jar) throws IOException
    {
        Map<String, ClassInfo> classes = new HashMap<>();
        try (ZipFile zip = new ZipFile(jar))
        {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().startsWith("net/minecraft/") || !entry.getName().endsWith(".class")) continue;
                String[] name = new String[1];
                String[] superName = new String[1];
                List<FieldInfo> fields = new ArrayList<>();
                try (InputStream stream = zip.getInputStream(entry))
                {
                    new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM5)
                    {
                        @Override
                        public void visit(int version, int access, String className, String signature, String superClass, String[] interfaces)
                        {
                            name[0] = className;
                            superName[0] = superClass;
                        }

                        @Override
                        public FieldVisitor visitField(int access, String fieldName, String descriptor, String signature, Object value)
                        {
                            fields.add(new FieldInfo(access, fieldName, descriptor));
                            return null;
                        }
                    }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
                classes.put(name[0], new ClassInfo(superName[0], fields));
            }
        }
        return classes;
    }

    /** {@code withTypes}: also the declared type of each field ({@code type}), a primitive. */
    private static void writeJava(File file, String stamp, String accessor, String argument, List<Found> fields, String doc, String importName, boolean withTypes) throws IOException
    {
        String className = file.getName().replaceFirst("\\.java$", "");
        StringJoiner cases = new StringJoiner("\n");
        StringJoiner typeCases = new StringJoiner("\n");
        for (Found field : fields)
        {
            cases.add("            case \"" + field.ownerName() + "#" + field.mcp + "\": return " + argument + " -> ((" + field.javaName() + ") " + argument + ")." + field.mcp + ";");
            typeCases.add("            case \"" + field.ownerName() + "#" + field.mcp + "\": return " + PRIMITIVES.get(field.descriptor) + ".class;");
        }
        String types = !withTypes ? "" : "\n"
                + "    /**\n"
                + "     * The declared type of the field {@code name} declared by {@code owner} (a binary class name), or null when\n"
                + "     * that class declares no such field.\n"
                + "     */\n"
                + "    public static Class<?> type(String owner, String name)\n"
                + "    {\n"
                + "        switch (owner + '#' + name)\n"
                + "        {\n"
                + typeCases + "\n"
                + "            default: return null;\n"
                + "        }\n"
                + "    }\n";
        String text = "// Generated by generateVanillaFields (src/tools) " + stamp + ". Do not edit; regenerate instead.\n"
                + "package goblinbob.mobends.core.vanilla;\n"
                + "\n"
                + "import " + importName + ";\n"
                + "\n"
                + "/**\n"
                + " * " + doc + "\n"
                + " */\n"
                + "public final class " + className + "\n"
                + "{\n"
                + "\n"
                + "    private " + className + "()\n"
                + "    {\n"
                + "    }\n"
                + "\n"
                + "    /**\n"
                + "     * The accessor of the field {@code name} declared by {@code owner} (a binary class name), or null when\n"
                + "     * that class declares no such field.\n"
                + "     */\n"
                + "    public static " + accessor + " get(String owner, String name)\n"
                + "    {\n"
                + "        switch (owner + '#' + name)\n"
                + "        {\n"
                + cases + "\n"
                + "            default: return null;\n"
                + "        }\n"
                + "    }\n"
                + types
                + "\n"
                + "}\n";
        Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeAccessTransformer(File file, String stamp, List<Found> fields) throws IOException
    {
        String existing = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String manual = existing.split("# --- generated by .*, do not edit below ---")[0].replaceAll("\n+$", "");
        Set<String> already = new HashSet<>();
        for (String line : manual.split("\n", -1))
        {
            if (line.trim().isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 3) already.add(parts[1] + " " + parts[2]);
        }
        StringJoiner out = new StringJoiner("\n");
        out.add(manual);
        out.add("");
        out.add("# --- generated by generateVanillaFields (src/tools) " + stamp + ", do not edit below ---");
        for (Found field : fields)
        {
            if (!field.isPublic && !already.contains(field.ownerName() + " " + field.srg))
            {
                out.add("public " + field.ownerName() + " " + field.srg + " # " + field.mcp);
            }
        }
        Files.write(file.toPath(), (out + "\n").getBytes(StandardCharsets.UTF_8));
    }

}

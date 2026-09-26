package goblinbob.mobends.core.types;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.PackTrust;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LegacyV2Adapter;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Finds the files of one kind ({@code assets/<namespace>/bends/<folder>/**.json}: types,
 * extensions) in every mod and enabled resource pack. The resource manager of 1.12 can't list a
 * folder, so the packs' folders and zips are read directly. Order: mods, then resource packs from the bottom of the list to the top, then
 * folders on the classpath no pack covered (a development environment's resources); files of one
 * pack by path.
 */
final class TypeFileDiscovery
{


    static final class TypeFile
    {
        /** The pack and the path, for messages. */
        final String source;
        final String json;
        /** False for a file from a resource pack the player enabled (see {@code PackTrust}). */
        final boolean trusted;

        TypeFile(String source, String json, boolean trusted)
        {
            this.trusted = trusted;
            this.source = source;
            this.json = json;
        }
    }

    private TypeFileDiscovery()
    {
    }

    /** The files under {@code bends/<folder>/} of every pack, in the order described above. */
    static List<TypeFile> discover(String folder)
    {
        Pattern pattern = Pattern.compile("assets/[^/]+/bends/" + Pattern.quote(folder) + "/.+\\.json");
        List<TypeFile> files = new ArrayList<>();
        Set<File> visited = new HashSet<>();
        for (IResourcePack pack : packs())
        {
            File root = rootOf(pack);
            if (root == null || !visited.add(canonical(root)))
            {
                continue;
            }
            try
            {
                if (root.isDirectory())
                {
                    readFolder(pack.getPackName(), root, !PackTrust.isUserPack(pack), pattern, files);
                }
                else if (root.isFile())
                {
                    readZip(pack.getPackName(), root, !PackTrust.isUserPack(pack), pattern, files);
                }
            }
            catch (IOException e)
            {
                Core.LOG.log(Level.WARNING, "Could not look for " + folder + " in " + pack.getPackName(), e);
            }
        }
        for (File root : classpathFolders())
        {
            if (!visited.add(canonical(root)))
            {
                continue;
            }
            try
            {
                readFolder("classpath", root, true, pattern, files);
            }
            catch (IOException e)
            {
                Core.LOG.log(Level.WARNING, "Could not look for " + folder + " in " + root, e);
            }
        }
        return files;
    }

    private static List<File> classpathFolders()
    {
        List<File> folders = new ArrayList<>();
        if (Launch.classLoader == null)
        {
            return folders;
        }
        for (URL url : Launch.classLoader.getSources())
        {
            if (!"file".equals(url.getProtocol()))
            {
                continue;
            }
            try
            {
                File file = new File(url.toURI());
                if (file.isDirectory())
                {
                    folders.add(file);
                }
            }
            catch (URISyntaxException | IllegalArgumentException ignored)
            {
            }
        }
        return folders;
    }

    private static File canonical(File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            return file.getAbsoluteFile();
        }
    }

    private static List<IResourcePack> packs()
    {
        Minecraft mc = Minecraft.getMinecraft();
        List<IResourcePack> packs = new ArrayList<>(ReflectionHelper.<List<IResourcePack>, Minecraft>getPrivateValue(
                Minecraft.class, mc, "defaultResourcePacks", "field_110449_ao"));
        for (ResourcePackRepository.Entry entry : mc.getResourcePackRepository().getRepositoryEntries())
        {
            packs.add(entry.getResourcePack());
        }
        return packs;
    }

    @Nullable
    private static File rootOf(IResourcePack pack)
    {
        if (pack instanceof LegacyV2Adapter)
        {
            pack = ReflectionHelper.getPrivateValue(LegacyV2Adapter.class, (LegacyV2Adapter) pack, "pack", "field_191383_a");
        }
        if (pack instanceof AbstractResourcePack)
        {
            return ReflectionHelper.getPrivateValue(AbstractResourcePack.class, (AbstractResourcePack) pack, "resourcePackFile", "field_110597_b");
        }
        return null;
    }

    private static void readFolder(String packName, File root, boolean trusted, Pattern pattern, List<TypeFile> files) throws IOException
    {
        Path rootPath = root.toPath();
        Path assets = rootPath.resolve("assets");
        if (!Files.isDirectory(assets))
        {
            return;
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(assets))
        {
            paths = walk.filter(Files::isRegularFile)
                    .filter(path -> pattern.matcher(relative(rootPath, path)).matches())
                    .sorted()
                    .collect(Collectors.toList());
        }
        for (Path path : paths)
        {
            files.add(new TypeFile(packName + "/" + relative(rootPath, path), new String(Files.readAllBytes(path), StandardCharsets.UTF_8), trusted));
        }
    }

    private static String relative(Path root, Path path)
    {
        return root.relativize(path).toString().replace(File.separatorChar, '/');
    }

    private static void readZip(String packName, File zip, boolean trusted, Pattern pattern, List<TypeFile> files) throws IOException
    {
        try (ZipFile zipFile = new ZipFile(zip))
        {
            List<ZipEntry> entries = new ArrayList<>();
            Enumeration<? extends ZipEntry> it = zipFile.entries();
            while (it.hasMoreElements())
            {
                ZipEntry entry = it.nextElement();
                if (!entry.isDirectory() && pattern.matcher(entry.getName()).matches())
                {
                    entries.add(entry);
                }
            }
            entries.sort((a, b) -> a.getName().compareTo(b.getName()));
            for (ZipEntry entry : entries)
            {
                try (InputStream stream = zipFile.getInputStream(entry))
                {
                    files.add(new TypeFile(packName + "/" + entry.getName(), IOUtils.toString(stream, StandardCharsets.UTF_8), trusted));
                }
            }
        }
    }

}

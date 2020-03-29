package goblinbob.mobends.core.pack;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.flux.Computed;
import goblinbob.mobends.core.flux.Observable;
import goblinbob.mobends.core.flux.ObservableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PackManager
{

    public static final PackManager INSTANCE = new PackManager();

    private File localDirectory;
    private PackCache cache;
    private ThumbnailProvider thumbnailProvider;

    private ObservableMap<String, LocalBendsPack> localPacks = new ObservableMap<>();
    private final Observable<CoreClientConfig> config;
    public final Computed<Collection<IBendsPack>> appliedPacks;

    public PackManager()
    {
        config = new Observable<>();

        appliedPacks = new Computed<>(() -> {
            List<IBendsPack> packs = new LinkedList<>();

            CoreClientConfig config = this.config.getValue();
            if (config == null)
            {
                return packs;
            }

            String[] keys = config.appliedPackKeys.getValue();
            for (String key : keys)
            {
                IBendsPack pack = localPacks.get(key);
                if (pack != null)
                {
                    packs.add(pack);
                }
            }

            return packs;
        });
    }

    public void initialize(CoreClientConfig config)
    {
        localDirectory = new File(Minecraft.getMinecraft().mcDataDir, "bendspacks");
        localDirectory.mkdir();

        cache = new PackCache(new File(localDirectory, "public_cache"));
        thumbnailProvider = new ThumbnailProvider(cache);

        initLocalPacks();

        this.config.next(config);
    }

    public void initLocalPacks()
    {
        localPacks.clear();

        File[] files = localDirectory.listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.getAbsolutePath().endsWith(".bendsmeta"))
            {
                try
                {
                    LocalBendsPack bendsPack = LocalBendsPack.readFromFile(file);
                    localPacks.put(bendsPack.getKey(), bendsPack);
                }
                catch(IOException ex)
                {
                    Core.LOG.severe(String.format("Couldn't load local bends pack: '%s'", file.getName()));
                }
            }
        }
    }

    public Collection<IBendsPack> getAppliedPacks()
    {
        return appliedPacks.getValue();
    }

    public Collection<LocalBendsPack> getLocalPacks()
    {
        return localPacks.values();
    }

    public ResourceLocation getThumbnailLocation(String packName, String thumbnailUrl)
    {
        return this.thumbnailProvider.getThumbnailLocation(packName, thumbnailUrl);
    }

    public File getLocalDirectory()
    {
        return localDirectory;
    }

    public File getMetaFileForPack(String filename) throws IOException
    {
        File packFile = new File(localDirectory, filename + ".bendsmeta");
        packFile.createNewFile();
        return packFile;
    }

    public File getDataFileForPack(String filename) throws IOException
    {
        File packFile = new File(localDirectory, filename + ".bends");
        packFile.createNewFile();
        return packFile;
    }

}

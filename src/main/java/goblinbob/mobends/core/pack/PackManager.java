package goblinbob.mobends.core.pack;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.flux.ObservableMap;
import goblinbob.mobends.core.util.ErrorReporter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PackManager
{

    public static final PackManager INSTANCE = new PackManager();

    private File localDirectory;
    private PackCache cache;
    private ThumbnailProvider thumbnailProvider;

    private ObservableMap<String, LocalBendsPack> localPacks = new ObservableMap<>();

    private CoreClientConfig config;
    private final List<IBendsPack> appliedPacks;

    public PackManager()
    {
        appliedPacks = new LinkedList<>();
    }

    public void initialize(CoreClientConfig config)
    {
        localDirectory = new File(Minecraft.getMinecraft().mcDataDir, "bendspacks");
        localDirectory.mkdir();

        cache = new PackCache(new File(localDirectory, "public_cache"));
        thumbnailProvider = new ThumbnailProvider(cache);

        this.config = config;
        try
        {
            initLocalPacks();
        }
        catch (InvalidPackFormatException e)
        {
            // Some of the packs were in an invalid format.
            e.printStackTrace();
            ErrorReporter.showErrorToPlayer(e);
        }
    }

    public void initLocalPacks() throws InvalidPackFormatException
    {
        localPacks.clear();
        appliedPacks.clear();

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

        // Re-adding the applied packs.
        for (String key : config.appliedPackKeys)
        {
            IBendsPack pack = localPacks.get(key);
            if (pack != null)
            {
                appliedPacks.add(pack);
            }
        }

        try
        {
            PackDataProvider.INSTANCE.createBendsPackData(appliedPacks);
        }
        catch(InvalidPackFormatException ex)
        {
            resetAppliedPacks(true);
            throw ex;
        }
    }

    public void setAppliedPacks(Collection<String> packKeys, boolean saveToConfig) throws InvalidPackFormatException
    {
        List<IBendsPack> newAppliedPacks = new LinkedList<>();

        for (String key : packKeys)
        {
            IBendsPack pack = localPacks.get(key);
            if (pack != null)
            {
                newAppliedPacks.add(pack);
            }
        }

        PackDataProvider.INSTANCE.createBendsPackData(newAppliedPacks);

        this.appliedPacks.clear();
        this.appliedPacks.addAll(newAppliedPacks);

        if (saveToConfig)
        {
            config.setAppliedPacks(packKeys);
        }
    }

    public void resetAppliedPacks(boolean saveToConfig)
    {
        appliedPacks.clear();

        if (saveToConfig)
        {
            config.setAppliedPacks(new String[] {});
        }

        try
        {
            PackDataProvider.INSTANCE.createBendsPackData(appliedPacks);
        }
        catch(InvalidPackFormatException ignored)
        {
            // Never should happen.
        }
    }

    public Collection<IBendsPack> getAppliedPacks()
    {
        return appliedPacks;
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

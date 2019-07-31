package net.gobbob.mobends.core.pack;

import net.gobbob.mobends.core.configuration.CoreConfig;
import net.gobbob.mobends.core.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class PackManager
{

    public static final PackManager instance = new PackManager();
    private static final String PUBLIC_DATABASE_URL = "https://www.dropbox.com/s/d8v028x6mjk6xh3/packDatabase.json?dl=1";

    private File localDirectory;
    private ThumbnailProvider thumbnailProvider;

    private HashMap<String, LocalBendsPack> localPacks = new HashMap<>();
    private HashMap<String, PublicBendsPack> publicPacks = new HashMap<>();

    enum LoadState
    {
        NOT_LOADED,
        LOADED,
        ERROR,
    }

    private LoadState publicDatabaseLoadState;
    private IBendsPack appliedPack;

    public void initialize(CoreConfig config)
    {
        this.localDirectory = new File(Minecraft.getMinecraft().mcDataDir, "bendspacks");
        this.localDirectory.mkdir();

        File cacheDirectory = new File(localDirectory, "public_cache");
        cacheDirectory.mkdir();
        this.thumbnailProvider = new ThumbnailProvider(cacheDirectory);

        this.updatePublicDatabase();

        try
        {
            initLocalPacks();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String savedPackName = config.getCurrentPack();
        if (localPacks.containsKey(savedPackName))
            choose(localPacks.get(savedPackName));
        else if (publicPacks.containsKey(savedPackName))
            choose(publicPacks.get(savedPackName));
    }

    public void initLocalPacks() throws IOException
    {
        localPacks.clear();

        File[] files = localDirectory.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.getAbsolutePath().endsWith(".bends"))
                {
                    LocalBendsPack bendsPack = LocalBendsPack.readFromFile(file);
                    if (bendsPack != null)
                    {
                        this.localPacks.put(bendsPack.getName(), bendsPack);
                    }
                }
            }
        }
    }

    public boolean addLocal(LocalBendsPack newPack)
    {
        if (!localPacks.containsKey(newPack.getName()))
        {
            localPacks.put(newPack.getName(), newPack);
            return true;
        }
        return false;
    }

    private boolean addPublic(PublicBendsPack newPack)
    {
        if (!publicPacks.containsKey(newPack.getName()))
        {
            publicPacks.put(newPack.getName(), newPack);
            return true;
        }
        return false;
    }

    public void choose(IBendsPack newPack)
    {
        appliedPack = newPack;
        if (appliedPack != null)
            appliedPack.apply();
    }

    private String[] downloadPublicDatabase()
    {
        try
        {
            URL publicDirectoryUrl = new URL(PUBLIC_DATABASE_URL);
            BufferedReader in = new BufferedReader(new InputStreamReader(publicDirectoryUrl.openStream()));

            List<String> lines = new ArrayList<>();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                lines.add(inputLine);
            in.close();

            return lines.toArray(new String[0]);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void parsePublicDatabase(PublicDatabase database)
    {
        publicPacks.clear();
        publicDatabaseLoadState = LoadState.NOT_LOADED;

        for (PublicDatabase.PackEntry entry : database.packs)
        {
            try
            {
                addPublic(PublicBendsPack.createPublicPack(entry));
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }
        publicDatabaseLoadState = LoadState.LOADED;
    }

    public void updatePublicDatabase()
    {
        PublicDatabase database = PublicDatabase.downloadPublicDatabase(PUBLIC_DATABASE_URL);
        if (database != null)
        {
            parsePublicDatabase(database);
        }
    }

    public void renamePack(String originalName, String name)
    {
        if (!localPacks.containsKey(originalName))
            return;

        LocalBendsPack pack = localPacks.get(originalName);

        if (pack.getName().equalsIgnoreCase(name) || !pack.canPackBeEdited())
            return;

        pack.rename(name);

        File packFile = new File(localDirectory, originalName);
        if (packFile.exists())
        {
            // Renaming the file.
            File newPackFile = new File(localDirectory, name);
            try
            {
                final BufferedWriter os = new BufferedWriter(new FileWriter(newPackFile));
                os.write(GUtil.readFile(packFile));
                os.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        localPacks.remove(originalName);
        localPacks.put(name, pack);
    }

    public IBendsPack getAppliedPack()
    {
        return appliedPack;
    }

    public boolean arePublicPacksLoaded()
    {
        return publicDatabaseLoadState == LoadState.LOADED;
    }

    public PublicBendsPack getPublic(String name)
    {
        return publicPacks.get(name);
    }

    public LocalBendsPack getLocal(String name)
    {
        return localPacks.get(name);
    }

    public Collection<PublicBendsPack> getPublicPacks()
    {
        return publicPacks.values();
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

    public File getFileForPack(String filename) throws IOException
    {
        File packFile = new File(localDirectory, filename);
        packFile.createNewFile();
        return packFile;
    }

}

package goblinbob.mobends.core.pack;

import java.io.File;

public class PackCache
{

    private final File cacheDirectory;

    public PackCache(File cacheDirectory)
    {
        this.cacheDirectory = cacheDirectory;
        this.cacheDirectory.mkdir();
    }

    public File getThumbnailFile(String packName)
    {
        return new File(cacheDirectory, packName + ".png");
    }

}

package goblinbob.mobends.core.connection;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AssetReloadListener implements IResourceManagerReloadListener
{
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final String directory;

    public AssetReloadListener(String directory)
    {
        this.directory = directory;
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {

    }
}

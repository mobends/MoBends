package goblinbob.mobends.forge;

import com.google.gson.Gson;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.FilePack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BinaryResourceManager extends ReloadListener<Collection<AnimatorTemplate>>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final String directory;
    private final Gson gson = new Gson();
    private final String suffix;
    private final int suffixLength;

    public BinaryResourceManager(String directory, String suffix)
    {
        this.directory = directory;
        this.suffix = suffix;
        this.suffixLength = suffix.length();
    }

    protected Collection<AnimatorTemplate> prepare(IResourceManager resourceManager, IProfiler profiler)
    {
        List<AnimatorTemplate> animatorTemplates = new ArrayList<>();
        int directoryPathLength = this.directory.length() + 1;

        List<IResourcePack> resourcePacks = resourceManager.listPacks().collect(Collectors.toList());

        // Listing all resource locations inside the armor states directory.
        Collection<ResourceLocation> resourceLocations = resourceManager.listResources(this.directory, path -> path.endsWith(suffix));

        System.out.println(resourceLocations);

        for (IResourcePack pack : resourcePacks)
        {
            if (pack instanceof FilePack)
            {
                // This pack is outside of the regular mod set.
            }
            else if()
            {

            }
        }

//        for (ResourceLocation resourcelocation : resourceLocations)
//        {
//            String resourcePath = resourcelocation.getPath();
//            ResourceLocation relativeLocation = new ResourceLocation(resourcelocation.getNamespace(), resourcePath.substring(directoryPathLength, resourcePath.length() - suffixLength));
//
//            List<IResource> resources = null;
//            try
//            {
//                resources = resourceManager.getResources(resourcelocation);
//            }
//            catch (IOException e)
//            {
//                LOGGER.error("Couldn't parse armor state file {} from {}", relativeLocation, resourcelocation, e);
//                continue;
//            }
//
//            for (IResource resource : resources)
//            {
//                try (
//                        InputStream inputstream = resource.getInputStream();
//                        Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
//                )
//                {
//                    ArmorStateManifest armorState = JSONUtils.fromJson(this.gson, reader, ArmorStateManifest.class);
//                    if (armorState != null)
//                    {
//                        armorStateEntries.add(new ArmorStateEntry(relativeLocation, armorState));
//                    }
//                    else
//                    {
//                        LOGGER.error("Couldn't load armor state file {} from {} as it's null or empty", relativeLocation, resourcelocation);
//                    }
//                }
//                catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception)
//                {
//                    LOGGER.error("Couldn't parse armor state file {} from {}", relativeLocation, resourcelocation, jsonparseexception);
//                }
//            }
//        }

        return animatorTemplates;
    }

    @Override
    protected void apply(Collection<AnimatorTemplate> states, IResourceManager resourceManager, IProfiler profiler)
    {
    }
}
package goblinbob.mobends.core.definition;

import com.google.gson.Gson;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GsonResources;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads the model definitions listed in {@code bends/models/index.json}. */
public class ModelDefinitions
{

    public static final ModelDefinitions INSTANCE = new ModelDefinitions();

    private static final Gson GSON = new Gson();

    public static class Index
    {
        public List<String> models = new ArrayList<>();
    }

    private final Map<ResourceLocation, EntityModelDefinition> loaded = new LinkedHashMap<>();

    public void clearCache()
    {
        loaded.clear();
    }

    public static ResourceLocation locationOf(String modId, String name)
    {
        return new ResourceLocation(modId, "bends/models/" + name + ".json");
    }

    public List<String> index(String modId) throws IOException
    {
        Index index = GsonResources.read(new ResourceLocation(modId, "bends/models/index.json"), GSON, Index.class);
        return index.models == null ? new ArrayList<>() : index.models;
    }

    public EntityModelDefinition load(String modId, String name) throws IOException, MalformedKumoTemplateException
    {
        return load(locationOf(modId, name));
    }

    /** Loads the definition at {@code location}, e.g. {@code yourmod:bends/models/beast.json}. */
    public EntityModelDefinition load(ResourceLocation location) throws IOException, MalformedKumoTemplateException
    {
        EntityModelDefinition definition = loaded.get(location);
        if (definition == null)
        {
            definition = GsonResources.read(location, GSON, EntityModelDefinition.class);
            definition.validate();
            loaded.put(location, definition);
        }
        return definition;
    }

}

package goblinbob.mobends.core.definition;

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

    public static class Index
    {
        public List<String> models = new ArrayList<>();
    }

    private final Map<String, EntityModelDefinition> loaded = new LinkedHashMap<>();

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
        Index index = GsonResources.get(new ResourceLocation(modId, "bends/models/index.json"), Index.class);
        return index == null || index.models == null ? new ArrayList<>() : index.models;
    }

    public EntityModelDefinition load(String modId, String name) throws IOException, MalformedKumoTemplateException
    {
        String key = modId + ":" + name;
        EntityModelDefinition definition = loaded.get(key);
        if (definition == null)
        {
            definition = GsonResources.get(locationOf(modId, name), EntityModelDefinition.class);
            if (definition == null)
            {
                throw new IOException("Missing model definition " + key);
            }
            definition.validate();
            loaded.put(key, definition);
        }
        return definition;
    }

}

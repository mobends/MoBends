package goblinbob.mobends.core.definition;

import com.google.gson.Gson;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GsonResources;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads model definitions ({@code <namespace>:bends/models/<name>.json}), which type files name as their model. */
public class ModelDefinitions
{

    public static final ModelDefinitions INSTANCE = new ModelDefinitions();

    private static final Gson GSON = new Gson();

    private final Map<ResourceLocation, EntityModelDefinition> loaded = new LinkedHashMap<>();

    public void clearCache()
    {
        loaded.clear();
    }

    public static ResourceLocation locationOf(String modId, String name)
    {
        return new ResourceLocation(modId, "bends/models/" + name + ".json");
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
            definition = GsonResources.read(location, GSON, EntityModelDefinition.class, "model definition", EntityModelDefinition.FORMAT_VERSION);
            definition.validate();
            loaded.put(location, definition);
        }
        return definition;
    }

}

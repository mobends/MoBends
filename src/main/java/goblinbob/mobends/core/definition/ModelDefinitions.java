package goblinbob.mobends.core.definition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.client.PackTrust;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.serializer.ScopeSerializers;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;
import goblinbob.mobends.core.kumo.state.template.StatementTemplate;
import goblinbob.mobends.core.util.FormatVersion;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Loads model definitions ({@code <namespace>:bends/models/<name>.json}) through the resource manager. */
public class ModelDefinitions {
    public static final ModelDefinitions INSTANCE = new ModelDefinitions();

    // The entity scope ("@define", "@on") is read as in an animator.
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DefinitionTemplate.class, new ScopeSerializers.Definition())
            .registerTypeAdapter(OnTemplate.class, new ScopeSerializers.On())
            .registerTypeAdapter(StatementTemplate.class, new ScopeSerializers.Statement())
            .registerTypeAdapter(ExpressionTemplate.class, new ExpressionTemplate.Deserializer())
            .create();

    private final Map<ResourceLocation, EntityModelDefinition> loaded = new HashMap<>();

    public void clearCache() {
        loaded.clear();
    }

    public static ResourceLocation locationOf(String modId, String name) {
        return Ids.of(modId, "bends/models/" + name + ".json");
    }

    /** Loads the definition at {@code location}, e.g. {@code yourmod:bends/models/beast.json}. */
    public EntityModelDefinition load(ResourceLocation location) throws IOException, MalformedKumoTemplateException {
        EntityModelDefinition definition = loaded.get(location);
        if (definition == null) {
            Set<ResourceLocation> visiting = new HashSet<>();
            visiting.add(location);
            try {
                definition = GSON.fromJson(resolve(read(location), visiting), EntityModelDefinition.class);
            } catch (JsonParseException | IllegalStateException e) {
                throw new IOException(location + ": " + e.getMessage(), e);
            }
            definition.validate();
            definition.location = location.toString();
            definition.trusted = PackTrust.opensTrusted(location);
            loaded.put(location, definition);
        }
        return definition;
    }

    /**
     * Reads a definition from {@code json} (a model definition file's content), validated; one that
     * {@code extends} another loads that one from the resources. The animation lab's tests read
     * definitions so; the mod {@link #load}s them.
     */
    public static EntityModelDefinition parse(JsonElement json) throws MalformedKumoTemplateException {
        JsonObject resolved;
        try {
            resolved = resolve(FormatVersion.check(json, "model definition", EntityModelDefinition.FORMAT_VERSION), new HashSet<>());
        } catch (IOException | JsonParseException e) {
            throw new MalformedKumoTemplateException(e.getMessage());
        }
        EntityModelDefinition definition = GSON.fromJson(resolved, EntityModelDefinition.class);
        definition.validate();
        return definition;
    }

    private static JsonObject read(ResourceLocation location) throws IOException {
        JsonElement json = GsonResources.read(location, GSON, JsonElement.class);
        try {
            return FormatVersion.check(json, "model definition", EntityModelDefinition.FORMAT_VERSION);
        } catch (JsonParseException e) {
            throw new IOException(location + ": " + e.getMessage(), e);
        }
    }

    /**
     * The definition's JSON, with what it {@code extends} merged in, then its {@code @variants} that
     * hold (see {@link DefinitionMerge}): a parent's variants are merged into the parent first, so
     * a child's own, and its variants, override them.
     */
    private static JsonObject resolve(JsonObject json, Set<ResourceLocation> visiting) throws IOException, MalformedKumoTemplateException {
        if (!json.has("extends")) {
            return DefinitionMerge.applyVariants(json);
        }
        ResourceLocation parentLocation = Ids.parse(json.get("extends").getAsString());
        if (!visiting.add(parentLocation)) {
            throw new MalformedKumoTemplateException("The model definition " + parentLocation + " extends itself, through " + visiting + ".");
        }
        return DefinitionMerge.applyVariants(DefinitionMerge.merge(resolve(read(parentLocation), visiting), json));
    }
}

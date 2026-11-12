package goblinbob.mobends.core.definition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** A model definition's {@code entity}: one registry id, or a list of them ({@link EntityModelDefinition#entity}). */
final class EntityNamesDeserializer implements JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        List<String> names = new ArrayList<>();
        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                names.add(name(element));
            }
        } else {
            names.add(name(json));
        }
        return names;
    }

    private static String name(JsonElement json) {
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("An 'entity' is a registry id, or a list of them, not " + json + ".");
        }
        return json.getAsString();
    }
}

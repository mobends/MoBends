package goblinbob.mobends.test.geometry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.definition.DefinitionMerge;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;

import java.io.FileReader;
import java.io.IOException;

/** Reads the mod's model definitions from the source tree, resolving {@code extends} there (there is no game to load resources). */
final class TestDefinitions
{

    private TestDefinitions()
    {
    }

    static EntityModelDefinition read(String name) throws Exception
    {
        return ModelDefinitions.parse(resolve("src/main/resources/assets/mobends/bends/models/" + name + ".json"));
    }

    private static JsonObject resolve(String path) throws Exception
    {
        JsonObject json;
        try (FileReader reader = new FileReader(path))
        {
            json = new JsonParser().parse(reader).getAsJsonObject();
        }
        if (!json.has("extends"))
        {
            return json;
        }
        String parent = json.get("extends").getAsString();
        if (!parent.startsWith("mobends:"))
        {
            throw new IOException("Only the mod's own definitions can be read here: " + parent);
        }
        return DefinitionMerge.merge(resolve("src/main/resources/assets/mobends/" + parent.substring("mobends:".length())), json);
    }

}

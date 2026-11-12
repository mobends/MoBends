package goblinbob.mobends.lab;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A model definition's {@code entity}: one registry id or a list ({@link EntityModelDefinition#entity}),
 * and the key made from the first ({@link EntityModelDefinition#defaultKey()}).
 */
class EntityNamesTest
{
    private static EntityModelDefinition definition(String entity) throws MalformedKumoTemplateException
    {
        JsonElement json = JsonParser.parseString("{\"formatVersion\": 2, \"entity\": " + entity
                + ", \"animator\": \"mobends:bends/animators/cow.json\", \"bones\": [{\"name\": \"body\", \"vanilla\": {\"part\": \"body\"}}]}");
        return ModelDefinitions.parse(json);
    }

    @Test
    void oneNameIsAListOfOne() throws Exception
    {
        assertEquals(List.of("minecraft:fox"), definition("\"minecraft:fox\"").entity);
    }

    @Test
    void aListKeepsItsOrder() throws Exception
    {
        assertEquals(List.of("minecraft:zombie", "minecraft:husk", "mymod:my_mob"),
                definition("[\"minecraft:zombie\", \"minecraft:husk\", \"mymod:my_mob\"]").entity);
    }

    @Test
    void classNamesAreRefused()
    {
        assertThrows(MalformedKumoTemplateException.class, () -> definition("\"net.minecraft.entity.passive.EntityCow\""));
        assertThrows(MalformedKumoTemplateException.class, () -> definition("[\"minecraft:cow\", \"com.example.Mob\"]"));
    }

    @Test
    void theKeyComesFromTheFirstEntity() throws Exception
    {
        assertEquals("fox", definition("[\"minecraft:fox\", \"minecraft:wolf\"]").defaultKey());
        assertEquals("mymod/my_mob", definition("\"mymod:my_mob\"").defaultKey());
        // 1.12's id, as written, as 1.12 named it (not today's zombified_piglin).
        assertEquals("zombie_pigman", definition("\"minecraft:zombie_pigman\"").defaultKey());
    }

    @Test
    void badEntitiesAreRefused()
    {
        assertThrows(MalformedKumoTemplateException.class, () -> definition("[]"));
        assertThrows(MalformedKumoTemplateException.class, () -> definition("\"\""));
        assertThrows(MalformedKumoTemplateException.class, () -> definition("\"Minecraft:Fox\""));
        // Not names at all: the JSON is refused as it's read.
        assertThrows(JsonParseException.class, () -> definition("[\"minecraft:fox\", 3]"));
        assertThrows(JsonParseException.class, () -> definition("{\"id\": \"minecraft:fox\"}"));
    }
}

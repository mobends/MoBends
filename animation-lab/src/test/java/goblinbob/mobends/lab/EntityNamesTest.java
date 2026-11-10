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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A model definition's {@code entity}: one name or a list, each a registry id or a class name
 * ({@link EntityModelDefinition#entity}), and the key made from the first
 * ({@link EntityModelDefinition#defaultKey()}).
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
        assertEquals(List.of("net.minecraft.entity.passive.EntityCow"), definition("\"net.minecraft.entity.passive.EntityCow\"").entity);
    }

    @Test
    void aListKeepsItsOrder() throws Exception
    {
        assertEquals(List.of("minecraft:zombie", "minecraft:husk", "com.example.Mob"),
                definition("[\"minecraft:zombie\", \"minecraft:husk\", \"com.example.Mob\"]").entity);
    }

    @Test
    void idsAreToldFromClassNames()
    {
        assertTrue(EntityModelDefinition.isEntityId("minecraft:fox"));
        assertTrue(EntityModelDefinition.isEntityId("mymod:my_mob"));
        assertFalse(EntityModelDefinition.isEntityId("net.minecraft.entity.passive.EntityCow"));
    }

    @Test
    void theKeyComesFromTheFirstEntity() throws Exception
    {
        assertEquals("fox", definition("[\"minecraft:fox\", \"minecraft:wolf\"]").defaultKey());
        assertEquals("mymod/my_mob", definition("\"mymod:my_mob\"").defaultKey());
        // A 1.12 class keeps 1.12's id, as 1.12 named it.
        assertEquals("zombie_pigman", definition("\"net.minecraft.entity.monster.EntityPigZombie\"").defaultKey());
        // Another mod's class has no id: its definition needs a key.
        assertNull(definition("\"com.example.Mob\"").defaultKey());
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

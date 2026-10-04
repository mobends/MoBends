package goblinbob.mobends.lab;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.definition.DefinitionMerge;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** A model definition that {@code extends} another: that one, with its own on top. */
public class DefinitionExtendsTest
{

    private static JsonObject json(String text)
    {
        return new JsonParser().parse(text.replace('\'', '"')).getAsJsonObject();
    }

    private static final JsonObject PARENT = json("{'formatVersion': 2, 'entity': 'a.B', 'animator': 'a:b.json', 'childScale': 0.5,"
            + " 'bones': [{'name': 'body'}, {'name': 'leg', 'pivot': [0, 12, 0]}],"
            + " 'components': {'trail': 'mobends:sword_trail'}, 'renderer': {'sneakOffset': 5},"
            + " '@define': {'phase': {'state': 0}}, '@on': {'update': [{'set': ['entity.phase', 1]}]}}");

    @Test
    void ownBonesReplaceTheirNamesakesAndTheRestComeAfter()
    {
        JsonObject merged = mergeOrFail(json("{'extends': 'x:y.json', 'bones': [{'name': 'leg'}, {'name': 'tail'}], 'animator': 'a:c.json'}"));
        assertEquals("[{\"name\":\"body\"},{\"name\":\"leg\"},{\"name\":\"tail\"}]", merged.get("bones").toString());
        assertEquals("a:c.json", merged.get("animator").getAsString());
        assertEquals(0.5, merged.get("childScale").getAsDouble(), 0);
        assertFalse(merged.has("extends"));
    }

    @Test
    void settingsMergeKeyByKeyAndStatementsRunTheParentsFirst()
    {
        JsonObject merged = mergeOrFail(json("{'renderer': {'flyingSneakOffset': 4}, 'components': {'cape': 'mobends:cape_wave'},"
                + " '@define': {'other': {'state': 0}}, '@on': {'update': [{'set': ['entity.other', 1]}]}}"));
        assertEquals(json("{'sneakOffset': 5, 'flyingSneakOffset': 4}"), merged.get("renderer"));
        assertEquals(2, merged.getAsJsonObject("components").size());
        assertEquals(2, merged.getAsJsonObject("@define").size());
        assertEquals("entity.phase", merged.getAsJsonObject("@on").getAsJsonArray("update").get(0).getAsJsonObject().getAsJsonArray("set").get(0).getAsString());
        assertEquals(2, merged.getAsJsonObject("@on").getAsJsonArray("update").size());
    }

    @Test
    void redeclaringAnInheritedDefinitionIsAnError()
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class,
                () -> DefinitionMerge.merge(PARENT, json("{'@define': {'phase': {'state': 1}}}")));
        assertTrue(e.getMessage().contains("'phase'"), e.getMessage());
    }

    @Test
    void theZombieVillagerIsTheZombieOnItsOwnModel() throws Exception
    {
        EntityModelDefinition zombie = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/zombie.json"));
        EntityModelDefinition villager = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/zombie_villager.json"));
        assertEquals("net.minecraft.client.model.ModelZombieVillager", villager.model);
        assertEquals(zombie.allBoneNames(), villager.allBoneNames());
        assertEquals(zombie.define.keySet(), villager.define.keySet());
        assertEquals(zombie.layers.keySet(), villager.layers.keySet());
        assertTrue(villager.trusted);
    }

    private static JsonObject mergeOrFail(JsonObject child)
    {
        try
        {
            return DefinitionMerge.merge(PARENT, child);
        }
        catch (MalformedKumoTemplateException e)
        {
            throw new AssertionError(e);
        }
    }

}

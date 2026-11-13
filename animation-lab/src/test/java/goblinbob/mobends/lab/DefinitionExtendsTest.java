package goblinbob.mobends.lab;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.compat.SharedAssets;
import goblinbob.mobends.core.definition.DefinitionMerge;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.lab.sim.LabBootstrap;
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

    @Test
    void variantsWhoseConditionHoldsAreMergedInOrder() throws Exception
    {
        LabBootstrap.ensure();
        JsonObject applied = DefinitionMerge.applyVariants(json("{'childScale': 0.5, 'renderer': {'sneakOffset': 5}, '@variants': ["
                + " {'@when': {'core:minecraft_version': ['>', '1.8.9']}, 'childScale': 0.25},"
                + " {'@when': {'core:minecraft_version': ['==', '1.8.9']}, 'renderer': {'flyingSneakOffset': 4}}]}"));
        assertFalse(applied.has("@variants"));
        assertEquals(0.5, applied.get("childScale").getAsDouble(), 0);
        assertEquals(json("{'sneakOffset': 5, 'flyingSneakOffset': 4}"), applied.get("renderer"));
    }

    @Test
    void aVariantsConditionHasToBeKnownWhenItLoads()
    {
        LabBootstrap.ensure();
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class,
                () -> DefinitionMerge.applyVariants(json("{'@variants': [{'@when': 'entity.sneaking', 'childScale': 0.25}]}")));
        assertTrue(e.getMessage().contains("'@variants' entry 1"), e.getMessage());
    }

    @Test
    void theWitherSkeletonIsTheSkeletonOfItsOwnEntity() throws Exception
    {
        EntityModelDefinition skeleton = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/skeleton.json"));
        EntityModelDefinition wither = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/wither_skeleton.json"));
        assertEquals(skeleton.model, wither.model);
        assertEquals(skeleton.animator, wither.animator);
        assertEquals(skeleton.allBoneNames(), wither.allBoneNames());
        assertEquals(skeleton.layers.keySet(), wither.layers.keySet());
        assertEquals("net.minecraft.entity.monster.EntityWitherSkeleton", wither.entity);
        // 1.8.9 has no class of its own for it: a skeleton of type 1 (EntityIds names it).
        assertEquals("net.minecraft.entity.monster.EntitySkeleton", SharedAssets.entityClass(wither.entity));
        assertTrue(wither.trusted);
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

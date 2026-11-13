package goblinbob.mobends.lab;

import goblinbob.mobends.core.compat.EntityIds;
import goblinbob.mobends.core.compat.SharedAssets;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreviewVariantsTest
{
    @Test
    void zombieVillagerPreviewSelectsTheVillagerModel() throws Exception
    {
        EntityZombie preview = (EntityZombie) preview("zombie_villager");
        assertTrue(preview.isVillager());
        assertEquals(new ResourceLocation("minecraft:zombie_villager"), EntityIds.of(preview));
    }

    @Test
    void witherSkeletonPreviewSelectsTheWitherSkinAndSize() throws Exception
    {
        EntitySkeleton preview = (EntitySkeleton) preview("wither_skeleton");
        assertEquals(1, preview.getSkeletonType());
        assertEquals(new ResourceLocation("minecraft:wither_skeleton"), EntityIds.of(preview));
    }

    @Test
    void ordinaryPreviewsKeepTheirOriginalVariant() throws Exception
    {
        assertFalse(((EntityZombie) preview("zombie")).isVillager());
        assertEquals(0, ((EntitySkeleton) preview("skeleton")).getSkeletonType());
    }

    private static EntityLivingBase preview(String model) throws Exception
    {
        LabBootstrap.ensure();
        EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(
                new ResourceLocation("mobends", "bends/models/" + model + ".json"));
        EntityLivingBase entity = (EntityLivingBase) Class.forName(SharedAssets.entityClass(definition.entity))
                .getConstructor(World.class).newInstance(new World());
        SharedAssets.preparePreview(definition.entity, entity);
        return entity;
    }
}

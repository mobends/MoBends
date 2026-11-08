package goblinbob.mobends.core;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.core.client.definition.DefinedModel;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.AnimatorResources;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.expr.EntityFields;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.vanilla.VanillaEntityFields;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.MoBendsOperations;
import goblinbob.mobends.standard.kumo.SwordTrailDriver;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;

/**
 * The client side of the mod: registers what the animators, definitions and type files can name
 * and the entities Mo' Bends animates, before any animator loads (1.12's {@code DefaultAddon}). The
 * game reaches it through the mixins (the ticks, the frames, the renders and the resource reloads).
 */
public final class CoreClient {
    private CoreClient() {
    }

    public static void init() {
        // The operations, drivers and built-ins the animators name; an animator naming one that
        // isn't registered fails to load.
        MinecraftKumoOperations.register();
        MoBendsOperations.register();
        DriverRegistry.INSTANCE.register("mobends:cape", CapeDriver::create, CapeDriver.Template.class);
        DriverRegistry.INSTANCE.register("mobends:sword_trail", SwordTrailDriver::create, SwordTrailDriver.Template.class);
        // How the definitions' "field" reads the entity: by the 1.12 names packs write.
        EntityFields.install(new VanillaEntityFields());

        // What the definitions' "layers" can name (see DefinedLayers, BendsLayers).
        DefinedLayers.register(DefinedLayers.ARMOR, HumanoidArmorLayer.class);
        DefinedLayers.register(DefinedLayers.HELD_ITEM, ItemInHandLayer.class);
        DefinedLayers.register(DefinedLayers.CUSTOM_HEAD, CustomHeadLayer.class);
        DefinedLayers.register(DefinedLayers.CAPE, CapeLayer.class);
        DefinedLayers.register(DefinedLayers.ELYTRA, ElytraLayer.class);
        // 1.12's own layer of supporter accessories, which this version doesn't draw yet.
        DefinedLayers.register(DefinedLayers.ACCESSORIES, null);
        // The drowned's outer layer is a copy of its model, which no 1.12 definition can name.
        DefinedLayers.registerImplicit("mobends:drowned_outer_layer", DrownedOuterLayer.class, DefinedLayers.Role.COPY);

        // These mobs are their model definitions (bends/models/), each the default model of its
        // entity class. Mobs described as data (creepers, villagers, ...) come from the type files
        // in bends/types/.
        DefinedEntities.register(MoBends.MOD_ID, "player");
        DefinedEntities.register(MoBends.MOD_ID, "zombie");
        DefinedEntities.register(MoBends.MOD_ID, "skeleton");
        DefinedEntities.register(MoBends.MOD_ID, "zombie_villager");
        DefinedEntities.register(MoBends.MOD_ID, "pig_zombie");
    }

    /**
     * Forgets everything read from the resource packs (definitions, types, extensions, animators,
     * clips, compiled programs, the bent parts built from them) and every entity's data, which is
     * all made again from the new files as the entities are drawn. Runs after every resource reload.
     */
    public static void reloadAnimation() {
        ModelDefinitions.INSTANCE.clearCache();
        DefinedEntities.clearCache();
        DefinedModel.clearCache();
        DefinedLayers.clearCache();
        EntityTypeRegistry.INSTANCE.markStale();
        AnimatorResources.INSTANCE.clearCache();
        KumoAnimatorController.clearCaches();
        EntityDatabase.instance.refresh();
    }
}

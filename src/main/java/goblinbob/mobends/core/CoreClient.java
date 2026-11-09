package goblinbob.mobends.core;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.core.client.definition.DefinedModel;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.data.EntityComponents;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.AnimatorResources;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.expr.EntityFields;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.vanilla.VanillaEntityFields;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import goblinbob.mobends.standard.data.CapeWave;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.MoBendsOperations;
import goblinbob.mobends.standard.kumo.SwordTrailDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsTemplate;
import goblinbob.mobends.standard.kumo.spider.SpiderLegs;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsTemplate;
import goblinbob.mobends.standard.client.renderer.entity.layers.WolfMiscLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;

/**
 * The client side of the mod: registers what the animators, definitions and type files can name
 * and the entities Mo' Bends animates, before any animator loads (1.12's {@code DefaultAddon}). The
 * game reaches it through the mixins (the ticks, the frames, the renders and the resource reloads).
 */
public final class CoreClient {
    private CoreClient() {
    }

    public static void init() {
        // The user's settings, before anything reads them (the options' values, what's animated).
        ClientConfig.INSTANCE.load(MoBends.platform().configDir());
        // The operations, drivers and built-ins the animators name; an animator naming one that
        // isn't registered fails to load.
        MinecraftKumoOperations.register();
        MoBendsOperations.register();
        DriverRegistry.INSTANCE.register("mobends:cape", CapeDriver::create, CapeDriver.Template.class);
        DriverRegistry.INSTANCE.register("mobends:sword_trail", SwordTrailDriver::create, SwordTrailDriver.Template.class);
        DriverRegistry.INSTANCE.register("mobends:spider_idle_legs", SpiderIdleLegsDriver::create, SpiderIdleLegsTemplate.class);
        DriverRegistry.INSTANCE.register("mobends:spider_moving_legs", SpiderMovingLegsDriver::create, SpiderMovingLegsTemplate.class);
        // How the definitions' "field" reads the entity: by the 1.12 names packs write.
        EntityFields.install(new VanillaEntityFields());
        // What the definitions' "components" can name: where a spider's feet are planted, the
        // sword trail, the cape's ripple.
        EntityComponents.register("mobends:spider_legs", SpiderLegs::new);
        EntityComponents.register("mobends:sword_trail", data -> new SwordTrail());
        EntityComponents.register("mobends:cape_wave", data -> new CapeWave(data.getEntity()));

        // What the definitions' "layers" can name (see DefinedLayers, BendsLayers).
        DefinedLayers.register(DefinedLayers.ARMOR, HumanoidArmorLayer.class);
        DefinedLayers.register(DefinedLayers.HELD_ITEM, ItemInHandLayer.class);
        DefinedLayers.register(DefinedLayers.CUSTOM_HEAD, CustomHeadLayer.class);
        DefinedLayers.register(DefinedLayers.CAPE, CapeLayer.class);
        DefinedLayers.register(DefinedLayers.ELYTRA, ElytraLayer.class);
        // 1.12's own layer of supporter accessories, which this version doesn't draw yet.
        DefinedLayers.register(DefinedLayers.ACCESSORIES, null);
        // Mo' Bends' own layer of the wolf's mouth (added to the wolf's renderer by WolfRendererMixin).
        DefinedLayers.register(WolfMiscLayer.KIND, WolfMiscLayer.class);
        // Layers that draw their own copy of the model: drawn bent, as 1.12's AnimatedCopyLayer drew them.
        DefinedLayers.registerCopy("mobends:creeper_charge", CreeperPowerLayer.class);
        DefinedLayers.registerCopy("mobends:sheep_wool", SheepFurLayer.class);
        // The drowned's outer layer, the wolf's armour and the pig's saddle are copies of their
        // model that no 1.12 definition names (the first two are new; the pig only bends with the
        // dev pack), and an unbent copy would show the mob twice.
        DefinedLayers.registerImplicit("mobends:drowned_outer_layer", DrownedOuterLayer.class, DefinedLayers.Role.COPY);
        DefinedLayers.registerImplicit("mobends:wolf_armor_layer", WolfArmorLayer.class, DefinedLayers.Role.COPY);
        DefinedLayers.registerImplicit("mobends:saddle_layer", SaddleLayer.class, DefinedLayers.Role.COPY);

        // These mobs are their model definitions (bends/models/), each the default model of its
        // entity class. Mobs described as data (creepers, villagers, ...) come from the type files
        // in bends/types/.
        DefinedEntities.register(MoBends.MOD_ID, "player");
        DefinedEntities.register(MoBends.MOD_ID, "zombie");
        DefinedEntities.register(MoBends.MOD_ID, "skeleton");
        DefinedEntities.register(MoBends.MOD_ID, "zombie_villager");
        DefinedEntities.register(MoBends.MOD_ID, "pig_zombie");
        DefinedEntities.register(MoBends.MOD_ID, "squid");
        DefinedEntities.register(MoBends.MOD_ID, "spider");
        DefinedEntities.register(MoBends.MOD_ID, "wolf");
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

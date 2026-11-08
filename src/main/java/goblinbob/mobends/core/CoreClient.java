package goblinbob.mobends.core;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.client.definition.DefinedModel;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.AnimatorResources;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.MoBendsOperations;
import goblinbob.mobends.standard.kumo.SwordTrailDriver;

/**
 * The client side of the mod: registers what the animators can name and the entities Mo' Bends
 * animates, before any animator loads. The game reaches it through the mixins (the ticks, the
 * frames, the renders and the resource reloads).
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

        DefinedEntities.register(MoBends.MOD_ID, "player");
    }

    /**
     * Forgets everything read from the resource packs (definitions, animators, clips, compiled
     * programs, the bent parts built from them) and every entity's data, which is all made again from the new files as the entities
     * are drawn. Runs after every resource reload.
     */
    public static void reloadAnimation() {
        ModelDefinitions.INSTANCE.clearCache();
        DefinedEntities.clearCache();
        DefinedModel.clearCache();
        AnimatorResources.INSTANCE.clearCache();
        KumoAnimatorController.clearCaches();
        EntityDatabase.instance.refresh();
    }
}

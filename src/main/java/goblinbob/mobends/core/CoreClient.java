package goblinbob.mobends.core;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.client.AnimationPolicy;
import goblinbob.mobends.core.client.PackTrust;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.core.client.definition.DefinedModel;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.connection.MoBendsAccount;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.kumo.AnimationLimits;
import goblinbob.mobends.core.kumo.AnimatorResources;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.expr.EntityFields;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.vanilla.VanillaEntityFields;
import goblinbob.mobends.standard.DefaultAddon;

/**
 * The client side of the mod: registers what the animators, definitions and type files can name
 * and the entities Mo' Bends animates, its own and the addons', before any animator loads. The
 * game reaches it through the mixins (the ticks, the frames, the renders and the resource reloads).
 */
public final class CoreClient {
    private CoreClient() {
    }

    public static void init() {
        // The user's settings, before anything reads them (the options' values, what's animated).
        ClientConfig.INSTANCE.load(MoBends.platform().configDir());
        // The mobends.com API (docs/api-communication.md): where it is, the signed-in account, the
        // downloaded assets; then, in the background, the join and pings, the accessory catalogue
        // and new assets. Nothing waits for them.
        EnvironmentModule.load(MoBends.platform().configDir());
        MoBendsAccount.init(MoBends.platform().configDir());
        AssetsModule.init(MoBends.platform().configDir());
        ConnectionManager.init(MoBends.platform().modFile());
        SupporterContent.fetchAccessoryDetails();
        AssetsModule.updateInBackground();
        // The server's rules: what resource packs may do to animation, and how far their animation may go.
        MoBends.platform().registerServerRules(AnimationPolicy.INSTANCE::onServerRules);
        AnimationLimits.setProvider(AnimationPolicy.INSTANCE::limits);
        // The engine's operations ("core:"), and how the definitions' "field" reads the entity: by the
        // 1.12 names packs write.
        MinecraftKumoOperations.register();
        EntityFields.install(new VanillaEntityFields());
        // What the animators, definitions and type files can name, and the mobs Mo' Bends animates:
        // Mo' Bends' own (DefaultAddon), then every other mod's addon (docs/addons.md).
        Addons.registerAddon(MoBends.MOD_ID, new DefaultAddon());
        MoBends.platform().findAddons(Addons::registerAddon);
        Addons.onClientCoreCreated();
    }

    /**
     * Forgets everything read from the resource packs (definitions, types, extensions, animators,
     * clips, compiled programs, the bent parts built from them) and every entity's data, which is
     * all made again from the new files as the entities are drawn. Runs after every resource reload.
     */
    public static void reloadAnimation() {
        PackTrust.clearCache();
        AnimationPolicy.INSTANCE.onContentReloaded();
        ModelDefinitions.INSTANCE.clearCache();
        DefinedEntities.clearCache();
        DefinedModel.clearCache();
        DefinedLayers.clearCache();
        EntityTypeRegistry.INSTANCE.markStale();
        AnimatorResources.INSTANCE.clearCache();
        KumoAnimatorController.clearCaches();
        EntityDatabase.instance.refresh();
        Addons.onRefresh();
    }
}

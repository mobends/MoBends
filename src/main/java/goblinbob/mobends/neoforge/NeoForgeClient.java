package goblinbob.mobends.neoforge;

//? if neoforge {
/*import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/^* What only NeoForge's client does: the mod list's Config button opens the Mo' Bends menu. ^/
final class NeoForgeClient {
    private NeoForgeClient() {
    }

    //? if >=26.1 {
    /^// Mo' Bends' reload listener, named, as NeoForge 26.x adds them (NeoForgePlatform#registersReloadListener).
    static void registerReloadListener(net.neoforged.bus.api.IEventBus modEventBus) {
        modEventBus.addListener((net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) -> event.addListener(
            goblinbob.mobends.core.util.Ids.of(goblinbob.mobends.MoBends.MOD_ID, "animation"),
            (net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> goblinbob.mobends.core.CoreClient.reloadAnimation()));
    }
    ^///?}

    static void registerConfigScreen(ModContainer modContainer) {
        // Leaving the menu goes back to the mod list; on the title screen, it says to open it in a world.
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
            (container, parent) -> goblinbob.mobends.core.client.gui.GuiBendsMenu.fromModList(parent));
    }
}
*///?}

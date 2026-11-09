package goblinbob.mobends.neoforge;

//? if neoforge {
/*import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/^* What only NeoForge's client does: the mod list's Config button opens the Mo' Bends menu. ^/
final class NeoForgeClient {
    private NeoForgeClient() {
    }

    static void registerConfigScreen(ModContainer modContainer) {
        // Leaving the menu goes back to the mod list; on the title screen, it says to open it in a world.
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> GuiBendsMenu.fromModList(parent));
    }
}
*///?}

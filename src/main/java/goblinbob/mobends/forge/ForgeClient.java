package goblinbob.mobends.forge;

//? if forge {
/*import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/^* What only Forge's client does: the mod list's Config button opens the Mo' Bends menu. ^/
final class ForgeClient {
    private ForgeClient() {
    }

    static void registerConfigScreen(FMLJavaModLoadingContext context) {
        // Leaving the menu goes back to the mod list; on the title screen, it says to open it in a world.
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> GuiBendsMenu.fromModList(parent)));
    }
}
*///?}

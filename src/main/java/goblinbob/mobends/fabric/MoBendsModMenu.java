package goblinbob.mobends.fabric;

//? if fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu's Config button for Mo' Bends opens the Mo' Bends menu (the {@code modmenu} entrypoint
 * of fabric.mod.json). Mod Menu is optional: without it, this class is never loaded.
 */
public class MoBendsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Leaving the menu goes back to the mod list; on the title screen, it says to open it in a world.
        return goblinbob.mobends.core.client.gui.GuiBendsMenu::fromModList;
    }
}
//?}

package goblinbob.mobends.core.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Map;

/**
 * The key that opens the Mo' Bends menu (G unless the player changes it, in Controls under
 * Mo' Bends). It joins the game's keys as the options are read ({@code OptionsMixin}), the same
 * way on every loader, and is checked with them ({@code MinecraftMixin}).
 */
public final class KeyboardHandler {
    private static final String MAIN_CATEGORY = "key.categories.mobends";
    private static final KeyMapping KEY_MENU = new KeyMapping("key.mobends.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, MAIN_CATEGORY);

    private KeyboardHandler() {
    }

    /** The game's keys with Mo' Bends' added (once), and its category given a place in the Controls screen. */
    public static KeyMapping[] withKeyMappings(KeyMapping[] keyMappings) {
        Map<String, Integer> categories = KeyMappingAccessor.mobends$getCategorySortOrder();
        if (!categories.containsKey(MAIN_CATEGORY)) {
            categories.put(MAIN_CATEGORY, categories.values().stream().max(Integer::compare).orElse(0) + 1);
        }
        if (Arrays.asList(keyMappings).contains(KEY_MENU)) {
            return keyMappings;
        }
        KeyMapping[] added = Arrays.copyOf(keyMappings, keyMappings.length + 1);
        added[keyMappings.length] = KEY_MENU;
        return added;
    }

    /** With the game's keys: opens the menu if its key was pressed. */
    public static void onKeybinds() {
        while (KEY_MENU.consumeClick()) {
            Minecraft.getInstance().setScreen(new GuiBendsMenu());
        }
    }
}

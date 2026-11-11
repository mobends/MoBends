package goblinbob.mobends.core.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

/** The screen the game shows, which 26.x keeps in its GUI rather than in {@code Minecraft}. */
public final class Screens {
    private Screens() {
    }

    /** Shows {@code screen}; null goes back to the game. */
    public static void open(@Nullable Screen screen) {
        //? if >=26.1 {
        /*Minecraft.getInstance().gui.setScreen(screen);
        *///?} else {
        Minecraft.getInstance().setScreen(screen);
        //?}
    }

    /** The screen shown; null in the game. */
    @Nullable
    public static Screen current() {
        //? if >=26.1 {
        /*return Minecraft.getInstance().gui.screen();
        *///?} else {
        return Minecraft.getInstance().screen;
        //?}
    }
}

package net.minecraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import javax.annotation.Nullable;

/** The client: the level it shows, and its player. The lab sets both for each scenario. */
public class Minecraft {
    private static final Minecraft INSTANCE = new Minecraft();

    @Nullable
    public ClientLevel level;
    @Nullable
    public LocalPlayer player;

    public static Minecraft getInstance() {
        return INSTANCE;
    }
}

package net.minecraft.client.player;

import net.minecraft.client.multiplayer.ClientLevel;

/** The client's own player. */
public class LocalPlayer extends AbstractClientPlayer {
    public LocalPlayer(ClientLevel level) {
        super(level, LAB_PROFILE);
    }
}

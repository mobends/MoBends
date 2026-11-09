package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;

/** Another player on a server. */
public class RemotePlayer extends AbstractClientPlayer {
    public RemotePlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }
}

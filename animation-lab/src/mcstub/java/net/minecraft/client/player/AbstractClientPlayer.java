package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/** A player as the client sees it: the client's own ({@link LocalPlayer}) or another ({@link RemotePlayer}). */
public abstract class AbstractClientPlayer extends Player {
    /** The lab's players' profile. */
    public static final GameProfile LAB_PROFILE = new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000ab1e"), "LabPlayer");

    /** Lab: set by the script (the game downloads it). */
    public PlayerSkin skin = new PlayerSkin(PlayerSkin.Model.WIDE);

    protected AbstractClientPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    public PlayerSkin getSkin() {
        return skin;
    }
}

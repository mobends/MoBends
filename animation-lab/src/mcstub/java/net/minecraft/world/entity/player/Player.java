package net.minecraft.world.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Stand-in for the game's Player (see {@link net.minecraft.world.entity.Entity}). */
public class Player extends LivingEntity {
    private final Abilities abilities = new Abilities();
    private final GameProfile gameProfile;
    /** Where the cape hangs from, trailing the player. */
    public double xCloakO, yCloakO, zCloakO;
    public double xCloak, yCloak, zCloak;
    /** How much the view bobs as the player walks. */
    public float oBob, bob;

    public Player(Level level, GameProfile gameProfile) {
        super(EntityType.PLAYER, level);
        this.gameProfile = gameProfile;
    }

    public Abilities getAbilities() {
        return abilities;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    /** The lab's players play in survival. */
    public boolean isCreative() {
        return false;
    }

    public boolean isSpectator() {
        return false;
    }
}

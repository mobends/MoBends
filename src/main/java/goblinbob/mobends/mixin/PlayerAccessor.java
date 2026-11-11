package goblinbob.mobends.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
//? if >=26.1 {
/*import net.minecraft.world.entity.Avatar;
*///?} else {
import net.minecraft.world.entity.player.Player;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Which parts of their skin a player shows, copied onto the player's preview in the Mo' Bends menu. */
//? if >=26.1 {
/*@Mixin(Avatar.class)
*///?} else {
@Mixin(Player.class)
//?}
public interface PlayerAccessor {
    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    static EntityDataAccessor<Byte> mobends$getModeCustomisation() {
        throw new AssertionError();
    }
}

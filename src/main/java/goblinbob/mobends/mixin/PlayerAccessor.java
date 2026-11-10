package goblinbob.mobends.mixin;

// Only the Mo' Bends menu's previews use it, which aren't ported to 26.x yet.
//? if <26.1 {
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Which parts of their skin a player shows, copied onto the player's preview in the Mo' Bends menu. */
@Mixin(Player.class)
public interface PlayerAccessor {
    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    static EntityDataAccessor<Byte> mobends$getModeCustomisation() {
        throw new AssertionError();
    }
}
//?}

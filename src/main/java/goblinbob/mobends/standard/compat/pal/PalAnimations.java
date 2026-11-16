package goblinbob.mobends.standard.compat.pal;

// Player Animation Library's API (26.x). Mo' Bends is built against it, but doesn't need it.
//? if >=26.1 {
/*import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.world.entity.player.Player;

/^*
 * What Player Animation Library plays on a player. Only {@link PalOperations} loads this class, and
 * only when PAL is installed, so nothing of PAL's is looked for without it.
 ^/
final class PalAnimations {
    private PalAnimations() {
    }

    /^* Whether PAL is playing an animation on the player: any of its layers is active. ^/
    static boolean isPlaying(Player player) {
        AvatarAnimManager animations = manager(player);
        return animations != null && animations.isActive();
    }

    /^*
     * Has PAL work out the player's animation for this frame now, which it does only as the player
     * is submitted, after Mo' Bends animated it: the pose the player model is given until then is
     * the last frame's. PAL keeps what it worked out for the frame's time, so its own call
     * then does nothing more.
     ^/
    static void sample(Player player) {
        AvatarAnimManager animations = manager(player);
        if (animations != null && animations.isActive()) {
            animations.handleAnimations(animations.getTickDelta(), false, false);
        }
    }

    private static AvatarAnimManager manager(Player player) {
        try {
            return PlayerAnimationAccess.getPlayerAnimManager(player);
        } catch (IllegalArgumentException e) {
            // A player PAL doesn't animate (one the client doesn't draw).
            return null;
        }
    }
}
*///?}

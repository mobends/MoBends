package goblinbob.mobends.core.data;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Whether another player flies (creative or spectator flight), read from how they move. The client
 * knows {@code capabilities.isFlying} only for its own player: for the others it's always false.
 * <p>
 * A player who can fly and is off the ground flies if they don't fall: gravity takes about 0.08
 * off their vertical speed every tick, flight keeps it steady. The speed is measured over four ticks,
 * as the server sends other players' positions every other tick. It takes a few steady ticks to
 * start flying (a jump is never steady), and many falling ones to stop, so turning round in the air
 * (which slows the player for a few ticks) doesn't make them fall for a moment.
 */
public class RemoteFlight
{

    /**
     * Whether a player flies (creative or spectator flight, not an elytra): their own flag for the
     * client's player, and read from how they move for the others.
     */
    public static boolean isFlying(EntityPlayer player)
    {
        if (player.capabilities.isFlying)
        {
            return true;
        }
        if (!(player instanceof EntityOtherPlayerMP))
        {
            return false;
        }
        LivingEntityData<EntityPlayer> data = EntityDatabase.instance.get(player);
        return data != null && data.getEntity() == player && data.isFlyingRemotely();
    }

    /** Ticks the vertical speed is compared over. */
    private static final int WINDOW = 4;
    /** A change in vertical speed over the window that counts as steady... */
    private static final double STEADY = 0.1;
    /** ...and one that counts as falling (gravity takes about 0.31 over the window). */
    private static final double FALLING = -0.2;
    /**
     * Faster down than flight goes (even a spectator's at full speed): a long fall, which gravity no
     * longer speeds up much.
     */
    private static final double FASTER_THAN_FLIGHT = -2.0;
    private static final int TICKS_TO_START = 4;
    private static final int TICKS_TO_STOP = 12;

    private final double[] motionY = new double[WINDOW];
    private int ticks;
    private int steadyTicks;
    private int fallingTicks;
    private boolean flying;

    /**
     * Moves it on by a game tick.
     *
     * @param canFly   Whether the player's game mode lets them fly (creative or spectator).
     * @param airborne Whether they're off the ground, and not riding, gliding or swimming.
     * @param motionY  How far they moved up this tick.
     */
    public void update(boolean canFly, boolean airborne, double motionY)
    {
        if (!canFly || !airborne)
        {
            ticks = steadyTicks = fallingTicks = 0;
            flying = false;
            return;
        }

        // The speed WINDOW ticks ago, replaced by this one.
        double before = this.motionY[ticks % WINDOW];
        this.motionY[ticks % WINDOW] = motionY;
        ticks++;
        if (ticks <= WINDOW)
        {
            // Not long enough in the air to tell.
            return;
        }

        double change = motionY - before;
        boolean tooFast = motionY < FASTER_THAN_FLIGHT;
        steadyTicks = Math.abs(change) < STEADY && !tooFast ? steadyTicks + 1 : 0;
        fallingTicks = change < FALLING || tooFast ? fallingTicks + 1 : 0;

        if (!flying && steadyTicks >= TICKS_TO_START)
        {
            flying = true;
        }
        else if (flying && fallingTicks >= TICKS_TO_STOP)
        {
            flying = false;
        }
    }

    public boolean isFlying()
    {
        return flying;
    }

}

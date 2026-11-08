package goblinbob.mobends.core.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Plays a creative player's moves as another client sees them: the player moves as vanilla moves
 * them, the server sends their position every other tick, and the client eases its copy towards
 * each position over three ticks (as EntityOtherPlayerMP does).
 */
public class RemoteFlightTest
{

    private static final int UP = 1, NONE = 0, DOWN = -1;

    /** The player, moving as vanilla moves them. */
    private static class Player
    {
        double y = 0, motionY = 0;
        boolean flying;

        /** One tick; flying, holding jump (UP) or sneak (DOWN). */
        void tick(int input)
        {
            if (flying)
            {
                // EntityPlayer#onLivingUpdate adds the input, #travel moves by it and keeps 0.6 of it.
                motionY += input * 0.15;
                y += motionY;
                motionY *= 0.6;
            }
            else
            {
                y += motionY;
                motionY = (motionY - 0.08) * 0.98;
            }
        }
    }

    /** Plays the ticks, returns whether RemoteFlight saw them flying after each. */
    private static List<Boolean> watch(Player player, int[] inputs, boolean[] flying)
    {
        RemoteFlight flight = new RemoteFlight();
        List<Boolean> seen = new ArrayList<>();
        double target = player.y, clientY = player.y;
        int increments = 0;
        for (int tick = 0; tick < inputs.length; tick++)
        {
            if (flying != null)
            {
                player.flying = flying[tick];
            }
            player.tick(inputs[tick]);
            if (tick % 2 == 0)
            {
                target = player.y;
                increments = 3;
            }
            double before = clientY;
            if (increments > 0)
            {
                clientY += (target - clientY) / increments;
                increments--;
            }
            flight.update(true, true, clientY - before);
            seen.add(flight.isFlying());
        }
        return seen;
    }

    private static int[] repeat(int input, int ticks)
    {
        int[] inputs = new int[ticks];
        java.util.Arrays.fill(inputs, input);
        return inputs;
    }

    @Test
    public void hoveringIsFlying()
    {
        Player player = new Player();
        player.flying = true;
        List<Boolean> seen = watch(player, repeat(NONE, 40), null);
        assertTrue(seen.get(39));
    }

    @Test
    public void flyingUpAndDownIsFlying()
    {
        Player player = new Player();
        player.flying = true;
        int[] inputs = new int[200];
        for (int i = 0; i < inputs.length; i++)
        {
            // Hovers, then turns round every few ticks: up, down, still.
            int phase = (i - 10) / 7 % 3;
            inputs[i] = i < 10 ? NONE : phase == 0 ? UP : phase == 1 ? DOWN : NONE;
        }
        List<Boolean> seen = watch(player, inputs, null);
        for (int i = 10; i < seen.size(); i++)
        {
            assertTrue("tick " + i, seen.get(i));
        }
    }

    @Test
    public void jumpIsNotFlying()
    {
        Player player = new Player();
        player.motionY = 0.42;
        // A jump lands after 12 ticks; an airborne check keeps it from going further.
        assertFalse(watch(player, repeat(NONE, 12), null).contains(true));
    }

    @Test
    public void longFallIsNotFlying()
    {
        Player player = new Player();
        assertFalse(watch(player, repeat(NONE, 300), null).contains(true));
    }

    @Test
    public void stopsFlyingWhenFalling()
    {
        Player player = new Player();
        boolean[] flying = new boolean[60];
        java.util.Arrays.fill(flying, 0, 20, true);
        List<Boolean> seen = watch(player, repeat(NONE, 60), flying);
        assertTrue(seen.get(19));
        // Falls for a short while as flying, then not.
        assertEquals(false, seen.get(59));
        assertFalse(seen.subList(40, 60).contains(true));
    }

    @Test
    public void notFlyingOnGroundOrWithoutFlight()
    {
        RemoteFlight flight = new RemoteFlight();
        for (int i = 0; i < 20; i++)
        {
            flight.update(true, true, 0);
        }
        assertTrue(flight.isFlying());
        flight.update(true, false, 0);
        assertFalse(flight.isFlying());

        for (int i = 0; i < 20; i++)
        {
            flight.update(false, true, 0);
        }
        assertFalse(flight.isFlying());
    }

}

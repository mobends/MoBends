package goblinbob.mobends.core.data;

import net.minecraft.entity.player.EntityPlayer;

/**
 * LAB SHIM. The real one reads another player's flight from the entity database. The lab has no
 * other players (EntityOtherPlayerMP), so a player flies exactly when its own flag says so.
 */
public class RemoteFlight
{
    public static boolean isFlying(EntityPlayer player)
    {
        return player.capabilities.isFlying;
    }

    public void update(boolean canFly, boolean airborne, double motionY)
    {
    }

    public boolean isFlying()
    {
        return false;
    }
}

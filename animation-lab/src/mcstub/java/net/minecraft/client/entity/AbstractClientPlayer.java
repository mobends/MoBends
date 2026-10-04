package net.minecraft.client.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class AbstractClientPlayer extends EntityPlayer
{
    /** "default" or "slim". */
    public String skinType = "default";

    public AbstractClientPlayer(World world) { super(world); }

    public String getSkinType() { return skinType; }
}

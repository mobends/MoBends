package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityWolf extends EntityLivingBase
{
    public boolean sitting;
    public float interestedAngle, prevInterestedAngle;
    public float shakeAngle, prevShakeAngle;
    public boolean shaking;
    public float timeWolfIsShaking, prevTimeWolfIsShaking;
    public float tailRotation = (float) Math.PI / 5F;

    public EntityWolf(World world) { super(world); }

    public boolean isSitting() { return sitting; }

    public float getInterestedAngle(float partialTicks)
    {
        return (prevInterestedAngle + (interestedAngle - prevInterestedAngle) * partialTicks) * 0.15F * (float) Math.PI;
    }

    /** Same formula as the game. */
    public float getShakeAngle(float partialTicks, float offset)
    {
        float f = (prevTimeWolfIsShaking + (timeWolfIsShaking - prevTimeWolfIsShaking) * partialTicks + offset) / 1.8F;
        if (f < 0.0F) f = 0.0F;
        else if (f > 1.0F) f = 1.0F;
        return MathHelper.sin(f * (float) Math.PI) * MathHelper.sin(f * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
    }

    public float getTailRotation() { return tailRotation; }
}

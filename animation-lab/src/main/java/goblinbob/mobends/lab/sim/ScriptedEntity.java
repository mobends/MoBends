package goblinbob.mobends.lab.sim;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Applies {@link EntityInputs} to a stub entity once per tick, reproducing the handful of vanilla
 * behaviours the animation code depends on: position integration with gravity and a flat floor,
 * limb swing accumulation, arm swing progress, and the prev/current pairs used for interpolation.
 */
public class ScriptedEntity
{
    private static final double GRAVITY = 0.08D;
    private static final double DRAG = 0.98D;
    private static final double JUMP_IMPULSE = 0.42D;
    private static final int SWING_DURATION = 6;

    public final EntityLivingBase entity;
    public final World world;
    private final EntityLivingBase mount;
    private final net.minecraft.entity.Entity vehicle;

    private int swingTicks = -1;
    private double velocityY = 0;
    private boolean ladderPlaced = false;
    private int waterPlaced = 0;

    public ScriptedEntity(EntityLivingBase entity, World world)
    {
        this.entity = entity;
        this.world = world;
        this.mount = new EntityLivingBase(world);
        this.vehicle = new net.minecraft.entity.Entity(world);
        entity.setLocationAndAngles(0.5D, world.floorY, 0.5D, 0.0F, 0.0F);
    }

    /** Runs one vanilla-style tick with the given inputs. */
    public void tick(EntityInputs in)
    {
        final EntityLivingBase e = entity;

        // Shift current -> previous.
        e.prevPosX = e.posX;
        e.prevPosY = e.posY;
        e.prevPosZ = e.posZ;
        e.prevRotationYaw = e.rotationYaw;
        e.prevRotationPitch = e.rotationPitch;
        e.prevRenderYawOffset = e.renderYawOffset;
        e.prevRotationYawHead = e.rotationYawHead;
        e.prevLimbSwingAmount = e.limbSwingAmount;
        e.prevSwingProgress = e.swingProgress;
        e.ticksExisted++;

        // Orientation.
        e.renderYawOffset = in.bodyYaw;
        e.rotationYaw = in.bodyYaw + in.headYaw;
        e.rotationYawHead = in.bodyYaw + in.headYaw;
        e.rotationPitch = in.headPitch;

        // Horizontal motion along the body yaw (vanilla: yaw 0 faces +Z, +X is to the left).
        float yawRad = in.bodyYaw * 0.017453292F;
        double dx = -MathHelper.sin(yawRad) * in.forwardSpeed + MathHelper.cos(yawRad) * in.strafeSpeed;
        double dz = MathHelper.cos(yawRad) * in.forwardSpeed + MathHelper.sin(yawRad) * in.strafeSpeed;

        // Vertical motion.
        if (in.noGravity)
        {
            velocityY = in.verticalSpeed != null ? in.verticalSpeed : 0;
        }
        else
        {
            boolean grounded = e.posY <= world.floorY + 1e-6;
            if (in.jump && grounded)
            {
                velocityY = JUMP_IMPULSE;
            }
            else if (in.verticalSpeed != null)
            {
                velocityY = in.verticalSpeed;
            }
            else if (!grounded || velocityY > 0)
            {
                velocityY = (velocityY - GRAVITY) * DRAG;
            }
            else
            {
                velocityY = 0;
            }
        }

        e.posX += dx;
        e.posZ += dz;
        e.posY += velocityY;
        if (!in.noGravity && e.posY < world.floorY)
        {
            e.posY = world.floorY;
            velocityY = 0;
        }

        e.motionX = dx;
        e.motionY = velocityY;
        e.motionZ = dz;

        // Limb swing, as in EntityLivingBase.onUpdate.
        float dist = MathHelper.sqrt(dx * dx + dz * dz);
        float target = Math.min(dist * 4.0F, 1.0F);
        e.limbSwingAmount += (target - e.limbSwingAmount) * 0.4F;
        e.limbSwing += e.limbSwingAmount;

        // Arm swing, as in EntityLivingBase.updateArmSwingProgress.
        if (in.attack && swingTicks < 0)
        {
            swingTicks = 0;
        }
        if (swingTicks >= 0)
        {
            swingTicks++;
            e.isSwingInProgress = true;
            e.swingProgress = (float) swingTicks / (float) SWING_DURATION;
            if (swingTicks >= SWING_DURATION)
            {
                swingTicks = -1;
                e.isSwingInProgress = false;
                e.swingProgress = 0.0F;
            }
        }
        else
        {
            e.isSwingInProgress = false;
            e.swingProgress = 0.0F;
        }

        // World features the mod queries (placed once, kept).
        if (in.ladderColumn && !ladderPlaced)
        {
            LabWorlds.placeLadderColumn(world, e);
            ladderPlaced = true;
        }
        if (in.waterHeight > 0 && waterPlaced < in.waterHeight)
        {
            LabWorlds.placeWaterColumn(world, e, in.waterHeight);
            waterPlaced = in.waterHeight;
        }

        // Flags and items.
        e.sprinting = in.sprinting;
        e.sneaking = in.sneaking;
        e.inWater = in.inWater;
        e.onLadder = in.onLadder;
        e.playerSleeping = in.sleeping;
        e.mainHand = in.mainHand;
        e.offHand = in.offHand;
        e.itemInUseCount = in.itemUseCount;
        e.itemInUseMaxCount = in.itemUseMaxCount;
        e.health = in.health;
        e.ridingEntity = in.riding ? (in.ridingLiving ? mount : vehicle) : null;
        if (in.riding)
        {
            mount.prevRenderYawOffset = mount.renderYawOffset;
            mount.renderYawOffset = in.bodyYaw;
        }

        if (e instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) e;
            player.capabilities.isFlying = in.flying;
            player.ticksElytraFlying = in.elytraTicks;
            player.prevChasingPosX = player.chasingPosX;
            player.prevChasingPosY = player.chasingPosY;
            player.prevChasingPosZ = player.chasingPosZ;
            player.chasingPosX += (player.posX - player.chasingPosX) * 0.25D;
            player.chasingPosY += (player.posY - player.chasingPosY) * 0.25D;
            player.chasingPosZ += (player.posZ - player.chasingPosZ) * 0.25D;
            player.prevCameraYaw = player.cameraYaw;
            player.prevDistanceWalkedModified = player.distanceWalkedModified;
            player.distanceWalkedModified += dist * 0.6F;
        }

        if (e instanceof EntityWolf)
        {
            EntityWolf wolf = (EntityWolf) e;
            wolf.sitting = in.wolfSitting;
            wolf.prevInterestedAngle = wolf.interestedAngle;
            wolf.interestedAngle = in.wolfInterested;
            wolf.prevTimeWolfIsShaking = wolf.timeWolfIsShaking;
            wolf.timeWolfIsShaking = in.wolfShaking;
        }

        if (e instanceof EntitySpider)
        {
            ((EntitySpider) e).besideClimbableBlock = in.spiderClimbing;
        }

        if (e instanceof EntitySquid)
        {
            EntitySquid squid = (EntitySquid) e;
            squid.prevSquidRotation = squid.squidRotation;
            squid.squidRotation = in.squidRotation;
        }
    }
}

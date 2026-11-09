package goblinbob.mobends.lab.sim;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.AABB;

/**
 * Applies {@link EntityInputs} to a stub entity once per tick, reproducing the handful of vanilla
 * behaviours the animation code depends on: position integration with gravity, a flat floor and
 * stone blocks to stand on (walking into one stops the entity; it has to jump onto it),
 * the walk animation, arm swing progress, and the previous/current pairs used for interpolation.
 */
public class ScriptedEntity
{
    private static final double GRAVITY = 0.08D;
    private static final double DRAG = 0.98D;
    private static final double JUMP_IMPULSE = 0.42D;
    private static final int SWING_DURATION = 6;

    public final LivingEntity entity;
    public final ClientLevel level;
    /** What the entity rides when it rides something living (a pig), or not (a boat). */
    private final LivingEntity mount;
    private final Entity vehicle;

    private int swingTicks = -1;
    private double velocityY = 0;
    private boolean ladderPlaced = false;
    private int waterPlaced = 0;

    public ScriptedEntity(LivingEntity entity, ClientLevel level)
    {
        this.entity = entity;
        this.level = level;
        this.mount = new Pig(level);
        this.vehicle = new Boat(level);
        level.addEntity(mount);
        level.addEntity(vehicle);
        entity.moveTo(0.5D, level.floorY, 0.5D, 0.0F, 0.0F);
    }

    /** Runs one vanilla-style tick with the given inputs. */
    public void tick(EntityInputs in)
    {
        final LivingEntity e = entity;

        // Shift current -> previous.
        e.xo = e.getX();
        e.yo = e.getY();
        e.zo = e.getZ();
        e.yRotO = e.getYRot();
        e.xRotO = e.getXRot();
        e.yBodyRotO = e.yBodyRot;
        e.yHeadRotO = e.yHeadRot;
        e.oAttackAnim = e.attackAnim;
        e.tickCount++;

        // Orientation.
        e.yBodyRot = in.bodyYaw;
        e.setYRot(in.bodyYaw + in.headYaw);
        e.yHeadRot = in.bodyYaw + in.headYaw;
        e.setXRot(in.headPitch);

        // Horizontal motion along the body yaw (vanilla: yaw 0 faces +Z, +X is to the left).
        float yawRad = in.bodyYaw * 0.017453292F;
        double dx = -Mth.sin(yawRad) * in.forwardSpeed + Mth.cos(yawRad) * in.strafeSpeed;
        double dz = Mth.cos(yawRad) * in.forwardSpeed + Mth.sin(yawRad) * in.strafeSpeed;
        AABB box = e.getBoundingBox();
        AABB moved = box.move(dx, 0, dz);
        if (!level.stoneIn(new AABB(moved.minX, moved.minY + 0.01D, moved.minZ, moved.maxX, moved.maxY, moved.maxZ)).isEmpty())
        {
            dx = 0;
            dz = 0;
        }
        double ground = level.groundUnder(box, e.getY() + 1e-6);

        // Vertical motion.
        if (in.noGravity)
        {
            velocityY = in.verticalSpeed != null ? in.verticalSpeed : 0;
        }
        else
        {
            boolean grounded = e.getY() <= ground + 1e-6;
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

        double y = e.getY() + velocityY;
        e.setPos(e.getX() + dx, y, e.getZ() + dz);
        // What it lands on: the floor, or stone whose top its feet were above.
        ground = level.groundUnder(e.getBoundingBox(), Math.max(e.yo, y) + 1e-6);
        if (!in.noGravity && y < ground)
        {
            e.setPos(e.getX(), ground, e.getZ());
            velocityY = 0;
        }

        // The walk animation, as in LivingEntity.calculateEntityAnimation.
        float dist = (float) Math.sqrt(dx * dx + dz * dz);
        e.walkAnimation.update(Math.min(dist * 4.0F, 1.0F), 0.4F);

        // Arm swing, as in LivingEntity.updateSwingTime.
        if (in.attack && swingTicks < 0)
        {
            swingTicks = 0;
        }
        if (swingTicks >= 0)
        {
            swingTicks++;
            e.swinging = true;
            e.swingTime = swingTicks;
            e.attackAnim = (float) swingTicks / (float) SWING_DURATION;
            if (swingTicks >= SWING_DURATION)
            {
                swingTicks = -1;
                e.swinging = false;
                e.swingTime = 0;
                e.attackAnim = 0.0F;
            }
        }
        else
        {
            e.swinging = false;
            e.swingTime = 0;
            e.attackAnim = 0.0F;
        }

        // World features the mod queries (placed once, kept).
        if (in.ladderColumn && !ladderPlaced)
        {
            LabWorlds.placeLadderColumn(level, e, in.ladderHeight);
            ladderPlaced = true;
        }
        if (in.waterHeight > 0 && waterPlaced < in.waterHeight)
        {
            LabWorlds.placeWaterColumn(level, e, in.waterHeight);
            waterPlaced = in.waterHeight;
        }

        // Flags and items.
        e.setSprinting(in.sprinting);
        e.setPose(in.sleeping ? Pose.SLEEPING : in.sneaking ? Pose.CROUCHING : Pose.STANDING);
        e.setMainArm(in.leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
        e.wasTouchingWater = in.inWater;
        e.onLadder = in.onLadder;
        e.setItemInHand(InteractionHand.MAIN_HAND, in.mainHand);
        e.setItemInHand(InteractionHand.OFF_HAND, in.offHand);
        e.setUsedItemHand(in.usedItemHand);
        e.useItemRemaining = in.useItemRemaining;
        e.ticksUsingItem = in.ticksUsingItem;
        e.setHealth(in.health);
        if (in.riding)
        {
            e.startRiding(in.ridingLiving ? mount : vehicle);
            mount.yBodyRotO = mount.yBodyRot;
            mount.yBodyRot = in.bodyYaw;
        }
        else
        {
            e.stopRiding();
        }

        if (e instanceof Player player)
        {
            player.getAbilities().flying = in.flying;
            player.fallFlyTicks = in.elytraTicks;
            player.xCloakO = player.xCloak;
            player.yCloakO = player.yCloak;
            player.zCloakO = player.zCloak;
            player.xCloak += (player.getX() - player.xCloak) * 0.25D;
            player.yCloak += (player.getY() - player.yCloak) * 0.25D;
            player.zCloak += (player.getZ() - player.zCloak) * 0.25D;
            player.oBob = player.bob;
            player.walkDistO = player.walkDist;
            player.walkDist += dist * 0.6F;
        }

        if (e instanceof Wolf wolf)
        {
            wolf.setInSittingPose(in.wolfSitting);
            wolf.interestedAngleO = wolf.interestedAngle;
            wolf.interestedAngle = in.wolfInterested;
            wolf.shakeAnimO = wolf.shakeAnim;
            wolf.shakeAnim = in.wolfShaking;
        }

        if (e instanceof Spider spider)
        {
            spider.setClimbing(in.spiderClimbing);
        }

        if (e instanceof Squid squid)
        {
            squid.oldTentacleMovement = squid.tentacleMovement;
            squid.tentacleMovement = in.squidRotation;
        }
    }
}

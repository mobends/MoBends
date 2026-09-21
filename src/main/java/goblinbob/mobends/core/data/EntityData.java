package goblinbob.mobends.core.data;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IBendsModel;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.bind.BoneSinks;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.VectorSink;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.pack.state.PackAnimationState;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

public abstract class EntityData<E extends Entity> implements IBendsModel, IKumoSubject
{

    protected int entityID;
    protected final E entity;

    protected double positionX, positionY, positionZ;
    protected double prevMotionX, prevMotionY, prevMotionZ;
    protected double motionX, motionY, motionZ;
    protected final HashMap<String, Object> nameToPartMap = new HashMap<>();

    /** Named numeric inputs exposed to KUMO animators (see {@link #registerVariable}). */
    private final Map<String, DoubleSupplier> kumoVariables = new HashMap<>();
    /** Named boolean inputs exposed to KUMO animators (see {@link #registerState}). */
    private final Map<String, BooleanSupplier> kumoStates = new HashMap<>();

    public SmoothVector3f globalOffset;
    public SmoothVector3f localOffset;
    public SmoothOrientation renderRotation;
    public SmoothOrientation centerRotation;

    public boolean onGround = true;
    public Boolean onGroundOverride = null;
    public Boolean stillnessOverride = null;

    public final PackAnimationState packAnimationState;

    public EntityData(E entity)
    {
        this.entity = entity;
        if (this.entity != null)
        {
            this.entityID = entity.getEntityId();
            this.positionX = this.entity.posX;
            this.positionY = this.entity.posY;
            this.positionZ = this.entity.posZ;
        }

        this.motionX = this.prevMotionX = 0.0D;
        this.motionY = this.prevMotionY = 1.0D;
        this.motionZ = this.prevMotionZ = 0.0D;

        this.packAnimationState = new PackAnimationState();

        this.initModelPose();
        this.registerKumoBindings();
    }

    // --- KUMO subject ------------------------------------------------------------------------

    /**
     * Registers the variables and states this data exposes to asset-driven animators. Subclasses
     * override to add their own and must call {@code super.registerKumoBindings()}.
     */
    protected void registerKumoBindings()
    {
        registerVariable("ticks", DataUpdateHandler::getTicks);
        registerVariable("partialTicks", () -> DataUpdateHandler.partialTicks);
        registerVariable("ticksPerFrame", () -> DataUpdateHandler.ticksPerFrame);
        registerVariable("random", Math::random);
        registerVariable("motionX", () -> motionX);
        registerVariable("motionY", () -> motionY);
        registerVariable("motionZ", () -> motionZ);
        registerVariable("prevMotionX", () -> prevMotionX);
        registerVariable("prevMotionY", () -> prevMotionY);
        registerVariable("prevMotionZ", () -> prevMotionZ);
        registerVariable("motionMagnitude", this::getInterpolatedMotionMagnitude);
        registerVariable("interpolatedMotionY", this::getInterpolatedMotionY);
        registerVariable("ticksExisted", () -> entity != null ? entity.ticksExisted : 0);
        registerVariable("xzMotionMagnitude", this::getInterpolatedXZMotionMagnitude);
        registerVariable("forwardMomentum", this::getForwardMomentum);
        registerVariable("sidewaysMomentum", this::getSidewaysMomentum);
        registerVariable("movementAngle", this::getMovementAngle);

        registerState("ON_GROUND", this::isOnGround);
        registerState("AIRBORNE", () -> !isOnGround());
        registerState("STANDING_STILL", this::isStillHorizontally);
        registerState("MOVING_HORIZONTALLY", () -> !isStillHorizontally());
        registerState("SPRINTING", () -> entity != null && entity.isSprinting());
        registerState("SNEAKING", () -> entity != null && entity.isSneaking());
        registerState("IN_WATER", () -> entity != null && entity.isInWater());
        registerState("UNDERWATER", this::isUnderwater);
        registerState("RIDING", () -> entity != null && entity.isRiding());
        registerState("ALIVE", () -> entity != null && entity.isEntityAlive());
        registerState("STRAFING", this::isStrafing);
    }

    protected final void registerVariable(String name, DoubleSupplier supplier)
    {
        kumoVariables.put(name, supplier);
    }

    protected final void registerState(String name, BooleanSupplier supplier)
    {
        kumoStates.put(name, supplier);
    }

    @Override
    public IBoneSink getBone(String name)
    {
        // The entity-level smoothed vectors are not model parts, but animators address them by name.
        if ("root".equals(name) || "globalOffset".equals(name))
        {
            return new VectorSink(globalOffset);
        }
        if ("localOffset".equals(name))
        {
            return new VectorSink(localOffset);
        }
        return BoneSinks.wrap(getPartForName(name));
    }

    @Override
    public boolean hasVariable(String name)
    {
        return kumoVariables.containsKey(name);
    }

    @Override
    public double getVariable(String name)
    {
        DoubleSupplier supplier = kumoVariables.get(name);
        if (supplier == null)
        {
            throw new IllegalArgumentException("Unknown animation variable: " + name);
        }
        return supplier.getAsDouble();
    }

    @Override
    public boolean hasState(String name)
    {
        return kumoStates.containsKey(name);
    }

    @Override
    public boolean getState(String name)
    {
        BooleanSupplier supplier = kumoStates.get(name);
        if (supplier == null)
        {
            throw new IllegalArgumentException("Unknown animation state: " + name);
        }
        return supplier.getAsBoolean();
    }

    public void overrideOnGroundState(boolean state)
    {
        this.onGroundOverride = state;
    }

    public void unsetOnGroundStateOverride()
    {
        this.onGroundOverride = null;
    }

    public void overrideStillness(boolean stillness)
    {
        this.stillnessOverride = stillness;
    }

    public void unsetStillnessOverride()
    {
        this.stillnessOverride = null;
    }

    public void initModelPose()
    {
        this.globalOffset = new SmoothVector3f();
        this.localOffset = new SmoothVector3f();
        this.renderRotation = new SmoothOrientation();
        this.centerRotation = new SmoothOrientation();

        this.nameToPartMap.put("renderRotation", renderRotation);
        this.nameToPartMap.put("centerRotation", centerRotation);
    }

    /**
     * Updates all the model's parts to be in their next frame. Called in {@code EntityData.update()}
     */
    public void updateParts(float ticksPerFrame)
    {
        this.globalOffset.update(ticksPerFrame);
        this.localOffset.update(ticksPerFrame);
        this.renderRotation.update(ticksPerFrame);
        this.centerRotation.update(ticksPerFrame);
    }

    public boolean calcOnGround()
    {
        if (this.onGroundOverride != null)
            return this.onGroundOverride;

        // Checking if we're going down stairs.
        BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        IBlockState block = entity.world.getBlockState(position);
        IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
    
        if (motionY <= 0 && (block.getBlock() instanceof BlockStairs || blockBelow.getBlock() instanceof BlockStairs))
            return true;

        // Checking collisions.
        List<AxisAlignedBB> list = entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(0, -0.125F, 0));
        return list.size() > 0;
    }

    public boolean calcCollidedHorizontally()
    {
        List<AxisAlignedBB> list = entity.world.getCollisionBoxes(entity,
                entity.getEntityBoundingBox().offset(this.motionX, 0, this.motionZ));

        return list.size() > 0;
    }

    public double getPositionX() { return this.positionX; }

    public double getPositionY() { return this.positionY; }

    public double getPositionZ() { return this.positionZ; }

    public double getMotionX() { return this.motionX; }

    public double getMotionY() { return this.motionY; }

    public double getMotionZ() { return this.motionZ; }

    public double getPrevMotionX() { return this.prevMotionX; }

    public double getPrevMotionY() { return this.prevMotionY; }

    public double getPrevMotionZ() { return this.prevMotionZ; }

    public double getInterpolatedMotionX() { return this.prevMotionX + (this.motionX - this.prevMotionX) * DataUpdateHandler.partialTicks; }

    public double getInterpolatedMotionY() { return this.prevMotionY + (this.motionY - this.prevMotionY) * DataUpdateHandler.partialTicks; }

    public double getInterpolatedMotionZ() { return this.prevMotionZ + (this.motionZ - this.prevMotionZ) * DataUpdateHandler.partialTicks; }

    public boolean isOnGround()
    {
        return this.onGround;
    }

    public boolean isStillHorizontally()
    {
        // The motion value that is the threshold for determining movement.
        final double deadZone = 0.0025;
        final double horizontalSqMagnitude = this.motionX * this.motionX + this.motionZ * this.motionZ;
        return this.stillnessOverride != null ? this.stillnessOverride : horizontalSqMagnitude < deadZone;
    }

    public abstract IAnimationController<?> getController();

    /**
     * Called during the render tick in {@code EntityDatabase.updateRender()}
     */
    public void update(float partialTicks)
    {
        if (this.entity == null)
            return;

        this.updateParts(DataUpdateHandler.ticksPerFrame);
    }

    public E getEntity()
    {
        return this.entity;
    }

    public float getLookAngle()
    {
        final Vec3d lookVec = this.entity.getLookVec();
        return (float) GUtil.angleFromCoordinates(lookVec.x, lookVec.z);
    }

    private float getWorldMovementAngle()
    {
        return (float) GUtil.angleFromCoordinates(this.motionX, this.motionZ);
    }

    public float getMovementAngle()
    {
        if (isStillHorizontally())
            return 0;
        return this.getWorldMovementAngle() - this.getLookAngle();
    }

    public double getForwardMomentum()
    {
        if (isStillHorizontally())
            return 0;

        final Vec3d lookVec = this.entity.getLookVec();
        final Vec3d lookVecHorizontal = new Vec3d(lookVec.x, 0, lookVec.z).normalize();
        return lookVecHorizontal.x * this.motionX + lookVecHorizontal.z * this.motionZ;
    }

    public double getSidewaysMomentum()
    {
        if (isStillHorizontally())
            return 0;
        Vec3d rightVec = entity.getLookVec().rotateYaw(-GUtil.PI / 2.0F);
        Vec3d rightVecHorizontal = new Vec3d(rightVec.x, 0, rightVec.z).normalize();
        return rightVecHorizontal.x * this.motionX + rightVecHorizontal.z * this.motionZ;
    }

    private static final float STRAFING_THRESHOLD = 30.0f;

    public boolean isStrafing()
    {
        float angle = this.getMovementAngle();
        return (angle >= STRAFING_THRESHOLD && angle <= 180.0F - STRAFING_THRESHOLD)
                || (angle >= -180.0F + STRAFING_THRESHOLD && angle <= -STRAFING_THRESHOLD);
    }

    /**
     * @return True, if the player is sufficiently underwater.
     */
    public boolean isUnderwater()
    {
        if (!this.entity.isInWater())
            return false;

        int blockX = MathHelper.floor(this.entity.posX);
        int blockY = MathHelper.floor(this.entity.posY + 2);
        int blockZ = MathHelper.floor(this.entity.posZ);
        IBlockState state = Minecraft.getMinecraft().world.getBlockState(new BlockPos(blockX, blockY, blockZ));
        return state.getBlock() instanceof BlockStaticLiquid;
    }

    public double getPrevMotionMagnitude()
    {
        return Math.sqrt(this.prevMotionX * this.prevMotionX + this.prevMotionY * this.prevMotionY + this.prevMotionZ * this.prevMotionZ);
    }

    public double getMotionMagnitude()
    {
        return Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
    }

    public double getInterpolatedMotionMagnitude()
    {
        return interpolateMagitude(this.getMotionMagnitude(), this.getPrevMotionMagnitude());
    }

    public double getXZMotionMagnitude()
    {
        return Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
    }

    public double getPrevXZMotionMagnitude()
    {
        return Math.sqrt(this.prevMotionX * this.prevMotionX + this.prevMotionZ * this.prevMotionZ);
    }

    public double getInterpolatedXZMotionMagnitude()
    {
        return interpolateMagitude(this.getXZMotionMagnitude(), this.getPrevXZMotionMagnitude());
    }

    private static double interpolateMagitude(double magnitude, double prevMagnitude)
    {
        return prevMagnitude + (magnitude - prevMagnitude) * DataUpdateHandler.partialTicks;
    }

    public void updateClient()
    {
        this.prevMotionX = this.motionX;
        this.prevMotionY = this.motionY;
        this.prevMotionZ = this.motionZ;

        this.motionX = this.entity.posX - this.positionX;
        this.motionY = this.entity.posY - this.positionY;
        this.motionZ = this.entity.posZ - this.positionZ;

        this.positionX = this.entity.posX;
        this.positionY = this.entity.posY;
        this.positionZ = this.entity.posZ;
    }

    @Override
    public Object getPartForName(String name)
    {
        return nameToPartMap.get(name);
    }

    public abstract void onTicksRestart();

}

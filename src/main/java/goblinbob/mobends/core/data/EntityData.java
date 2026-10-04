package goblinbob.mobends.core.data;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IBendsModel;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.core.kumo.bind.BoneSinks;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.VectorSink;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.logging.Level;

public abstract class EntityData<E extends Entity> implements IBendsModel, IKumoSubject
{

    protected int entityID;
    protected final E entity;

    protected double positionX, positionY, positionZ;
    protected double prevMotionX, prevMotionY, prevMotionZ;
    protected double motionX, motionY, motionZ;
    /** How far the entity has moved, in blocks, summed tick by tick: {@code entityDistanceMoved}. */
    protected float prevDistanceMoved, distanceMoved;
    protected final HashMap<String, Object> nameToPartMap = new HashMap<>();
    /** Draws the {@code random} built-in: the entity's own, so a test can seed it. */
    private Random random = new Random();
    /** What the data carries besides its bones, by name (see {@link EntityComponent}). */
    private final Map<String, EntityComponent> components = new LinkedHashMap<>();

    /** Named numeric inputs exposed to KUMO animators (see {@link #registerVariable}), by index. */
    private final Map<String, Integer> kumoVariableIndices = new HashMap<>();
    private final List<DoubleSupplier> kumoVariables = new ArrayList<>();
    /** Named boolean inputs exposed to KUMO animators (see {@link #registerState}), by index. */
    private final Map<String, Integer> kumoStateIndices = new HashMap<>();
    private final List<BooleanSupplier> kumoStates = new ArrayList<>();

    public SmoothVector3f globalOffset;
    public SmoothVector3f localOffset;
    public SmoothOrientation renderRotation;
    public SmoothOrientation centerRotation;

    public boolean onGround = true;

    /** Animates the entity: its type's animator, or else {@link #getDefaultAnimator()}. */
    private KumoAnimatorController animator;

    /**
     * Only stores the entity: the parts and the animator bindings are made by {@link #initialize()},
     * once every subclass constructor has run.
     */
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
    }

    /** Makes the model's parts and the animator bindings. Whoever creates the data calls it once. */
    public void initialize()
    {
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
        // The built-ins every entity has (see EntityBuiltIns, which declares their types).
        registerVariable("ticks", DataUpdateHandler::getTicks);
        registerVariable("partialTicks", () -> DataUpdateHandler.partialTicks);
        registerVariable("ticksPerFrame", () -> DataUpdateHandler.ticksPerFrame);
        // Read through the field, which the lab replaces with a seeded one.
        registerVariable("random", () -> random.nextDouble());
        registerVariable("entityId", () -> entity != null ? entity.getEntityId() : 0);
        registerVariable("entityMotionY", () -> motionY);
        registerVariable("entityPrevMotionY", () -> prevMotionY);
        registerVariable("entityInterpolatedMotionY", this::getInterpolatedMotionY);
        registerVariable("entitySpeed", this::getInterpolatedMotionMagnitude);
        registerVariable("entityXZSpeed", this::getInterpolatedXZMotionMagnitude);
        registerVariable("entityForwardMomentum", this::getForwardMomentum);
        registerVariable("entitySidewaysMomentum", this::getSidewaysMomentum);
        registerVariable("entityTicksExisted", () -> entity != null ? entity.ticksExisted : 0);
        registerVariable("entityDistanceMoved", () -> GUtil.lerp(prevDistanceMoved, distanceMoved, DataUpdateHandler.partialTicks));
        // Interpolated like the renderer does, in doubles: step_turn plants feet in the world.
        registerVariable("entityWorldX", () -> entity != null ? entity.prevPosX + (entity.posX - entity.prevPosX) * DataUpdateHandler.partialTicks : 0);
        registerVariable("entityWorldY", () -> entity != null ? entity.prevPosY + (entity.posY - entity.prevPosY) * DataUpdateHandler.partialTicks : 0);
        registerVariable("entityWorldZ", () -> entity != null ? entity.prevPosZ + (entity.posZ - entity.prevPosZ) * DataUpdateHandler.partialTicks : 0);

        registerState("entityIsOnGround", this::isOnGround);
        registerState("entityIsStandingStill", this::isStillHorizontally);
        registerState("entityIsSprinting", () -> entity != null && entity.isSprinting());
        registerState("entityIsSneaking", () -> entity != null && entity.isSneaking());
        registerState("entityIsInWater", () -> entity != null && entity.isInWater());
        registerState("entityIsUnderwater", this::isUnderwater);
        registerState("entityIsRiding", () -> entity != null && entity.isRiding());
        registerState("entityIsAlive", () -> entity != null && entity.isEntityAlive());
        registerState("entityIsStrafing", this::isStrafing);
    }

    /** Exposes a number to animators; registering a name again replaces it. */
    protected final void registerVariable(String name, DoubleSupplier supplier)
    {
        register(kumoVariableIndices, kumoVariables, name, supplier);
    }

    /** Exposes a boolean to animators; registering a name again replaces it. */
    protected final void registerState(String name, BooleanSupplier supplier)
    {
        register(kumoStateIndices, kumoStates, name, supplier);
    }

    private static <T> void register(Map<String, Integer> indices, List<T> suppliers, String name, T supplier)
    {
        Integer index = indices.get(name);
        if (index == null)
        {
            indices.put(name, suppliers.size());
            suppliers.add(supplier);
        }
        else
        {
            suppliers.set(index, supplier);
        }
    }

    @Override
    public IBoneSink getBone(String name)
    {
        // The entity-level smoothed vectors are not model parts, but animators address them by name.
        if (Skeleton.ROOT.equals(name) || Skeleton.GLOBAL_OFFSET.equals(name))
        {
            return new VectorSink(globalOffset);
        }
        if (Skeleton.LOCAL_OFFSET.equals(name))
        {
            return new VectorSink(localOffset);
        }
        return BoneSinks.wrap(getPartForName(name));
    }

    @Override
    public int indexOfVariable(String name)
    {
        Integer index = kumoVariableIndices.get(name);
        return index == null ? -1 : index;
    }

    @Override
    public double getVariable(int index)
    {
        return kumoVariables.get(index).getAsDouble();
    }

    /** The current value of the variable {@code name}. */
    public double getVariable(String name)
    {
        int index = indexOfVariable(name);
        if (index < 0)
        {
            throw new IllegalArgumentException("Unknown animation variable: " + name);
        }
        return getVariable(index);
    }

    @Override
    public int indexOfState(String name)
    {
        Integer index = kumoStateIndices.get(name);
        return index == null ? -1 : index;
    }

    @Override
    public boolean getState(int index)
    {
        return kumoStates.get(index).getAsBoolean();
    }

    public void initModelPose()
    {
        this.globalOffset = new SmoothVector3f();
        this.localOffset = new SmoothVector3f();
        this.renderRotation = new SmoothOrientation();
        this.centerRotation = new SmoothOrientation();

        this.nameToPartMap.put("renderRotation", renderRotation);
        this.nameToPartMap.put(Skeleton.CENTER_ROTATION, centerRotation);
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
        for (EntityComponent component : components.values())
        {
            component.update(ticksPerFrame);
        }
    }

    /**
     * Gives the data a component under {@code name}, updated with the parts. Animators can pose it
     * by that name if it is something a bone can be (an {@link OrientationComponent}).
     */
    protected final void addComponent(String name, EntityComponent component)
    {
        components.put(name, component);
        nameToPartMap.put(name, component);
    }

    /** The component named {@code name}, or null if there is none of that type. */
    @Nullable
    public <T> T getComponent(String name, Class<T> type)
    {
        EntityComponent component = components.get(name);
        return type.isInstance(component) ? type.cast(component) : null;
    }

    public Collection<EntityComponent> getComponents()
    {
        return components.values();
    }

    public boolean calcOnGround()
    {
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
        return horizontalSqMagnitude < deadZone;
    }

    /** The animator the entity has when its type doesn't choose another. */
    protected abstract ResourceLocation getDefaultAnimator();

    /**
     * The model definition whose entity scope a Java model's animator reads, when the mob has
     * one (the Java models kept to compare with the defined ones); null for none.
     */
    @Nullable
    protected ResourceLocation getModelDefinition()
    {
        return null;
    }

    /** The model definition {@link #getModelDefinition()} names, or null (logged if it can't be read). */
    @Nullable
    protected EntityModelDefinition loadModelDefinition()
    {
        ResourceLocation location = getModelDefinition();
        if (location == null)
        {
            return null;
        }
        try
        {
            return ModelDefinitions.INSTANCE.load(location);
        }
        catch (Exception e)
        {
            Core.LOG.log(Level.WARNING, "Could not load the model definition " + location + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * The entity its animators animate: its class, and the entity scope they read ({@code entity.x}),
     * which only a model definition declares.
     */
    @Nullable
    public EntityTemplate getEntityScope()
    {
        if (entity == null)
        {
            return null;
        }
        EntityModelDefinition definition = loadModelDefinition();
        return definition == null ? new EntityTemplate(entity.getClass()) : definition.entityScope(entity.getClass());
    }

    /**
     * Animates this entity with its type's animator and extensions: {@code animator} (null for
     * the default one), with the layers of each of {@code extensions} on top.
     */
    public void setAnimator(@Nullable ResourceLocation animator, List<ResourceLocation> extensions)
    {
        this.animator = new KumoAnimatorController(getEntityScope(), animator != null ? animator : getDefaultAnimator(), extensions);
    }

    /** Runs the entity's animator for this frame. */
    public void animate()
    {
        if (animator == null)
        {
            setAnimator(null, Collections.emptyList());
        }
        animator.animate(this);
    }

    /** True when the entity's animator asks for the vanilla model and animation right now (a {@code core:vanilla} node). */
    public boolean wantsVanilla()
    {
        return animator != null && animator.wantsVanilla();
    }

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

        this.prevDistanceMoved = this.distanceMoved;
        this.distanceMoved += MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

        for (EntityComponent component : components.values())
        {
            component.updateClient();
        }
    }

    @Override
    public Object getPartForName(String name)
    {
        return nameToPartMap.get(name);
    }

}

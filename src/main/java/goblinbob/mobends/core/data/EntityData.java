package goblinbob.mobends.core.data;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IBendsModel;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.core.kumo.bind.BoneSinks;
import goblinbob.mobends.core.kumo.bind.EntityBone;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

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

    /** The bone the whole model hangs from: moved, then turned about the entity's feet ({@link Skeleton#ROOT}). */
    public EntityBone root;
    /** The child of {@link #root} the rest of the model hangs from: moved, then turned about {@link #getCenterPivot} ({@link Skeleton#CENTER}). */
    public EntityBone center;
    private final Vec3f centerPivot = new Vec3f();

    public boolean onGround = true;

    /** Animates the entity: its type's animator, or else {@link #getDefaultAnimator()}. */
    private KumoAnimatorController animator;
    /** The frame the animator last ran in (see {@link DataUpdateHandler#frame}). */
    private int animatedFrame = -1;

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
        // The motion, interpolated by partialTicks; the Tick ones are the motion over the last tick
        // and the one before it, as they are, for catching what happens from one tick to the next.
        registerVariable("entityMotionX", this::getInterpolatedMotionX);
        registerVariable("entityMotionY", this::getInterpolatedMotionY);
        registerVariable("entityMotionZ", this::getInterpolatedMotionZ);
        registerVariable("entityTickMotionX", () -> motionX);
        registerVariable("entityTickMotionY", () -> motionY);
        registerVariable("entityTickMotionZ", () -> motionZ);
        registerVariable("entityPrevTickMotionX", () -> prevMotionX);
        registerVariable("entityPrevTickMotionY", () -> prevMotionY);
        registerVariable("entityPrevTickMotionZ", () -> prevMotionZ);
        registerVariable("entitySpeed", this::getInterpolatedMotionMagnitude);
        registerVariable("entityXZSpeed", this::getInterpolatedXZMotionMagnitude);
        registerVariable("entityForwardMomentum", this::getForwardMomentum);
        registerVariable("entitySidewaysMomentum", this::getSidewaysMomentum);
        registerVariable("entityTicksExisted", () -> entity != null ? entity.ticksExisted + DataUpdateHandler.partialTicks : 0);
        registerVariable("entityDistanceMoved", () -> GUtil.lerp(prevDistanceMoved, distanceMoved, DataUpdateHandler.partialTicks));
        // Interpolated like the renderer does, in doubles: step_turn plants feet in the world.
        registerVariable("entityWorldX", () -> entity != null ? entity.prevPosX + (entity.posX - entity.prevPosX) * DataUpdateHandler.partialTicks : 0);
        registerVariable("entityWorldY", () -> entity != null ? entity.prevPosY + (entity.posY - entity.prevPosY) * DataUpdateHandler.partialTicks : 0);
        registerVariable("entityWorldZ", () -> entity != null ? entity.prevPosZ + (entity.posZ - entity.prevPosZ) * DataUpdateHandler.partialTicks : 0);
        registerVariable("entityHeight", () -> entity != null ? entity.height : 0);
        // Where the renderer turns the center bone about.
        registerVariable("centerPivotX", () -> getCenterPivot(centerPivot).x);
        registerVariable("centerPivotY", () -> getCenterPivot(centerPivot).y);
        registerVariable("centerPivotZ", () -> getCenterPivot(centerPivot).z);

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
        // The entity's own bones are not model parts, but animators address them by name.
        if (Skeleton.ROOT.equals(name))
        {
            return root;
        }
        if (Skeleton.CENTER.equals(name))
        {
            return center;
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
        this.root = new EntityBone();
        this.center = new EntityBone();
    }

    /**
     * Where the renderer turns {@link #center} about, in blocks from the entity's feet (+Y up, in
     * the frame {@link #root} leaves): half the entity's height, unless its model says otherwise.
     *
     * @return {@code dest}
     */
    public Vec3f getCenterPivot(Vec3f dest)
    {
        dest.set(0F, entity != null ? entity.height / 2 : 0F, 0F);
        return dest;
    }

    /**
     * Updates all the model's parts to be in their next frame. Called in {@code EntityData.update()}
     */
    public void updateParts(float ticksPerFrame)
    {
        this.root.update(ticksPerFrame);
        this.center.update(ticksPerFrame);
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

        IBlockState block = entity.worldObj.getBlockState(position);
        IBlockState blockBelow = entity.worldObj.getBlockState(position.add(0, -1, 0));
    
        if (motionY <= 0 && (block.getBlock() instanceof BlockStairs || blockBelow.getBlock() instanceof BlockStairs))
            return true;

        // Checking collisions.
        List<AxisAlignedBB> list = entity.worldObj.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(0, -0.125F, 0));
        return list.size() > 0;
    }

    public boolean calcCollidedHorizontally()
    {
        List<AxisAlignedBB> list = entity.worldObj.getCollidingBoundingBoxes(entity,
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
        setAnimator(animator, extensions, OptionSet.EMPTY, Collections.nCopies(extensions.size(), OptionSet.EMPTY));
    }

    /**
     * As above, with the options the type file declares (read by the animator, besides the model
     * definition's) and those each extension file declares (read by its own animator).
     */
    public void setAnimator(@Nullable ResourceLocation animator, List<ResourceLocation> extensions, OptionSet typeOptions, List<OptionSet> extensionOptions)
    {
        this.animator = new KumoAnimatorController(getEntityScope(), animator != null ? animator : getDefaultAnimator(), extensions, typeOptions, extensionOptions);
    }

    /**
     * Runs the entity's animator for this frame, once: drawn again in the same frame (the player in
     * the inventory), the entity shows the pose it already has, as its animators keep state (where
     * the feet stand) that a second run would advance twice, with what that draw sets up.
     *
     * @return False if the animator already ran this frame.
     */
    public boolean animate()
    {
        if (animatedFrame == DataUpdateHandler.frame)
        {
            return false;
        }
        animatedFrame = DataUpdateHandler.frame;
        if (animator == null)
        {
            setAnimator(null, Collections.emptyList());
        }
        animator.animate(this);
        return true;
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
        final Vec3 lookVec = this.entity.getLookVec();
        return (float) GUtil.angleFromCoordinates(lookVec.xCoord, lookVec.zCoord);
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

    /**
     * The interpolated motion's part along where the entity looks (its look interpolated too), so
     * it changes every frame and not once a tick.
     */
    public double getForwardMomentum()
    {
        if (isStillHorizontallyInterpolated())
            return 0;

        final Vec3 lookVec = this.entity.getLook(DataUpdateHandler.partialTicks);
        final Vec3 lookVecHorizontal = new Vec3(lookVec.xCoord, 0, lookVec.zCoord).normalize();
        return lookVecHorizontal.xCoord * this.getInterpolatedMotionX() + lookVecHorizontal.zCoord * this.getInterpolatedMotionZ();
    }

    /** See {@link #getForwardMomentum}: the part to the entity's right. */
    public double getSidewaysMomentum()
    {
        if (isStillHorizontallyInterpolated())
            return 0;
        Vec3 rightVec = entity.getLook(DataUpdateHandler.partialTicks).rotateYaw(-GUtil.PI / 2.0F);
        Vec3 rightVecHorizontal = new Vec3(rightVec.xCoord, 0, rightVec.zCoord).normalize();
        return rightVecHorizontal.xCoord * this.getInterpolatedMotionX() + rightVecHorizontal.zCoord * this.getInterpolatedMotionZ();
    }

    /** {@link #isStillHorizontally}, of the interpolated motion. */
    private boolean isStillHorizontallyInterpolated()
    {
        final double x = this.getInterpolatedMotionX();
        final double z = this.getInterpolatedMotionZ();
        return x * x + z * z < 0.0025;
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

        int blockX = MathHelper.floor_double(this.entity.posX);
        int blockY = MathHelper.floor_double(this.entity.posY + 2);
        int blockZ = MathHelper.floor_double(this.entity.posZ);
        IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(blockX, blockY, blockZ));
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
        this.distanceMoved += MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

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

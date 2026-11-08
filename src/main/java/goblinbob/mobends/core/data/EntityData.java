package goblinbob.mobends.core.data;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.core.kumo.bind.BoneSinks;
import goblinbob.mobends.core.kumo.bind.EntityBone;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * What Mo' Bends keeps about an entity it animates, between frames and ticks: its motion measured
 * tick by tick, its bones, and its animator. It's the subject the animator reads (the built-ins
 * registered in {@link #registerKumoBindings}) and writes (the bones, by name).
 */
public abstract class EntityData<E extends Entity> implements IKumoSubject {
    protected final E entity;

    protected double positionX, positionY, positionZ;
    protected double prevMotionX, prevMotionY, prevMotionZ;
    protected double motionX, motionY, motionZ;
    /** How far the entity has moved, in blocks, summed tick by tick: {@code entityDistanceMoved}. */
    protected float prevDistanceMoved, distanceMoved;
    protected final Map<String, Object> nameToPartMap = new HashMap<>();
    /** Draws the {@code random} built-in. */
    private final Random random = new Random();
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

    private KumoAnimatorController animator;
    /** The frame the animator last ran in (see {@link DataUpdateHandler#frame}). */
    private int animatedFrame = -1;

    /**
     * Only stores the entity: the parts and the animator bindings are made by {@link #initialize()},
     * once every subclass constructor has run.
     */
    public EntityData(E entity) {
        this.entity = entity;
        this.positionX = entity.getX();
        this.positionY = entity.getY();
        this.positionZ = entity.getZ();

        this.motionX = this.prevMotionX = 0.0D;
        this.motionY = this.prevMotionY = 1.0D;
        this.motionZ = this.prevMotionZ = 0.0D;
    }

    /** Makes the model's parts and the animator bindings. Whoever creates the data calls it once. */
    public void initialize() {
        this.initModelPose();
        this.registerKumoBindings();
    }

    // --- KUMO subject ------------------------------------------------------------------------

    /**
     * Registers the variables and states this data exposes to animators. Subclasses override to
     * add their own and must call {@code super.registerKumoBindings()}.
     */
    protected void registerKumoBindings() {
        // The built-ins every entity has (see EntityBuiltIns, which declares their types).
        registerVariable("ticks", DataUpdateHandler::getTicks);
        registerVariable("partialTicks", () -> DataUpdateHandler.partialTicks);
        registerVariable("ticksPerFrame", () -> DataUpdateHandler.ticksPerFrame);
        registerVariable("random", random::nextDouble);
        registerVariable("entityId", entity::getId);
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
        registerVariable("entityTicksExisted", () -> entity.tickCount + DataUpdateHandler.partialTicks);
        registerVariable("entityDistanceMoved", () -> Mth.lerp(DataUpdateHandler.partialTicks, prevDistanceMoved, distanceMoved));
        // Interpolated like the renderer does, in doubles: step_turn plants feet in the world.
        registerVariable("entityWorldX", () -> Mth.lerp(DataUpdateHandler.partialTicks, entity.xo, entity.getX()));
        registerVariable("entityWorldY", () -> Mth.lerp(DataUpdateHandler.partialTicks, entity.yo, entity.getY()));
        registerVariable("entityWorldZ", () -> Mth.lerp(DataUpdateHandler.partialTicks, entity.zo, entity.getZ()));
        registerVariable("entityHeight", entity::getBbHeight);
        // Where the renderer turns the center bone about.
        registerVariable("centerPivotX", () -> getCenterPivot(centerPivot).x);
        registerVariable("centerPivotY", () -> getCenterPivot(centerPivot).y);
        registerVariable("centerPivotZ", () -> getCenterPivot(centerPivot).z);

        registerState("entityIsOnGround", this::isOnGround);
        registerState("entityIsStandingStill", this::isStillHorizontally);
        registerState("entityIsSprinting", entity::isSprinting);
        // Crouching is what vanilla draws the model sneaking for (the pose, not the key held).
        registerState("entityIsSneaking", entity::isCrouching);
        registerState("entityIsInWater", entity::isInWater);
        registerState("entityIsUnderwater", this::isUnderwater);
        registerState("entityIsRiding", entity::isPassenger);
        registerState("entityIsAlive", entity::isAlive);
        registerState("entityIsStrafing", this::isStrafing);
    }

    /** Exposes a number to animators; registering a name again replaces it. */
    protected final void registerVariable(String name, DoubleSupplier supplier) {
        register(kumoVariableIndices, kumoVariables, name, supplier);
    }

    /** Exposes a boolean to animators; registering a name again replaces it. */
    protected final void registerState(String name, BooleanSupplier supplier) {
        register(kumoStateIndices, kumoStates, name, supplier);
    }

    private static <T> void register(Map<String, Integer> indices, List<T> suppliers, String name, T supplier) {
        Integer index = indices.get(name);
        if (index == null) {
            indices.put(name, suppliers.size());
            suppliers.add(supplier);
        } else {
            suppliers.set(index, supplier);
        }
    }

    @Override
    public IBoneSink getBone(String name) {
        // The entity's own bones are not model parts, but animators address them by name.
        if (Skeleton.ROOT.equals(name)) {
            return root;
        }
        if (Skeleton.CENTER.equals(name)) {
            return center;
        }
        return BoneSinks.wrap(getPartForName(name));
    }

    @Override
    public int indexOfVariable(String name) {
        Integer index = kumoVariableIndices.get(name);
        return index == null ? -1 : index;
    }

    @Override
    public double getVariable(int index) {
        return kumoVariables.get(index).getAsDouble();
    }

    @Override
    public int indexOfState(String name) {
        Integer index = kumoStateIndices.get(name);
        return index == null ? -1 : index;
    }

    @Override
    public boolean getState(int index) {
        return kumoStates.get(index).getAsBoolean();
    }

    public void initModelPose() {
        this.root = new EntityBone();
        this.center = new EntityBone();
    }

    /**
     * Where the renderer turns {@link #center} about, in blocks from the entity's feet (+Y up, in
     * the frame {@link #root} leaves): half the entity's height, unless its model says otherwise.
     *
     * @return {@code dest}
     */
    public Vec3f getCenterPivot(Vec3f dest) {
        dest.set(0F, entity.getBbHeight() / 2, 0F);
        return dest;
    }

    /** Moves the parts on by a frame, toward the targets the animator set them last. */
    public void updateParts(float ticksPerFrame) {
        this.root.update(ticksPerFrame);
        this.center.update(ticksPerFrame);
        for (EntityComponent component : components.values()) {
            component.update(ticksPerFrame);
        }
    }

    /**
     * Gives the data a component under {@code name}, updated with the parts. Animators can pose it
     * by that name if it is something a bone can be (an {@link OrientationComponent}).
     */
    protected final void addComponent(String name, EntityComponent component) {
        components.put(name, component);
        nameToPartMap.put(name, component);
    }

    /** The component named {@code name}, or null if there is none of that type. */
    @Nullable
    public <T> T getComponent(String name, Class<T> type) {
        EntityComponent component = components.get(name);
        return type.isInstance(component) ? type.cast(component) : null;
    }

    public Collection<EntityComponent> getComponents() {
        return components.values();
    }

    public boolean calcOnGround() {
        // Checking if we're going down stairs.
        BlockPos position = entity.blockPosition();
        if (motionY <= 0 && (entity.level().getBlockState(position).getBlock() instanceof StairBlock
                || entity.level().getBlockState(position.below()).getBlock() instanceof StairBlock)) {
            return true;
        }
        // Anything to stand on just below the feet (blocks, and entities one can stand on: a boat).
        return !entity.level().noCollision(entity, entity.getBoundingBox().move(0, -0.125F, 0));
    }

    /** Where the entity was as the last client tick started (before it moved in it). */
    public double getPositionX() { return this.positionX; }

    public double getPositionY() { return this.positionY; }

    public double getPositionZ() { return this.positionZ; }

    public double getMotionX() { return this.motionX; }

    public double getMotionY() { return this.motionY; }

    public double getMotionZ() { return this.motionZ; }

    public double getInterpolatedMotionX() { return Mth.lerp(DataUpdateHandler.partialTicks, prevMotionX, motionX); }

    public double getInterpolatedMotionY() { return Mth.lerp(DataUpdateHandler.partialTicks, prevMotionY, motionY); }

    public double getInterpolatedMotionZ() { return Mth.lerp(DataUpdateHandler.partialTicks, prevMotionZ, motionZ); }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean isStillHorizontally() {
        // The motion value that is the threshold for determining movement.
        final double deadZone = 0.0025;
        return motionX * motionX + motionZ * motionZ < deadZone;
    }

    /** The animator the entity has. */
    protected abstract ResourceLocation getDefaultAnimator();

    /**
     * The entity its animator animates: its class, and the entity scope it reads ({@code entity.x}),
     * which only a model definition declares.
     */
    @Nullable
    public EntityTemplate getEntityScope() {
        return new EntityTemplate(entity.getClass());
    }

    /**
     * Gives the entity {@code animator} (null: its own, {@link #getDefaultAnimator}) with the layers
     * of each of {@code extensions} on top, in order: what its type says. The type's options are
     * read by the animator besides the model definition's; each extension's by its own animator.
     */
    public void setAnimator(@Nullable ResourceLocation animator, List<ResourceLocation> extensions, OptionSet typeOptions, List<OptionSet> extensionOptions) {
        this.animator = new KumoAnimatorController(getEntityScope(), animator != null ? animator : getDefaultAnimator(), extensions,
                typeOptions, extensionOptions);
    }

    /**
     * Runs the entity's animator for this frame, once: drawn again in the same frame (the player in
     * the inventory), the entity shows the pose it already has, as its animators keep state (where
     * the feet stand) that a second run would advance twice, with what that draw sets up.
     *
     * @return False if the animator already ran this frame.
     */
    public boolean animate() {
        if (animatedFrame == DataUpdateHandler.frame) {
            return false;
        }
        animatedFrame = DataUpdateHandler.frame;
        if (animator == null) {
            animator = new KumoAnimatorController(getEntityScope(), getDefaultAnimator());
        }
        animator.animate(this);
        return true;
    }

    /** True when the entity's animator asks for the vanilla model and animation right now (a {@code core:vanilla} node). */
    public boolean wantsVanilla() {
        return animator != null && animator.wantsVanilla();
    }

    /** At the start of every rendered frame (see {@code EntityDatabase.updateRender}). */
    public void update(float partialTicks) {
        this.updateParts(DataUpdateHandler.ticksPerFrame);
    }

    @Override
    public E getEntity() {
        return this.entity;
    }

    public float getLookAngle() {
        Vec3 look = entity.getLookAngle();
        return (float) angleFromCoordinates(look.x, look.z);
    }

    private float getWorldMovementAngle() {
        return (float) angleFromCoordinates(this.motionX, this.motionZ);
    }

    /** The angle of (x, z) from +Z towards +X, in degrees. */
    private static double angleFromCoordinates(double x, double z) {
        return Math.atan2(x, z) / Math.PI * 180.0;
    }

    public float getMovementAngle() {
        if (isStillHorizontally()) {
            return 0;
        }
        return this.getWorldMovementAngle() - this.getLookAngle();
    }

    /**
     * The interpolated motion's part along where the entity looks (its look interpolated too), so
     * it changes every frame and not once a tick.
     */
    public double getForwardMomentum() {
        if (isStillHorizontallyInterpolated()) {
            return 0;
        }
        Vec3 look = entity.getViewVector(DataUpdateHandler.partialTicks);
        Vec3 lookHorizontal = new Vec3(look.x, 0, look.z).normalize();
        return lookHorizontal.x * getInterpolatedMotionX() + lookHorizontal.z * getInterpolatedMotionZ();
    }

    /** See {@link #getForwardMomentum}: the part to the entity's right. */
    public double getSidewaysMomentum() {
        if (isStillHorizontallyInterpolated()) {
            return 0;
        }
        Vec3 right = entity.getViewVector(DataUpdateHandler.partialTicks).yRot(-Mth.HALF_PI);
        Vec3 rightHorizontal = new Vec3(right.x, 0, right.z).normalize();
        return rightHorizontal.x * getInterpolatedMotionX() + rightHorizontal.z * getInterpolatedMotionZ();
    }

    /** {@link #isStillHorizontally}, of the interpolated motion. */
    private boolean isStillHorizontallyInterpolated() {
        double x = getInterpolatedMotionX();
        double z = getInterpolatedMotionZ();
        return x * x + z * z < 0.0025;
    }

    private static final float STRAFING_THRESHOLD = 30.0f;

    public boolean isStrafing() {
        float angle = this.getMovementAngle();
        return (angle >= STRAFING_THRESHOLD && angle <= 180.0F - STRAFING_THRESHOLD)
                || (angle >= -180.0F + STRAFING_THRESHOLD && angle <= -STRAFING_THRESHOLD);
    }

    /** True, if the entity is in water with water (well) above its head. */
    public boolean isUnderwater() {
        if (!entity.isInWater()) {
            return false;
        }
        BlockState state = entity.level().getBlockState(BlockPos.containing(entity.getX(), entity.getY() + 2, entity.getZ()));
        return state.getBlock() instanceof LiquidBlock;
    }

    public double getMotionMagnitude() {
        return Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
    }

    public double getPrevMotionMagnitude() {
        return Math.sqrt(prevMotionX * prevMotionX + prevMotionY * prevMotionY + prevMotionZ * prevMotionZ);
    }

    public double getInterpolatedMotionMagnitude() {
        return Mth.lerp(DataUpdateHandler.partialTicks, getPrevMotionMagnitude(), getMotionMagnitude());
    }

    public double getXZMotionMagnitude() {
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public double getPrevXZMotionMagnitude() {
        return Math.sqrt(prevMotionX * prevMotionX + prevMotionZ * prevMotionZ);
    }

    public double getInterpolatedXZMotionMagnitude() {
        return Mth.lerp(DataUpdateHandler.partialTicks, getPrevXZMotionMagnitude(), getXZMotionMagnitude());
    }

    /** At the start of every client tick, before the entity moves: measures its last tick's motion. */
    public void updateClient() {
        this.prevMotionX = this.motionX;
        this.prevMotionY = this.motionY;
        this.prevMotionZ = this.motionZ;

        this.motionX = entity.getX() - this.positionX;
        this.motionY = entity.getY() - this.positionY;
        this.motionZ = entity.getZ() - this.positionZ;

        this.positionX = entity.getX();
        this.positionY = entity.getY();
        this.positionZ = entity.getZ();

        this.prevDistanceMoved = this.distanceMoved;
        this.distanceMoved += (float) getMotionMagnitude();

        for (EntityComponent component : components.values()) {
            component.updateClient();
        }
    }

    /** The bone or component named {@code name}, or null. */
    public Object getPartForName(String name) {
        return nameToPartMap.get(name);
    }
}

package goblinbob.mobends.forge;

import goblinbob.bendslib.math.vector.Vec3d;
import goblinbob.mobends.core.IModelPart;
import goblinbob.mobends.core.ModelPartTransform;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.data.PropertyStorage;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.KumoAnimatorState;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class EntityData implements IEntityData
{
    private final PropertyStorage propertyStorage = new PropertyStorage();
    private final Entity entity;
    private final KumoAnimatorState<EntityData> animatorState;

    protected final Vec3d position = new Vec3d();
    protected final Vec3d prevMotion = new Vec3d();
    protected final Vec3d motion = new Vec3d();

    protected boolean onGround = true;

    private Map<String, IModelPart> parts = new HashMap<>();

    public EntityData(Entity entity, AnimatorTemplate animatorTemplate, IKumoInstancingContext<EntityData> context)
    {
        this.entity = entity;
        this.animatorState = new KumoAnimatorState<>(animatorTemplate, context);

        this.position.set(entity.getX(), entity.getY(), entity.getZ());
        this.prevMotion.set(0, 1, 0);
        this.motion.set(0, 1, 0);
    }

    public boolean calcOnGround()
    {
        // TODO Implement this
        return true;
    }

    public void updateClient()
    {
        this.prevMotion.set(this.motion);
        this.motion.set(
                this.entity.getX() - this.position.x,
                this.entity.getY() - this.position.y,
                this.entity.getZ() - this.position.z
        );
        this.position.set(entity.getX(), entity.getY(), entity.getZ());
    }

    public void update()
    {

    }

    public boolean isOnGround()
    {
        return this.onGround;
    }

    @Override
    public boolean isSprinting()
    {
        return entity.isSprinting();
    }

    @Override
    public boolean isStillHorizontally()
    {
        // The motion value that is the threshold for determining movement.
        double deadZone = 0.0025;
        double horizontalSqMagnitude = this.motion.x * this.motion.x + this.motion.z * this.motion.z;
        return horizontalSqMagnitude < deadZone;
    }

    @Override
    public PropertyStorage getPropertyStorage()
    {
        return propertyStorage;
    }

    public KumoAnimatorState<EntityData> getAnimatorState()
    {
        return animatorState;
    }

    @Override
    public IModelPart getPartForName(String key)
    {
        if (!parts.containsKey(key))
        {
            parts.put(key, new ModelPartTransform());
        }

        return parts.get(key);
    }

    public Entity getEntity()
    {
        return entity;
    }
}

package goblinbob.mobends.forge;

import goblinbob.mobends.core.IModelPart;
import goblinbob.mobends.core.ModelPartTransform;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.data.PropertyStorage;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.KumoAnimatorState;
import goblinbob.mobends.core.math.vector.Vec3d;
import net.minecraft.entity.Entity;

import java.io.IOException;
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

    private IModelPart rootPart = new ModelPartTransform();
    private Map<String, IModelPart> parts = new HashMap<>();

    public EntityData(Entity entity, AnimatorTemplate animatorTemplate)
    {
        this.entity = entity;
        this.animatorState = new KumoAnimatorState<>(animatorTemplate, new IKumoInstancingContext<EntityData>()
        {
            @Override
            public KeyframeAnimation getAnimation(String key)
            {
                try
                {
                    return AnimationLoader.loadFromPath(key);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        this.position.set(entity.getPosX(), entity.getPosY(), entity.getPosZ());
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
                this.entity.getPosX() - this.position.x,
                this.entity.getPosY() - this.position.y,
                this.entity.getPosZ() - this.position.z
        );
        this.position.set(entity.getPosX(), entity.getPosY(), entity.getPosZ());
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
    public IModelPart getRootPart()
    {
        return rootPart;
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

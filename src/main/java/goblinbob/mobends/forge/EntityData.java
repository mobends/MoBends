package goblinbob.mobends.forge;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.data.PropertyStorage;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.*;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.ITriggerCondition;
import goblinbob.mobends.core.math.vector.Vec3d;
import net.minecraft.entity.Entity;

import java.io.IOException;

public class EntityData implements IEntityData
{
    private final PropertyStorage propertyStorage = new PropertyStorage();
    private final Entity entity;
    private final KumoAnimatorState<EntityData> animatorState;

    protected final Vec3d position = new Vec3d();
    protected final Vec3d prevMotion = new Vec3d();
    protected final Vec3d motion = new Vec3d();

    protected boolean onGround = true;

    public EntityData(Entity entity, AnimatorTemplate animatorTemplate) throws MalformedKumoTemplateException
    {
        this.entity = entity;
        this.animatorState = new KumoAnimatorState<EntityData>(animatorTemplate, new IKumoInstancingContext<EntityData>() {
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

            @Override
            public INodeState createNodeState(IKumoInstancingContext<EntityData> context, KeyframeNodeTemplate nodeTemplate) throws MalformedKumoTemplateException
            {
                return null;
            }

            @Override
            public ITriggerCondition<EntityData> createTriggerCondition(TriggerConditionTemplate conditionTemplate, IKumoInstancingContext<EntityData> context) throws MalformedKumoTemplateException
            {
                return null;
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

    public void update()
    {

    }

    public boolean isOnGround()
    {
        return this.onGround;
    }

    @Override
    public PropertyStorage getPropertyStorage()
    {
        return propertyStorage;
    }

    public Entity getEntity()
    {
        return entity;
    }
}

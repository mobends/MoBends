package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.standard.data.BipedEntityData;

/**
 * {@code "driver": "mobends:sword_trail"}: feeds the sword trail effect with the current arm
 * pose every frame the item is evaluated (gate it with {@code when}), optionally with a
 * velocity offset, and can clear the trail when its node is entered. Writes no bones.
 */
public class SwordTrailDriver implements IPoseItem
{

    private final float[] velocity;
    private final boolean add;
    private final boolean resetOnEnter;
    private final boolean resetEachFrame;

    public SwordTrailDriver(float[] velocity, boolean add, boolean resetOnEnter, boolean resetEachFrame)
    {
        this.velocity = velocity;
        this.add = add;
        this.resetOnEnter = resetOnEnter;
        this.resetEachFrame = resetEachFrame;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, Template template) throws MalformedKumoTemplateException
    {
        if (template.velocity != null && template.velocity.length != 3)
        {
            throw new MalformedKumoTemplateException("mobends:sword_trail 'velocity' needs three components.");
        }
        return new SwordTrailDriver(template.velocity, template.add, template.resetOnEnter, template.resetEachFrame);
    }

    private static BipedEntityData<?> biped(IKumoContext context)
    {
        return context.getSubject() instanceof BipedEntityData ? (BipedEntityData<?>) context.getSubject() : null;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        BipedEntityData<?> data = biped(context);
        if (data == null || data.swordTrail == null)
        {
            return;
        }
        if (resetEachFrame)
        {
            data.swordTrail.reset();
        }
        if (!add)
        {
            return;
        }
        if (velocity != null)
        {
            data.swordTrail.add(data, velocity[0], velocity[1], velocity[2]);
        }
        else
        {
            data.swordTrail.add(data);
        }
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return false;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        BipedEntityData<?> data = biped(context);
        if (resetOnEnter && data != null && data.swordTrail != null)
        {
            data.swordTrail.reset();
        }
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

    public static class Template extends DriverItemTemplate
    {
        public float[] velocity;
        /** Feed the trail with the current pose (default). */
        public boolean add = true;
        /** Clear the trail when the node is entered. */
        public boolean resetOnEnter;
        /** Clear the trail on every frame this item is evaluated (gate it with {@code when}). */
        public boolean resetEachFrame;
    }

}

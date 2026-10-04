package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;

/**
 * {@code {"mobends:sword_trail": {...}}}: feeds the sword trail effect with the current arm
 * pose every frame the item is evaluated (gate it with {@code when}), optionally with a
 * velocity offset, and can clear the trail when its node is entered. Writes no bones; feeds the
 * entity's {@code swordTrail} component, if it has one.
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

    /** The entity's data, if it has a sword trail (its {@code swordTrail} component). */
    private static LivingEntityData<?> data(IKumoContext context)
    {
        if (!(context.getSubject() instanceof LivingEntityData))
        {
            return null;
        }
        LivingEntityData<?> data = (LivingEntityData<?>) context.getSubject();
        return trail(data) == null ? null : data;
    }

    private static SwordTrail trail(LivingEntityData<?> data)
    {
        return data.getComponent("swordTrail", SwordTrail.class);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        LivingEntityData<?> data = data(context);
        if (data == null)
        {
            return;
        }
        SwordTrail swordTrail = trail(data);
        if (resetEachFrame)
        {
            swordTrail.reset();
        }
        if (!add)
        {
            return;
        }
        if (velocity != null)
        {
            swordTrail.add(data, velocity[0], velocity[1], velocity[2]);
        }
        else
        {
            swordTrail.add(data);
        }
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        LivingEntityData<?> data = data(context);
        if (resetOnEnter && data != null)
        {
            trail(data).reset();
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

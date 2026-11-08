package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;

/**
 * {@code {"mobends:sword_trail": {...}}}: feeds the sword trail effect, which this version doesn't
 * draw yet. It reads the template as the full driver does, so animators written for it load and
 * fail on the same mistakes, and writes nothing.
 */
public class SwordTrailDriver implements IPoseItem {
    private static final SwordTrailDriver INSTANCE = new SwordTrailDriver();

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, Template template) throws MalformedKumoTemplateException {
        if (template.velocity != null && template.velocity.length != 3) {
            throw new MalformedKumoTemplateException("mobends:sword_trail 'velocity' needs three components.");
        }
        return INSTANCE;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) {
    }

    @Override
    public void onNodeStarted(IKumoContext context) {
    }

    @Override
    public void advance(IKumoContext context, float deltaTime) {
    }

    public static class Template extends DriverItemTemplate {
        public float[] velocity;
        /** Feed the trail with the current pose (default). */
        public boolean add = true;
        /** Clear the trail when the node is entered. */
        public boolean resetOnEnter;
        /** Clear the trail on every frame this item is evaluated (gate it with {@code when}). */
        public boolean resetEachFrame;
    }
}

package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * An animation controller that is entirely an animator asset: every frame the animator's layers
 * write the bone targets of the entity data (which does the smoothing), and the current nodes'
 * tags are the actions bends packs react to.
 *
 * <p>If the animator fails to load, the controller logs once and animates nothing, so a broken
 * asset never crashes the render.
 */
public class KumoAnimatorController<T extends EntityData<?>> implements IAnimationController<T>
{

    private final ResourceLocation animator;
    @Nullable
    private KumoAnimatorState<T> state;
    private boolean failed;

    public KumoAnimatorController(ResourceLocation animator)
    {
        this.animator = animator;
    }

    public KumoAnimatorController(String modId, String path)
    {
        this(new ResourceLocation(modId, path));
    }

    @Nullable
    public KumoAnimatorState<T> getState()
    {
        return state;
    }

    private boolean ensureLoaded()
    {
        if (state != null)
        {
            return true;
        }
        if (failed)
        {
            return false;
        }
        try
        {
            state = new KumoAnimatorState<>(AnimatorResources.INSTANCE.loadAnimator(animator), AnimatorResources.INSTANCE);
            return true;
        }
        catch (Exception e)
        {
            failed = true;
            Core.LOG.log(java.util.logging.Level.SEVERE, "Could not load the animator " + animator, e);
            return false;
        }
    }

    /** Drops the instanced animator so it is rebuilt from (reloaded) resources on the next frame. */
    public void reload()
    {
        state = null;
        failed = false;
    }

    @Nullable
    @Override
    public Collection<String> perform(T entityData)
    {
        if (!ensureLoaded())
        {
            return null;
        }
        try
        {
            state.update(entityData, DataUpdateHandler.ticksPerFrame);
        }
        catch (MalformedKumoTemplateException e)
        {
            failed = true;
            state = null;
            Core.LOG.log(java.util.logging.Level.SEVERE, "The animator " + animator + " failed while animating", e);
            return null;
        }
        return state.getActions();
    }

}

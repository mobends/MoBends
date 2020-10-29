package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.standard.data.WolfData;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;

/**
 * This is an animation controller for a wolf instance. It's a part of the
 * EntityData structure.
 *
 * @author Iwo Plaza
 */
public class WolfController implements IAnimationController<WolfData>
{

    protected static final ResourceLocation WOLF_ANIMATOR = new ResourceLocation(ModStatics.MODID, "animators/wolf.json");
    protected AnimatorTemplate animatorTemplate;
    protected KumoAnimatorState<WolfData> kumoAnimatorState;

    public WolfController()
    {
        try
        {
            animatorTemplate = GsonResources.get(WOLF_ANIMATOR, AnimatorTemplate.class);
            kumoAnimatorState = new KumoAnimatorState<>(animatorTemplate, key -> {
                try
                {
                    return AnimationLoader.loadFromPath(key);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                }
            });
        }
        catch (IOException | MalformedKumoTemplateException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    @Nullable
    public Collection<String> perform(WolfData data)
    {
        EntityWolf wolf = data.getEntity();
        final float ticks = wolf.ticksExisted + DataUpdateHandler.partialTicks;

        try
        {
            kumoAnimatorState.update(data, DataUpdateHandler.ticksPerFrame);
        }
        catch (MalformedKumoTemplateException e)
        {
            e.printStackTrace();
        }

        if (wolf.isChild())
        {
            data.head.offsetScale = 0.5F;
            data.head.globalOffset.set(0.0F, 5.0F, -2.0F);
        }
        else
        {
            data.head.offsetScale = 1.0F;
            data.head.globalOffset.set(0.0F, 0.0F, 0.0F);
        }
        data.head.position.set(0.0F, -0.5F, -13.0F);

        // Head rotation
        data.head.rotation.localRotateY(data.headYaw.get()).finish();
        data.head.rotation.localRotateX(data.headPitch.get()).finish();

        data.head.rotation.localRotateZ((wolf.getInterestedAngle(DataUpdateHandler.partialTicks)
                + wolf.getShakeAngle(DataUpdateHandler.partialTicks, 0.0F)) * GUtil.RAD_TO_DEG).finish();
        data.mane.rotation.localRotateZ(wolf.getShakeAngle(DataUpdateHandler.partialTicks, -0.08F) * GUtil.RAD_TO_DEG).finish();
        data.tail.rotation.localRotateZ(wolf.getShakeAngle(DataUpdateHandler.partialTicks, -0.2F) * GUtil.RAD_TO_DEG).finish();

        // Tail wagging on interest
        data.tail.rotation.localRotateZ(wolf.getInterestedAngle(DataUpdateHandler.partialTicks) * MathHelper.sin(ticks) * 20.0F).finish();

        // Tail rotating based on health
        data.tail.rotation.localRotateX(wolf.getTailRotation() * GUtil.RAD_TO_DEG - 90.0F).finish();

        data.head.offset.set(0, 0, 0);

        return null;
    }

}

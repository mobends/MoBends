package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class LongSwordAction extends AnimationBit<BipedEntityData<?>>
{
    protected static final ResourceLocation LONG_SWORD_ANIMATOR = new ResourceLocation(ModStatics.MODID, "animators/longsword.json");

    private final EnumHandSide handSide;
    protected AnimatorTemplate animatorTemplate;
    protected KumoAnimatorState<BipedEntityData<?>> kumoAnimatorState;

    public LongSwordAction(EnumHandSide handSide)
    {
        this.handSide = handSide;

        try
        {
            animatorTemplate = GsonResources.get(LONG_SWORD_ANIMATOR, AnimatorTemplate.class);
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
    public void perform(BipedEntityData<?> entityData)
    {
        try
        {
            kumoAnimatorState.update(entityData, DataUpdateHandler.ticksPerFrame);
        }
        catch (MalformedKumoTemplateException e)
        {
            e.printStackTrace();
        }
    }
}

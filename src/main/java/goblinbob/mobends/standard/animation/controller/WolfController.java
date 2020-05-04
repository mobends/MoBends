package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.bit.KeyframeAnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.wolf.WolfIdleAnimationBit;
import goblinbob.mobends.standard.animation.bit.wolf.WolfSittingAnimationBit;
import goblinbob.mobends.standard.data.WolfData;
import net.minecraft.entity.passive.EntityWolf;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is an animation controller for a wolf instance. It's a part of the
 * EntityData structure.
 *
 * @author Iwo Plaza
 *
 */
public class WolfController implements IAnimationController<WolfData>
{

    protected HardAnimationLayer<WolfData> layerBase = new HardAnimationLayer<>();

    protected KeyframeAnimationBit<WolfData> bitSitting = new WolfSittingAnimationBit(1.2F);
    protected AnimationBit<WolfData> bitIdle = new WolfIdleAnimationBit();

    public WolfController()
    {

    }

    @Override
    @Nullable
    public Collection<String> perform(WolfData data)
    {
        EntityWolf wolf = data.getEntity();

        if (wolf.isSitting())
        {
            layerBase.playOrContinueBit(bitSitting, data);
        }
        else
        {
            layerBase.playOrContinueBit(bitIdle, data);
        }

        final List<String> actions = new ArrayList<>();
        layerBase.perform(data, actions);

        // Head rotation
        data.head.rotation.localRotateY(data.headYaw.get()).finish();
        data.head.rotation.localRotateX(data.headPitch.get()).finish();

        return actions;
    }

}

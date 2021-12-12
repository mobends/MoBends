package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import net.minecraft.util.EnumHandSide;

@FunctionalInterface
public interface ItemActionFactory<T extends AnimationBit<?>>
{
    T create(EnumHandSide actionHand);
}

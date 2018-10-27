package net.gobbob.mobends.client.mutators.functions;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;

@FunctionalInterface
public interface ApplyMutationFunction {
	public abstract void apply(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity, float partialTicks);
}

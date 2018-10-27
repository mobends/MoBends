package net.gobbob.mobends.client.mutators.functions;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;

@FunctionalInterface
public interface DeapplyMutationFunction {
	public abstract void deapply(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity);
}

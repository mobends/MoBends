package net.gobbob.mobends.client.mutators;

import java.util.List;

import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomBipedArmor;
import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomHeldItem;
import net.gobbob.mobends.util.FieldMiner;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public abstract class Mutator<T extends EntityLivingBase, M extends ModelBase>
{
	protected M vanillaModel;
	protected float headYaw, headPitch, limbSwing, limbSwingAmount;
	
	protected List<LayerRenderer<EntityLivingBase>> layerRenderers;
	
	/*
	 * Used to fetch private data from the original
	 * renderer.
	 */
	public void fetchFields(RenderLivingBase<? extends T> renderer)
	{
		// Getting the layer renderers
		this.layerRenderers = FieldMiner.getObfuscatedValue(renderer, "layerRenderers", "field_177097_h");
	}
	
	public abstract void storeVanillaModel(M model);
	
	/*
	 * Sets the model parameter back to it's vanilla
	 * state. Used to demutate the model.
	 */
	public abstract void applyVanillaModel(M model);
	
	/*
	 * Swaps out the vanilla layers for their custom counterparts,
	 * and if it's a vanilla model, it stores the vanilla layers
	 * for future mutation reversal.
	 */
	public abstract void swapLayer(RenderLivingBase<? extends T> renderer, int index, boolean isModelVanilla);
	
	/*
	 * Swaps the custom layers back with the vanilla layers.
	 * Used to demutate the model.
	 */
	public abstract void deswapLayer(RenderLivingBase<? extends T> renderer, int index);
	
	/*
	 * Creates all the custom parts you need! It swaps all the
	 * original parts with newly created custom parts.
	 */
	public abstract boolean createParts(M original, float scaleFactor);
	
	public boolean mutate(T entity, RenderLivingBase<? extends T> renderer)
	{
		if (renderer.getMainModel() == null || !this.isModelEligible(renderer.getMainModel()))
			return false;
		
		this.fetchFields(renderer);
		
		M model = (M) renderer.getMainModel();
		float scaleFactor = 0F;
		
		boolean isModelVanilla = this.isModelVanilla(model);
		if (isModelVanilla)
		{
			// If this model wasn't mutated before, save it
			// as the vanilla model.
			this.storeVanillaModel(model);
		}
		
		this.createParts(model, scaleFactor);
		
		// Swapping layers
		if (this.layerRenderers != null)
		{
			for (int i = 0; i < layerRenderers.size(); ++i)
			{
				swapLayer(renderer, i, isModelVanilla);
			}
		}
		
		return true;
	}
	
	/*
	 * Performs the steps needed to demutate the model.
	 */
	public void demutate(T entity, RenderLivingBase<? extends T> renderer)
	{
		if (!this.isModelEligible(renderer.getMainModel()))
			return;
		
		M model = (M) renderer.getMainModel();
		
		this.applyVanillaModel(model);
		
		if (this.layerRenderers != null)
		{
			for (int i = 0; i < layerRenderers.size(); ++i)
			{
				this.deswapLayer(renderer, i);
			}
		}
	}
	
	public void updateModel(T entity, RenderLivingBase<? extends T> renderer, float partialTicks)
	{
		boolean shouldSit = entity.isRiding()
				&& (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
		float f = GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
		float f1 = GUtil.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
		float yaw = f1 - f;

		if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase)
		{
			EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
			f = GUtil.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset,
					partialTicks);
			yaw = f1 - f;
			float f3 = MathHelper.wrapDegrees(yaw);

			if (f3 < -85.0F)
				f3 = -85.0F;
			if (f3 >= 85.0F)
				f3 = 85.0F;

			f = f1 - f3;

			if (f3 * f3 > 2500.0F)
				f += f3 * 0.2F;

			yaw = f1 - f;
		}

		float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
		float f5 = 0.0F;
		float f6 = 0.0F;

		if (!entity.isRiding())
		{
			f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
			f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

			if (entity.isChild())
				f6 *= 3.0F;
			if (f5 > 1.0F)
				f5 = 1.0F;
			yaw = f1 - f;
		}

		this.headYaw = yaw;
		this.headPitch = pitch;
		this.limbSwing = f6;
		this.limbSwingAmount = f5;
	}
	
	public abstract void performAnimations(T entity, RenderLivingBase<? extends T> renderer, float partialTicks);
	
	/*
	 * True, if this renderer wasn't mutated before.
	 */
	public abstract boolean isModelVanilla(M model);
	
	/*
	 * Returns true, if this model should be mutated.
	 */
	public abstract boolean isModelEligible(ModelBase model);
}

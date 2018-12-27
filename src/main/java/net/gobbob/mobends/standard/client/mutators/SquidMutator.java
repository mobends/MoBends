package net.gobbob.mobends.standard.client.mutators;

import java.util.HashMap;
import java.util.Map.Entry;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.client.model.ModelPart;
import net.gobbob.mobends.core.client.mutators.Mutator;
import net.gobbob.mobends.standard.data.SquidData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderSquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;

public class SquidMutator extends Mutator<EntitySquid, ModelSquid>
{
	public static HashMap<RenderSquid, SquidMutator> mutatorMap = new HashMap<>();

	public ModelPart squidBody;
	public ModelPart[][] squidTentacles = new ModelPart[8][SquidData.TENTACLE_SECTIONS];

	@Override
	public void storeVanillaModel(ModelSquid model)
	{
		this.vanillaModel = new ModelSquid();
		this.vanillaModel.squidBody = model.squidBody;
		this.vanillaModel.squidTentacles = model.squidTentacles;
	}

	@Override
	public void applyVanillaModel(ModelSquid model)
	{
		model.squidBody = this.vanillaModel.squidBody;
		model.squidTentacles = this.vanillaModel.squidTentacles;
	}

	@Override
	public void swapLayer(RenderLivingBase<? extends EntitySquid> renderer, int index, boolean isModelVanilla)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void deswapLayer(RenderLivingBase<? extends EntitySquid> renderer, int index)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean createParts(ModelSquid original, float scaleFactor)
	{
		float legLength = 12F;
		float foreLegLength = 15F;

		original.squidBody = this.squidBody = new ModelPart(original, 0, 0).setBox(-6.0F, -8.0F, -6.0F, 12, 16, 12)
				.setPosition(0.0F, 8.0F, 0.0F);

		original.squidTentacles = new ModelRenderer[8];
		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			original.squidTentacles[i] = this.squidTentacles[i][0] = new ModelPart(original, 48, 0);
			double d0 = (double) i * Math.PI * 2.0D / (double) this.squidTentacles.length;
			float f = (float) Math.cos(d0) * 4.0F;
			float f1 = (float) Math.sin(d0) * 4.0F;
			this.squidTentacles[i][0].setBox(-1.0F, 0.0F, 0.0F, 2, SquidData.SECTION_HEIGHT, 2).setPosition(f, 16.0F, f1);
			d0 = (double) i * -360.0D / (double) this.squidTentacles.length + 90.0D;
			this.squidTentacles[i][0].rotation.rotateY((float) d0);

			for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j] = new ModelPart(original, 48, 0);
				this.squidTentacles[i][j].setBox(-1.0F, 0.0F, -2.0F, 2, SquidData.SECTION_HEIGHT, 2).setPosition(0, SquidData.SECTION_HEIGHT, 0);
				this.squidTentacles[i][j - 1].addChild(this.squidTentacles[i][j]);
			}
		}

		return true;
	}

	@Override
	public void performAnimations(EntitySquid entity, RenderLivingBase<? extends EntitySquid> renderer,
			float partialTicks)
	{
		SquidData data = EntityDatabase.instance.getAndMake(SquidData::new, entity);

		data.setHeadYaw(this.headYaw);
		data.setHeadPitch(this.headPitch);
		data.setLimbSwing(this.limbSwing);
		data.setLimbSwingAmount(this.limbSwingAmount);

		Controller controller = data.getController();
		if (controller != null && data.canBeUpdated())
		{
			controller.perform(data);
		}

		// Sync up with the EntityData
		this.squidBody.syncUp(data.squidBody);
		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			for (int j = 0; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j].syncUp(data.squidTentacles[i][j]);
			}
		}
	}

	@Override
	public boolean isModelVanilla(ModelSquid model)
	{
		return !(model.squidBody instanceof IModelPart);
	}

	@Override
	public boolean isModelEligible(ModelBase model)
	{
		return model instanceof ModelSquid;
	}

	/*
	 * Used to apply the effect of the mutation, or just to update the model if it
	 * was already mutated. Called from AnimatedEntity.
	 */
	public static void apply(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity,
			float partialTicks)
	{
		if (!(renderer instanceof RenderSquid))
			return;
		if (!(entity instanceof EntitySquid))
			return;

		RenderSquid rendererSpider = (RenderSquid) renderer;
		EntitySquid entitySpider = (EntitySquid) entity;

		SquidMutator mutator = mutatorMap.get(renderer);
		if (!mutatorMap.containsKey(renderer))
		{
			mutator = new SquidMutator();
			mutator.mutate(entitySpider, rendererSpider);
			mutatorMap.put(rendererSpider, mutator);
		}

		mutator.updateModel(entitySpider, rendererSpider, partialTicks);
		mutator.performAnimations(entitySpider, rendererSpider, partialTicks);
	}

	/*
	 * Used to reverse the effect of the mutation. Called from AnimatedEntity.
	 */
	public static void deapply(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity)
	{
		if (!(renderer instanceof RenderSquid))
			return;
		if (!(entity instanceof EntitySquid))
			return;

		if (mutatorMap.containsKey(renderer))
		{
			SquidMutator mutator = mutatorMap.get(renderer);
			mutator.demutate((EntitySquid) entity, (RenderLivingBase<EntitySquid>) renderer);
			mutatorMap.remove(renderer);
		}
	}

	/*
	 * Used to refresh the mutators in case of real-time changes during development.
	 */
	public static void refresh()
	{
		for (Entry<RenderSquid, SquidMutator> mutator : mutatorMap.entrySet())
		{
			mutator.getValue().mutate(null, mutator.getKey());
		}
	}
}

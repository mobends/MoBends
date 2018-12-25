package net.gobbob.mobends.client.mutators;

import java.util.HashMap;

import net.gobbob.mobends.client.model.ModelPart;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.client.renderer.entity.RenderSquid;
import net.minecraft.entity.passive.EntitySquid;

public class SquidMutator extends Mutator<EntitySquid, ModelSquid>
{
	public static HashMap<RenderSquid, SquidMutator> mutatorMap = new HashMap<>();
	
	public ModelPart squidBody;
	public ModelPart[] squidTentacles = new ModelPart[8];
	
	@Override
	public void storeVanillaModel(ModelSquid model)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyVanillaModel(ModelSquid model)
	{
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void performAnimations(EntitySquid entity, RenderLivingBase<? extends EntitySquid> renderer,
			float partialTicks)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isModelVanilla(ModelSquid model)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isModelEligible(ModelBase model)
	{
		// TODO Auto-generated method stub
		return false;
	}

}

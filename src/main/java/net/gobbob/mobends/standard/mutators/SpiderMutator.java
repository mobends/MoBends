package net.gobbob.mobends.standard.mutators;

import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.client.model.ModelPart;
import net.gobbob.mobends.core.data.IEntityDataFactory;
import net.gobbob.mobends.core.mutators.Mutator;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.monster.EntitySpider;

public class SpiderMutator extends Mutator<SpiderData, EntitySpider, ModelSpider>
{
	
	public ModelPart spiderHead;
    public ModelPart spiderNeck;
    public ModelPart spiderBody;
    public ModelPart[] spiderUpperLimbs;
    public ModelPart[] spiderLowerLimbs;
    
    public SpiderMutator(IEntityDataFactory<EntitySpider> dataFactory)
    {
    	super(dataFactory);
    }
    
	@Override
	public void storeVanillaModel(ModelSpider model)
	{
		this.vanillaModel = new ModelSpider();
		
		this.vanillaModel.spiderLeg1 = model.spiderLeg1;
		this.vanillaModel.spiderLeg2 = model.spiderLeg2;
		this.vanillaModel.spiderLeg3 = model.spiderLeg3;
		this.vanillaModel.spiderLeg4 = model.spiderLeg4;
		this.vanillaModel.spiderLeg5 = model.spiderLeg5;
		this.vanillaModel.spiderLeg6 = model.spiderLeg6;
		this.vanillaModel.spiderLeg7 = model.spiderLeg7;
		this.vanillaModel.spiderLeg8 = model.spiderLeg8;
	}

	@Override
	public void applyVanillaModel(ModelSpider model)
	{
		model.spiderLeg1 = this.vanillaModel.spiderLeg1;
		model.spiderLeg2 = this.vanillaModel.spiderLeg2;
		model.spiderLeg3 = this.vanillaModel.spiderLeg3;
		model.spiderLeg4 = this.vanillaModel.spiderLeg4;
		model.spiderLeg5 = this.vanillaModel.spiderLeg5;
		model.spiderLeg6 = this.vanillaModel.spiderLeg6;
		model.spiderLeg7 = this.vanillaModel.spiderLeg7;
		model.spiderLeg8 = this.vanillaModel.spiderLeg8;
	}

	@Override
	public void swapLayer(RenderLivingBase<? extends EntitySpider> renderer, int index, boolean isModelVanilla)
	{
	}

	@Override
	public void deswapLayer(RenderLivingBase<? extends EntitySpider> renderer, int index)
	{
	}

	@Override
	public boolean createParts(ModelSpider original, float scaleFactor)
	{
		float legLength = 12F;
		float foreLegLength = 12F;
		
		original.spiderHead = this.spiderHead = new ModelPart(original, 32, 4);
		this.spiderHead.setPosition(0.0F, 15.0F, -3.0F);
		this.spiderHead.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, 0.0F);
		
		original.spiderNeck = this.spiderNeck = new ModelPart(original, 0, 0);
		this.spiderNeck.setPosition(0.0F, 15.0F, 0.0F);
		this.spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, 0.0F);
		
		original.spiderBody = this.spiderBody = new ModelPart(original, 0, 12);
        this.spiderBody.setPosition(0.0F, 15.0F, 9.0F);;
        this.spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, 0.0F);
        
		this.spiderUpperLimbs = new ModelPart[8];
		this.spiderLowerLimbs = new ModelPart[8];
		
		for (int i = 0; i < 8; ++i)
		{
			boolean odd = i%2 == 1;
			int z = 2 - (i/2);
			
			this.spiderUpperLimbs[i] = new ModelPart(original, odd ? 18 : 26, 0);
			this.spiderUpperLimbs[i].setPosition(odd ? 4F : -4F, 15F, z);
			this.spiderUpperLimbs[i].developBox(odd ? -1F : (-legLength + 1F), -1.0F, -1.0F, 8, 2, 2, 0.0F)
				.setWidth(legLength).create();
			
			this.spiderLowerLimbs[i] = new ModelPart(original, odd ? 26 : 18, 0);
			this.spiderLowerLimbs[i].setPosition(odd ? foreLegLength : -foreLegLength, 0F, 0F);
			this.spiderLowerLimbs[i].developBox(odd ? 0F : -foreLegLength, 0F, -1F, 8, 2, 2, 0F)
				.offset(0F, 0F, 0.005F)
				.resize(foreLegLength, 1.99F, 1.99F).create();
			
			this.spiderUpperLimbs[i].addChild(this.spiderLowerLimbs[i]);
		}
		
		original.spiderLeg1 = this.spiderUpperLimbs[0];
		original.spiderLeg2 = this.spiderUpperLimbs[1];
		original.spiderLeg3 = this.spiderUpperLimbs[2];
		original.spiderLeg4 = this.spiderUpperLimbs[3];
		original.spiderLeg5 = this.spiderUpperLimbs[4];
		original.spiderLeg6 = this.spiderUpperLimbs[5];
		original.spiderLeg7 = this.spiderUpperLimbs[6];
		original.spiderLeg8 = this.spiderUpperLimbs[7];
        
		return true;
	}
	
	@Override
	public void syncUpWithData(SpiderData data)
	{
		spiderHead.syncUp(data.spiderHead);
		spiderNeck.syncUp(data.spiderNeck);
		spiderBody.syncUp(data.spiderBody);
		
		for (int i = 0; i < 8; ++i)
		{
			this.spiderUpperLimbs[i].syncUp(data.limbs[i].upperPart);
			this.spiderLowerLimbs[i].syncUp(data.limbs[i].lowerPart);
		}
	}
	
	@Override
	public boolean isModelVanilla(ModelSpider model)
	{
		return !(model.spiderLeg1 instanceof IModelPart);
	}
	
	@Override
	public boolean isModelEligible(ModelBase model)
	{
		return model instanceof ModelSpider;
	}
	
}

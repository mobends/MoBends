package net.gobbob.mobends.standard.client.mutators;

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
    public ModelPart spiderLeg1;
    public ModelPart spiderLeg2;
    public ModelPart spiderLeg3;
    public ModelPart spiderLeg4;
    public ModelPart spiderLeg5;
    public ModelPart spiderLeg6;
    public ModelPart spiderLeg7;
    public ModelPart spiderLeg8;
    public ModelPart spiderForeLeg1;
    public ModelPart spiderForeLeg2;
    public ModelPart spiderForeLeg3;
    public ModelPart spiderForeLeg4;
    public ModelPart spiderForeLeg5;
    public ModelPart spiderForeLeg6;
    public ModelPart spiderForeLeg7;
    public ModelPart spiderForeLeg8;
    
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
		
		original.spiderHead = this.spiderHead = new ModelPart(original, 32, 4)
		        .setBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, 0.0F)
		        .setPosition(0.0F, 15.0F, -3.0F);
		original.spiderNeck = this.spiderNeck = new ModelPart(original, 0, 0)
		        .setBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, 0.0F)
		        .setPosition(0.0F, 15.0F, 0.0F);
		original.spiderBody = this.spiderBody = new ModelPart(original, 0, 12)
		        .setBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, 0.0F)
		        .setPosition(0.0F, 15.0F, 9.0F);
        
		original.spiderLeg1 = this.spiderLeg1 = new ModelPart(original, 26, 0)
		        .setBox(-legLength + 1F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
		        .setPosition(-4.0F, 15.0F, 2.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
		original.spiderLeg2 = this.spiderLeg2 = new ModelPart(original, 18, 0)
		        .setBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
		        .setPosition(4.0F, 15.0F, 2.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
		original.spiderLeg3 = this.spiderLeg3 = new ModelPart(original, 26, 0)
		        .setBox(-legLength + 1F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
		        .setPosition(-4.0F, 15.0F, 1.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
		original.spiderLeg4 = this.spiderLeg4 = new ModelPart(original, 18, 0)
		        .setBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
		        .setPosition(4.0F, 15.0F, 1.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
		original.spiderLeg5 = this.spiderLeg5 = new ModelPart(original, 26, 0)
		        .setBox(-legLength + 1F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
		        .setPosition(-4.0F, 15.0F, 0.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
		original.spiderLeg6 = this.spiderLeg6 = new ModelPart(original, 18, 0)
		        .setBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
		        .setPosition(4.0F, 15.0F, 0.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
		original.spiderLeg7 = this.spiderLeg7 = new ModelPart(original, 26, 0)
        		.setBox(-legLength + 1F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
        		.setPosition(-4.0F, 15.0F, -1.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
		original.spiderLeg8 = this.spiderLeg8 = new ModelPart(original, 18, 0)
        		.setBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, 0.0F)
        		.setPosition(4.0F, 15.0F, -1.0F)
		        .resizeBox(legLength, 2, 2).updateVertices();
        
        this.spiderForeLeg1 = new ModelPart(original, 18, 0)
        		.setBox(-foreLegLength, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(-legLength + 1F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        this.spiderForeLeg2 = new ModelPart(original, 26, 0)
        		.setBox(0F, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(7F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        this.spiderForeLeg3 = new ModelPart(original, 18, 0)
        		.setBox(-foreLegLength, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(-8F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        this.spiderForeLeg4 = new ModelPart(original, 26, 0)
        		.setBox(0F, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(8F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        this.spiderForeLeg5 = new ModelPart(original, 18, 0)
        		.setBox(-foreLegLength, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(-8F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        this.spiderForeLeg6 = new ModelPart(original, 26, 0)
        		.setBox(0F, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(8F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        this.spiderForeLeg7 = new ModelPart(original, 18, 0)
        		.setBox(-foreLegLength, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(-8F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        this.spiderForeLeg8 = new ModelPart(original, 26, 0)
        		.setBox(0F, 0F, -1F, 8, 2, 2, 0F)
        		.setPosition(8F, 0F, 0F)
        		.offsetBoxBy(0F, 0F, 0.005F)
		        .resizeBox(foreLegLength, 1.99F, 1.99F).updateVertices();
        
        this.spiderLeg1.addChild(this.spiderForeLeg1);
        this.spiderLeg2.addChild(this.spiderForeLeg2);
        this.spiderLeg3.addChild(this.spiderForeLeg3);
        this.spiderLeg4.addChild(this.spiderForeLeg4);
        this.spiderLeg5.addChild(this.spiderForeLeg5);
        this.spiderLeg6.addChild(this.spiderForeLeg6);
        this.spiderLeg7.addChild(this.spiderForeLeg7);
        this.spiderLeg8.addChild(this.spiderForeLeg8);
        
		return true;
	}
	
	@Override
	public void syncUpWithData(SpiderData data)
	{
		spiderHead.syncUp(data.spiderHead);
		spiderNeck.syncUp(data.spiderNeck);
		spiderBody.syncUp(data.spiderBody);
		spiderLeg1.syncUp(data.upperLimbs[0].part);
		spiderLeg2.syncUp(data.upperLimbs[1].part);
		spiderLeg3.syncUp(data.upperLimbs[2].part);
		spiderLeg4.syncUp(data.upperLimbs[3].part);
		spiderLeg5.syncUp(data.upperLimbs[4].part);
		spiderLeg6.syncUp(data.upperLimbs[5].part);
		spiderLeg7.syncUp(data.upperLimbs[6].part);
		spiderLeg8.syncUp(data.upperLimbs[7].part);
		spiderForeLeg1.syncUp(data.lowerLimbs[0].part);
		spiderForeLeg2.syncUp(data.lowerLimbs[1].part);
		spiderForeLeg3.syncUp(data.lowerLimbs[2].part);
		spiderForeLeg4.syncUp(data.lowerLimbs[3].part);
		spiderForeLeg5.syncUp(data.lowerLimbs[4].part);
		spiderForeLeg6.syncUp(data.lowerLimbs[5].part);
		spiderForeLeg7.syncUp(data.lowerLimbs[6].part);
		spiderForeLeg8.syncUp(data.lowerLimbs[7].part);
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

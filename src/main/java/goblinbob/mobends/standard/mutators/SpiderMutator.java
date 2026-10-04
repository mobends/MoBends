package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.data.SpiderData;
import goblinbob.mobends.standard.kumo.spider.SpiderLegs;
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

	@Override
	public void swapLayer(RenderLivingBase<? extends EntitySpider> renderer, int index)
	{
	}

	@Override
	public boolean createParts(ModelSpider original)
	{
		final float legLength = SpiderLegs.LEG_SEGMENT_LENGTH;

		original.spiderHead = this.spiderHead = new ModelPart(original, 32, 4);
		this.spiderHead.setPosition(0.0F, 15.0F, -3.0F);
		this.spiderHead.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, 0.0F);
		
		original.spiderNeck = this.spiderNeck = new ModelPart(original, 0, 0);
		this.spiderNeck.setPosition(0.0F, 15.0F, 0.0F);
		this.spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, 0.0F);
		
		original.spiderBody = this.spiderBody = new ModelPart(original, 0, 12);
        this.spiderBody.setPosition(0.0F, 15.0F, 9.0F);
        this.spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, 0.0F);
        
		this.spiderUpperLimbs = new ModelPart[SpiderLegs.LIMBS];
		this.spiderLowerLimbs = new ModelPart[SpiderLegs.LIMBS];

		for (int i = 0; i < SpiderLegs.LIMBS; ++i)
		{
			boolean odd = i % 2 == 1;

			this.spiderUpperLimbs[i] = new ModelPart(original, odd ? 18 : 26, 0);
			this.spiderUpperLimbs[i].setPosition(odd ? SpiderLegs.HIP_X : -SpiderLegs.HIP_X, SpiderLegs.HIP_Y, SpiderLegs.hipZ(i));
			this.spiderUpperLimbs[i].developBox(odd ? -1F : (-legLength + 1F), -1.0F, -1.0F, 8, 2, 2, 0.0F)
				.setWidth(legLength).create();
			
			this.spiderLowerLimbs[i] = new ModelPart(original, odd ? 26 : 18, 0);
			this.spiderLowerLimbs[i].setPosition(odd ? SpiderLegs.KNEE_X : -SpiderLegs.KNEE_X, SpiderLegs.KNEE_Y, 0F);
			this.spiderLowerLimbs[i].developBox(odd ? 0F : -legLength, 0F, -1F, 8, 2, 2, 0F)
				.offset(0F, 0F, 0.005F)
				.resize(legLength, 1.99F, 1.99F).create();
			
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
		
		for (int i = 0; i < SpiderLegs.LIMBS; ++i)
		{
			this.spiderUpperLimbs[i].syncUp(data.upperLimbs[i]);
			this.spiderLowerLimbs[i].syncUp(data.lowerLimbs[i]);
		}
	}
	
	@Override
	public boolean shouldModelBeSkipped(ModelBase model)
	{
		return !(model instanceof ModelSpider);
	}
	
}

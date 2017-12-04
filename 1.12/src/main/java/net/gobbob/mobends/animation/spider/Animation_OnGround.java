package net.gobbob.mobends.animation.spider;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsSpider;
import net.gobbob.mobends.data.Data_Spider;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.math.MathHelper;

public class Animation_OnGround extends Animation{
	
	public String getName(){
		return "onGround";
	}

	public String[] getAlterableList() {
		return new String[]{
			getName(),
			"stand",
			"walk"
		};
	}
	
	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		EntitySpider spider = (EntitySpider) argEntity;
		ModelBendsSpider model = (ModelBendsSpider) argModel;
		Data_Spider data = (Data_Spider) argData;
		
		float f9 = -(MathHelper.cos(model.armSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * model.armSwingAmount;
        float f10 = -(MathHelper.cos(model.armSwing * 0.6662F * 2.0F + (float)Math.PI) * 0.4F) * model.armSwingAmount;
        float f11 = -(MathHelper.cos(model.armSwing * 0.6662F * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * model.armSwingAmount;
        float f12 = -(MathHelper.cos(model.armSwing * 0.6662F * 2.0F + ((float)Math.PI * 3F / 2F)) * 0.4F) * model.armSwingAmount;
        float f13 = Math.abs(MathHelper.sin(model.armSwing * 0.6662F + 0.0F) * 0.4F) * model.armSwingAmount;
        float f14 = Math.abs(MathHelper.sin(model.armSwing * 0.6662F + (float)Math.PI) * 0.4F) * model.armSwingAmount;
        float f15 = Math.abs(MathHelper.sin(model.armSwing * 0.6662F + ((float)Math.PI / 2F)) * 0.4F) * model.armSwingAmount;
        float f16 = Math.abs(MathHelper.sin(model.armSwing * 0.6662F + ((float)Math.PI * 3F / 2F)) * 0.4F) * model.armSwingAmount;
		
        model.renderOffset.setSmoothY((MathHelper.cos(model.armSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * model.armSwingAmount * -0.2f);
        model.renderRotation.setX(0.0f);
        
        model.renderOffset.setSmoothX(0.0f,0.6f);
        model.renderOffset.setSmoothZ(0.0f,0.6f);
        
        ((ModelPart)model.spiderHead).rotation.setY(model.headRotationY /* / (180F / (float)Math.PI) */);
        ((ModelPart)model.spiderHead).rotation.setX(model.headRotationX /* / (180F / (float)Math.PI) */);
        float f6 = ((float)Math.PI / 4F);
        float sm = 40;
        ((ModelPart)model.spiderLeg1).rotation.setZ(sm+f13/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg2).rotation.setZ(-sm-f13/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg3).rotation.setZ(sm+f14/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg4).rotation.setZ(-sm-f14/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg5).rotation.setZ(sm+f15/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg6).rotation.setZ(-sm-f15/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg7).rotation.setZ(sm+f16/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg8).rotation.setZ(-sm-f16/(float)Math.PI*180.0f);
        float f7 = -0.0F;
        float f8 = 0.3926991F;
        ((ModelPart)model.spiderLeg1).pre_rotation.setY(-70 + f9/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg2).pre_rotation.setY(70 - f9/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg3).pre_rotation.setY(-40 + f10/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg4).pre_rotation.setY(40 - f10/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg5).pre_rotation.setY(40 + f11/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg6).pre_rotation.setY(-40 - f11/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg7).pre_rotation.setY(70 + f12/(float)Math.PI*180.0f);
        ((ModelPart)model.spiderLeg8).pre_rotation.setY(-70 - f12/(float)Math.PI*180.0f);
        
        float foreBend = 89;
        
        model.spiderForeLeg1.rotation.setZ(-foreBend);
        model.spiderForeLeg2.rotation.setZ(foreBend);
        model.spiderForeLeg3.rotation.setZ(-foreBend);
        model.spiderForeLeg4.rotation.setZ(foreBend);
        model.spiderForeLeg5.rotation.setZ(-foreBend);
        model.spiderForeLeg6.rotation.setZ(foreBend);
        model.spiderForeLeg7.rotation.setZ(-foreBend);
        model.spiderForeLeg8.rotation.setZ(foreBend);
        
        ((ModelPart)model.spiderBody).rotation.setX(-30.0f);
	}
}

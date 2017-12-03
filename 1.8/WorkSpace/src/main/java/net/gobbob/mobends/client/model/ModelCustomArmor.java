package net.gobbob.mobends.client.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.AnimatedEntity;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsVar;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;

public class ModelCustomArmor extends ModelBiped{
	public ModelRenderer bipedRightForeArm;
    public ModelRenderer bipedLeftForeArm;
    public ModelRenderer bipedRightForeLeg;
    public ModelRenderer bipedLeftForeLeg;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderItemRotation = new SmoothVector3f();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    
	public ModelCustomArmor()
    {
        this(0.0F);
    }

    public ModelCustomArmor(float p_i1148_1_)
    {
        this(p_i1148_1_, 0.0F, 64, 32);
    }

    public ModelCustomArmor(float p_i1149_1_, float p_i1149_2_, int p_i1149_3_, int p_i1149_4_)
    {
        this.textureWidth = p_i1149_3_;
        this.textureHeight = p_i1149_4_;
        this.bipedHead = new ModelRendererBends(this, 0, 0);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i1149_1_);
        this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1149_2_-12, 0.0F);
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i1149_1_ + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody = new ModelRendererBends(this, 16, 16).setShowChildIfHidden(true);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, p_i1149_1_);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + p_i1149_2_ + 12, 0.0F);
        this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody);
        this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, p_i1149_1_);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + p_i1149_2_ - 12.0f, 0.0F);
        this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, p_i1149_1_);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + p_i1149_2_ - 12.0f, 0.0F);
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i1149_1_);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F + p_i1149_2_, 0.0F);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i1149_1_);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F + p_i1149_2_, 0.0F);
        
        this.bipedRightForeArm = new ModelRendererBends(this, 40, 16+6);
        this.bipedRightForeArm.addBox(0, 0, -4.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightForeArm.setRotationPoint(-4.0f+1.0f, 4.0f, 2.0F);
        ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedLeftForeArm = new ModelRendererBends(this, 40, 16+6);
        this.bipedLeftForeArm.mirror = true;
        this.bipedLeftForeArm.addBox(0, 0, -4.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftForeArm.setRotationPoint(-1.0f, 4.0f, 2.0F);
        ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
    
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedRightForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i1149_1_);
        this.bipedRightForeLeg.setRotationPoint(0.0f, 6.0f, -2.0F);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i1149_1_);
        this.bipedLeftForeLeg.setRotationPoint(0.0f, 6.0f, -2.0F);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        this.bipedBody.addChild(this.bipedHead);
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        
        this.bipedHead.addChild(this.bipedHeadwear);
        
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart((ModelRendererBends) this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart((ModelRendererBends) this.bipedLeftForeArm);
    }
    
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);

        if (this.isChild)
        {
            float f6 = 2.0F;
            GL11.glPushMatrix();
            GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
            GL11.glTranslatef(0.0F, 16.0F * p_78088_7_, 0.0F);
            this.bipedHead.render(p_78088_7_);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GL11.glTranslatef(0.0F, 24.0F * p_78088_7_, 0.0F);
            this.bipedBody.render(p_78088_7_);
            this.bipedRightArm.render(p_78088_7_);
            this.bipedLeftArm.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
            this.bipedHeadwear.render(p_78088_7_);
            GL11.glPopMatrix();
        }
        else
        {
            this.bipedBody.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
        }
    }
    
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity)
    {
    }
	
	public void updateWithModelData(ModelBendsPlayer argModel){
		if(argModel == null)
			return;
		
		((ModelRendererBends)this.bipedHead).sync((ModelRendererBends) argModel.bipedHead);
		((ModelRendererBends)this.bipedHeadwear).sync((ModelRendererBends) argModel.bipedHeadwear);
		((ModelRendererBends)this.bipedBody).sync((ModelRendererBends) argModel.bipedBody);
		((ModelRendererBends)this.bipedLeftArm).sync((ModelRendererBends) argModel.bipedLeftArm);
		((ModelRendererBends)this.bipedLeftForeArm).sync((ModelRendererBends) argModel.bipedLeftForeArm);
		((ModelRendererBends)this.bipedLeftForeLeg).sync((ModelRendererBends) argModel.bipedLeftForeLeg);
		((ModelRendererBends)this.bipedLeftLeg).sync((ModelRendererBends) argModel.bipedLeftLeg);
		((ModelRendererBends)this.bipedRightArm).sync((ModelRendererBends) argModel.bipedRightArm);
		((ModelRendererBends)this.bipedRightForeArm).sync((ModelRendererBends) argModel.bipedRightForeArm);
		((ModelRendererBends)this.bipedRightForeLeg).sync((ModelRendererBends) argModel.bipedRightForeLeg);
		((ModelRendererBends)this.bipedRightLeg).sync((ModelRendererBends) argModel.bipedRightLeg);
	}
}

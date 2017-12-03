package net.gobbob.mobends.client.model.entity;

import net.gobbob.mobends.AnimatedEntity;
import net.gobbob.mobends.client.model.ModelBoxBends;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.ModelRendererBends_Child;
import net.gobbob.mobends.client.model.ModelRendererBends_SeperatedChild;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsVar;
import net.gobbob.mobends.util.BendsLogger;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class ModelBendsPlayer extends ModelPlayer{
    public ModelRendererBends bipedRightForeArm;
    public ModelRendererBends bipedLeftForeArm;
    public ModelRendererBends bipedRightForeLeg;
    public ModelRendererBends bipedLeftForeLeg;
    
    public ModelRendererBends bipedRightForeArmwear;
    public ModelRendererBends bipedLeftForeArmwear;
    public ModelRendererBends bipedRightForeLegwear;
    public ModelRendererBends bipedLeftForeLegwear;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    
    private ModelRenderer bipedCape;
    private ModelRenderer bipedDeadmau5Head;
    private boolean smallArms;
    
    public ModelBendsPlayer(float p_i46304_1_, boolean p_i46304_2)
    {
    	this(p_i46304_1_, p_i46304_2, true);
    }
    
    public ModelBendsPlayer(float p_i46304_1_, boolean p_i46304_2_, boolean bigTexture)
    {
        super(p_i46304_1_, p_i46304_2_);
        
        this.textureWidth = 64;
        this.textureHeight = bigTexture ? 64 : 32;
        
        this.smallArms = p_i46304_2_;
        this.bipedDeadmau5Head = new ModelRendererBends(this, 24, 0);
        this.bipedDeadmau5Head.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, p_i46304_1_);
        this.bipedCape = new ModelRendererBends(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, p_i46304_1_);
        
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_ + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody = new ModelRendererBends(this, 16, 16);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, p_i46304_1_);
        this.bipedBody.setRotationPoint(0.0F, 12.0F, 0.0F);
        this.bipedHead = new ModelRendererBends(this, 0, 0).setShowChildIfHidden(true);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_);
        this.bipedHead.setRotationPoint(0.0F, -12.0F, 0.0F);
        
        if (p_i46304_2_)
        {
            this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 32, 48).setMother((ModelRendererBends) this.bipedBody).setShowChildIfHidden(true);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 6, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.5F-12.0f, 0.0F);
            this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setShowChildIfHidden(true);
            this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 6, 4, p_i46304_1_);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.5F-12.0f, 0.0F);
            this.bipedLeftArmwear = new ModelRendererBends(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 6, 4, p_i46304_1_ + 0.25F);
            ((ModelRendererBends)this.bipedLeftArmwear).getBox().resY-=0.25f;
            ((ModelRendererBends)this.bipedLeftArmwear).getBox().updateVertexPositions(this.bipedLeftArmwear);
            this.bipedLeftArmwear.setRotationPoint(0.0f,0.0f,0.0f);
            this.bipedRightArmwear = new ModelRendererBends(this, 40, 32);
            this.bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 6, 4, p_i46304_1_ + 0.25F);
            ((ModelRendererBends)this.bipedRightArmwear).getBox().resY-=0.25f;
            ((ModelRendererBends)this.bipedRightArmwear).getBox().updateVertexPositions(this.bipedRightArmwear);
            this.bipedRightArmwear.setRotationPoint(0.0f,0.0f,0.0f);
            ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(3.02f, 6.0f, 4.02f).updateVertices();
            ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(3.02f, 6.0f, 4.02f).updateVertices();
            
            this.bipedLeftForeArm = new ModelRendererBends(this, 32, 48+6);
            this.bipedLeftForeArm.addBox(-1.0F, 0.0F, -4.0F, 3, 6, 4, p_i46304_1_);
            this.bipedLeftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            this.bipedRightForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedRightForeArm.addBox(-2.0F, 0.0F, -4.0F, 3, 6, 4, p_i46304_1_);
            this.bipedRightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            
            this.bipedLeftForeArmwear = new ModelRendererBends(this, 48, 48+6);
            this.bipedLeftForeArmwear.addBox(-1.0F, 0.0F, -4.0F, 3, 6, 4, p_i46304_1_ + 0.25F);
            this.bipedLeftForeArmwear.getBox().resY-=0.25f;
            this.bipedLeftForeArmwear.getBox().offsetY+=0.25f;
            this.bipedLeftForeArmwear.getBox().updateVertexPositions(this.bipedLeftForeArmwear);
            this.bipedLeftForeArmwear.setRotationPoint(0.0f,0.0f,0.0f);
            ((ModelRendererBends)this.bipedLeftForeArmwear).getBox().offsetTextureQuad(this.bipedLeftForeArmwear,ModelBoxBends.BOTTOM, 0, -6.0f);
            this.bipedRightForeArmwear = new ModelRendererBends(this, 40, 32+6);
            this.bipedRightForeArmwear.addBox(-2.0F, 0.0F, -4.0F, 3, 6, 4, p_i46304_1_ + 0.25F);
            this.bipedRightForeArmwear.getBox().resY-=0.25f;
            this.bipedRightForeArmwear.getBox().offsetY+=0.25f;
            this.bipedRightForeArmwear.getBox().updateVertexPositions(this.bipedRightForeArmwear);
            this.bipedRightForeArmwear.setRotationPoint(0.0f,0.0f,0.0f);
            ((ModelRendererBends)this.bipedRightForeArmwear).getBox().offsetTextureQuad(this.bipedRightForeArmwear,ModelBoxBends.BOTTOM, 0, -6.0f);
            //((ModelRendererBends)this.bipedLeftForeArmwear).offsetBox_Add(-0.005f, 0, -0.005f).resizeBox(3.01f+0.5f, 6.0f+0.25f, 4.01f+0.5f).updateVertices();
            //((ModelRendererBends)this.bipedRightForeArmwear).offsetBox_Add(-0.005f, 0, -0.005f).resizeBox(3.01f+0.5f, 6.0f+0.25f, 4.01f+0.5f).updateVertices();
        }
        else
        {
            this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 32, 48).setMother((ModelRendererBends) this.bipedBody).setShowChildIfHidden(true);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F-12.0f, 0.0F);
            this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setShowChildIfHidden(true);
            this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F-12.0f, 0.0F);
            this.bipedLeftArmwear = new ModelRendererBends(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
            ((ModelRendererBends)this.bipedLeftArmwear).getBox().resY-=0.25f;
            ((ModelRendererBends)this.bipedLeftArmwear).getBox().updateVertexPositions(this.bipedLeftArmwear);
            this.bipedLeftArmwear.setRotationPoint(0.0f, 0.0f, 0.0f);
            this.bipedRightArmwear = new ModelRendererBends(this, 40, 32);
            this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
            this.bipedRightArmwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        
            ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
            ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        
            this.bipedLeftForeArm = new ModelRendererBends(this, 32, 48+6);
            this.bipedLeftForeArm.addBox(-1.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_);
            this.bipedLeftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            this.bipedRightForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedRightForeArm.addBox(-3.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_);
            this.bipedRightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
        
            this.bipedLeftForeArmwear = new ModelRendererBends(this, 48, 48+6);
            this.bipedLeftForeArmwear.addBox(-1.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
            this.bipedLeftForeArmwear.getBox().resY-=0.25f;
            this.bipedLeftForeArmwear.getBox().offsetY+=0.25f;
            this.bipedLeftForeArmwear.getBox().updateVertexPositions(this.bipedLeftForeArmwear);
            this.bipedLeftForeArmwear.setRotationPoint(0.0f,0.0f,0.0f);
            ((ModelRendererBends)this.bipedLeftForeArmwear).getBox().offsetTextureQuad(this.bipedLeftForeArmwear,ModelBoxBends.BOTTOM, 0, -6.0f);
            this.bipedRightForeArmwear = new ModelRendererBends(this, 40, 32+6);
            this.bipedRightForeArmwear.addBox(-3.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
            this.bipedRightForeArmwear.setRotationPoint(0.0f,0.0f,0.0f);
            ((ModelRendererBends)this.bipedRightForeArmwear).getBox().offsetTextureQuad(this.bipedRightForeArmwear,ModelBoxBends.BOTTOM, 0, -6.0f);
        }
        
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg = new ModelRendererBends(this, 16, 48);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRendererBends(this, 0, 48);
        this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
        ((ModelRendererBends)this.bipedLeftLegwear).getBox().resY-=0.25f;
        ((ModelRendererBends)this.bipedLeftLegwear).getBox().updateVertexPositions(this.bipedLeftLegwear);
        this.bipedLeftLegwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.bipedRightLegwear = new ModelRendererBends(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
        ((ModelRendererBends)this.bipedRightLegwear).getBox().resY-=0.25f;
        ((ModelRendererBends)this.bipedRightLegwear).getBox().updateVertexPositions(this.bipedRightLegwear);
        this.bipedRightLegwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.bipedBodyWear = new ModelRendererBends(this, 16, 32);
        this.bipedBodyWear.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, p_i46304_1_ + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedRightForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_);
        this.bipedRightForeLeg.setRotationPoint(0, 6.0F, -2.0F);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedLeftForeLeg = new ModelRendererBends(this, 16, 48+6);
        this.bipedLeftForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_);
        this.bipedLeftForeLeg.setRotationPoint(0, 6.0F, -2.0F);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedRightForeLegwear = new ModelRendererBends(this, 0, 32+6);
        this.bipedRightForeLegwear.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
        this.bipedRightForeLegwear.getBox().resY-=0.25f;
        this.bipedRightForeLegwear.getBox().offsetY+=0.25f;
        this.bipedRightForeLegwear.getBox().updateVertexPositions(this.bipedRightForeLegwear);
        this.bipedRightForeLegwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        ((ModelRendererBends)this.bipedRightForeLegwear).getBox().offsetTextureQuad(this.bipedRightForeLegwear,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedLeftForeLegwear = new ModelRendererBends(this, 0, 48+6);
        this.bipedLeftForeLegwear.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
        this.bipedLeftForeLegwear.getBox().resY-=0.25f;
        this.bipedLeftForeLegwear.getBox().offsetY+=0.25f;
        this.bipedLeftForeLegwear.getBox().updateVertexPositions(this.bipedLeftForeLegwear);
        this.bipedLeftForeLegwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        ((ModelRendererBends)this.bipedLeftForeLegwear).getBox().offsetTextureQuad(this.bipedLeftForeLegwear,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        /*this.bipedBody = new ModelRendererBends(this, 16, 16).setShowChildIfHidden(true);
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
        */
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        this.bipedBody.addChild(this.bipedHead);
        this.bipedBody.addChild(this.bipedBodyWear);
        
        this.bipedHead.addChild(this.bipedHeadwear);
        
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        this.bipedRightArm.addChild(this.bipedRightArmwear);
        this.bipedLeftArm.addChild(this.bipedLeftArmwear);
        this.bipedRightForeArm.addChild(this.bipedRightForeArmwear);
        this.bipedLeftForeArm.addChild(this.bipedLeftForeArmwear);
        
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        this.bipedRightLeg.addChild(this.bipedRightLegwear);
        this.bipedLeftLeg.addChild(this.bipedLeftLegwear);
        this.bipedRightForeLeg.addChild(this.bipedRightForeLegwear);
        this.bipedLeftForeLeg.addChild(this.bipedLeftForeLegwear);
        
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart((ModelRendererBends) this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart((ModelRendererBends) this.bipedLeftForeArm);
        
        ((ModelRendererBends)this.bipedRightLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
    }
    
    @Override
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);
        
        GL11.glPushMatrix();
        	/*GL11.glRotatef(-this.renderRotation.getX(),1.0f,0.0f,0.0f);
        	GL11.glRotatef(-this.renderRotation.getY(),0.0f,1.0f,0.0f);
        	GL11.glRotatef(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
	        */
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
	            
	            /*GL11.glPushMatrix();
	            	this.bipedBody.postRender(p_78088_7_);
	            	this.bipedHead.render(p_78088_7_);
	            GL11.glPopMatrix();*/
	        }
	        
        GL11.glPopMatrix();
    }
    
    @Override
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity)
    {
    	if(Minecraft.getMinecraft().theWorld == null){
    		return;
    	}
    	
    	if(Minecraft.getMinecraft().theWorld.isRemote){
    		if(Minecraft.getMinecraft().isGamePaused()){
    			return;
    		}
    	}
    	
    	Data_Player data = (Data_Player) Data_Player.get(argEntity.getEntityId());
    	
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	this.headRotationX = argHeadX;
    	this.headRotationY = argHeadY;
    	
    	if(Minecraft.getMinecraft().currentScreen != null){
    		this.headRotationY = 0;
    	}
    	
    	((ModelRendererBends) this.bipedHead).sync(data.head);
    	((ModelRendererBends) this.bipedHeadwear).sync(data.headwear);
    	((ModelRendererBends) this.bipedBody).sync(data.body);
    	((ModelRendererBends) this.bipedRightArm).sync(data.rightArm);
    	((ModelRendererBends) this.bipedLeftArm).sync(data.leftArm);
    	((ModelRendererBends) this.bipedRightLeg).sync(data.rightLeg);
    	((ModelRendererBends) this.bipedLeftLeg).sync(data.leftLeg);
    	((ModelRendererBends) this.bipedRightForeArm).sync(data.rightForeArm);
    	((ModelRendererBends) this.bipedLeftForeArm).sync(data.leftForeArm);
    	((ModelRendererBends) this.bipedRightForeLeg).sync(data.rightForeLeg);
    	((ModelRendererBends) this.bipedLeftForeLeg).sync(data.leftForeLeg);
    	
    	this.renderOffset.set(data.renderOffset);
    	this.renderRotation.set(data.renderRotation);
    	this.renderItemRotation.set(data.renderItemRotation);
    	this.swordTrail = data.swordTrail;
    	
    		if(Data_Player.get(argEntity.getEntityId()).canBeUpdated()){
    			this.renderOffset.setSmooth(new Vector3f(0,-1f,0),0.5f);
    			this.renderRotation.setSmooth(new Vector3f(0,0,0),0.5f);
    			this.renderItemRotation.setSmooth(new Vector3f(0,0,0),0.5f);
    			
    			((ModelRendererBends) this.bipedHead).resetScale();
    			((ModelRendererBends) this.bipedHeadwear).resetScale();
    			((ModelRendererBends) this.bipedBody).resetScale();
    			((ModelRendererBends) this.bipedRightArm).resetScale();
    			((ModelRendererBends) this.bipedLeftArm).resetScale();
    			((ModelRendererBends) this.bipedRightLeg).resetScale();
    			((ModelRendererBends) this.bipedLeftLeg).resetScale();
    			((ModelRendererBends) this.bipedRightForeArm).resetScale();
    			((ModelRendererBends) this.bipedLeftForeArm).resetScale();
    			((ModelRendererBends) this.bipedRightForeLeg).resetScale();
    			((ModelRendererBends) this.bipedLeftForeLeg).resetScale();
    			
    			BendsVar.tempData = Data_Player.get(argEntity.getEntityId());
    			
    			if(argEntity.isRiding()){
    				AnimatedEntity.getByEntity(argEntity).get("riding").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
		        	BendsPack.animate(this,"player","riding");
    			}else{
	    			if(argEntity.isInWater()){
	    				AnimatedEntity.getByEntity(argEntity).get("swimming").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
	    				BendsPack.animate(this,"player","swimming");
	    			}else{
		    			if(!Data_Player.get(argEntity.getEntityId()).isOnGround() | Data_Player.get(argEntity.getEntityId()).ticksAfterTouchdown < 2){
				        	AnimatedEntity.getByEntity(argEntity).get("jump").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
				        	BendsPack.animate(this,"player","jump");
		    			}else{
					        if(Data_Player.get(argEntity.getEntityId()).motion.x == 0.0f & Data_Player.get(argEntity.getEntityId()).motion.z == 0.0f){
					        	AnimatedEntity.getByEntity(argEntity).get("stand").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
					        	BendsPack.animate(this,"player","stand");
					        }else{
					        	if(argEntity.isSprinting()){
					        		AnimatedEntity.getByEntity(argEntity).get("sprint").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
					        		BendsPack.animate(this,"player","sprint");
					        	}else{
					        		AnimatedEntity.getByEntity(argEntity).get("walk").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
					        		BendsPack.animate(this,"player","walk");
					        	}
					        }
					        
					        if(argEntity.isSneaking()){
					        	AnimatedEntity.getByEntity(argEntity).get("sneak").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
					        	BendsPack.animate(this,"player","sneak");
					        }
				        }
	    			}
    			}
    			
    			if(this.aimedBow){
    				AnimatedEntity.getByEntity(argEntity).get("bow").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
    				BendsPack.animate(this,"player","bow");
    			}else{
    				if((((EntityPlayer)argEntity).getCurrentEquippedItem() != null && ((EntityPlayer)argEntity).getCurrentEquippedItem().getItem() instanceof ItemPickaxe) ||
    					(((EntityPlayer)argEntity).getCurrentEquippedItem() != null && Block.getBlockFromItem(((EntityPlayer)argEntity).getCurrentEquippedItem().getItem()) != null)){
    					AnimatedEntity.getByEntity(argEntity).get("mining").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
    					BendsPack.animate(this,"player","mining");
    				}else if(((EntityPlayer)argEntity).getCurrentEquippedItem() != null && ((EntityPlayer)argEntity).getCurrentEquippedItem().getItem() instanceof ItemAxe){
    					AnimatedEntity.getByEntity(argEntity).get("axe").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
    					BendsPack.animate(this,"player","axe");
    				}else{
    					AnimatedEntity.getByEntity(argEntity).get("attack").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
    				}
    			}
    			
		        ((ModelRendererBends) this.bipedHead).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedHeadwear).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedBody).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedLeftArm).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedRightArm).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedLeftLeg).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedRightLeg).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedLeftForeArm).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedRightForeArm).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedLeftForeLeg).update(data.ticksPerFrame);
		        ((ModelRendererBends) this.bipedRightForeLeg).update(data.ticksPerFrame);
		        
		        this.renderOffset.update(data.ticksPerFrame);
		        this.renderRotation.update(data.ticksPerFrame);
		        this.renderItemRotation.update(data.ticksPerFrame);
		        
		        this.swordTrail.update(data.ticksPerFrame);
		        
		        data.updatedThisFrame = true;
    		}
	        Data_Player.get(argEntity.getEntityId()).syncModelInfo(this);
    }
    
    public void postRender(float argScale){
    	GlStateManager.translate(this.renderOffset.vSmooth.x*argScale,-this.renderOffset.vSmooth.y*argScale,this.renderOffset.vSmooth.z*argScale);
    	GlStateManager.rotate(this.renderRotation.getX(),1.0f,0.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getY(),0.0f,1.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
    }
    
    public void postRenderTranslate(float argScale){
    	GlStateManager.translate(this.renderOffset.vSmooth.x*argScale,-this.renderOffset.vSmooth.y*argScale,this.renderOffset.vSmooth.z*argScale);
    }
    
    public void postRenderRotate(float argScale){
    	GlStateManager.rotate(this.renderRotation.getX(),1.0f,0.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getY(),0.0f,1.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
    }
    
    public void renderRightArm()
    {
        this.bipedRightArm.render(0.0625F);
        this.bipedRightArmwear.render(0.0625F);
    }

    public void renderLeftArm()
    {
        this.bipedLeftArm.render(0.0625F);
        this.bipedLeftArmwear.render(0.0625F);
    }

    public void setInvisible(boolean invisible)
    {
        super.setInvisible(invisible);
        this.bipedLeftArmwear.showModel = invisible;
        this.bipedRightArmwear.showModel = invisible;
        this.bipedLeftLegwear.showModel = invisible;
        this.bipedRightLegwear.showModel = invisible;
        this.bipedBodyWear.showModel = invisible;
        this.bipedCape.showModel = invisible;
        this.bipedDeadmau5Head.showModel = invisible;
    }
    
	public void postRenderArm(float scale)
    {
        if (this.smallArms)
        {
            ++this.bipedRightArm.rotationPointX;
            this.bipedRightArm.postRender(scale);
            --this.bipedRightArm.rotationPointX;
        }
        else
        {
            this.bipedRightArm.postRender(scale);
        }
    }
	
	public void updateWithEntityData(AbstractClientPlayer argPlayer){
		Data_Player data = Data_Player.get(argPlayer.getEntityId());
		if(data != null){
			this.renderOffset.set(data.renderOffset);
			this.renderRotation.set(data.renderRotation);
			this.renderItemRotation.set(data.renderItemRotation);
		}
	}
}

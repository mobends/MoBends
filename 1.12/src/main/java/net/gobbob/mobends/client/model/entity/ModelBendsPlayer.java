package net.gobbob.mobends.client.model.entity;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class ModelBendsPlayer extends ModelPlayer implements IBendsModel
{
    public ModelPart bipedRightForeArm;
    public ModelPart bipedLeftForeArm;
    public ModelPart bipedRightForeLeg;
    public ModelPart bipedLeftForeLeg;
    
    public ModelPart bipedRightForeArmwear;
    public ModelPart bipedLeftForeArmwear;
    public ModelPart bipedRightForeLegwear;
    public ModelPart bipedLeftForeLegwear;
    
    public ModelPart bipedCape;
    public ModelPart bipedEars;
    public boolean smallArms;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    public EnumHandSide primaryHand = EnumHandSide.RIGHT;
    
    private HashMap nameToRendererMap;
    
    public ModelBendsPlayer(float p_i46304_1_, boolean p_i46304_2)
    {
    	this(p_i46304_1_, p_i46304_2, true);
    }
    
    public ModelBendsPlayer(float p_i46304_1_, boolean p_i46304_2_, boolean bigTexture)
    {
        super(p_i46304_1_, p_i46304_2_);
        this.boxList.clear();
        
        this.textureWidth = 64;
        this.textureHeight = bigTexture ? 64 : 32;
        
        this.smallArms = p_i46304_2_;
        /*this.bipedEars = new ModelRendererBends(this, 24, 0);
        this.bipedEars.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, p_i46304_1_);
        this.bipedCape = new ModelRendererBends(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, p_i46304_1_);
        
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_ + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody = new ModelRendererBends(this, 16, 16);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, p_i46304_1_);
        this.bipedBody.setRotationPoint(0.0F, 12.0F, 0.0F);
        this.bipedHead = new ModelRendererBendsChild(this, 0, 0).setParent((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_);
        this.bipedHead.setRotationPoint(0.0F, -12.0F, 0.0F);
        
        if (p_i46304_2_)
        {
            this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 32, 48).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 6, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.5F-12.0f, 0.0F);
            this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
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
            this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 32, 48).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F-12.0f, 0.0F);
            this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
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
        ((ModelRendererBends)this.bipedLeftLegwear).getBox().offsetTextureQuad(this.bipedLeftLegwear,ModelBoxBends.BOTTOM, 4, 0.0f);
        this.bipedLeftLegwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.bipedRightLegwear = new ModelRendererBends(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
        ((ModelRendererBends)this.bipedRightLegwear).getBox().resY-=0.25f;
        ((ModelRendererBends)this.bipedRightLegwear).getBox().updateVertexPositions(this.bipedRightLegwear);
        ((ModelRendererBends)this.bipedRightLegwear).getBox().offsetTextureQuad(this.bipedRightLegwear,ModelBoxBends.BOTTOM, 4, 0.0f);
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
        ((ModelRendererBends)this.bipedRightForeLegwear).getBox().offsetTextureQuad(this.bipedRightForeLegwear,ModelBoxBends.BOTTOM, 0, -6.0f)
        														 .offsetTextureQuad(this.bipedRightForeLegwear,ModelBoxBends.TOP, -4.0f, -6.0f);
        this.bipedLeftForeLegwear = new ModelRendererBends(this, 0, 48+6);
        this.bipedLeftForeLegwear.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_ + 0.25F);
        this.bipedLeftForeLegwear.getBox().resY-=0.25f;
        this.bipedLeftForeLegwear.getBox().offsetY+=0.25f;
        this.bipedLeftForeLegwear.getBox().updateVertexPositions(this.bipedLeftForeLegwear);
        this.bipedLeftForeLegwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        ((ModelRendererBends)this.bipedLeftForeLegwear).getBox().offsetTextureQuad(this.bipedLeftForeLegwear,ModelBoxBends.BOTTOM, 0, -6.0f)
		 														.offsetTextureQuad(this.bipedLeftForeLegwear,ModelBoxBends.TOP, -4.0f, -6.0f);
 
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
    
        nameToRendererMap = new HashMap<String, Object>();
        nameToRendererMap.put("head", bipedHead);
        nameToRendererMap.put("body", bipedBody);
        nameToRendererMap.put("leftArm", bipedLeftArm);
        nameToRendererMap.put("rightArm", bipedRightArm);
        nameToRendererMap.put("leftForeArm", bipedLeftForeArm);
        nameToRendererMap.put("rightForeArm", bipedRightForeArm);
        nameToRendererMap.put("leftLeg", bipedLeftLeg);
        nameToRendererMap.put("rightLeg", bipedRightLeg);
        nameToRendererMap.put("leftForeLeg", bipedLeftForeLeg);
        nameToRendererMap.put("rightForeLeg", bipedRightForeLeg);
        nameToRendererMap.put("rightItemRotation", renderRightItemRotation);
        nameToRendererMap.put("leftItemRotation", renderLeftItemRotation);
        nameToRendererMap.put("playerRotation", renderRotation);*/
    }
    
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);
        
        GL11.glPushMatrix();
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
        GL11.glPopMatrix();
    }
    
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity)
    {
    	Data_Player data = (Data_Player) EntityData.get(EntityData.PLAYER_DATA, argEntity.getEntityId());
    	AnimatedEntity aEntity = AnimatedEntity.getByEntity(argEntity);
    	
    	if(Minecraft.getMinecraft().world == null)
    		return;
    	
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	this.headRotationX = MathHelper.wrapDegrees(argHeadX);
    	this.headRotationY = MathHelper.wrapDegrees(argHeadY);
    	
    	((ModelPart) this.bipedHead).sync(data.head);
    	((ModelPart) this.bipedHeadwear).sync(data.headwear);
    	((ModelPart) this.bipedBody).sync(data.body);
    	((ModelPart) this.bipedRightArm).sync(data.rightArm);
    	((ModelPart) this.bipedLeftArm).sync(data.leftArm);
    	((ModelPart) this.bipedRightLeg).sync(data.rightLeg);
    	((ModelPart) this.bipedLeftLeg).sync(data.leftLeg);
    	((ModelPart) this.bipedRightForeArm).sync(data.rightForeArm);
    	((ModelPart) this.bipedLeftForeArm).sync(data.leftForeArm);
    	((ModelPart) this.bipedRightForeLeg).sync(data.rightForeLeg);
    	((ModelPart) this.bipedLeftForeLeg).sync(data.leftForeLeg);
    	
    	this.renderOffset.set(data.renderOffset);
    	this.renderRotation.set(data.renderRotation);
    	this.renderRightItemRotation.set(data.renderRightItemRotation);
    	this.renderLeftItemRotation.set(data.renderLeftItemRotation);
    	this.swordTrail = data.swordTrail;
    	
    	if(this.isRenderedInGui()){
    		((ModelPart) this.bipedHead).rotation.setY(this.headRotationY);
    		((ModelPart) this.bipedHead).rotation.setX(this.headRotationX);
    	}
    	
    	if(data.canBeUpdated()){
    		this.renderOffset.setSmooth(new Vector3f(0,-1f,0),0.5f);
    		this.renderRotation.setSmooth(new Vector3f(0,0,0),0.5f);
    		this.renderRightItemRotation.setSmooth(new Vector3f(0,0,0),0.5f);
    		this.renderLeftItemRotation.setSmooth(new Vector3f(0,0,0),0.5f);
    		
    		((ModelPart) this.bipedHead).resetScale();
    		((ModelPart) this.bipedHeadwear).resetScale();
    		((ModelPart) this.bipedBody).resetScale();
    		((ModelPart) this.bipedRightArm).resetScale();
    		((ModelPart) this.bipedLeftArm).resetScale();
    		((ModelPart) this.bipedRightLeg).resetScale();
    		((ModelPart) this.bipedLeftLeg).resetScale();
    		((ModelPart) this.bipedRightForeArm).resetScale();
    		((ModelPart) this.bipedLeftForeArm).resetScale();
    		((ModelPart) this.bipedRightForeLeg).resetScale();
    		((ModelPart) this.bipedLeftForeLeg).resetScale();
    		
    		BendsVariable.tempData = data;
    		
    		/**
    		 * Handling getting of a ladder, to instantly set the renderRotation parameter
    		 * without dealing with the smooth interpolation, which can make
    		 * the player do a 360 no scope ;).
    		 */
    		
    		boolean handleLadderGetoff = false;
    		if(!data.calcClimbing() && data.climbing){
    			data.climbing = false;
    			handleLadderGetoff = true;
    		}
    		
    		if(((EntityLivingBase)argEntity).isElytraFlying()){
    			aEntity.getAnimation("elytra").animate((EntityLivingBase)argEntity, this, data);
		        BendsPack.animate(this,"player","elytra");
    		}else if(argEntity.isRiding()){
    			aEntity.getAnimation("riding").animate((EntityLivingBase)argEntity, this, data);
		        BendsPack.animate(this,"player","riding");
    		}else if(data.isClimbing()){
				aEntity.getAnimation("climbing").animate((EntityLivingBase)argEntity, this, data);
    			BendsPack.animate(this,"player","climbing");
			}else if(argEntity.isInWater()){
    			aEntity.getAnimation("swimming").animate((EntityLivingBase)argEntity, this, data);
    			BendsPack.animate(this,"player","swimming");
    		}else if(!data.isOnGround() | data.ticksAfterTouchdown < 2){
    			aEntity.getAnimation("jump").animate((EntityLivingBase)argEntity, this, data);
		        BendsPack.animate(this,"player","jump");
    		}else{
    			if(data.motion.x == 0.0f & data.motion.z == 0.0f){
    				aEntity.getAnimation("stand").animate((EntityLivingBase)argEntity, this, data);
			        BendsPack.animate(this,"player","stand");
    			}else{
			        if(argEntity.isSprinting()){
			        	aEntity.getAnimation("sprint").animate((EntityLivingBase)argEntity, this, data);
			        	BendsPack.animate(this,"player","sprint");
			        }else{
			        	aEntity.getAnimation("walk").animate((EntityLivingBase)argEntity, this, data);
			        	BendsPack.animate(this,"player","walk");
			        }
    			}
    			
    			if(argEntity.isSneaking()){
    				aEntity.getAnimation("sneak").animate((EntityLivingBase)argEntity, this, data);
			        BendsPack.animate(this,"player","sneak");
    			}
			}
    		
    		if(handleLadderGetoff) {
    			this.renderRotation.setY(this.renderRotation.vFinal.y);
    		}
    		
    		this.primaryHand = ((EntityPlayer)argEntity).getPrimaryHand();
    		boolean mainHand = ((EntityPlayer)argEntity).getPrimaryHand() == EnumHandSide.LEFT;
    		
    		ItemStack leftHandItemStack = ((EntityPlayer)argEntity).getHeldItem(mainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
    		if(leftHandItemStack != null && leftHandItemStack.getItem() == Items.BOW && ((EntityPlayer)argEntity).getItemInUseMaxCount() != 0){
    			this.leftArmPose = ArmPose.BOW_AND_ARROW;
    		}
    		
    		ItemStack rightHandItemStack = ((EntityPlayer)argEntity).getHeldItem(mainHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
    		if(rightHandItemStack != null && rightHandItemStack.getItem() == Items.BOW && ((EntityPlayer)argEntity).getItemInUseMaxCount() != 0){
    			this.rightArmPose = ArmPose.BOW_AND_ARROW;
    		}
    		
    		if(this.shouldPerformGuardingAnimation((EntityPlayer)argEntity)){
    			aEntity.getAnimation("guard").animate((EntityLivingBase)argEntity, this, data);
    			if(this.rightArmPose == ArmPose.BLOCK){
    				this.renderRightItemRotation.set(45.0f, -30.0f, 50.0f);
    			}else{
    				this.renderLeftItemRotation.set(45.0f, 30.0f, -50.0f);
    			}
    		}else{
	    		if(this.leftArmPose == ArmPose.BOW_AND_ARROW || this.rightArmPose == ArmPose.BOW_AND_ARROW){
	    			aEntity.getAnimation("bow").animate((EntityLivingBase)argEntity, this, data);
	    			BendsPack.animate(this,"player","bow");
	    		}else{
	    			ItemStack heldItem = ((EntityPlayer)argEntity).getHeldItem(EnumHand.MAIN_HAND);
	    			if((heldItem != null && heldItem.getItem() instanceof ItemPickaxe) ||
	    				(heldItem != null && Block.getBlockFromItem(heldItem.getItem()) != Blocks.AIR)){
	    				aEntity.getAnimation("mining").animate((EntityLivingBase)argEntity, this, data);
	    				BendsPack.animate(this,"player","mining");
	    			}else if(((EntityPlayer)argEntity).getHeldItem(EnumHand.MAIN_HAND) != null && ((EntityPlayer)argEntity).getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemAxe){
	    				aEntity.getAnimation("axe").animate((EntityLivingBase)argEntity, this, data);
	    				BendsPack.animate(this,"player","axe");
	    			}else{
	    				aEntity.getAnimation("attack").animate((EntityLivingBase)argEntity, this, data);
	    			}
	    		}
    		}
    		
    		updateModelRenderers(data);
		    
		    data.updatedThisFrame = true;
		    if(!this.isRenderedInGui()){
		    	data.syncModelInfo(this);
		    }
    	}
    	
    	if(!data.isInitialized()){
    		data.initModelPose();
    	}
    }
    
    public void renderCape(float scale)
    {
        this.bipedCape.render(scale);
    }
    
    public void updateModelRenderers(Data_Player data) {
    	for(int i = 0; i < this.boxList.size(); i++) {
			if(this.boxList.get(i) instanceof ModelPart)
				((ModelPart)this.boxList.get(i)).update(DataUpdateHandler.ticksPerFrame);
		}
    	
    	this.renderOffset.update(DataUpdateHandler.ticksPerFrame);
	    this.renderRotation.update(DataUpdateHandler.ticksPerFrame);
	    this.renderRightItemRotation.update(DataUpdateHandler.ticksPerFrame);
	    this.renderLeftItemRotation.update(DataUpdateHandler.ticksPerFrame);
	    this.swordTrail.update(DataUpdateHandler.ticksPerFrame);
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
    
	public void postRenderArm(float argScale)
    {
        this.bipedRightArm.postRender(argScale);
		this.bipedRightForeArm.postRender(argScale);
		GL11.glTranslatef(0.0f*argScale, (-4.0f+8)*argScale, -2.0f*argScale);
		GL11.glRotatef(this.renderRightItemRotation.vSmooth.x,1,0,0);
		GL11.glRotatef(this.renderRightItemRotation.vSmooth.y,0,-1,0);
		GL11.glRotatef(this.renderRightItemRotation.vSmooth.z,0,0,1);
		GL11.glTranslatef(0.0f*argScale, -8.0f*argScale, 0.0f*argScale);
    }
	
	public void postRenderArm(float scale, EnumHandSide side)
    {
        ModelPart armModelRenderer = (ModelPart) this.getArmForSide(side);
        ModelPart foreArmModelRenderer = (ModelPart) this.getForeArmForSide(side);

        if (this.smallArms)
        {
            float f = 0.5F * (float)(side == EnumHandSide.RIGHT ? 1 : -1);
            armModelRenderer.rotationPointX += f;
            armModelRenderer.postRender(scale);
            armModelRenderer.rotationPointX -= f;
        }
        else
        {
            armModelRenderer.postRender(scale);
        }
        GL11.glTranslatef(0.0f*scale, (-1.0f + 8.0f)*scale, 0.0f*scale);
        if(side == EnumHandSide.RIGHT){
	        GL11.glRotatef(this.renderRightItemRotation.vSmooth.x,1,0,0);
			GL11.glRotatef(this.renderRightItemRotation.vSmooth.y,0,-1,0);
			GL11.glRotatef(this.renderRightItemRotation.vSmooth.z,0,0,1);
        }else{
        	GL11.glRotatef(this.renderLeftItemRotation.vSmooth.x,1,0,0);
			GL11.glRotatef(this.renderLeftItemRotation.vSmooth.y,0,-1,0);
			GL11.glRotatef(this.renderLeftItemRotation.vSmooth.z,0,0,1);
        }
		GL11.glTranslatef(0.0f*scale, -8.0f*scale, 0.0f*scale);
    }
	
	protected ModelRenderer getForeArmForSide(EnumHandSide side)
    {
        return side == EnumHandSide.LEFT ? this.bipedLeftForeArm : this.bipedRightForeArm;
    }
	
	public void updateWithEntityData(AbstractClientPlayer argPlayer){
		Data_Player data = (Data_Player) EntityData.get(EntityData.PLAYER_DATA, argPlayer.getEntityId());
		if(data != null){
			this.renderOffset.set(data.renderOffset);
			this.renderRotation.set(data.renderRotation);
			this.renderRightItemRotation.set(data.renderRightItemRotation);
			this.renderLeftItemRotation.set(data.renderLeftItemRotation);
		}
	}
	
	public boolean shouldPerformGuardingAnimation(EntityPlayer player)
    {
        if (player.isHandActive() && player.getActiveItemStack() != null)
        {
            Item item = player.getActiveItemStack().getItem();
            return item.getItemUseAction(player.getActiveItemStack()) == EnumAction.BLOCK;
        }
        else
        {
            return false;
        }
    }
	
	public boolean isRenderedInGui() {
		return EventHandlerRenderPlayer.renderingGuiScreen;
	}
	
	public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        this.bipedRightForeArm.showModel = visible;
        this.bipedLeftForeArm.showModel = visible;
        this.bipedRightForeLeg.showModel = visible;
        this.bipedLeftForeLeg.showModel = visible;
        
        this.bipedRightForeArmwear.showModel = visible;
        this.bipedLeftForeArmwear.showModel = visible;
        this.bipedRightForeLegwear.showModel = visible;
        this.bipedLeftForeLegwear.showModel = visible;
        
        this.bipedCape.showModel = visible;
        this.bipedEars.showModel = visible;
    }

	@Override
	public Object getPartForName(String name) {
		return nameToRendererMap.get(name);
	}
}

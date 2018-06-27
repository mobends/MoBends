package net.gobbob.mobends.client.model.entity;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.client.event.PlayerRenderHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartExtended;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.data.DataSkeleton;
import net.gobbob.mobends.data.ZombieData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;

public class ModelBipedArmorM extends ModelBiped
{
    public ModelPart bipedRightForeArm;
    public ModelPart bipedLeftForeArm;
    public ModelPart bipedRightForeLeg;
    public ModelPart bipedLeftForeLeg;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    
    public ModelBipedArmorM(float modelSize)
    {
    	this(modelSize, 0);
    }
    
    public ModelBipedArmorM(float modelSize, float offset)
    {
        super(modelSize, offset, 64, 32);
        
        this.textureWidth = 64;
        this.textureHeight = 32;
        
        this.bipedHeadwear = new ModelPart(this, 32, 0).setPosition(0.0F, 0.0F, 0.0F);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize + 0.5F);
        this.bipedBody = new ModelPart(this, 16, 16).setPosition(0.0F, 12.0F, 0.0F);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, modelSize);
        this.bipedHead = new ModelPartChild(this, 0, 0).setParent((IModelPart) this.bipedBody).setPosition(0.0F, -12.0F, 0.0F);;
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize);
        this.bipedHead.addChild(this.bipedHeadwear);
        
        // Arms
        float armHeight = -10.0F;
        this.bipedLeftArm = new ModelPartChildExtended(this, 40, 16)
        		.setParent((IModelPart) this.bipedBody).setHideLikeParent(true)
        		.setPosition(5.0F, armHeight, 0.0F);
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, modelSize);
        this.bipedLeftArm.mirror = true;
        this.bipedRightArm = new ModelPartChildExtended(this, 40, 16)
        		.setParent((IModelPart) this.bipedBody).setHideLikeParent(true)
        		.setPosition(-5.0F, armHeight, 0.0F);
        this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, modelSize);
        ((ModelPart)this.bipedRightArm).offsetBoxBy(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelPart)this.bipedLeftArm).offsetBoxBy(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        
        this.bipedLeftForeArm = new ModelPart(this, 40, 16+6).setPosition(0.0F, 4.0F, 2.0F);;
        this.bipedLeftForeArm.addBox(-1.0F, 0.0F, -4.0F, 4, 6, 4, modelSize);
        this.bipedRightForeArm = new ModelPart(this, 40, 16+6).setPosition(0.0F, 4.0F, 2.0F);
        this.bipedRightForeArm.addBox(-2.0F, 0.0F, -4.0F, 4, 6, 4, modelSize);
        
        ((ModelPart)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm, ModelBox.BOTTOM, 0, -6.0f);
        ((ModelPart)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm, ModelBox.TOP, -4.0f, -6.0f);
        ((ModelPart)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, ModelBox.BOTTOM, 0, -6.0f);
        ((ModelPart)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, ModelBox.TOP, -4.0f, -6.0f);
        
        // Legs
        this.bipedRightLeg = new ModelPartExtended(this, 0, 16).setPosition(-1.9F, 12.0F, 0.0F);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, modelSize);
        ((ModelPart)this.bipedRightLeg).offsetBoxBy(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        this.bipedLeftLeg = new ModelPartExtended(this, 0, 16).setPosition(1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, modelSize);
        ((ModelPart)this.bipedLeftLeg).offsetBoxBy(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        this.bipedLeftLeg.mirror = true;
        
        this.bipedRightForeLeg = new ModelPart(this, 0, 16+6).setPosition(0, 6.0F, -2.0F);
        this.bipedRightForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, modelSize);
        ((ModelPart)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        ((ModelPartExtended)this.bipedRightLeg).setExtension(this.bipedRightForeLeg);
        this.bipedLeftForeLeg = new ModelPart(this, 0, 16+6).setPosition(0, 6.0F, -2.0F);
        this.bipedLeftForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, modelSize);
        ((ModelPart)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        this.bipedLeftForeLeg.mirror = true;
        ((ModelPartExtended)this.bipedLeftLeg).setExtension(this.bipedLeftForeLeg);
    }
    
    public void render(Entity entity, float swingTime, float swingAmount, float armSway, float headYaw, float headPitch, float scale)
    {
        this.setRotationAngles(swingTime, swingAmount, armSway, headYaw, headPitch, scale, entity);
        
        GL11.glPushMatrix();
        if (this.isChild)
        {
            float f6 = 2.0F;
            GL11.glPushMatrix();
            GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
            GL11.glTranslatef(0.0F, 16.0F * scale, 0.0F);
            this.bipedHead.render(scale);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            GL11.glPopMatrix();
        }
        else
        {
        	if (entity.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
        	
        	this.bipedHead.render(scale);
            this.bipedBody.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
        }
        GL11.glPopMatrix();
    }
    
    public void setRotationAngles(float swingTime, float swingAmount, float armSway, float headYaw, float headPitch, float scale, Entity entity)
    {
    	if(Minecraft.getMinecraft().world == null)
    		return;
    	
    	EntityData entityData = EntityDatabase.instance.get(entity.getEntityId());
    	if(entityData == null || !(entityData instanceof BipedEntityData))
    		return;
    	
    	BipedEntityData dataBiped = (BipedEntityData) entityData;
    	
    	((ModelPart) this.bipedHead).syncUp(dataBiped.head);
    	((ModelPart) this.bipedHeadwear).syncUp(dataBiped.headwear);
    	((ModelPart) this.bipedBody).syncUp(dataBiped.body);
    	((ModelPart) this.bipedRightArm).syncUp(dataBiped.rightArm);
    	((ModelPart) this.bipedLeftArm).syncUp(dataBiped.leftArm);
    	((ModelPart) this.bipedRightLeg).syncUp(dataBiped.rightLeg);
    	((ModelPart) this.bipedLeftLeg).syncUp(dataBiped.leftLeg);
    	((ModelPart) this.bipedRightForeArm).syncUp(dataBiped.rightForeArm);
    	((ModelPart) this.bipedLeftForeArm).syncUp(dataBiped.leftForeArm);
    	((ModelPart) this.bipedRightForeLeg).syncUp(dataBiped.rightForeLeg);
    	((ModelPart) this.bipedLeftForeLeg).syncUp(dataBiped.leftForeLeg);
    }
	
	@Override
	public void setVisible(boolean visible)
    {
        super.setVisible(visible);
    }
}

package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsCustomHead;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsElytra;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsHeldItem;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsBipedArmor;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.settings.SettingManager;
import net.gobbob.mobends.settings.SettingsBoolean;
import net.gobbob.mobends.settings.SettingsNode;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderBendsPlayer extends RenderPlayer{
    private boolean smallArms;
	private int refreshModel;
    
    public RenderBendsPlayer(RenderManager renderManager)
    {
        super(renderManager, false);
        this.mainModel = new ModelBendsPlayer(0.0F, this.smallArms);
        this.layerRenderers.clear();
        this.addLayer(new LayerBendsBipedArmor(this));
        this.addLayer(new LayerBendsHeldItem(this));
        //this.addLayer(new LayerArrow(this));
        //this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerBendsCustomHead(this.getMainModel().bipedHead));
        this.addLayer(new LayerBendsElytra(this));
    }

    public RenderBendsPlayer(RenderManager renderManager, boolean useSmallArms)
    {
    	super(renderManager, useSmallArms);
    	this.smallArms = useSmallArms;
    	this.mainModel = new ModelBendsPlayer(0.0F, this.smallArms);
    	this.layerRenderers.clear();
    	this.addLayer(new LayerBendsBipedArmor(this));
        this.addLayer(new LayerBendsHeldItem(this));
        //this.addLayer(new LayerArrow(this));
        //this.addLayer(new LayerDeadmau5Head(this));
        //this.addLayer(new LayerBendsCape(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerBendsCustomHead(((ModelBendsPlayer)this.getMainModel()).bipedHead));
        this.addLayer(new LayerBendsElytra(this));
    }
    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    @Override
    protected void preRenderCallback(AbstractClientPlayer clientPlayer, float p_77041_2_)
    {
    	if(this.refreshModel != MoBends.refreshModel){
    		this.refreshModel = MoBends.refreshModel;
    		this.mainModel = new ModelBendsPlayer(0.0F, this.smallArms);
    	}
    	
        float f1 = 0.9375F;
        GlStateManager.scale(f1, f1, f1);
        
        ((ModelBendsPlayer)this.mainModel).updateWithEntityData(clientPlayer);
        ((ModelBendsPlayer)this.mainModel).postRenderTranslate(0.0625f);
    
        Data_Player data = (Data_Player) EntityData.get(EntityData.PLAYER_DATA, clientPlayer.getEntityId());
    
        if(SettingManager.SWORD_TRAIL.isEnabled()){
			GL11.glPushMatrix();
				float f5 = 0.0625F;
				GL11.glScalef(-f5, -f5, f5);
				data.swordTrail.render();
				GL11.glColor4f(1,1,1,1);
			GL11.glPopMatrix();
        }
        
        ((ModelBendsPlayer)this.mainModel).postRenderRotate(0.0625f);
        
        this.setModelVisibilities(clientPlayer);
    }
    
    @Override
    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float p_76986_8_, float partialTicks)
    {
    	super.doRender(entity, x, y, z, p_76986_8_, partialTicks);
    }
    
    public ModelPlayer getMainModel()
    {
    	if(!(this.mainModel instanceof ModelBendsPlayer)){
    		this.mainModel = new ModelBendsPlayer(0.0F, this.smallArms);
    	}
    	return (ModelBendsPlayer)this.mainModel;
    }
    
    private void setModelVisibilities(AbstractClientPlayer clientPlayer)
    {
        ModelBendsPlayer modelplayer = (ModelBendsPlayer) this.getMainModel();

        if (clientPlayer.isSpectator())
        {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        }
        else
        {
            ItemStack itemstack = clientPlayer.getHeldItemMainhand();
            ItemStack itemstack1 = clientPlayer.getHeldItemOffhand();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedLeftForeLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedRightForeLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedLeftForeArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.bipedRightForeArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.isSneak = clientPlayer.isSneaking();
            ModelBiped.ArmPose modelbiped$armpose = ModelBiped.ArmPose.EMPTY;
            ModelBiped.ArmPose modelbiped$armpose1 = ModelBiped.ArmPose.EMPTY;

            if (!itemstack.isEmpty())
            {
                modelbiped$armpose = ModelBiped.ArmPose.ITEM;

                if (clientPlayer.getItemInUseCount() > 0)
                {
                    EnumAction enumaction = itemstack.getItemUseAction();

                    if (enumaction == EnumAction.BLOCK)
                    {
                        modelbiped$armpose = ModelBiped.ArmPose.BLOCK;
                    }
                    else if (enumaction == EnumAction.BOW)
                    {
                        modelbiped$armpose = ModelBiped.ArmPose.BOW_AND_ARROW;
                    }
                }
            }

            if (!itemstack1.isEmpty())
            {
                modelbiped$armpose1 = ModelBiped.ArmPose.ITEM;

                if (clientPlayer.getItemInUseCount() > 0)
                {
                    EnumAction enumaction1 = itemstack1.getItemUseAction();

                    if (enumaction1 == EnumAction.BLOCK)
                    {
                        modelbiped$armpose1 = ModelBiped.ArmPose.BLOCK;
                    }
                    // FORGE: fix MC-88356 allow offhand to use bow and arrow animation
                    else if (enumaction1 == EnumAction.BOW)
                    {
                        modelbiped$armpose1 = ModelBiped.ArmPose.BOW_AND_ARROW;
                    }
                }
            }

            if (clientPlayer.getPrimaryHand() == EnumHandSide.RIGHT)
            {
                modelplayer.rightArmPose = modelbiped$armpose;
                modelplayer.leftArmPose = modelbiped$armpose1;
            }
            else
            {
                modelplayer.rightArmPose = modelbiped$armpose1;
                modelplayer.leftArmPose = modelbiped$armpose;
            }
        }
    }
}

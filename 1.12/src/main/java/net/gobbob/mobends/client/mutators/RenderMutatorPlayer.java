package net.gobbob.mobends.client.mutators;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartExtended;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class RenderMutatorPlayer implements IBendsModel
{
	public static HashMap<RenderPlayer, RenderMutatorPlayer> mutatorMap = new HashMap<RenderPlayer, RenderMutatorPlayer>();
	
	public ModelPart body;
	public ModelPartChild head;
	public ModelPartChildExtended leftArm;
	public ModelPartChildExtended rightArm;
	public ModelPartExtended leftForeArm;
	public ModelPartExtended rightForeArm;
	public ModelPartExtended leftLeg;
	public ModelPartExtended rightLeg;
	public ModelPart leftForeLeg;
	public ModelPart rightForeLeg;
	public ModelPartTransform leftItemTransform;
	public ModelPartTransform rightItemTransform;
	
	protected HashMap<String, IModelPart> nameToPartMap;
	
	public float headYaw, headPitch;
	public List<LayerRenderer<EntityLivingBase>> layerRenderers;
	
	public RenderMutatorPlayer()
	{
		nameToPartMap = new HashMap<String, IModelPart>();
	}
	
	private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException
	{
		try
		{
			return clazz.getDeclaredField(fieldName);
		}
		catch (NoSuchFieldException e)
		{
			Class superClass = clazz.getSuperclass();
			System.out.println("Getting super class" + superClass);
			if (superClass == null)
			{
				throw e;
			}
			else
			{
				return getField(superClass, fieldName);
			}
		}
	}
	
	public void fetchLayers(RenderPlayer renderer) {
		this.layerRenderers = null;
		Field field = null;
		
		try
		{
			//field_177097_h is the obfuscated name of layerRenders.
			field = getField(renderer.getClass(), "field_177097_h");
		}
		catch (NoSuchFieldException e)
		{
			try
			{
				field = getField(renderer.getClass(), "layerRenderers");
			}
			catch (NoSuchFieldException e1) {}
		}
		catch (SecurityException | IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		if(field == null) return;
		
		field.setAccessible(true);
		List<LayerRenderer<EntityLivingBase>> layers = null;
		try
		{
			layers = (List<LayerRenderer<EntityLivingBase>>) field.get(renderer);
		} catch (IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		this.layerRenderers = layers;
	}
	
	public void mutate(RenderPlayer renderer)
	{
		fetchLayers(renderer);
		
		if(this.layerRenderers != null)
		{
			for(LayerRenderer<EntityLivingBase> layer : layerRenderers)
			{
				if(layer instanceof LayerBipedArmor)
				{
					System.out.println(layer);
				}
			}
		}
		
		// Model Section
		ModelPlayer model = renderer.getMainModel();
		float scaleFactor = 0.0f;
		
		// Body
		model.bipedBody = body = new ModelPart(model, 16, 16).setPosition(0.0F, 12.0F, 0.0F);
		body.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);
		
		// Head
		model.bipedHead = head = (ModelPartChild) new ModelPartChild(model, 0, 0)
				.setParent(body).setHideLikeParent(false)
				.setPosition(0.0F, -12.0F, 0.0F);
		head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
		
		// Arms
		int armWidth = 4;
		float armHeight = -10.0f;
		model.bipedLeftArm = leftArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 32, 48)
				.setParent(body).setHideLikeParent(false)
				.setPosition(5.0F, armHeight, 0.0F);
		leftArm.addBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
		model.bipedRightArm = rightArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 40, 16)
				.setParent(body).setHideLikeParent(false)
				.setPosition(-5.0F, armHeight, 0.0F);
		rightArm.addBox(-3.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
		
		rightArm.offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        leftArm.offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
    
        leftForeArm = new ModelPartExtended(model, 32, 48+6);
        leftForeArm.addBox(-1.0F, 0.0F, -4.0F, 4, 6, 4, scaleFactor);
        leftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
        leftForeArm.getBox().offsetTextureQuad(leftForeArm, ModelBox.BOTTOM, 0, -6.0f);
        leftArm.setExtension(leftForeArm);
        rightForeArm = new ModelPartExtended(model, 40, 16+6);
        rightForeArm.addBox(-3.0F, 0.0F, -4.0F, 4, 6, 4, scaleFactor);
        rightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
        rightForeArm.getBox().offsetTextureQuad(rightForeArm, ModelBox.BOTTOM, 0, -6.0f);
        rightArm.setExtension(rightForeArm);
        
        // Items
        leftItemTransform = new ModelPartTransform();
        leftForeArm.setExtension(leftItemTransform);
        rightItemTransform = new ModelPartTransform();
        rightForeArm.setExtension(rightItemTransform);
        
        // Legs
        model.bipedRightLeg = rightLeg = (ModelPartExtended) new ModelPartExtended(model, 0, 16)
        		.setPosition(-1.9F, 12.0F, 0.0F);
        rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
        model.bipedLeftLeg = leftLeg = (ModelPartExtended)  new ModelPartExtended(model, 16, 48)
        		.setPosition(1.9F, 12.0F, 0.0F);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
        
        leftForeLeg = new ModelPart(model, 16, 48+6).setPosition(0, 6.0F, -2.0F);
        leftForeLeg.addModelBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
        		.offsetTextureQuad(leftForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        leftLeg.setExtension(leftForeLeg);
        rightForeLeg = new ModelPart(model, 0, 16+6).setPosition(0, 6.0F, -2.0F);
        rightForeLeg.addModelBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
        		.offsetTextureQuad(rightForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        rightLeg.setExtension(rightForeLeg);
        
		//TODO remove the cape if you start to draw your own please.
		//renderer.getMainModel().boxList.remove(o);
        
        nameToPartMap = new HashMap<String, IModelPart>();
        nameToPartMap.put("body", body);
        nameToPartMap.put("head", head);
        nameToPartMap.put("leftArm", leftArm);
        nameToPartMap.put("rightArm", rightArm);
        nameToPartMap.put("leftLeg", leftLeg);
        nameToPartMap.put("rightLeg", rightLeg);
        nameToPartMap.put("leftForeArm", leftForeArm);
        nameToPartMap.put("rightForeArm", rightForeArm);
        nameToPartMap.put("leftForeLeg", leftForeLeg);
        nameToPartMap.put("rightForeLeg", rightForeLeg);
	}
	
	public void updateModel(AbstractClientPlayer player, RenderPlayer renderer, float partialTicks)
	{
		boolean shouldSit = player.isRiding() && (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit());
		float f = GUtil.interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
        float f1 = GUtil.interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialTicks);
        float yaw = f1 - f;
        
        if (shouldSit && player.getRidingEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)player.getRidingEntity();
            f = GUtil.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
            yaw = f1 - f;
            float f3 = MathHelper.wrapDegrees(yaw);

            if (f3 < -85.0F)
                f3 = -85.0F;
            if (f3 >= 85.0F)
                f3 = 85.0F;

            f = f1 - f3;

            if (f3 * f3 > 2500.0F)
                f += f3 * 0.2F;

            yaw = f1 - f;
        }

        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!player.isRiding())
        {
            f5 = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * partialTicks;
            f6 = player.limbSwing - player.limbSwingAmount * (1.0F - partialTicks);

            if (player.isChild())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
            yaw = f1 - f;
        }
        
        this.headYaw = yaw;
        this.headPitch = pitch;
        performAnimations(player, renderer, yaw, pitch, partialTicks);
        updateParts();
	}
	
	public void performAnimations(AbstractClientPlayer player, RenderPlayer renderer, float yaw, float pitch, float partialTicks)
	{
		Data_Player data = (Data_Player) EntityData.get(EntityData.PLAYER_DATA, player.getEntityId());
    	AnimatedEntity animatedEntity = AnimatedEntity.getByEntity(player);
		float ticks = player.ticksExisted + partialTicks;
		
		leftItemTransform.position.setY(-3);
		rightItemTransform.position.setY(-3);
		
		animatedEntity.getAnimation("stand").animate((EntityLivingBase)player, this, data);
        BendsPack.animate(this, "player", "stand");
	}
	
	public void updateParts() {
    	for(IModelPart modelPart : nameToPartMap.values()) {
    		modelPart.update(DataUpdateHandler.ticksPerFrame);
		}
    }
	
	public static void apply(RenderPlayer renderer, AbstractClientPlayer entityPlayer, float partialTicks)
	{
		RenderMutatorPlayer mutator = mutatorMap.get(renderer);
		if(!mutatorMap.containsKey(renderer))
		{
			mutator = new RenderMutatorPlayer();
			mutator.mutate(renderer);
			mutatorMap.put(renderer, mutator);
		}
		
		mutator.updateModel(entityPlayer, renderer, partialTicks);
	}
	
	/*
	 * Used to refresh the mutators in case of real-time
	 * changes during development.
	 */
	public static void refresh() {
		for(Map.Entry<RenderPlayer, RenderMutatorPlayer> mutator : mutatorMap.entrySet()) {
			mutator.getValue().mutate(mutator.getKey());
		}
	}

	@Override
	public Object getPartForName(String name)
	{
		return nameToPartMap.get(name);
	}
}

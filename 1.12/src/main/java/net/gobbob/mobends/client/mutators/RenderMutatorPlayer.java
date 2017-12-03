package net.gobbob.mobends.client.mutators;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.ModelRendererBendsChild;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class RenderMutatorPlayer
{
	public static HashMap<RenderPlayer, RenderMutatorPlayer> mutatorMap = new HashMap<RenderPlayer, RenderMutatorPlayer>();
	
	protected ModelRendererBends leftForeArm;
	protected ModelRendererBends body;
	protected ModelRendererBends head;
	
	public static float interpolateRotation(float a, float b, float partialTicks)
    {
        float f;
        for (f = b - a; f < -180.0F; f += 360.0F);

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return a + partialTicks * f;
    }
	
	public void mutate(RenderPlayer renderer)
	{
		ModelPlayer model = renderer.getMainModel();
		
		float scaleFactor = 0.0f;
		body = new ModelRendererBends(model, 16, 16);
		body.addBox(-4.0F, -12.0F, -2.0F, 8, 6, 4, scaleFactor);
		body.setRotationPoint(0.0F, 12.0F, 0.0F);
		model.bipedBody = body;
		
		head = new ModelRendererBendsChild(model, 0, 0).setParent(body).setShowChildIfHidden(true);
		head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
		head.setRotationPoint(0.0F, -12.0F, 0.0F);
		model.bipedHead = head;
		
		//TODO remove the cape if you start to draw your own please.
		//renderer.getMainModel().boxList.remove(o);
	}
	
	public void updateModel(AbstractClientPlayer player, RenderPlayer renderer, float partialTicks)
	{
		boolean shouldSit = player.isRiding() && (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit());
		float f = interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
        float f1 = interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialTicks);
        float yaw = f1 - f;
        
        if (shouldSit && player.getRidingEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)player.getRidingEntity();
            f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
            yaw = f1 - f;
            float f3 = MathHelper.wrapDegrees(yaw);

            if (f3 < -85.0F)
            {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F)
            {
                f3 = 85.0F;
            }

            f = f1 - f3;

            if (f3 * f3 > 2500.0F)
            {
                f += f3 * 0.2F;
            }

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
            {
                f6 *= 3.0F;
            }

            if (f5 > 1.0F)
            {
                f5 = 1.0F;
            }
            yaw = f1 - f; // Forge: Fix MC-1207
        }
        
        performAnimations(player, renderer, yaw, pitch, partialTicks);
	}
	
	public void performAnimations(AbstractClientPlayer player, RenderPlayer renderer, float yaw, float pitch, float partialTicks)
	{
		ModelPlayer model = renderer.getMainModel();
		body = (ModelRendererBends) model.bipedBody;
		head = (ModelRendererBends) model.bipedHead;
		head.rotation.setY(yaw);
		head.rotation.setX(pitch);
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
}

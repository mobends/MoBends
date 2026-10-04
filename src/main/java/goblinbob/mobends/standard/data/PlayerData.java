package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.ModStatics;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * The Java player's data. What the player's animator reads of the entity (its sprint-jump leg,
 * how fast and steeply it flies) and its attack combo come from the player's model definition, as
 * for the defined player.
 */
public class PlayerData extends BipedEntityData<AbstractClientPlayer>
{
	public ModelPartTransform cape;

	private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/player.json");
	private static final ResourceLocation DEFINITION = new ResourceLocation(ModStatics.MODID, "bends/models/player.json");

	public PlayerData(AbstractClientPlayer entity)
	{
		super(entity);
	}

	@Override
	protected ResourceLocation getDefaultAnimator()
	{
		return ANIMATOR;
	}

	@Override
	protected ResourceLocation getModelDefinition()
	{
		return DEFINITION;
	}

	@Override
	public void initModelPose()
	{
		super.initModelPose();
		
		Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(this.entity);

		EntityModelDefinition definition = loadModelDefinition();
		setAttackComboTicks(definition == null ? 0 : definition.attackComboTicks);
		addComponent("capeWave", new CapeWave(entity));
		cape = new ModelPartTransform(body);
		nameToPartMap.put("cape", cape);
		cape.position.set(0F, 0F, 0F);

		if (((RenderPlayer) render).smallArms)
		{
			rightArm.position.set(-5F, -9.5F, 0F);
			leftArm.position.set(5F, -9.5F, 0F);
		}
	}

	@Override
	public void updateParts(float ticksPerFrame)
	{
		super.updateParts(ticksPerFrame);

		cape.update(ticksPerFrame);
	}

}

package net.gobbob.mobends.standard.data;

import net.gobbob.mobends.core.LivingEntityData;
import net.gobbob.mobends.core.client.model.ModelPartTransform;
import net.gobbob.mobends.standard.animation.controller.SquidController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySquid;

public class SquidData extends LivingEntityData<SquidData, EntitySquid>
{
	public static final int TENTACLE_SECTIONS = 9;
	public static final int SECTION_HEIGHT = 18 / TENTACLE_SECTIONS;

	public ModelPartTransform squidBody;
	public ModelPartTransform[][] squidTentacles;

	public SquidData(EntitySquid entity)
	{
		super(entity);
		this.controller = new SquidController();
	}

	@Override
	public void onTicksRestart()
	{
	}

	@Override
	public void initModelPose()
	{
		super.initModelPose();

		this.squidBody = new ModelPartTransform();
		this.squidBody.rotation.finish();
		this.squidBody.position.set(0.0F, 8.0F, 0.0F);
		nameToPartMap.put("body", this.squidBody);

		this.squidTentacles = new ModelPartTransform[8][TENTACLE_SECTIONS];
		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			double d0 = (double) i * Math.PI * 2.0D / (double) this.squidTentacles.length;
			float x = (float) Math.cos(d0) * 4.0F;
			float z = (float) Math.sin(d0) * 4.0F;

			this.squidTentacles[i][0] = new ModelPartTransform();
			this.squidTentacles[i][0].position.set(x, 16.0F, z);

			for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j] = new ModelPartTransform();
				this.squidTentacles[i][j].rotation.finish();
				this.squidTentacles[i][j].position.set(0.0F, SECTION_HEIGHT, 0.0F);
				nameToPartMap.put("tentacle_" + i + "_" + j, this.squidTentacles[i][j]);
			}
			
			this.squidTentacles[i][1].position.set(0, SquidData.SECTION_HEIGHT, 2);
		}
	}

	@Override
	public void updateParts(float ticksPerFrame)
	{
		super.updateParts(ticksPerFrame);

		this.squidBody.update(ticksPerFrame);

		for (int i = 0; i < this.squidTentacles.length; ++i)
			for (int j = 0; j < SquidData.TENTACLE_SECTIONS; ++j)
				this.squidTentacles[i][j].update(ticksPerFrame);
	}
}

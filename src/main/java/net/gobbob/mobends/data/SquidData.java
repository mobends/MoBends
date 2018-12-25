package net.gobbob.mobends.data;

import net.gobbob.mobends.animation.controller.SquidController;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.minecraft.entity.Entity;

public class SquidData extends LivingEntityData
{
	public ModelPartTransform squidBody;
	public ModelPartTransform[] squidTentacles = new ModelPartTransform[8];
	
	public SquidData(Entity entity)
	{
		super(entity);
		this.controller = new SquidController();
	}

	@Override
	public void onTicksRestart()
	{
	}
}

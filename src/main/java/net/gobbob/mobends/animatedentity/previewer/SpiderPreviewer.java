package net.gobbob.mobends.animatedentity.previewer;

import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.SpiderData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;

public class SpiderPreviewer extends Previewer
{

	/*
	 * The Entity is generated specificly just for preview, so
	 * it can be manipulated in any way.
	 */
	@Override
	public void prePreview(Entity entity, String animationToPreview)
	{
		if (!(entity instanceof EntitySpider))
			return;
		EntityData entityData = EntityDatabase.instance.getAndMake(SpiderData.class, entity);
		if (!(entityData instanceof SpiderData))
			return;
		
		SpiderData data = (SpiderData) entityData;
		data.forceOnGround(true);
		data.getController().perform(entityData);
	}

	@Override
	public void postPreview(Entity entity, String animationToPreview)
	{
	}
}

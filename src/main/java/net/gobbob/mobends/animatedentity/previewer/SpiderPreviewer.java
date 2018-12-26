package net.gobbob.mobends.animatedentity.previewer;

import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.SpiderData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;

public class SpiderPreviewer extends Previewer<EntitySpider>
{

	/*
	 * The Entity is generated specificly just for preview, so
	 * it can be manipulated in any way.
	 */
	@Override
	public void prePreview(EntitySpider spider, String animationToPreview)
	{
		SpiderData data = EntityDatabase.instance.getAndMake(SpiderData::new, spider);
		
		data.forceOnGround(true);
		data.getController().perform(data);
	}

	@Override
	public void postPreview(EntitySpider entity, String animationToPreview)
	{
	}
}

package net.gobbob.mobends.standard.previewer;

import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animatedentity.Previewer;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.entity.monster.EntitySpider;

public class SpiderPreviewer extends Previewer<EntitySpider>
{

	/*
	 * The Entity is generated specifically just for preview, so
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

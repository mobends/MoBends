package net.gobbob.mobends.core.animatedentity;

import net.gobbob.mobends.core.data.EntityDatabase;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.minecraft.entity.EntityLivingBase;

public class DefaultAlterEntry<T extends EntityLivingBase> extends AlterEntry<T>
{
	
	T previewEntity;
	
	public DefaultAlterEntry(String postfix, String unlocalizedName, IPreviewer previewer)
	{
		super(postfix, unlocalizedName, previewer);
	}
	
	public DefaultAlterEntry(IPreviewer previewer)
	{
		this("", null, previewer);
	}

	@Override
	public LivingEntityData<T> getDataForPreview()
	{
		if (previewEntity == null)
			this.previewEntity = this.createPreviewEntity();
		
		return EntityDatabase.instance.getOrMake(this.owner.getDataFactory(), previewEntity);
	}
	
}
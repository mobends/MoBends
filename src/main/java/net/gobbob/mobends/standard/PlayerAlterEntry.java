package net.gobbob.mobends.standard;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.entity.AbstractClientPlayer;

public class PlayerAlterEntry extends AlterEntry<AbstractClientPlayer>
{

	public PlayerAlterEntry()
	{
		super();
	}
	
	@Override
	public LivingEntityData getDataForPreview()
	{
		return PlayerPreviewer.getPreviewData();
	}
	
}

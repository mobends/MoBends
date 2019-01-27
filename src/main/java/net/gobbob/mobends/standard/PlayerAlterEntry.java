package net.gobbob.mobends.standard;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.math.TransformUtils;
import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;

public class PlayerAlterEntry extends AlterEntry<AbstractClientPlayer>
{

	public PlayerAlterEntry(PlayerPreviewer previewer)
	{
		super(previewer);
	}
	
	@Override
	public LivingEntityData<AbstractClientPlayer> getDataForPreview()
	{
		return PlayerPreviewer.getPreviewData();
	}
	
	@Override
	public void transformModelToCharacterSpace(IMat4x4d matrixOut)
	{
		TransformUtils.scale(matrixOut, -1.0F, -1.0F, 1.0F);
		
		// preRenderCallback
		TransformUtils.scale(matrixOut, 0.9375F, 0.9375F, 0.9375F);
		
		TransformUtils.translate(matrixOut, 0.0F, -1.501F, 0.0F);
	}
	
}

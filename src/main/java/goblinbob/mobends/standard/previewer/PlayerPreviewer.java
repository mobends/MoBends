package goblinbob.mobends.standard.previewer;

import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;

public class PlayerPreviewer extends BipedPreviewer<PlayerData>
{

    private static PlayerData PREVIEW_DATA;
    private static boolean previewInProgress = false;

    public static void createPreviewData()
    {
        if (Minecraft.getMinecraft().player == null)
        {
            PREVIEW_DATA = null;
            return;
        }
        PREVIEW_DATA = new PlayerData(Minecraft.getMinecraft().player);
    }

    public static void deletePreviewData()
    {
        PREVIEW_DATA = null;
    }

    public static PlayerData getPreviewData()
    {
        if (PREVIEW_DATA == null || PREVIEW_DATA.getEntity() == null)
            createPreviewData();
        return PREVIEW_DATA;
    }

    public static void updatePreviewData(float partialTicks)
    {
        PlayerData data = getPreviewData();
        if (data != null)
        {
            data.update(partialTicks);
        }
    }

    public static void updatePreviewDataClient()
    {
        PlayerData data = getPreviewData();
        if (data != null)
        {
            data.updateClient();
        }
    }

    public static boolean isPreviewInProgress()
    {
        return previewInProgress;
    }

    @Override
    public void prePreview(PlayerData data, String animationToPreview)
    {
        previewInProgress = true;

        data.overrideFlyingState(false);

        super.prePreview(data, animationToPreview);

        switch (animationToPreview)
        {
            case "flying":
                data.overrideOnGroundState(false);
                data.overrideFlyingState(true);
                data.limbSwingAmount.override(0F);

                data.overrideStillness(true);
                break;
        }
    }

    @Override
    protected void prepareForWalk(PlayerData data)
    {
        super.prepareForWalk(data);
        data.overrideFlyingState(false);
    }

    @Override
    protected void prepareForDefault(PlayerData data)
    {
        super.prepareForDefault(data);
        data.overrideFlyingState(false);
    }

    @Override
    public void postPreview(PlayerData data, String animationToPreview)
    {
        previewInProgress = false;
    }

}

package goblinbob.mobends.standard.client.renderer.entity.layers;

// Not ported to 26.x yet (the supporters' accessories).
//? if <26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.asset.AssetModels;
import goblinbob.mobends.core.asset.AssetTextures;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.core.supporters.AccessoryDetails;
import goblinbob.mobends.core.supporters.AccessoryPart;
import goblinbob.mobends.core.supporters.AccessorySettings;
import goblinbob.mobends.core.supporters.SupporterContent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * {@code mobends:accessories}: the supporter accessories a player wears (1.12's
 * {@code LayerPlayerAccessories}), each part's model worn on the bone of its bind point, drawn with
 * its diffuse texture, then again with its inked texture in the colour the player picked. Mo'
 * Bends adds the layer to the player's renderer ({@code PlayerRendererMixin}); it draws only for a
 * bent player whose definition asks for it, as 1.12's did, so a player drawn as vanilla has no
 * more than vanilla's. Nothing is fetched for a player it doesn't draw.
 */
public class PlayerAccessoriesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final String KIND = DefinedLayers.ACCESSORIES;

    public PlayerAccessoriesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        Map<String, AccessoryDetails> accessories = SupporterContent.getAccessories();
        if (accessories.isEmpty() || player.isInvisible() || !BendsLayers.asks(player, KIND)) {
            return;
        }
        Map<String, AccessorySettings> settings = SupporterContent.getAccessorySettingsMapFor(player);
        if (settings.isEmpty()) {
            return;
        }
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        for (Map.Entry<String, AccessoryDetails> entry : accessories.entrySet()) {
            AccessorySettings accessory = settings.getOrDefault(entry.getKey(), AccessorySettings.DEFAULT);
            if (!accessory.isShown()) {
                continue;
            }
            int color = 0xFF000000 | accessory.colorOr(entry.getValue().defaultColorOrWhite());
            for (AccessoryPart part : entry.getValue().parts()) {
                renderPart(poseStack, buffers, packedLight, overlay, player, part, color);
            }
        }
    }

    private static void renderPart(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, AbstractClientPlayer player,
                                   AccessoryPart part, int color) {
        if (part.bindPoint() == null || part.modelPath() == null || part.diffuseTexturePath() == null) {
            return;
        }
        AssetModels.AssetModel model = AssetModels.get(part.modelPath());
        ResourceLocation diffuse = AssetTextures.get(part.diffuseTexturePath());
        if (model == null || diffuse == null) {
            return;
        }
        poseStack.pushPose();
        if (BendsLayers.translateToPart(player, KIND, part.bindPoint().bone(), poseStack)) {
            model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(diffuse)), light, overlay, 0xFFFFFFFF);
            // The inked pass, over the same faces, in the player's colour.
            ResourceLocation inked = part.inkedTexturePath() == null ? null : AssetTextures.get(part.inkedTexturePath());
            if (inked != null) {
                model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(inked)), light, overlay, color);
            }
        }
        poseStack.popPose();
    }
}
//?}

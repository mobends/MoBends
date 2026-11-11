package goblinbob.mobends.standard.client.renderer.entity.layers;

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
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
//? if >=26.1 {
/*import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?} else {
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
//?}

import java.util.Map;

/**
 * {@code mobends:accessories}: the supporter accessories a player wears (1.12's
 * {@code LayerPlayerAccessories}), each part's model worn on the bone of its bind point, drawn with
 * its diffuse texture, then again with its inked texture in the colour the player picked. Mo'
 * Bends adds the layer to the player's renderer ({@code PlayerRendererMixin}); it draws only for a
 * bent player whose definition asks for it, as 1.12's did, so a player drawn as vanilla has no
 * more than vanilla's. Nothing is fetched for a player it doesn't draw.
 */
//? if >=26.1 {
/*public class PlayerAccessoriesLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
*///?} else {
public class PlayerAccessoriesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
//?}
    public static final String KIND = DefinedLayers.ACCESSORIES;

    //? if >=26.1 {
    /*public PlayerAccessoriesLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    /^* On 26.x each part is submitted in the frame it's worn in, and drawn later in the frame. ^/
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, AvatarRenderState state, float yRot, float xRot) {
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        if (state.isInvisible || bent == null) {
            return;
        }
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        forEachPart(bent.getEntity(), (part, color) -> submitPart(poseStack, collector, packedLight, overlay, bent.getEntity(), part, color));
    }

    private static void submitPart(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, LivingEntity player,
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
            model.applyHeadTransform(poseStack);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(diffuse),
                (pose, consumer) -> model.draw(pose, consumer, light, overlay, 0xFFFFFFFF));
            // The inked pass, over the same faces, in the player's colour.
            ResourceLocation inked = part.inkedTexturePath() == null ? null : AssetTextures.get(part.inkedTexturePath());
            if (inked != null) {
                collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(inked),
                    (pose, consumer) -> model.draw(pose, consumer, light, overlay, color));
            }
        }
        poseStack.popPose();
    }
    *///?} else {
    public PlayerAccessoriesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        forEachPart(player, (part, color) -> renderPart(poseStack, buffers, packedLight, overlay, player, part, color));
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
    //?}

    /**
     * Each part of each accessory {@code player} wears, with the colour (ARGB) it's inked in; none
     * if the definition doesn't ask for the layer here.
     */
    private static void forEachPart(LivingEntity player, PartVisitor visitor) {
        Map<String, AccessoryDetails> accessories = SupporterContent.getAccessories();
        if (accessories.isEmpty() || !BendsLayers.asks(player, KIND)) {
            return;
        }
        Map<String, AccessorySettings> settings = SupporterContent.getAccessorySettingsMapFor(player);
        if (settings.isEmpty()) {
            return;
        }
        for (Map.Entry<String, AccessoryDetails> entry : accessories.entrySet()) {
            AccessorySettings accessory = settings.getOrDefault(entry.getKey(), AccessorySettings.DEFAULT);
            if (!accessory.isShown()) {
                continue;
            }
            int color = 0xFF000000 | accessory.colorOr(entry.getValue().defaultColorOrWhite());
            for (AccessoryPart part : entry.getValue().parts()) {
                visitor.visit(part, color);
            }
        }
    }

    @FunctionalInterface
    private interface PartVisitor {
        void visit(AccessoryPart part, int color);
    }
}

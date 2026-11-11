package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
//? if >=26.1 {
/*import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.LivingEntity;
*///?} else {
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.animal.Wolf;
//?}

/**
 * {@code mobends:wolf_misc}: the wolf's mouth, its inside and its tongue, textured planes hung from
 * its {@code head}, {@code nose}, {@code mouth} and {@code tongue} bones (1.12's {@code LayerWolfMisc}).
 * Mo' Bends adds the layer to the wolf's renderer ({@code WolfRendererMixin}); it draws only for a
 * bent wolf whose definition asks for it, so a wolf drawn as vanilla has no more than vanilla's.
 */
//? if >=26.1 {
/*public class WolfMiscLayer extends RenderLayer<WolfRenderState, WolfModel> {
*///?} else {
public class WolfMiscLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
//?}
    public static final String KIND = "mobends:wolf_misc";

    private static final ResourceLocation TEXTURE = Ids.of(MoBends.MOD_ID, "textures/entity/wolf_misc.png");
    private static final int TEXTURE_WIDTH = 8;
    private static final int TEXTURE_HEIGHT = 8;
    private static final float SCALE = 0.0625F;

    //? if >=26.1 {
    /*public WolfMiscLayer(RenderLayerParent<WolfRenderState, WolfModel> parent) {
        super(parent);
    }

    /^* On 26.x the planes are submitted, each in the frame it's drawn in, and drawn later in the frame. ^/
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, WolfRenderState state, float yRot, float xRot) {
        // Nothing of the wolf is drawn while it's invisible, so neither is its mouth.
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        if (state.isInvisible || bent == null) {
            return;
        }
        LivingEntity wolf = bent.getEntity();
        poseStack.pushPose();
        // The mouth hangs from the head (which hangs from the body), by the bones' names.
        if (!BendsLayers.translateToBone(wolf, KIND, "head", poseStack)) {
            poseStack.popPose();
            return;
        }
        // Culled, as 1.12 drew the planes: each shows one side.
        RenderType type = RenderTypes.entityCutoutCull(TEXTURE);
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);

        poseStack.pushPose();
        VanillaDraw.rotate(poseStack, Axis.XP.rotationDegrees(90.0F));
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> insideOfMouth(pose, consumer, packedLight, overlay));
        poseStack.popPose();

        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "nose", poseStack)) {
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> topOfMouth(pose, consumer, packedLight, overlay));
        }
        poseStack.popPose();

        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "mouth", poseStack)) {
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> bottomOfMouth(pose, consumer, packedLight, overlay));
        }
        poseStack.popPose();

        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "tongue", poseStack)) {
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> tongue(pose, consumer, packedLight, overlay));
        }
        poseStack.popPose();

        poseStack.popPose();
    }
    *///?} else {
    public WolfMiscLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Nothing of the wolf is drawn while it's invisible, so neither is its mouth.
        if (wolf.isInvisible()) {
            return;
        }
        poseStack.pushPose();
        // The mouth hangs from the head (which hangs from the body), by the bones' names.
        if (!BendsLayers.translateToBone(wolf, KIND, "head", poseStack)) {
            poseStack.popPose();
            return;
        }
        // Culled, as 1.12 drew the planes: each shows one side.
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        int overlay = LivingEntityRenderer.getOverlayCoords(wolf, 0.0F);

        poseStack.pushPose();
        VanillaDraw.rotate(poseStack, Axis.XP.rotationDegrees(90.0F));
        insideOfMouth(poseStack.last(), consumer, packedLight, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "nose", poseStack)) {
            topOfMouth(poseStack.last(), consumer, packedLight, overlay);
        }
        poseStack.popPose();

        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "mouth", poseStack)) {
            bottomOfMouth(poseStack.last(), consumer, packedLight, overlay);
        }
        poseStack.popPose();

        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "tongue", poseStack)) {
            tongue(poseStack.last(), consumer, packedLight, overlay);
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    //?}

    /** The inside of the mouth, across the back of it (the front of the head, 2 px before where it turns, which is 1 px left of the head's middle). */
    private static void insideOfMouth(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay) {
        plane(pose, consumer, light, overlay, -0.5F, -2.1F, -3.0F, 3, 1, true, 0, 0, 3, 4);
    }

    /** Under the nose, the top of the mouth. */
    private static void topOfMouth(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay) {
        plane(pose, consumer, light, overlay, -1.5F, 1.0F, -4.0F, 3, 4, false, 0, 0, 3, 4);
    }

    /** Over the jaw, the bottom of the mouth. */
    private static void bottomOfMouth(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay) {
        plane(pose, consumer, light, overlay, -1.5F, 0.0F, -4.0F, 3, 4, true, 0, 0, 3, 4);
    }

    private static void tongue(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay) {
        plane(pose, consumer, light, overlay, -1.5F, 0.0F, -4.0F, 3, 6, true, 3, 0, 6, 6);
    }

    /**
     * A plane across X and Z at height {@code y}, in model units, textured from pixel {@code (u0, v0)}
     * to {@code (u1, v1)} (1.12's {@code MeshBuilder.texturedXZPlane}: the same corners, the same
     * winding, its normal up or down).
     */
    private static void plane(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float minX, float y, float minZ, float width,
                              float length, boolean facingUp, int u0, int v0, int u1, int v1) {
        float maxX = minX + width;
        float maxZ = minZ + length;
        float minU = u0 / (float) TEXTURE_WIDTH, maxU = u1 / (float) TEXTURE_WIDTH;
        float minV = v0 / (float) TEXTURE_HEIGHT, maxV = v1 / (float) TEXTURE_HEIGHT;
        float normalY = facingUp ? -1.0F : 1.0F;
        vertex(pose, consumer, light, overlay, minX, y, maxZ, minU, minV, normalY);
        vertex(pose, consumer, light, overlay, minX, y, minZ, minU, maxV, normalY);
        vertex(pose, consumer, light, overlay, maxX, y, minZ, maxU, maxV, normalY);
        vertex(pose, consumer, light, overlay, maxX, y, maxZ, maxU, minV, normalY);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float x, float y, float z, float u, float v,
                               float normalY) {
        VanillaDraw.vertex(consumer, pose, x * SCALE, y * SCALE, z * SCALE, -1, u, v, overlay, light, 0.0F, normalY, 0.0F);
    }
}

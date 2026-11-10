package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

/**
 * {@code mobends:wolf_misc}: the wolf's mouth, its inside and its tongue, textured planes hung from
 * its {@code head}, {@code nose}, {@code mouth} and {@code tongue} bones (1.12's {@code LayerWolfMisc}).
 * Mo' Bends adds the layer to the wolf's renderer ({@code WolfRendererMixin}); it draws only for a
 * bent wolf whose definition asks for it, so a wolf drawn as vanilla has no more than vanilla's.
 */
public class WolfMiscLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
    public static final String KIND = "mobends:wolf_misc";

    private static final ResourceLocation TEXTURE = Ids.of(MoBends.MOD_ID, "textures/entity/wolf_misc.png");
    private static final int TEXTURE_WIDTH = 8;
    private static final int TEXTURE_HEIGHT = 8;
    private static final float SCALE = 0.0625F;

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

        // The inside of the mouth, across the back of it (the front of the head, 2 px before where it turns,
        // which is 1 px left of the head's middle).
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        plane(poseStack, consumer, packedLight, overlay, -0.5F, -2.1F, -3.0F, 3, 1, true, 0, 0, 3, 4);
        poseStack.popPose();

        // Under the nose, the top of the mouth.
        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "nose", poseStack)) {
            plane(poseStack, consumer, packedLight, overlay, -1.5F, 1.0F, -4.0F, 3, 4, false, 0, 0, 3, 4);
        }
        poseStack.popPose();

        // Over the jaw, the bottom of the mouth.
        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "mouth", poseStack)) {
            plane(poseStack, consumer, packedLight, overlay, -1.5F, 0.0F, -4.0F, 3, 4, true, 0, 0, 3, 4);
        }
        poseStack.popPose();

        poseStack.pushPose();
        if (BendsLayers.applyLocalTransform(wolf, "tongue", poseStack)) {
            plane(poseStack, consumer, packedLight, overlay, -1.5F, 0.0F, -4.0F, 3, 6, true, 3, 0, 6, 6);
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    /**
     * A plane across X and Z at height {@code y}, in model units, textured from pixel {@code (u0, v0)}
     * to {@code (u1, v1)} (1.12's {@code MeshBuilder.texturedXZPlane}: the same corners, the same
     * winding, its normal up or down).
     */
    private static void plane(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float minX, float y, float minZ, float width,
                              float length, boolean facingUp, int u0, int v0, int u1, int v1) {
        PoseStack.Pose pose = poseStack.last();
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

package goblinbob.mobends.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Vanilla's drawing calls that differ between Minecraft versions, so the rest of the mod makes them
 * the same way on every version. A colour is one ARGB int, as 1.21 passes it; 1.20.1 takes four
 * floats, made from it here.
 */
public final class VanillaDraw {
    private VanillaDraw() {
    }

    /** A whole vertex, as a model's box writes it: its position and normal already transformed. */
    public static void vertex(VertexConsumer consumer, float x, float y, float z, int color, float u, float v, int overlay, int light,
                              float normalX, float normalY, float normalZ) {
        //? if >=1.21 {
        consumer.addVertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
        //?} else {
        /*consumer.vertex(x, y, z, red(color), green(color), blue(color), alpha(color), u, v, overlay, light, normalX, normalY, normalZ);
        *///?}
    }

    /** A whole vertex, its position and normal transformed by {@code pose}. */
    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int color, float u, float v,
                              int overlay, int light, float normalX, float normalY, float normalZ) {
        //? if >=1.21 {
        consumer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(pose, normalX, normalY, normalZ);
        //?} else {
        /*consumer.vertex(pose.pose(), x, y, z)
            .color(color)
            .uv(u, v)
            .overlayCoords(overlay)
            .uv2(light)
            .normal(pose.normal(), normalX, normalY, normalZ)
            .endVertex();
        *///?}
    }

    /** A vertex of only a position (transformed by {@code matrix}) and a colour, as the debug and GUI shapes take. */
    public static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha) {
        //? if >=1.21 {
        consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
        //?} else {
        /*consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
        *///?}
    }

    /** {@code normal} turned by {@code pose}, into {@code dest}. */
    public static Vector3f transformNormal(PoseStack.Pose pose, Vector3f normal, Vector3f dest) {
        //? if >=1.21 {
        return pose.transformNormal(normal, dest);
        //?} else {
        /*return pose.normal().transform(normal, dest);
        *///?}
    }

    /** Multiplies the top of {@code poseStack} by {@code matrix}. */
    public static void mulPose(PoseStack poseStack, Matrix4f matrix) {
        //? if >=1.21 {
        poseStack.mulPose(matrix);
        //?} else {
        /*poseStack.mulPoseMatrix(matrix);
        *///?}
    }

    /** Draws {@code part} (and its children) as vanilla does, tinted {@code color}. */
    public static void render(ModelPart part, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
        //? if >=1.21 {
        part.render(poseStack, consumer, light, overlay, color);
        //?} else {
        /*part.render(poseStack, consumer, light, overlay, red(color), green(color), blue(color), alpha(color));
        *///?}
    }

    /** Writes {@code cube}'s faces as vanilla does, tinted {@code color}. */
    public static void compile(ModelPart.Cube cube, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, int color) {
        //? if >=1.21 {
        cube.compile(pose, consumer, light, overlay, color);
        //?} else {
        /*cube.compile(pose, consumer, light, overlay, red(color), green(color), blue(color), alpha(color));
        *///?}
    }

    /** The colour 1.20.1 hands a model as four floats (0 to 1), as one ARGB int. */
    public static int argb(float red, float green, float blue, float alpha) {
        return channel(alpha) << 24 | channel(red) << 16 | channel(green) << 8 | channel(blue);
    }

    public static float alpha(int color) {
        return (color >>> 24) / 255.0F;
    }

    public static float red(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    public static float green(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    public static float blue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    private static int channel(float value) {
        return Math.round(Math.max(0.0F, Math.min(1.0F, value)) * 255.0F);
    }
}

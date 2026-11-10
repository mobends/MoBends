package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import goblinbob.mobends.core.client.VanillaDraw;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

import java.util.EnumSet;

/**
 * A rippling cape: vanilla's cape cut into {@link #SLAB_AMOUNT} strips a pixel tall, each hanging
 * from the one above and turned by the wave ({@link goblinbob.mobends.standard.data.CapeWave}), as
 * 1.12 drew it. It takes the place of vanilla's cape box, in the same frame and with the same
 * texture: each strip is the matching row of vanilla's box.
 */
public final class BendsCapeRenderer {
    public static final int MODEL_WIDTH = 10;
    public static final int MODEL_LENGTH = 16;
    public static final int MODEL_DEPTH = 1;
    public static final int SLAB_AMOUNT = 16;
    private static final int SLAB_LENGTH = MODEL_LENGTH / SLAB_AMOUNT;

    /** The strips, top to bottom; each is the box vanilla's cape has, one row of it. */
    private static final ModelPart.Cube[] SLABS = new ModelPart.Cube[SLAB_AMOUNT];

    static {
        for (int i = 0; i < SLAB_AMOUNT; i++) {
            // Vanilla's cape is the box (-5, 0, -1) 10 x 16 x 1, at texture (0, 0) of 64 x 32.
            SLABS[i] = new ModelPart.Cube(0, i * SLAB_LENGTH, -MODEL_WIDTH / 2.0F, 0.0F, -MODEL_DEPTH, MODEL_WIDTH, SLAB_LENGTH, MODEL_DEPTH,
                    0.0F, 0.0F, 0.0F, false, 64.0F, 32.0F, EnumSet.allOf(Direction.class));
        }
    }

    private BendsCapeRenderer() {
    }

    /**
     * The angle of strip {@code i} (degrees, about X, from the one above) at {@code phase}. 1.12's
     * magnitude was {@code 80 / SLAB_AMOUNT * (0.7 + i / SLAB_AMOUNT)}, its last term an integer
     * division (always 0), so every strip swings as far. The top one leans 10 degrees further.
     */
    static float angle(int i, float phase) {
        float waveSpeed = 0.2F;
        float waveFrequency = 7.2F;
        float waveOffset = (float) i / SLAB_AMOUNT;
        float magnitude = 80.0F / SLAB_AMOUNT * 0.7F;
        float angle = (float) Math.cos(phase * waveSpeed + waveOffset * waveFrequency) * magnitude;
        return i == 0 ? angle - 10.0F : angle;
    }

    /**
     * Draws the cape rippled at {@code phase}, where vanilla's cape model would be drawn
     * ({@code PlayerModel.renderCloak}), {@code cloak} being vanilla's cape part, for where it sits.
     */
    public static void render(ModelPart cloak, float phase, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(cloak.x / 16.0F, cloak.y / 16.0F, cloak.z / 16.0F);
        for (int i = 0; i < SLAB_AMOUNT; i++) {
            float angle = angle(i, phase);
            // A strip turns about the edge it bends towards, so the strips don't cut into each
            // other: the front (toward the back of the player) or the back.
            float hinge = angle < 0 ? 0.0F : -MODEL_DEPTH;
            poseStack.translate(0.0F, (i == 0 ? 0 : SLAB_LENGTH) / 16.0F, hinge / 16.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(angle));
            poseStack.translate(0.0F, 0.0F, -hinge / 16.0F);
            VanillaDraw.compile(SLABS[i], poseStack.last(), consumer, packedLight, packedOverlay, -1);
        }
        poseStack.popPose();
    }
}

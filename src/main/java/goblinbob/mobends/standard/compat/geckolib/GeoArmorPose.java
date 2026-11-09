package goblinbob.mobends.standard.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.WornPiece;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Where GeckoLib's armour goes on a bent entity, for one draw of a piece of it (its
 * {@code GeoArmorRenderer}'s {@code renderToBuffer}, {@code mixin.geckolib.GeoArmorRendererMixin}),
 * as 1.12's {@code GeoArmorGeometry} and {@code GeoArmorMesh} had it. This class doesn't touch
 * GeckoLib's: the mixin hands over what it needs, and only runs when GeckoLib is installed.
 * <p>
 * GeckoLib poses each armour bone (the head, the body, the arms, the legs and the boots) as the
 * vanilla part it's worn on is posed: moved from where it rests by as much as the part, and turned
 * as it is, about the bone's own pivot. Here the bone is drawn as it rests, in the frame of the bent
 * bone that stands for that part instead, an arm's and a leg's cut at the elbow and the knee (see
 * {@link WornPiece}); what's under it, GeckoLib's own animation included, follows. A bone beside an
 * armour bone (under the same group, a mod's own piece) is worn on the same part, and bones outside
 * any go with the body, as 1.12 had them.
 * <p>
 * GeckoLib's units are blocks, from the model's feet, y up and x mirrored; a vanilla part's are
 * pixels from where it turns, y down. GeckoLib draws a bone at rest where vanilla's part would be
 * at {@link Slot}'s resting point, so a point of the bone at rest goes where vanilla's part at rest
 * has it, minus that resting point.
 */
public final class GeoArmorPose {
    /** What an armour bone is worn on, and where GeckoLib has vanilla's part resting (in pixels, see {@code GeoArmorRenderer.applyBaseTransformations}). */
    public enum Slot {
        HEAD("bipedHead", "head", 0, 0),
        BODY("bipedBody", "body", 0, 0),
        RIGHT_ARM("bipedRightArm", "rightArm", -5, 2),
        LEFT_ARM("bipedLeftArm", "leftArm", 5, 2),
        RIGHT_LEG("bipedRightLeg", "rightLeg", -2, 12),
        LEFT_LEG("bipedLeftLeg", "leftLeg", 2, 12);

        /** The 1.12 field of the vanilla part, and the bone that stands for it if no bone names it. */
        final String field;
        final String fallbackBone;
        final float restX;
        final float restY;

        Slot(String field, String fallbackBone, float restX, float restY) {
            this.field = field;
            this.fallbackBone = fallbackBone;
            this.restX = restX;
            this.restY = restY;
        }
    }

    private final BendsLayers.Frame frame;
    /** Per slot, found the first time it's asked for; empty if the definition has no bone for it. */
    private final Map<Slot, Optional<WornPiece>> pieces = new EnumMap<>(Slot.class);

    private GeoArmorPose(BendsLayers.Frame frame) {
        this.frame = frame;
    }

    /**
     * How GeckoLib's armour on {@code entity} is posed in the draw starting now, or null if it isn't
     * drawn bent here (the armour layer of a bent entity, its definition asking for
     * {@code mobends:armor}): the armour draws as GeckoLib draws it then.
     */
    @Nullable
    public static GeoArmorPose begin(@Nullable Entity entity) {
        BendsLayers.Frame frame = entity == null ? null : BendsLayers.armourFrame(entity);
        return frame == null ? null : new GeoArmorPose(frame);
    }

    @Nullable
    private WornPiece piece(Slot slot) {
        return pieces.computeIfAbsent(slot, s -> Optional.ofNullable(BendsLayers.wornPiece(frame, s.field, s.fallbackBone))).orElse(null);
    }

    /** Whether a bone worn on {@code slot} is drawn on the bent entity (there's a bone for it). */
    public boolean bends(Slot slot) {
        return piece(slot) != null;
    }

    /**
     * Sets the pose on top of {@code poseStack} (pushed for it) to where a bone worn on
     * {@code slot} is drawn from: GeckoLib's model space (in which the bone's {@code parents} have
     * moved it), in the frame of the bent bone. The bone's own transform, at rest, follows.
     * Only if {@link #bends}.
     *
     * @param parents what the bone's parents do to it, in GeckoLib's model space
     */
    public void place(Slot slot, Matrix4fc parents, PoseStack poseStack) {
        WornPiece piece = piece(slot);
        Matrix4f pose = new Matrix4f(piece.frame())
                .translate(-slot.restX / 16.0F, -slot.restY / 16.0F, 0.0F)
                // GeckoLib's model space, as GeoArmorRenderer.actuallyRender moves into it.
                .translate(0.0F, 24.0F / 16.0F, 0.0F)
                .scale(-1.0F, -1.0F, 1.0F)
                .mul(parents);
        poseStack.last().pose().set(pose);
        // GeckoLib takes the normals as the pose makes them: unit length if it only turns them, as
        // the model's pose does for the entity's size.
        Matrix3f normal = pose.normal(new Matrix3f());
        float size = (float) Math.cbrt(Math.abs(pose.determinant3x3()));
        if (size > 0.0F) {
            normal.scale(size);
        }
        poseStack.last().normal().set(normal);
    }

    /**
     * What a bone worn on {@code slot} draws through (see {@link WornPiece#cut}); only if
     * {@link #bends}. {@code buffer} is a buffer underneath, not a cut one.
     */
    public VertexConsumer cut(Slot slot, VertexConsumer buffer) {
        return piece(slot).cut(buffer);
    }
}

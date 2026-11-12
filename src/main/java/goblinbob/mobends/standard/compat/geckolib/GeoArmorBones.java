package goblinbob.mobends.standard.compat.geckolib;

// GeckoLib 5's armour (26.x); on older versions GeoArmorRendererMixin draws GeckoLib 4's itself.
//? if >=26.1 {
/*import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

/^*
 * GeckoLib 5's armour bends with the body (26.x; GeckoLib 4's is {@code GeoArmorRendererMixin} on
 * the older versions). GeckoLib 5 submits a piece of armour as the armour layer runs, and draws it
 * later in the frame, each armour bone ({@code armorHead}, ...) on its own, posed as the wearer's
 * vanilla part is. So:
 * <ul>
 * <li>as the layer submits the piece ({@code GeoArmorRenderer.tryRenderGeoArmorPiece}), the piece's
 *     render state is given how it's posed ({@link #noteSubmit}), if the entity is drawn bent and
 *     the definition asks for {@code mobends:armor};</li>
 * <li>as a bone of that piece is drawn ({@code GeoBone.positionAndRender}, and each child bone of
 *     {@code renderChildren}, which a re-render of the whole model goes through), an armour bone, or a
 *     bone beside one, is drawn as it rests in the frame of the bent bone it's worn on, cut at the
 *     elbow and the knee ({@link #drawBent}), what's under it following.</li>
 * </ul>
 * Anything else draws as GeckoLib draws it. Only the mixins of {@code mixin.geckolib}, which apply
 * only when GeckoLib is installed, load this class.
 ^/
public final class GeoArmorBones {
    /^* How a piece's draws are posed, kept on the piece's render state (one per equipment slot). ^/
    private static final DataTicket<GeoArmorPose> POSE = DataTicket.create("mobends_armor_pose", GeoArmorPose.class);
    /^* The armour bones are drawn as they rest, from where the pose puts their model space: no parents move them. ^/
    private static final Matrix4fc NO_PARENTS = new Matrix4f();

    /^* Whether a bone of a bent piece is being drawn, so its children aren't placed again; render thread only. ^/
    private static boolean drawingBent;

    private GeoArmorBones() {
    }

    /^*
     * As the armour layer submits {@code piece} in {@code slot}, keeps how it's posed with its render
     * state (GeckoLib keeps one per slot with the entity's, the head's being the entity's own), or
     * clears it there, the piece then drawn as GeckoLib draws it.
     ^/
    public static void noteSubmit(HumanoidRenderState entityState, EquipmentSlot slot, ItemStack piece) {
        Map<EquipmentSlot, ? extends HumanoidRenderState> perSlot = ((GeoRenderState) entityState).getGeckolibData(DataTickets.PER_SLOT_RENDER_DATA);
        HumanoidRenderState slotState = perSlot == null ? null : perSlot.get(slot);
        if (slotState == null) {
            return;
        }
        GeoRenderState state = (GeoRenderState) slotState;
        GeoArmorPose pose = GeoArmorPose.forSubmit(piece);
        if (pose != null) {
            state.addGeckolibData(POSE, pose);
        } else if (state.hasGeckolibData(POSE)) {
            state.getDataMap().remove(POSE);
        }
    }

    /^*
     * Draws {@code bone} bent, as {@code GeoBone.positionAndRender} would draw it, if it's a bone of
     * a piece drawn bent that's worn on a bent bone; else returns false, to draw it as GeckoLib does.
     ^/
    public static boolean positionAndRender(GeoBone bone, RenderPassInfo<?> info, VertexConsumer buffer, int packedLight, int packedOverlay,
                                            int colour) {
        GeoArmorPose.Slot slot = slotOf(bone, info);
        if (slot == null) {
            return false;
        }
        drawBent(bone, slot, info, buffer, packedLight, packedOverlay, colour);
        return true;
    }

    /^*
     * Draws the children of {@code bone} as {@code GeoBone.renderChildren} does, those worn on a bent
     * bone bent, if {@code bone} is of a piece drawn bent; else returns false, to draw them as
     * GeckoLib does.
     ^/
    public static boolean renderChildren(GeoBone bone, RenderPassInfo<?> info, PoseStack poseStack, VertexConsumer buffer, int packedLight,
                                         int packedOverlay, int colour) {
        if (drawingBent || poseOf(info) == null) {
            return false;
        }
        if (bone.frameSnapshot != null && bone.frameSnapshot.areChildrenHidden()) {
            return true;
        }
        for (GeoBone child : bone.children()) {
            GeoArmorPose.Slot slot = slotOf(child, info);
            if (slot != null) {
                drawBent(child, slot, info, buffer, packedLight, packedOverlay, colour);
                continue;
            }
            poseStack.pushPose();
            RenderUtil.prepMatrixForBoneAndUpdateListeners(poseStack, child, info);
            child.render(info, poseStack, buffer, packedLight, packedOverlay, colour);
            child.renderChildren(info, poseStack, buffer, packedLight, packedOverlay, colour);
            poseStack.popPose();
        }
        return true;
    }

    @Nullable
    private static GeoArmorPose poseOf(RenderPassInfo<?> info) {
        return info.renderState().getGeckolibData(POSE);
    }

    /^* What {@code bone} is worn on, if it's of a piece drawn bent and that is bent; else null. ^/
    @Nullable
    private static GeoArmorPose.Slot slotOf(GeoBone bone, RenderPassInfo<?> info) {
        if (drawingBent) {
            return null;
        }
        GeoArmorPose pose = poseOf(info);
        if (pose == null) {
            return null;
        }
        GeoArmorPose.Slot slot = pose.boneSlots(() -> findSlots(info)).get(bone);
        return slot != null && pose.bends(slot) ? slot : null;
    }

    /^*
     * Draws {@code bone} as it rests (GeckoLib's copy of the vanilla part's pose left out), in the
     * frame of the bent bone it's worn on, through the cut there, what's under it following.
     ^/
    private static void drawBent(GeoBone bone, GeoArmorPose.Slot slot, RenderPassInfo<?> info, VertexConsumer buffer, int packedLight,
                                 int packedOverlay, int colour) {
        GeoArmorPose pose = poseOf(info);
        PoseStack poseStack = info.poseStack();
        BoneSnapshot snapshot = bone.frameSnapshot;
        float rotX = 0, rotY = 0, rotZ = 0, posX = 0, posY = 0, posZ = 0;
        if (snapshot != null) {
            rotX = snapshot.getRotX();
            rotY = snapshot.getRotY();
            rotZ = snapshot.getRotZ();
            posX = snapshot.getTranslateX();
            posY = snapshot.getTranslateY();
            posZ = snapshot.getTranslateZ();
            snapshot.setRotation(0.0F, 0.0F, 0.0F).setTranslation(0.0F, 0.0F, 0.0F);
        }
        poseStack.pushPose();
        drawingBent = true;
        try {
            pose.place(slot, NO_PARENTS, poseStack);
            RenderUtil.prepMatrixForBoneAndUpdateListeners(poseStack, bone, info);
            VertexConsumer cut = pose.cut(slot, buffer);
            bone.render(info, poseStack, cut, packedLight, packedOverlay, colour);
            bone.renderChildren(info, poseStack, cut, packedLight, packedOverlay, colour);
        } finally {
            drawingBent = false;
            poseStack.popPose();
            if (snapshot != null) {
                snapshot.setRotation(rotX, rotY, rotZ).setTranslation(posX, posY, posZ);
            }
        }
    }

    /^*
     * The armour bones of the piece's model, by the renderer's own bone names (which a renderer may
     * override), and the bones beside them, and what each is worn on.
     ^/
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map<Object, GeoArmorPose.Slot> findSlots(RenderPassInfo<?> info) {
        Map<Object, GeoArmorPose.Slot> slots = new IdentityHashMap<>();
        if (!(info.renderer() instanceof GeoArmorRenderer renderer) || !(info.renderState() instanceof HumanoidRenderState state)) {
            return slots;
        }
        BakedGeoModel model = info.model();
        for (GeoArmorRenderer.ArmorSegment segment : GeoArmorRenderer.ArmorSegment.values()) {
            GeoArmorPose.Slot slot = slotOf(segment);
            model.getBone(renderer.getBoneNameForSegment(state, segment)).ifPresent(bone -> slots.putIfAbsent(bone, slot));
        }
        // A bone beside an armour bone, in the group GeckoLib's model puts it in (bipedLeftLeg), is
        // worn where it is: a mod's own piece, posed as the vanilla part is (Fantasy Armor's
        // armorLeftLegCloth).
        for (Map.Entry<Object, GeoArmorPose.Slot> armour : new ArrayList<>(slots.entrySet())) {
            GeoBone group = ((GeoBone) armour.getKey()).parent();
            if (group != null) {
                for (GeoBone beside : group.children()) {
                    slots.putIfAbsent(beside, armour.getValue());
                }
            }
        }
        return slots;
    }

    private static GeoArmorPose.Slot slotOf(GeoArmorRenderer.ArmorSegment segment) {
        return switch (segment) {
            case HEAD -> GeoArmorPose.Slot.HEAD;
            case CHEST -> GeoArmorPose.Slot.BODY;
            case LEFT_ARM -> GeoArmorPose.Slot.LEFT_ARM;
            case RIGHT_ARM -> GeoArmorPose.Slot.RIGHT_ARM;
            case LEFT_LEG, LEFT_FOOT -> GeoArmorPose.Slot.LEFT_LEG;
            case RIGHT_LEG, RIGHT_FOOT -> GeoArmorPose.Slot.RIGHT_LEG;
        };
    }
}
*///?}

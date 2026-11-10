package goblinbob.mobends.mixin.geckolib;

// GeckoLib 5 (26.x) isn't supported yet.
//? if <26.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.WornPiece;
import goblinbob.mobends.standard.compat.geckolib.GeoArmorPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
//? if >=1.21 {
import software.bernie.geckolib.util.RenderUtil;
//?} else {
/*import software.bernie.geckolib.util.RenderUtils;
*///?}

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * GeckoLib's armour bends with the body (1.12's {@code GeckoLibCompat}). {@code GeoArmorRenderer}
 * is a {@code HumanoidModel} that draws its own model in its {@code renderToBuffer}, its armour bones
 * posed as the wearer's vanilla parts are, so it never reaches Mo' Bends' parts
 * ({@code BendsLayers.drawInLayer}). Here, while an armour layer of a bent entity draws it (and the
 * definition asks for {@code mobends:armor}), each armour bone is drawn in the frame of the bent bone
 * it's worn on, an arm's and a leg's cut at the elbow and the knee ({@link GeoArmorPose}). Otherwise
 * it draws as GeckoLib draws it.
 * <p>
 * In its own mixin config ({@code mobends.geckolib.mixins.json}, not required): it applies only
 * when GeckoLib is installed, and if a GeckoLib version doesn't fit it, the armour draws as GeckoLib
 * draws it. Nothing else of Mo' Bends touches GeckoLib's classes.
 */
@Pseudo
@Mixin(GeoArmorRenderer.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class GeoArmorRendererMixin {
    /** How this draw is posed, while it draws a piece of armour on a bent entity; else null. Render thread only. */
    @Unique
    @Nullable
    private GeoArmorPose mobends$pose;
    /** The armour bones of this draw and what each is worn on, found the first time they're needed. */
    @Unique
    @Nullable
    private Map<GeoBone, GeoArmorPose.Slot> mobends$slots;

    //? if >=1.21 {
    @WrapMethod(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")
    private void mobends$poseBent(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int colour,
                                  Operation<Void> original) {
    //?} else {
    /*@WrapMethod(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V")
    private void mobends$poseBent(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green,
                                  float blue, float alpha, Operation<Void> original) {
    *///?}
        GeoArmorPose outer = mobends$pose;
        Map<GeoBone, GeoArmorPose.Slot> outerSlots = mobends$slots;
        mobends$pose = GeoArmorPose.begin(((GeoArmorRenderer) (Object) this).getCurrentEntity());
        mobends$slots = null;
        try {
            //? if >=1.21 {
            original.call(poseStack, buffer, packedLight, packedOverlay, colour);
            //?} else {
            /*original.call(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            *///?}
        } finally {
            mobends$pose = outer;
            mobends$slots = outerSlots;
        }
    }

    /**
     * Draws an armour bone (and a bone outside any) in the frame of the bent bone it's worn on, as
     * it rests (GeckoLib's copy of the vanilla part's pose left out), what's under it following.
     */
    //? if >=1.21 {
    @WrapMethod(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/Item;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V")
    private void mobends$drawBoneBent(PoseStack poseStack, Item animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                      VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour,
                                      Operation<Void> original) {
        // The bone drawn as GeckoLib draws it, into the buffer given.
        Consumer<VertexConsumer> draw = into ->
            original.call(poseStack, animatable, bone, renderType, bufferSource, into, isReRender, partialTick, packedLight, packedOverlay, colour);
    //?} else {
    /*@WrapMethod(
        // GeckoLib's own method: Forge's SRG names have nothing for it, and leave the classes as they are.
        //? if forge {
        /^remap = false,
        ^///?}
        method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/Item;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIIFFFF)V"
    )
    private void mobends$drawBoneBent(PoseStack poseStack, Item animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                      VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                      float red, float green, float blue, float alpha, Operation<Void> original) {
        // The bone drawn as GeckoLib draws it, into the buffer given.
        Consumer<VertexConsumer> draw = into -> original.call(poseStack, animatable, bone, renderType, bufferSource, into, isReRender, partialTick,
            packedLight, packedOverlay, red, green, blue, alpha);
    *///?}
        GeoArmorPose pose = mobends$pose;
        if (pose == null) {
            draw.accept(buffer);
            return;
        }
        //? if >=1.21 {
        if (buffer instanceof WornPiece.Cut cut) {
            // As GeckoLib swaps a buffer that's been finished meanwhile (a layer drew with another) for a fresh one.
            cut.setDelegate(((GeoRenderer) (Object) this).checkAndRefreshBuffer(isReRender, cut.delegate(), bufferSource, renderType));
        }
        //?}
        GeoArmorPose.Slot armour = mobends$slotsOf().get(bone);
        GeoArmorPose.Slot slot = armour != null ? armour : bone.getParent() == null ? GeoArmorPose.Slot.BODY : null;
        if (slot == null || !pose.bends(slot)) {
            draw.accept(buffer);
            return;
        }
        float rotX = bone.getRotX(), rotY = bone.getRotY(), rotZ = bone.getRotZ();
        float posX = bone.getPosX(), posY = bone.getPosY(), posZ = bone.getPosZ();
        if (armour != null) {
            bone.updateRotation(0.0F, 0.0F, 0.0F);
            bone.updatePosition(0.0F, 0.0F, 0.0F);
        }
        VertexConsumer under = buffer instanceof WornPiece.Cut cut ? cut.delegate() : buffer;
        poseStack.pushPose();
        try {
            pose.place(slot, mobends$parentsOf(bone), poseStack);
            draw.accept(pose.cut(slot, under));
        } finally {
            poseStack.popPose();
            if (armour != null) {
                bone.updateRotation(rotX, rotY, rotZ);
                bone.updatePosition(posX, posY, posZ);
            }
        }
    }

    /**
     * The armour bones, by the renderer's own getters (which a renderer may override), and the bones
     * beside them, and what each is worn on.
     */
    @Unique
    private Map<GeoBone, GeoArmorPose.Slot> mobends$slotsOf() {
        Map<GeoBone, GeoArmorPose.Slot> slots = mobends$slots;
        if (slots == null) {
            GeoArmorRenderer renderer = (GeoArmorRenderer) (Object) this;
            GeoModel model = renderer.getGeoModel();
            slots = new IdentityHashMap<>();
            //? if >=1.21 {
            mobends$put(slots, renderer.getHeadBone(model), GeoArmorPose.Slot.HEAD);
            mobends$put(slots, renderer.getBodyBone(model), GeoArmorPose.Slot.BODY);
            mobends$put(slots, renderer.getRightArmBone(model), GeoArmorPose.Slot.RIGHT_ARM);
            mobends$put(slots, renderer.getLeftArmBone(model), GeoArmorPose.Slot.LEFT_ARM);
            mobends$put(slots, renderer.getRightLegBone(model), GeoArmorPose.Slot.RIGHT_LEG);
            mobends$put(slots, renderer.getLeftLegBone(model), GeoArmorPose.Slot.LEFT_LEG);
            mobends$put(slots, renderer.getRightBootBone(model), GeoArmorPose.Slot.RIGHT_LEG);
            mobends$put(slots, renderer.getLeftBootBone(model), GeoArmorPose.Slot.LEFT_LEG);
            //?} else {
            /*mobends$put(slots, renderer.getHeadBone(), GeoArmorPose.Slot.HEAD);
            mobends$put(slots, renderer.getBodyBone(), GeoArmorPose.Slot.BODY);
            mobends$put(slots, renderer.getRightArmBone(), GeoArmorPose.Slot.RIGHT_ARM);
            mobends$put(slots, renderer.getLeftArmBone(), GeoArmorPose.Slot.LEFT_ARM);
            mobends$put(slots, renderer.getRightLegBone(), GeoArmorPose.Slot.RIGHT_LEG);
            mobends$put(slots, renderer.getLeftLegBone(), GeoArmorPose.Slot.LEFT_LEG);
            mobends$put(slots, renderer.getRightBootBone(), GeoArmorPose.Slot.RIGHT_LEG);
            mobends$put(slots, renderer.getLeftBootBone(), GeoArmorPose.Slot.LEFT_LEG);
            *///?}
            // A bone beside an armour bone, in the group GeckoLib's model puts it in (bipedLeftLeg),
            // is worn where it is: a mod's own piece, posed as the vanilla part is
            // (Fantasy Armor's armorLeftLegCloth), else it keeps vanilla's pose.
            for (Map.Entry<GeoBone, GeoArmorPose.Slot> armour : new ArrayList<>(slots.entrySet())) {
                GeoBone group = armour.getKey().getParent();
                if (group != null) {
                    for (GeoBone beside : group.getChildBones()) {
                        slots.putIfAbsent(beside, armour.getValue());
                    }
                }
            }
            mobends$slots = slots;
        }
        return slots;
    }

    @Unique
    private static void mobends$put(Map<GeoBone, GeoArmorPose.Slot> slots, @Nullable GeoBone bone, GeoArmorPose.Slot slot) {
        if (bone != null) {
            slots.put(bone, slot);
        }
    }

    /** What the bone's parents do to it, in GeckoLib's model space, as GeckoLib applies them. */
    @Unique
    private static org.joml.Matrix4f mobends$parentsOf(GeoBone bone) {
        Deque<GeoBone> parents = new ArrayDeque<>();
        for (GeoBone parent = bone.getParent(); parent != null; parent = parent.getParent()) {
            parents.push(parent);
        }
        PoseStack poseStack = new PoseStack();
        for (GeoBone parent : parents) {
            //? if >=1.21 {
            RenderUtil.prepMatrixForBone(poseStack, parent);
            //?} else {
            /*RenderUtils.prepMatrixForBone(poseStack, parent);
            *///?}
        }
        return poseStack.last().pose();
    }
}
//?}

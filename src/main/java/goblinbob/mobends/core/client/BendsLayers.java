package goblinbob.mobends.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.core.client.definition.DefinedModel;
import goblinbob.mobends.core.client.definition.DefinedModelPart;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.data.OrientationComponent;
import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.vanilla.PlayerModelParts;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import javax.annotation.Nullable;

/**
 * Where vanilla's render layers of a bent entity meet Mo' Bends: what they draw (held items, armour,
 * the custom head, the cape, the elytra, a copy of the model) is moved onto the bent parts, as
 * 1.12's layers ({@code LayerCustomHeldItem}, {@code LayerCustomBipedArmor}, ...) did. The layers
 * themselves stay vanilla's, and so do their checks (what's worn, what's shown); the mixins on them
 * only change where they draw, through the methods here, each of which says when it did nothing,
 * so the layer draws as vanilla does.
 * <p>
 * Which layers follow the bones, and which bones, is the definition's {@code layers} block
 * ({@link DefinedLayers}): a layer it doesn't name draws as vanilla does, as in 1.12. The held items
 * go in the hands of the bones that stand for vanilla's arms (1.12 put them in the bent
 * {@code bipedRightArm}'s frame), a custom head on the block's {@code bone} (the head by default),
 * the cape and the elytra on the {@code body} (and the {@code cape} bone), as 1.12's layers had them.
 * <p>
 * A layer of a bent entity runs inside {@link #begin}/{@link #end} ({@code LivingEntityRendererMixin}),
 * which keeps the {@link Frame} it needs. Outside that (another entity, a model drawn in a menu, an
 * entity drawn as vanilla) there is none, and every method here leaves vanilla alone.
 */
public final class BendsLayers {
    /** The layer being drawn for a bent entity; render thread only. */
    @Nullable
    private static Frame current;

    private BendsLayers() {
    }

    /** What a layer of a bent entity draws with: the entity's posed data and parts, and the frames they're drawn in. */
    public static final class Frame {
        final DefinedEntityData<?> data;
        final EntityModel<?> model;
        final DefinedModel parts;
        final RenderLayer<?, ?> layer;
        /** What the definition asks of this layer; null to leave what the layer draws on its own as vanilla. */
        @Nullable
        final DefinedLayers.Layer named;
        final Matrix4fc entityFrame;
        final float bodyYaw;
        /** The pose the layer was handed, the model's, as it is drawn. */
        final Matrix4f modelPose;

        private Frame(DefinedEntityData<?> data, EntityModel<?> model, DefinedModel parts, RenderLayer<?, ?> layer, Matrix4fc entityFrame,
                      float bodyYaw, Matrix4f modelPose) {
            this.data = data;
            this.model = model;
            this.parts = parts;
            this.layer = layer;
            this.named = DefinedLayers.of(data.getDefinition(), layer.getClass());
            this.entityFrame = entityFrame;
            this.bodyYaw = bodyYaw;
            this.modelPose = modelPose;
        }

        boolean names(String kind) {
            return named != null && named.is(kind);
        }

        /** How the entity is drawn as a baby, or null if it's no baby (or its model draws babies as adults). */
        @Nullable
        DefinedModel.BabyLayout baby() {
            return data.getEntity().isBaby() ? parts.babyLayout() : null;
        }
    }

    /**
     * Starts {@code layer}'s draw of a bent entity, the renderer's model being {@code model}; end it
     * with {@link #end}, however the layer returns, with what this returns. Nothing changes for a
     * model the definition doesn't bend.
     *
     * @param entityFrame the pose as the renderer's {@code setupRotations} starts, this render
     * @param bodyYaw     the body yaw vanilla turns the entity by
     * @return the frame of the layer this one runs inside of (another entity's, drawn by a layer), or null
     */
    @Nullable
    public static Frame begin(DefinedEntityData<?> data, EntityModel<?> model, RenderLayer<?, ?> layer, PoseStack poseStack,
                              Matrix4fc entityFrame, float bodyYaw) {
        Frame outer = current;
        DefinedModel parts = BendsRenderer.partsFor(data, model);
        current = parts == null ? null
                : new Frame(data, model, parts, layer, entityFrame, bodyYaw, new Matrix4f(poseStack.last().pose()));
        return outer;
    }

    public static void end(@Nullable Frame outer) {
        current = outer;
    }

    /** The frame of the layer drawing {@code entity}, or null if it isn't drawing it bent. */
    @Nullable
    private static Frame frameFor(Entity entity) {
        Frame frame = current;
        return frame != null && frame.data.getEntity() == entity ? frame : null;
    }

    /** The frame of the layer drawing {@code entity} bent, if the definition asks for that layer as {@code kind}; else null. */
    @Nullable
    private static Frame frameFor(Entity entity, String kind) {
        Frame frame = frameFor(entity);
        return frame != null && frame.names(kind) ? frame : null;
    }

    /**
     * The bone that stands for the vanilla part a 1.12 model kept in {@code field}
     * ({@code bipedRightArm}), or {@code fallback} if no bone names it.
     */
    private static String boneOf(EntityModelDefinition definition, String field, String fallback) {
        for (BoneDefinition bone : definition.bones) {
            if (bone.vanilla != null && field.equals(bone.vanilla.field)) {
                return bone.name;
            }
        }
        return fallback;
    }

    /**
     * Moves {@code poseStack} from the model's pose into the space the bent parts are drawn in:
     * moved and turned by the root and center bones and the sneak offset, and, for a baby, made
     * smaller as the part {@code bone} hangs from is (in place of whatever vanilla's layer did for
     * a baby, made for vanilla's sizes).
     */
    private static void toBentModel(Frame frame, PoseStack poseStack, String bone) {
        DefinedModel.BabyLayout baby = frame.baby();
        if (baby != null) {
            backToModelPose(frame, poseStack);
        }
        DefinedRenderer.transformModel(frame.data, frame.model, poseStack, frame.entityFrame, frame.bodyYaw);
        if (baby != null) {
            baby.apply(frame.parts.rootPartOf(bone), poseStack);
        }
    }

    /**
     * Moves {@code poseStack} back to the pose the layer was handed (the model's), from wherever
     * vanilla's layer has turned it since, inside the layer's own push.
     */
    private static void backToModelPose(Frame frame, PoseStack poseStack) {
        Matrix4f back = new Matrix4f(poseStack.last().pose()).invert().mul(frame.modelPose);
        poseStack.mulPose(back);
    }

    // --- held items (ItemInHandLayer) ----------------------------------------------------------------

    /**
     * In place of the model's {@code translateToHand} ({@code ItemInHandLayerMixin}): moves
     * {@code poseStack} into the bent arm's frame, through the forearm, then turns the item in the
     * hand as the {@code rightHeldItem} or {@code leftHeldItem} component says (1.12's
     * {@code LayerCustomHeldItem.translateToHand}), about a point half a block down the arm, the hand.
     *
     * @return false if {@code entity} isn't drawn bent here, to do vanilla's
     */
    public static boolean translateToHand(LivingEntity entity, HumanoidArm arm, PoseStack poseStack) {
        Frame frame = frameFor(entity, DefinedLayers.HELD_ITEM);
        if (frame == null) {
            return false;
        }
        String bone = armBone(frame.data.getDefinition(), arm);
        if (!frame.parts.hasPart(bone)) {
            return false;
        }
        toBentModel(frame, poseStack, bone);
        toHand(frame.data, frame.model, frame.parts, arm, bone, poseStack);
        return true;
    }

    /** The bone that stands for vanilla's arm on side {@code arm}. */
    public static String armBone(EntityModelDefinition definition, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? boneOf(definition, "bipedRightArm", "rightArm") : boneOf(definition, "bipedLeftArm", "leftArm");
    }

    /**
     * Moves {@code poseStack}, in the space the bent parts are drawn in, into the frame an item
     * held in the {@code arm} hand is drawn in (see {@link #translateToHand}); {@code bone} is
     * {@link #armBone}, which the parts must have.
     */
    public static void toHand(DefinedEntityData<?> data, EntityModel<?> model, DefinedModel parts, HumanoidArm arm, String bone, PoseStack poseStack) {
        boolean right = arm == HumanoidArm.RIGHT;
        // The slim model moves its arm half a pixel out before turning it, to hold the item in the
        // middle of the narrower hand (PlayerModel.translateToHand).
        float nudge = model instanceof PlayerModelParts player && player.mobends$isSlim() ? (right ? 0.5F : -0.5F) : 0.0F;
        parts.applyVanillaFrame(bone, data, poseStack, nudge, 0.0F, 0.0F);
        OrientationComponent item = data.getComponent(right ? "rightHeldItem" : "leftHeldItem", OrientationComponent.class);
        if (item != null) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
            poseStack.mulPose(DefinedModelPart.toJoml(item.getSmooth()));
            poseStack.translate(0.0F, -0.5F, 0.0F);
        }
    }

    // --- the head (CustomHeadLayer, the spyglass) ---------------------------------------------------

    /**
     * In place of the head's {@code translateAndRotate} in the layers that draw on the head
     * ({@code CustomHeadLayerMixin}, {@code PlayerItemInHandLayerMixin}'s spyglass): moves
     * {@code poseStack} into the bent head's frame: the custom head's {@code bone}, or for the
     * spyglass, a held item, the bone that stands for vanilla's head.
     *
     * @return false if {@code entity} isn't drawn bent here, to do vanilla's
     */
    public static boolean translateToHead(LivingEntity entity, PoseStack poseStack) {
        Frame frame = frameFor(entity);
        if (frame == null || frame.named == null) {
            return false;
        }
        String bone;
        if (frame.names(DefinedLayers.CUSTOM_HEAD)) {
            bone = frame.named.option("bone", "head");
        } else if (frame.names(DefinedLayers.HELD_ITEM)) {
            bone = boneOf(frame.data.getDefinition(), "bipedHead", "head");
        } else {
            return false;
        }
        if (!frame.parts.hasPart(bone)) {
            return false;
        }
        toBentModel(frame, poseStack, bone);
        frame.parts.applyVanillaFrame(bone, frame.data, poseStack, 0.0F, 0.0F, 0.0F);
        return true;
    }

    // --- a part of 1.12's model (IronGolemFlowerLayer, WitchItemLayer, MushroomCowMushroomLayer) -----

    /**
     * In place of the {@code translateAndRotate} of a vanilla part a layer draws on (the iron
     * golem's flower on its arm, the witch's potion at its head, the mooshroom's mushroom on its
     * head): moves {@code poseStack} into the frame of the bent bone that stands for the part 1.12's
     * model kept in {@code field}. 1.12's layers drew there by that field, which Mo' Bends had
     * replaced with the bent part, so they followed it without naming a layer in the definition.
     *
     * @return false if {@code entity} isn't drawn bent here, or no bone stands for that part, to do vanilla's
     */
    public static boolean translateToPartOf(LivingEntity entity, String field, PoseStack poseStack) {
        Frame frame = frameFor(entity);
        if (frame == null) {
            return false;
        }
        String bone = boneOf(frame.data.getDefinition(), field, null);
        if (bone == null || !frame.parts.hasPart(bone)) {
            return false;
        }
        toBentModel(frame, poseStack, bone);
        frame.parts.applyVanillaFrame(bone, frame.data, poseStack, 0.0F, 0.0F, 0.0F);
        return true;
    }

    // --- Mo' Bends' own layers (WolfMiscLayer) -------------------------------------------------------

    /**
     * Moves {@code poseStack}, at the model's pose, into the frame bone {@code bone} of a bent
     * {@code entity} is drawn in, for a layer of Mo' Bends' own that the definition asks for as
     * {@code kind}.
     *
     * @return false if {@code entity} isn't drawn bent here, the definition doesn't ask for
     *         {@code kind}, or there's no such bone: the layer draws nothing then
     */
    public static boolean translateToBone(LivingEntity entity, String kind, String bone, PoseStack poseStack) {
        Frame frame = frameFor(entity, kind);
        if (frame == null || !frame.parts.hasPart(bone)) {
            return false;
        }
        toBentModel(frame, poseStack, bone);
        frame.parts.applyVanillaFrame(bone, frame.data, poseStack, 0.0F, 0.0F, 0.0F);
        return true;
    }

    /**
     * Applies bone {@code bone}'s own transform alone, as it is drawn, in its parent's frame (after
     * {@link #translateToBone} to the parent). Does nothing if {@code entity} isn't drawn bent here.
     *
     * @return false if it did nothing
     */
    public static boolean applyLocalTransform(LivingEntity entity, String bone, PoseStack poseStack) {
        Frame frame = frameFor(entity);
        return frame != null && frame.parts.applyLocalTransform(bone, frame.data, poseStack);
    }

    // --- what's stuck in the entity (ArrowLayer, BeeStingerLayer) -----------------------------------

    /**
     * In place of vanilla's pick of a part and a box to stick an arrow or a stinger in, and its move
     * into that part ({@code StuckInBodyLayerMixin}): picks a bent part and box with {@code random},
     * and moves {@code poseStack} from the model's pose into that part's frame (see
     * {@link DefinedModel#stickIn}).
     *
     * @return the box, as a vanilla cube with its bounds; null if {@code entity} isn't drawn bent
     *         here, or has no part with boxes, to do vanilla's
     */
    @Nullable
    public static ModelPart.Cube stickIn(LivingEntity entity, RandomSource random, PoseStack poseStack) {
        Frame frame = frameFor(entity);
        if (frame == null || !frame.parts.canStickIn()) {
            return null;
        }
        backToModelPose(frame, poseStack);
        DefinedRenderer.transformModel(frame.data, frame.model, poseStack, frame.entityFrame, frame.bodyYaw);
        return frame.parts.stickIn(random, frame.data, frame.baby(), poseStack);
    }

    // --- the body (ElytraLayer) ---------------------------------------------------------------------

    /**
     * Before vanilla's elytra layer moves the elytra back from the body ({@code ElytraLayerMixin}):
     * moves {@code poseStack} into the bent body's frame, which vanilla's body frame is at rest
     * (1.12's {@code LayerCustomElytra}). Does nothing if {@code entity} isn't drawn bent here.
     */
    public static void translateToBody(LivingEntity entity, PoseStack poseStack) {
        Frame frame = frameFor(entity, DefinedLayers.ELYTRA);
        if (frame == null || !frame.parts.hasPart("body")) {
            return;
        }
        toBentModel(frame, poseStack, "body");
        frame.parts.applyVanillaFrame("body", frame.data, poseStack, 0.0F, 0.0F, 0.0F);
    }

    // --- the cape (CapeLayer) -----------------------------------------------------------------------

    /**
     * In place of vanilla's turns of the cape ({@code CapeLayerMixin}, just before the cape is
     * drawn): puts {@code poseStack} where the cape hangs from the bent body, two pixels behind its
     * shoulders as vanilla hangs it, turned by the {@code cape} bone (which the {@code mobends:cape}
     * driver turns with vanilla's cape swing), then half a turn about Y, as vanilla's cape model faces
     * the other way. 1.12's {@code LayerCustomCape}, in vanilla's place.
     *
     * @return false if {@code entity} isn't drawn bent here, to leave vanilla's turns
     */
    public static boolean placeCape(LivingEntity entity, PoseStack poseStack) {
        Frame frame = frameFor(entity, DefinedLayers.CAPE);
        if (frame == null || !frame.parts.hasPart("body") || !frame.parts.hasPart("cape")) {
            return false;
        }
        backToModelPose(frame, poseStack);
        toBentModel(frame, poseStack, "body");
        frame.parts.applyVanillaFrame("body", frame.data, poseStack, 0.0F, 0.0F, 0.0F);
        poseStack.translate(0.0F, 0.0F, 0.125F);
        frame.parts.applyLocalTransform("cape", frame.data, poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        return true;
    }

    /** The component {@code name} of {@code entity}, if the layer drawing it draws it bent; else null. */
    @Nullable
    public static <T> T componentOf(LivingEntity entity, String name, Class<T> type) {
        Frame frame = frameFor(entity);
        return frame == null ? null : frame.data.getComponent(name, type);
    }

    /**
     * Sets the cape model where vanilla's {@code PlayerModel.setupAnim} has it for a player standing
     * up: vanilla also moves it for a sneaking player's leaning body, which the bent body leans its
     * own way. It stays away from a chestplate, as vanilla's does.
     */
    public static void standCape(LivingEntity entity, ModelPart cloak) {
        if (entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            cloak.y = 0.0F;
            cloak.z = 0.0F;
        } else {
            cloak.y = -0.85F;
            cloak.z = -1.1F;
        }
    }

    // --- models a layer draws (HumanoidArmorLayer, VillagerProfessionLayer, DrownedOuterLayer, ...) ---

    /**
     * In place of the own draw of a model a layer of a bent entity draws ({@code AgeableListModelMixin},
     * {@code HierarchicalModelMixin}), it draws the model bent with the entity:
     * <ul>
     * <li>the entity's own model, drawn again (a zombie villager's type and profession): always, as
     *     drawing it unbent would show the entity twice, in two poses;</li>
     * <li>the armour an armour layer draws, if the definition asks for {@code mobends:armor}: vanilla's
     *     armour models and another mod's (any humanoid model the layer draws), each piece in the
     *     frame of the bone it's worn on, the limbs cut at the elbows and knees, and what hangs off a
     *     limb (another mod's gauntlet, knee pad) on the half it hangs from (1.12's
     *     {@code ArmorWrapper}). A model that draws itself its own way (GeckoLib's) isn't reached
     *     here, and draws as it does;</li>
     * <li>a copy of the entity's model the layer draws (a drowned's outer layer, a charged creeper's
     *     armour, the wolf's armour), if the definition asks for, or implies, a copying kind: cut
     *     and posed as the entity's parts are, the boxes the definition declares grown as the
     *     copy's are.</li>
     * </ul>
     *
     * @return true if it drew, and the model's own draw must be skipped
     */
    public static boolean drawInLayer(EntityModel<?> model, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        Frame frame = current;
        if (frame == null) {
            return false;
        }
        DefinedModel drawn;
        if (model == frame.model) {
            drawn = frame.parts;
        } else if (frame.names(DefinedLayers.ARMOR) && isArmour(frame, model)
                || frame.named != null && frame.named.kind().role() == DefinedLayers.Role.COPY && isCopy(frame, model)) {
            drawn = DefinedModel.over(frame.data.getDefinition(), model, frame.parts);
        } else {
            return false;
        }
        if (drawn == null) {
            return false;
        }
        poseStack.pushPose();
        DefinedRenderer.transformModel(frame.data, frame.model, poseStack, frame.entityFrame, frame.bodyYaw);
        drawn.render(frame.data, poseStack, consumer, packedLight, packedOverlay, color, frame.baby());
        poseStack.popPose();
        return true;
    }

    private static boolean isArmour(Frame frame, EntityModel<?> model) {
        return frame.layer instanceof HumanoidArmorLayer<?, ?, ?> && model instanceof HumanoidModel<?>
                && (armourPiece.isEmpty() || !ClientConfig.INSTANCE.keepsArmorVanilla(BuiltInRegistries.ITEM.getKey(armourPiece.getItem()).toString()));
    }

    /** The armour piece an armour layer is drawing ({@code HumanoidArmorLayerMixin}); render thread only. */
    private static ItemStack armourPiece = ItemStack.EMPTY;

    /** Notes the armour piece an armour layer is about to draw, which the user may keep as vanilla draws it. */
    public static void noteArmourPiece(ItemStack stack) {
        armourPiece = stack;
    }

    /**
     * Whether {@code model} can be drawn as a copy of the entity's: of the class the definition
     * bends (the sheep's wool is another class of the same model), or, for a definition that names
     * no class, of the entity's own model's class.
     */
    private static boolean isCopy(Frame frame, EntityModel<?> model) {
        String modelName = frame.data.getDefinition().model;
        if (modelName == null) {
            return model.getClass() == frame.model.getClass();
        }
        Class<?> modelClass = VanillaNames.modelClass(modelName);
        return modelClass != null && modelClass.isInstance(model);
    }
}

package goblinbob.mobends.core.vanilla;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Squid;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Translates the vanilla names packs write, which are Minecraft 1.12's (the pack format is shared by
 * every version), into this version's: an entity id into today's, a model class name into the
 * class, a model's field into the name of its part in the model tree ({@code ModelPart.getChild}),
 * and an entity's field into what reads it now.
 * <p>
 * The classes are written as class literals, not names: a loader can rename the game's classes at
 * runtime (Fabric does, outside development), and the literals are renamed with them.
 * <p>
 * Each model's parts are mapped by where 1.12's model put them and what it drew there (read off
 * 1.12.2's classes), not by their names: 1.12's {@code ironGolemLeftLeg} stood on the golem's right,
 * which is today's {@code right_leg}.
 */
public final class VanillaNames {
    /** 1.12 entity ids that were renamed since, to today's. */
    private static final Map<String, String> RENAMED_ENTITY_IDS = new HashMap<>();
    private static final Map<String, Class<?>> MODEL_CLASSES = new HashMap<>();
    /** A 1.12 model class's superclass among the 1.12 models, whose fields it has too. */
    private static final Map<String, String> MODEL_SUPERCLASSES = new HashMap<>();
    /** {@code <1.12 model class>#<field>} to the part's name. */
    private static final Map<String, String> MODEL_PARTS = new HashMap<>();
    /** {@code <modern entity class>#<1.12 field>} to what reads it on this version's class. */
    private static final Map<String, EntityField> ENTITY_FIELDS = new HashMap<>();

    private static final String MODEL_BIPED = "net.minecraft.client.model.ModelBiped";
    private static final String MODEL_PLAYER = "net.minecraft.client.model.ModelPlayer";
    private static final String MODEL_ZOMBIE = "net.minecraft.client.model.ModelZombie";
    private static final String MODEL_SKELETON = "net.minecraft.client.model.ModelSkeleton";
    private static final String MODEL_ZOMBIE_VILLAGER = "net.minecraft.client.model.ModelZombieVillager";
    private static final String MODEL_CREEPER = "net.minecraft.client.model.ModelCreeper";
    private static final String MODEL_IRON_GOLEM = "net.minecraft.client.model.ModelIronGolem";
    private static final String MODEL_VILLAGER = "net.minecraft.client.model.ModelVillager";
    private static final String MODEL_WITCH = "net.minecraft.client.model.ModelWitch";
    private static final String MODEL_SPIDER = "net.minecraft.client.model.ModelSpider";
    private static final String MODEL_SQUID = "net.minecraft.client.model.ModelSquid";
    private static final String MODEL_WOLF = "net.minecraft.client.model.ModelWolf";
    private static final String MODEL_QUADRUPED = "net.minecraft.client.model.ModelQuadruped";
    private static final String MODEL_CHICKEN = "net.minecraft.client.model.ModelChicken";
    /**
     * The zombified piglin's model on 26.x, which 1.12 had no counterpart of, by today's name (see
     * {@code pig_zombie.json}): before 26.x the piglin's model was a {@code PlayerModel}.
     */
    private static final String MODEL_ZOMBIFIED_PIGLIN = "net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel";

    /** A vanilla entity field, as {@code field} expressions read it. */
    public record EntityField(Expression.Type type, Function<Object, Object> reader, @Nullable ToDoubleFunction<Object> number) {
    }

    static {
        // Living entities whose id changed in 1.13 (the flattening).
        RENAMED_ENTITY_IDS.put("minecraft:villager_golem", "minecraft:iron_golem");
        RENAMED_ENTITY_IDS.put("minecraft:zombie_pigman", "minecraft:zombified_piglin");
        RENAMED_ENTITY_IDS.put("minecraft:snowman", "minecraft:snow_golem");
        RENAMED_ENTITY_IDS.put("minecraft:evocation_illager", "minecraft:evoker");
        RENAMED_ENTITY_IDS.put("minecraft:vindication_illager", "minecraft:vindicator");
        RENAMED_ENTITY_IDS.put("minecraft:illusion_illager", "minecraft:illusioner");

        model(MODEL_BIPED, HumanoidModel.class, null);
        model(MODEL_PLAYER, PlayerModel.class, MODEL_BIPED);
        // ZombieModel covers DrownedModel, the drowned's; the husk draws with a ZombieModel.
        model(MODEL_ZOMBIE, ZombieModel.class, MODEL_BIPED);
        model(MODEL_SKELETON, SkeletonModel.class, MODEL_BIPED);
        // Redone in 1.14 but still 1.12's shape: the head 10 high with the nose among its boxes, a
        // hat over it (with a brim, a child of its own), and the robe among the body's boxes.
        model(MODEL_ZOMBIE_VILLAGER, ZombieVillagerModel.class, MODEL_BIPED);

        // The creeper's model is 1.12's, but for the armour box 1.12 kept and never drew (creeperArmor);
        // the charged creeper's armour is a model of its own in both.
        model(MODEL_CREEPER, CreeperModel.class, null);
        model(MODEL_IRON_GOLEM, IronGolemModel.class, null);
        // Redone in 1.14, but still 1.12's parts where it matters: the head (10 high), the nose under
        // it, the body, the crossed arms and the legs, at the same places. The robe is a child of the
        // body now (and longer, for the new texture), and the hat a child of the head; both are drawn
        // with the part they hang from.
        model(MODEL_VILLAGER, VillagerModel.class, null);
        // The witch's hat and mole hang from the head and the nose, as in 1.12. On 26.x WitchModel is
        // no VillagerModel, but still has its parts, by the same names.
        model(MODEL_WITCH, WitchModel.class, MODEL_VILLAGER);
        model(MODEL_SPIDER, SpiderModel.class, null);
        model(MODEL_SQUID, SquidModel.class, null);
        // The wolf's head and tail draw their boxes from a child now (real_head, real_tail), which the
        // definition's own boxes stand in for (DefinedModelBuilder); the mane is the upper body.
        model(MODEL_WOLF, WolfModel.class, null);
        // QuadrupedModel covers the cow's, the pig's, the sheep's (and its wool's) and the polar bear's.
        model(MODEL_QUADRUPED, QuadrupedModel.class, null);
        model(MODEL_CHICKEN, ChickenModel.class, null);
        //? if >=26.1 {
        /*// No PlayerModel on 26.x but a HumanoidModel on the player's mesh, so with the player's parts
        // (but the jacket and the hat, which it drops).
        model(MODEL_ZOMBIFIED_PIGLIN, net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel.class, MODEL_PLAYER);
        *///?}

        // HumanoidModel's parts (1.12's ModelBiped). The hat is a child of the root on 1.21.1, of the
        // head again on 26.x; so are the player's outer layers of the parts they cover there.
        part(MODEL_BIPED, "bipedHead", "head");
        //? if >=26.1 {
        /*part(MODEL_BIPED, "bipedHeadwear", "head/hat");
        *///?} else {
        part(MODEL_BIPED, "bipedHeadwear", "hat");
        //?}
        part(MODEL_BIPED, "bipedBody", "body");
        part(MODEL_BIPED, "bipedRightArm", "right_arm");
        part(MODEL_BIPED, "bipedLeftArm", "left_arm");
        part(MODEL_BIPED, "bipedRightLeg", "right_leg");
        part(MODEL_BIPED, "bipedLeftLeg", "left_leg");
        // PlayerModel's own (1.12's ModelPlayer).
        //? if >=26.1 {
        /*part(MODEL_PLAYER, "bipedBodyWear", "body/jacket");
        part(MODEL_PLAYER, "bipedRightArmwear", "right_arm/right_sleeve");
        part(MODEL_PLAYER, "bipedLeftArmwear", "left_arm/left_sleeve");
        part(MODEL_PLAYER, "bipedRightLegwear", "right_leg/right_pants");
        part(MODEL_PLAYER, "bipedLeftLegwear", "left_leg/left_pants");
        *///?} else {
        part(MODEL_PLAYER, "bipedBodyWear", "jacket");
        part(MODEL_PLAYER, "bipedRightArmwear", "right_sleeve");
        part(MODEL_PLAYER, "bipedLeftArmwear", "left_sleeve");
        part(MODEL_PLAYER, "bipedRightLegwear", "right_pants");
        part(MODEL_PLAYER, "bipedLeftLegwear", "left_pants");
        //?}
        part(MODEL_PLAYER, "bipedCape", "cloak");
        part(MODEL_PLAYER, "bipedDeadmau5Head", "ear");

        // ModelCreeper's: leg1 and leg2 at the back, leg3 and leg4 at the front, odd ones on the right.
        part(MODEL_CREEPER, "head", "head");
        part(MODEL_CREEPER, "body", "body");
        part(MODEL_CREEPER, "leg1", "right_hind_leg");
        part(MODEL_CREEPER, "leg2", "left_hind_leg");
        part(MODEL_CREEPER, "leg3", "right_front_leg");
        part(MODEL_CREEPER, "leg4", "left_front_leg");
        // ModelIronGolem's: its "left" leg stood on the golem's right (x = -4), its "right" one on the left.
        part(MODEL_IRON_GOLEM, "ironGolemHead", "head");
        part(MODEL_IRON_GOLEM, "ironGolemBody", "body");
        part(MODEL_IRON_GOLEM, "ironGolemRightArm", "right_arm");
        part(MODEL_IRON_GOLEM, "ironGolemLeftArm", "left_arm");
        part(MODEL_IRON_GOLEM, "ironGolemLeftLeg", "right_leg");
        part(MODEL_IRON_GOLEM, "ironGolemRightLeg", "left_leg");
        // ModelVillager's (and so ModelWitch's).
        part(MODEL_VILLAGER, "villagerHead", "head");
        part(MODEL_VILLAGER, "villagerNose", "head/nose");
        part(MODEL_VILLAGER, "villagerBody", "body");
        part(MODEL_VILLAGER, "villagerArms", "arms");
        part(MODEL_VILLAGER, "rightVillagerLeg", "right_leg");
        part(MODEL_VILLAGER, "leftVillagerLeg", "left_leg");
        // ModelSpider's: the legs in pairs from the back, odd ones on the right.
        part(MODEL_SPIDER, "spiderHead", "head");
        part(MODEL_SPIDER, "spiderNeck", "body0");
        part(MODEL_SPIDER, "spiderBody", "body1");
        part(MODEL_SPIDER, "spiderLeg1", "right_hind_leg");
        part(MODEL_SPIDER, "spiderLeg2", "left_hind_leg");
        part(MODEL_SPIDER, "spiderLeg3", "right_middle_hind_leg");
        part(MODEL_SPIDER, "spiderLeg4", "left_middle_hind_leg");
        part(MODEL_SPIDER, "spiderLeg5", "right_middle_front_leg");
        part(MODEL_SPIDER, "spiderLeg6", "left_middle_front_leg");
        part(MODEL_SPIDER, "spiderLeg7", "right_front_leg");
        part(MODEL_SPIDER, "spiderLeg8", "left_front_leg");
        // ModelSquid's: the tentacles were an array, placed round the body in the same order.
        part(MODEL_SQUID, "squidBody", "body");
        for (int i = 0; i < 8; i++) {
            part(MODEL_SQUID, element("squidTentacles", i), "tentacle" + i);
        }
        // ModelWolf's: wolfLeg1 and 2 at the back, 3 and 4 at the front, odd ones on the right.
        part(MODEL_WOLF, "wolfHeadMain", "head");
        part(MODEL_WOLF, "wolfBody", "body");
        part(MODEL_WOLF, "wolfMane", "upper_body");
        part(MODEL_WOLF, "wolfLeg1", "right_hind_leg");
        part(MODEL_WOLF, "wolfLeg2", "left_hind_leg");
        part(MODEL_WOLF, "wolfLeg3", "right_front_leg");
        part(MODEL_WOLF, "wolfLeg4", "left_front_leg");
        part(MODEL_WOLF, "wolfTail", "tail");
        // ModelQuadruped's: leg1 and 2 at the back, 3 and 4 at the front, odd ones on the right.
        part(MODEL_QUADRUPED, "head", "head");
        part(MODEL_QUADRUPED, "body", "body");
        part(MODEL_QUADRUPED, "leg1", "right_hind_leg");
        part(MODEL_QUADRUPED, "leg2", "left_hind_leg");
        part(MODEL_QUADRUPED, "leg3", "right_front_leg");
        part(MODEL_QUADRUPED, "leg4", "left_front_leg");
        // ModelChicken's: the bill and the wattle are parts of their own, beside the head, as they were.
        part(MODEL_CHICKEN, "head", "head");
        part(MODEL_CHICKEN, "bill", "beak");
        part(MODEL_CHICKEN, "chin", "red_thing");
        part(MODEL_CHICKEN, "body", "body");
        part(MODEL_CHICKEN, "rightLeg", "right_leg");
        part(MODEL_CHICKEN, "leftLeg", "left_leg");
        part(MODEL_CHICKEN, "rightWing", "right_wing");
        part(MODEL_CHICKEN, "leftWing", "left_wing");

        // The entities' fields the shipped definitions read with "field", by their 1.12 names.
        number(Entity.class, "rotationYaw", e -> ((Entity) e).getYRot());
        number(Entity.class, "prevRotationYaw", e -> ((Entity) e).yRotO);
        number(Entity.class, "rotationPitch", e -> ((Entity) e).getXRot());
        number(Entity.class, "prevRotationPitch", e -> ((Entity) e).xRotO);
        number(Entity.class, "ticksExisted", e -> ((Entity) e).tickCount);
        number(Entity.class, "fallDistance", e -> ((Entity) e).fallDistance);
        //? if >=26.1 {
        /*// Only players (avatars) keep the walked distance 1.12 read; other entities their moved distance.
        number(Entity.class, "distanceWalkedModified", e -> e instanceof net.minecraft.client.entity.ClientAvatarEntity avatar
            ? avatar.avatarState().getInterpolatedWalkDistance(1.0F) : ((Entity) e).moveDist);
        number(Entity.class, "prevDistanceWalkedModified", e -> e instanceof net.minecraft.client.entity.ClientAvatarEntity avatar
            ? avatar.avatarState().getInterpolatedWalkDistance(0.0F) : ((Entity) e).moveDist);
        *///?} else {
        number(Entity.class, "distanceWalkedModified", e -> ((Entity) e).walkDist);
        number(Entity.class, "prevDistanceWalkedModified", e -> ((Entity) e).walkDistO);
        //?}
        number(LivingEntity.class, "renderYawOffset", e -> ((LivingEntity) e).yBodyRot);
        number(LivingEntity.class, "prevRenderYawOffset", e -> ((LivingEntity) e).yBodyRotO);
        number(LivingEntity.class, "rotationYawHead", e -> ((LivingEntity) e).yHeadRot);
        number(LivingEntity.class, "prevRotationYawHead", e -> ((LivingEntity) e).yHeadRotO);
        number(LivingEntity.class, "hurtTime", e -> ((LivingEntity) e).hurtTime);
        number(LivingEntity.class, "deathTime", e -> ((LivingEntity) e).deathTime);
        number(IronGolem.class, "attackTimer", e -> ((IronGolem) e).getAttackAnimationTick());
        number(Squid.class, "squidPitch", e -> ((Squid) e).xBodyRot);
        number(Squid.class, "prevSquidPitch", e -> ((Squid) e).xBodyRotO);
        number(Squid.class, "squidYaw", e -> ((Squid) e).zBodyRot);
        number(Squid.class, "prevSquidYaw", e -> ((Squid) e).zBodyRotO);
        number(Squid.class, "squidRotation", e -> ((Squid) e).tentacleMovement);
        number(Squid.class, "prevSquidRotation", e -> ((Squid) e).oldTentacleMovement);
        number(Squid.class, "tentacleAngle", e -> ((Squid) e).tentacleAngle);
        number(Squid.class, "lastTentacleAngle", e -> ((Squid) e).oldTentacleAngle);
        number(Chicken.class, "wingRotation", e -> ((Chicken) e).flap);
        number(Chicken.class, "destPos", e -> ((Chicken) e).flapSpeed);
        number(Chicken.class, "oFlapSpeed", e -> ((Chicken) e).oFlapSpeed);
        number(Chicken.class, "oFlap", e -> ((Chicken) e).oFlap);
        number(Chicken.class, "wingRotDelta", e -> ((Chicken) e).flapping);
        bool(Chicken.class, "chickenJockey", e -> ((Chicken) e).isChickenJockey);
    }

    private VanillaNames() {
    }

    private static void model(String name, Class<?> modern, @Nullable String superclass) {
        MODEL_CLASSES.put(name, modern);
        if (superclass != null) {
            MODEL_SUPERCLASSES.put(name, superclass);
        }
    }

    private static void part(String modelClass, String field, String part) {
        MODEL_PARTS.put(modelClass + '#' + field, part);
    }

    /** How the element {@code index} of a 1.12 model's array field ({@code squidTentacles}) is named here. */
    public static String element(String field, int index) {
        return field + '[' + index + ']';
    }

    private static void number(Class<?> owner, String field, ToDoubleFunction<Object> reader) {
        ENTITY_FIELDS.put(owner.getName() + '#' + field, new EntityField(Expression.Type.NUMBER, reader::applyAsDouble, reader));
    }

    private static void bool(Class<?> owner, String field, Predicate<Object> reader) {
        ENTITY_FIELDS.put(owner.getName() + '#' + field, new EntityField(Expression.Type.BOOLEAN, reader::test, null));
    }

    /** Today's id of the entities a 1.12 entity id names (as a type file's selector writes it). */
    public static ResourceLocation entityType(ResourceLocation legacyId) {
        String renamed = RENAMED_ENTITY_IDS.get(legacyId.toString());
        return renamed == null ? legacyId : Ids.parse(renamed);
    }

    /** The model class a 1.12 model class name stands for, or null if it isn't one this version knows. */
    @Nullable
    public static Class<?> modelClass(String name) {
        return MODEL_CLASSES.get(name);
    }

    /**
     * The path of the part ({@code ModelPart.getChild} names from the model's root, joined by
     * {@code /}) a 1.12 model's field holds, looked up in the model's class and then its
     * superclasses, as the field was; or null if there's none. An element of an array field is
     * named as {@link #element} names it.
     */
    @Nullable
    public static String modelPart(String modelClass, String field) {
        for (String model = modelClass; model != null; model = MODEL_SUPERCLASSES.get(model)) {
            String part = MODEL_PARTS.get(model + '#' + field);
            if (part != null) {
                return part;
            }
        }
        return null;
    }

    /**
     * What reads the field a 1.12 entity class declared as {@code field}, on {@code type}, this
     * version's class that took its place; or null if it isn't one this version translates.
     */
    @Nullable
    public static EntityField entityField(Class<?> type, String field) {
        return ENTITY_FIELDS.get(type.getName() + '#' + field);
    }
}

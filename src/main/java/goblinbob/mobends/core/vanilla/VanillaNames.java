package goblinbob.mobends.core.vanilla;

import goblinbob.mobends.core.kumo.expr.Expression;
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
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Translates the vanilla names packs write, which are Minecraft 1.12's (the pack format is shared by
 * every version), into this version's: an entity class name into the class, an entity id into
 * today's, a model class name into the class, a model's field into the name of its part in the
 * model tree ({@code ModelPart.getChild}), and an entity's field into what reads it now.
 * <p>
 * The classes are written as class literals, not names: a loader can rename the game's classes at
 * runtime (Fabric does, outside development), and the literals are renamed with them.
 * <p>
 * What a 1.12 entity class stands for here covers the modern subclasses of the class it maps to,
 * as the 1.12 class covered its own: a definition for a class bends every entity of a subclass
 * that no definition of a closer class claims ({@code DefinedEntities.defaultFor}). So
 * {@code EntityZombie} (today's {@link Zombie}) also bends the husk, as it did in 1.12, and the
 * drowned, which is a zombie now; the zombie villager and the zombified piglin have definitions of
 * their own. {@code EntitySkeleton} is {@link Skeleton} alone: the stray, the wither skeleton and the
 * bogged are other subclasses of {@code AbstractSkeleton}, as the stray and the wither skeleton were
 * in 1.12, and stay vanilla. {@code EntitySpider} bends the cave spider too, as in 1.12, and
 * {@code EntitySquid} the glow squid, which is a squid now (both drawn with their parent's model).
 * <p>
 * Each model's parts are mapped by where 1.12's model put them and what it drew there (read off
 * 1.12.2's classes), not by their names: 1.12's {@code ironGolemLeftLeg} stood on the golem's right,
 * which is today's {@code right_leg}.
 */
public final class VanillaNames {
    private static final Map<String, Class<?>> ENTITY_CLASSES = new HashMap<>();
    /** A 1.12 entity class name to its 1.12 registry id, from which a definition's key is made. */
    private static final Map<String, String> ENTITY_IDS = new HashMap<>();
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

    /** A vanilla entity field, as {@code field} expressions read it. */
    public record EntityField(Expression.Type type, Function<Object, Object> reader, @Nullable ToDoubleFunction<Object> number) {
    }

    static {
        entity("net.minecraft.client.entity.AbstractClientPlayer", AbstractClientPlayer.class, "minecraft:player");
        entity("net.minecraft.entity.monster.EntityZombie", Zombie.class, "minecraft:zombie");
        entity("net.minecraft.entity.monster.EntitySkeleton", Skeleton.class, "minecraft:skeleton");
        entity("net.minecraft.entity.monster.EntityZombieVillager", ZombieVillager.class, "minecraft:zombie_villager");
        // The zombie pigman is the zombified piglin now: a zombie still, drawn with PiglinModel (a
        // PlayerModel), which the pig zombie's definition names through a variant for later versions.
        entity("net.minecraft.entity.monster.EntityPigZombie", ZombifiedPiglin.class, "minecraft:zombie_pigman");
        // The other mobs Mo' Bends ships definitions for (the animals' only in misc/dev-pack).
        entity("net.minecraft.entity.monster.EntityCreeper", Creeper.class, "minecraft:creeper");
        entity("net.minecraft.entity.monster.EntityIronGolem", IronGolem.class, "minecraft:villager_golem");
        entity("net.minecraft.entity.passive.EntityVillager", Villager.class, "minecraft:villager");
        entity("net.minecraft.entity.monster.EntityWitch", Witch.class, "minecraft:witch");
        entity("net.minecraft.entity.monster.EntitySpider", Spider.class, "minecraft:spider");
        entity("net.minecraft.entity.passive.EntitySquid", Squid.class, "minecraft:squid");
        entity("net.minecraft.entity.passive.EntityWolf", Wolf.class, "minecraft:wolf");
        entity("net.minecraft.entity.passive.EntityChicken", Chicken.class, "minecraft:chicken");
        entity("net.minecraft.entity.passive.EntityCow", Cow.class, "minecraft:cow");
        entity("net.minecraft.entity.passive.EntityPig", Pig.class, "minecraft:pig");
        entity("net.minecraft.entity.passive.EntitySheep", Sheep.class, "minecraft:sheep");
        entity("net.minecraft.entity.monster.EntityPolarBear", PolarBear.class, "minecraft:polar_bear");
        entity("net.minecraft.entity.passive.EntityMooshroom", MushroomCow.class, "minecraft:mooshroom");

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
        // The witch's hat and mole hang from the head and the nose, as in 1.12.
        model(MODEL_WITCH, WitchModel.class, MODEL_VILLAGER);
        model(MODEL_SPIDER, SpiderModel.class, null);
        model(MODEL_SQUID, SquidModel.class, null);
        // The wolf's head and tail draw their boxes from a child now (real_head, real_tail), which the
        // definition's own boxes stand in for (DefinedModelBuilder); the mane is the upper body.
        model(MODEL_WOLF, WolfModel.class, null);
        // QuadrupedModel covers the cow's, the pig's, the sheep's (and its wool's) and the polar bear's.
        model(MODEL_QUADRUPED, QuadrupedModel.class, null);
        model(MODEL_CHICKEN, ChickenModel.class, null);

        // HumanoidModel's parts (1.12's ModelBiped). The hat is a child of the root now, not of the head.
        part(MODEL_BIPED, "bipedHead", "head");
        part(MODEL_BIPED, "bipedHeadwear", "hat");
        part(MODEL_BIPED, "bipedBody", "body");
        part(MODEL_BIPED, "bipedRightArm", "right_arm");
        part(MODEL_BIPED, "bipedLeftArm", "left_arm");
        part(MODEL_BIPED, "bipedRightLeg", "right_leg");
        part(MODEL_BIPED, "bipedLeftLeg", "left_leg");
        // PlayerModel's own (1.12's ModelPlayer).
        part(MODEL_PLAYER, "bipedBodyWear", "jacket");
        part(MODEL_PLAYER, "bipedRightArmwear", "right_sleeve");
        part(MODEL_PLAYER, "bipedLeftArmwear", "left_sleeve");
        part(MODEL_PLAYER, "bipedRightLegwear", "right_pants");
        part(MODEL_PLAYER, "bipedLeftLegwear", "left_pants");
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
        number(Entity.class, "distanceWalkedModified", e -> ((Entity) e).walkDist);
        number(Entity.class, "prevDistanceWalkedModified", e -> ((Entity) e).walkDistO);
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

    private static void entity(String name, Class<?> modern, String id) {
        ENTITY_CLASSES.put(name, modern);
        ENTITY_IDS.put(name, id);
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

    /** The entity class a 1.12 class name stands for, or null if it isn't one this version knows. */
    @Nullable
    public static Class<?> entityClass(String name) {
        return ENTITY_CLASSES.get(name);
    }

    /**
     * The registry id 1.12 gave the entities of a 1.12 class (the zombie pigman's is
     * {@code minecraft:zombie_pigman}), from which a definition without a key is named, as 1.12
     * named it; or null if this version doesn't know the class.
     */
    @Nullable
    public static String legacyEntityId(String name) {
        return ENTITY_IDS.get(name);
    }

    /** Today's id of the entities a 1.12 entity id names (as a type file's selector writes it). */
    public static ResourceLocation entityType(ResourceLocation legacyId) {
        String renamed = RENAMED_ENTITY_IDS.get(legacyId.toString());
        return renamed == null ? legacyId : ResourceLocation.parse(renamed);
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

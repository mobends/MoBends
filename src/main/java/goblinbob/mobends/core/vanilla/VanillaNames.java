package goblinbob.mobends.core.vanilla;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Translates the vanilla names packs write, which are Minecraft 1.12's (the pack format is shared by
 * every version), into this version's: an entity class name into the class, a model class name into
 * the class, and a model's field into the name of its part in the model tree
 * ({@code ModelPart.getChild}).
 * <p>
 * The classes are written as class literals, not names: a loader can rename the game's classes at
 * runtime (Fabric does, outside development), and the literals are renamed with them.
 */
public final class VanillaNames {
    private static final Map<String, Class<?>> ENTITY_CLASSES = new HashMap<>();
    private static final Map<String, Class<?>> MODEL_CLASSES = new HashMap<>();
    /** A 1.12 model class's superclass among the 1.12 models, whose fields it has too. */
    private static final Map<String, String> MODEL_SUPERCLASSES = new HashMap<>();
    /** {@code <1.12 model class>#<field>} to the part's name. */
    private static final Map<String, String> MODEL_PARTS = new HashMap<>();

    private static final String MODEL_BIPED = "net.minecraft.client.model.ModelBiped";
    private static final String MODEL_PLAYER = "net.minecraft.client.model.ModelPlayer";

    static {
        ENTITY_CLASSES.put("net.minecraft.client.entity.AbstractClientPlayer", AbstractClientPlayer.class);

        MODEL_CLASSES.put(MODEL_BIPED, HumanoidModel.class);
        MODEL_CLASSES.put(MODEL_PLAYER, PlayerModel.class);
        MODEL_SUPERCLASSES.put(MODEL_PLAYER, MODEL_BIPED);

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
    }

    private VanillaNames() {
    }

    private static void part(String modelClass, String field, String part) {
        MODEL_PARTS.put(modelClass + '#' + field, part);
    }

    /** The entity class a 1.12 class name stands for, or null if it isn't one this version knows. */
    @Nullable
    public static Class<?> entityClass(String name) {
        return ENTITY_CLASSES.get(name);
    }

    /** The model class a 1.12 model class name stands for, or null if it isn't one this version knows. */
    @Nullable
    public static Class<?> modelClass(String name) {
        return MODEL_CLASSES.get(name);
    }

    /**
     * The path of the part ({@code ModelPart.getChild} names from the model's root, joined by
     * {@code /}) a 1.12 model's field holds, looked up in the model's class and then its
     * superclasses, as the field was; or null if there's none.
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
}

package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * The part of a model a bone takes over ({@link BoneDefinition.VanillaPart}), down the model's part
 * tree:
 * <ul>
 *   <li>a {@code part} path: {@code ModelPart.getChild} names from the root ({@code body/left_arm}),
 *   for any model;</li>
 *   <li>a vanilla model's {@code field}, by its 1.12 name, translated to a path
 *   ({@link VanillaNames#modelPart});</li>
 *   <li>another mod's model's {@code field}, by its own name: the {@code ModelPart} the model holds
 *   there, found by reflection, as 1.12 found every field (a mod's names aren't renamed in the game,
 *   unlike Minecraft's).</li>
 * </ul>
 * Box-list indices (1.12's other way to name a part) have nothing to stand for in this version's
 * models.
 */
public final class ModelPartLookup {
    private ModelPartLookup() {
    }

    /**
     * The part {@code ref} names in {@code model}, whose part tree starts at {@code root}; null if
     * there's none.
     */
    @Nullable
    public static ModelPart find(EntityModelDefinition definition, BoneDefinition.VanillaPart ref, ModelPart root, @Nullable Object model) {
        if (ref.part != null) {
            return at(root, ref.part);
        }
        if (ref.field == null || definition.model == null) {
            return null;
        }
        if (definition.namesVanillaModel()) {
            String field = ref.element >= 0 ? VanillaNames.element(ref.field, ref.element) : ref.field;
            String path = VanillaNames.modelPart(definition.model, field);
            return path == null ? null : at(root, path);
        }
        return model == null ? null : field(model, ref.field, ref.element);
    }

    /** The part at {@code path} ({@code getChild} names joined by {@code /}) from {@code root}; null if there's none. */
    @Nullable
    public static ModelPart at(ModelPart root, String path) {
        ModelPart part = root;
        for (String name : path.split("/")) {
            if (name.isEmpty() || !part.hasChild(name)) {
                return null;
            }
            part = part.getChild(name);
        }
        return part;
    }

    /**
     * The {@code ModelPart} {@code model} holds in its field {@code name} (declared by its class or a
     * superclass), or in the element {@code element} of an array field; null if there's none.
     */
    @Nullable
    static ModelPart field(Object model, String name, int element) {
        for (Class<?> type = model.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            Field field;
            try {
                field = type.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(model);
                if (element >= 0) {
                    if (value == null || !value.getClass().isArray() || element >= Array.getLength(value)) {
                        return null;
                    }
                    value = Array.get(value, element);
                }
                return value instanceof ModelPart part ? part : null;
            } catch (ReflectiveOperationException | RuntimeException e) {
                return null;
            }
        }
        return null;
    }
}

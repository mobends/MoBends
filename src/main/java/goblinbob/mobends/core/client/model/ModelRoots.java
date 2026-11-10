package goblinbob.mobends.core.client.model;

import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The root of any model's part tree, which the model definitions' part paths start from, for a
 * model that doesn't keep it (one that extends {@code EntityModel} and draws its parts its own way,
 * a mod's or vanilla's). Every tree the game builds from a layer definition is recorded
 * ({@code LayerDefinitionMixin}), and a model's root is the one whose parts the model holds in
 * its fields. The fields are found by what they hold, not by name, so this works on every loader
 * (Fabric renames Minecraft's fields) and for every mod.
 */
public final class ModelRoots {
    /** Each part of every tree built, to the tree's root; both weakly, so trees no model holds go. */
    private static final Map<ModelPart, WeakReference<ModelPart>> ROOT_OF_PART = Collections.synchronizedMap(new WeakHashMap<>());

    private ModelRoots() {
    }

    /** Remembers the tree built from {@code root} (a layer definition's {@code bakeRoot}). */
    public static void record(ModelPart root) {
        WeakReference<ModelPart> reference = new WeakReference<>(root);
        root.getAllParts().forEach(part -> ROOT_OF_PART.put(part, reference));
    }

    /** The root of the tree {@code part} was built in, or null if it wasn't built from a layer definition. */
    @Nullable
    public static ModelPart rootOfPart(ModelPart part) {
        WeakReference<ModelPart> reference = ROOT_OF_PART.get(part);
        return reference == null ? null : reference.get();
    }

    /**
     * The root of the tree {@code model} holds parts of, in its fields (declared by its class or a
     * superclass; a part, an array of them or a collection of them): the tree most of them come
     * from; null if it holds none of a tree that was recorded.
     */
    @Nullable
    public static ModelPart find(Object model) {
        Map<ModelPart, Integer> votes = new IdentityHashMap<>();
        ModelPart best = null;
        for (Class<?> type = model.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) {
                    continue;
                }
                Object value;
                try {
                    field.setAccessible(true);
                    value = field.get(model);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    continue;
                }
                if (value instanceof ModelPart part) {
                    best = vote(votes, part, best);
                } else if (value instanceof ModelPart[] parts) {
                    for (ModelPart part : parts) {
                        best = vote(votes, part, best);
                    }
                } else if (value instanceof Collection<?> collection) {
                    for (Object element : collection) {
                        if (element instanceof ModelPart part) {
                            best = vote(votes, part, best);
                        }
                    }
                }
            }
        }
        return best;
    }

    /** Counts {@code part} for its tree; the tree with the most so far (the first on a tie). */
    private static ModelPart vote(Map<ModelPart, Integer> votes, @Nullable ModelPart part, @Nullable ModelPart best) {
        ModelPart root = part == null ? null : rootOfPart(part);
        if (root == null) {
            return best;
        }
        int count = votes.merge(root, 1, Integer::sum);
        return best == null || count > votes.get(best) ? root : best;
    }
}

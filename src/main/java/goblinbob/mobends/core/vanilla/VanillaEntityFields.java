package goblinbob.mobends.core.vanilla;

import goblinbob.mobends.core.kumo.expr.ReflectedEntityFields;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * How a model definition's {@code field} reads the entity on this version. Packs name fields as
 * 1.12 did ({@code rotationYaw}, {@code squidRotation}), so a field of one of the game's own
 * classes is only found through {@link VanillaNames#entityField}, which reads it under today's
 * name (or through its getter). The game's classes are never read by reflection: on Fabric their
 * fields are renamed outside development, so a pack reading today's names that way would work in
 * one place and not another. A class of another mod is read by reflection, as 1.12 did.
 */
public class VanillaEntityFields extends ReflectedEntityFields {
    @Nullable
    @Override
    protected Step step(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            VanillaNames.EntityField field = VanillaNames.entityField(c, name);
            if (field != null) {
                // Its type stands in for the declared one: the next step looks nothing up on it.
                Class<?> declared = switch (field.type()) {
                    case BOOLEAN -> boolean.class;
                    case DOUBLE -> double.class;
                    default -> float.class;
                };
                return new Step(declared, field.reader()::apply, field.number());
            }
            if (!isGameClass(c)) {
                Step reflected = reflect(c, name);
                if (reflected != null) {
                    return reflected;
                }
            }
        }
        return null;
    }

    /** Whether Minecraft declares the class (renamed on Fabric outside development, still in this package). */
    private static boolean isGameClass(Class<?> c) {
        return c.getName().startsWith("net.minecraft.") || c.getName().startsWith("com.mojang.");
    }

    /** The field {@code name} that {@code c} itself declares, read by reflection; or null. */
    @Nullable
    private static Step reflect(Class<?> c, String name) {
        Field field;
        try {
            field = c.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            field.setAccessible(true);
        } catch (NoSuchFieldException | RuntimeException e) {
            return null;
        }
        boolean number = field.getType().isPrimitive() && field.getType() != boolean.class && field.getType() != char.class;
        return new Step(field.getType(), owner -> {
            try {
                return field.get(owner);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                return null;
            }
        }, !number ? null : owner -> {
            try {
                return field.getDouble(owner);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                return 0;
            }
        });
    }
}

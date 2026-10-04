package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.bind.IBoneSink;

import javax.annotation.Nullable;

/**
 * Everything KUMO needs to know about the thing it animates. Deliberately Minecraft-agnostic:
 * bones are resolved by name into sinks, and all entity state arrives as named variables (numbers)
 * and named states (booleans).
 *
 * @author Iwo Plaza
 */
public interface IKumoSubject
{

    /**
     * Resolves a named part into a sink KUMO can write animation targets to.
     *
     * @return the sink, or null if the subject has no part with that name.
     */
    IBoneSink getBone(String name);

    /** The object {@code field} reads (the entity), or null if there is none. */
    @Nullable
    default Object getEntity()
    {
        return null;
    }

    /**
     * @return the index of the numeric variable {@code name} (e.g. "limbSwing", "headYaw",
     *         "ticksInAir") for {@link #getVariable(int)}, or -1 if the subject has none. Animators
     *         look every name up once, when they are bound to the subject.
     */
    int indexOfVariable(String name);

    /** @return the current value of the variable at {@code index} (see {@link #indexOfVariable}). */
    double getVariable(int index);

    /**
     * @return a string-valued input (e.g. "mainHandItem" = "minecraft:torch", "attackActionType" =
     *         "SWORD"), or null if the subject has no such property or it is currently unset.
     */
    default String getProperty(String name)
    {
        return null;
    }

    /**
     * @return the index of the boolean state {@code name} (e.g. "ON_GROUND", "SPRINTING") for
     *         {@link #getState(int)}, or -1 if the subject has none.
     */
    int indexOfState(String name);

    /** @return whether the state at {@code index} holds (see {@link #indexOfState}). */
    boolean getState(int index);

}

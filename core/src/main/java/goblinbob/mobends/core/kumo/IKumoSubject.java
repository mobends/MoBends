package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.IVanillaPose;

import javax.annotation.Nullable;

/**
 * Everything KUMO needs to know about the thing it animates. Deliberately Minecraft-agnostic:
 * bones are resolved by name into sinks, entity state arrives as named variables (numbers) and
 * named states (booleans), and operations read the entity itself.
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

    /**
     * The pose the subject's own model has this frame, which a {@code core:vanilla_pose} node
     * poses its bones from; null where there is none to read (the node then poses nothing).
     */
    @Nullable
    default IVanillaPose getVanillaPose()
    {
        return null;
    }

    /**
     * Told, once the subject is animated, how much its frame is vanilla's this frame, 0..1: 1 where
     * a {@code core:vanilla_pose} node put the entity's own bones ({@code root}, {@code center}) at
     * rest, 0 where nothing did, and in between while it fades in or out (a layer's {@code @fade},
     * a transition). Whatever the host does to the whole entity in place of vanilla, for the
     * animator to do instead (a swimming tilt, a sneaking offset), it leaves to vanilla by as much.
     */
    default void setEntityFrameVanilla(float weight)
    {
    }

    /** The entity, which operations and {@code field} read, or null if there is none. */
    @Nullable
    default Object getEntity()
    {
        return null;
    }

    /**
     * @return the index of the numeric variable {@code name} (e.g. "entityLimbSwing", "entityHeadYaw",
     *         "entityTicksInAir") for {@link #getVariable(int)}, or -1 if the subject has none. Animators
     *         look every name up once, when they are bound to the subject.
     */
    int indexOfVariable(String name);

    /** @return the current value of the variable at {@code index} (see {@link #indexOfVariable}). */
    double getVariable(int index);

    /**
     * @return the index of the boolean state {@code name} (e.g. "entityIsOnGround", "entityIsSprinting") for
     *         {@link #getState(int)}, or -1 if the subject has none.
     */
    int indexOfState(String name);

    /** @return whether the state at {@code index} holds (see {@link #indexOfState}). */
    boolean getState(int index);

}

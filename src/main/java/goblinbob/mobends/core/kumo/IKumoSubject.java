package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.bind.IBoneSink;

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

    boolean hasVariable(String name);

    /**
     * @return the current value of a numeric variable (e.g. "limbSwing", "headYaw", "ticksInAir").
     * @throws IllegalArgumentException if the variable is unknown; check with {@link #hasVariable}.
     */
    double getVariable(String name);

    boolean hasState(String name);

    /**
     * @return a string-valued input (e.g. "mainHandItem" = "minecraft:torch", "attackActionType" =
     *         "SWORD"), or null if the subject has no such property or it is currently unset.
     */
    default String getProperty(String name)
    {
        return null;
    }

    /**
     * @return whether a boolean state holds (e.g. "ON_GROUND", "SPRINTING").
     * @throws IllegalArgumentException if the state is unknown; check with {@link #hasState}.
     */
    boolean getState(String name);

}

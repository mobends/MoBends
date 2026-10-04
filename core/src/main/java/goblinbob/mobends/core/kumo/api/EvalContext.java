package goblinbob.mobends.core.kumo.api;

import javax.annotation.Nullable;

/** What an operation evaluates against. Reused from frame to frame; don't keep it. */
public interface EvalContext
{

    /** The entity animated, an instance of the class the operation was bound for; null if there is none. */
    @Nullable
    Object entity();

    /** The ticks this frame lasted (0 while paused). */
    float deltaTime();

}

package goblinbob.mobends.core.kumo.expr;

import goblinbob.mobends.core.kumo.state.EntityState;

/** State an operation or a driver declared ({@link DeclaredSlot}, {@link DeclaredDoubleSlot}). */
public interface DeclaredState
{

    /** Back to the initial values: the scope holding the use started. */
    void reset(EntityState state);

}

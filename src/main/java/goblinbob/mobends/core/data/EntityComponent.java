package goblinbob.mobends.core.data;

/**
 * Something an entity's data carries besides its bones: a sword trail, a held item's orientation,
 * the ripple of a cape. A model definition switches them on by name (its {@code components}), and
 * the layers and drivers that use one find it by that name ({@link EntityData#getComponent}),
 * whatever class the data is.
 */
public interface EntityComponent
{

    /** Moves it on by a frame; called with the parts' update. */
    void update(float ticksPerFrame);

    /** Draws it in the entity's frame, before the model is drawn (a sword trail). Most draw nothing. */
    default void renderLocal(float scale)
    {
    }

}

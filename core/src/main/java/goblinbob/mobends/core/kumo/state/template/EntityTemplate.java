package goblinbob.mobends.core.kumo.state.template;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * The entity scope a mob's model definition declares: the values it exposes and remembers, read
 * by every animator and extension of the entity as {@code entity.x}. Its definitions are the only
 * place {@code field} reads the entity.
 */
public class EntityTemplate
{

    /** The entity's class, which {@code field} paths are resolved against. */
    public final Class<?> entityClass;
    @Nullable
    public final Map<String, DefinitionTemplate> define;
    @Nullable
    public final OnTemplate on;
    /** Whether the model definition comes from a trusted source (see {@code DefinitionScope#state}). */
    public final boolean trusted;

    public EntityTemplate(Class<?> entityClass, @Nullable Map<String, DefinitionTemplate> define, @Nullable OnTemplate on, boolean trusted)
    {
        this.entityClass = entityClass;
        this.define = define;
        this.on = on;
        this.trusted = trusted;
    }

}

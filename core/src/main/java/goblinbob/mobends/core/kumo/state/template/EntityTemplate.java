package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.option.OptionSet;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * The entity an animator animates: its class, which operations bind against, and the entity scope
 * its model definition declares (none if it has no definition), the values it exposes and
 * remembers, read by every animator and extension of the entity as {@code entity.x}. Its
 * definitions are the only place {@code field} reads the entity.
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
    /** The options the model definition declares, read by the entity's animator (not by extensions) as {@code option.x}. */
    public final OptionSet options;

    /** An entity of {@code entityClass} whose model has no definition. */
    public EntityTemplate(Class<?> entityClass)
    {
        this(entityClass, null, null, true);
    }

    public EntityTemplate(Class<?> entityClass, @Nullable Map<String, DefinitionTemplate> define, @Nullable OnTemplate on, boolean trusted)
    {
        this(entityClass, define, on, trusted, OptionSet.EMPTY);
    }

    public EntityTemplate(Class<?> entityClass, @Nullable Map<String, DefinitionTemplate> define, @Nullable OnTemplate on, boolean trusted, OptionSet options)
    {
        this.entityClass = entityClass;
        this.define = define;
        this.on = on;
        this.trusted = trusted;
        this.options = options;
    }

}

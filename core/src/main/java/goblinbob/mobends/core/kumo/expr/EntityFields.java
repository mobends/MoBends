package goblinbob.mobends.core.kumo.expr;

import javax.annotation.Nullable;
import java.util.List;

/**
 * How {@code field} reads the entity: the host resolves a path of fields on the entity's class
 * (Minecraft's own classes go through its generated accessors, so their development names work in
 * the obfuscated game too). The engine itself knows no Minecraft class.
 */
public interface EntityFields
{

    /**
     * The path of fields {@code path} from {@code type}: each step looked up on the declared type
     * of the step before it, starting at {@code type} and walking up superclasses; the last one a
     * number or a boolean. Null if a step can't be found.
     */
    @Nullable
    Path resolve(Class<?> type, List<String> path);

    /** A resolved path. */
    interface Path
    {
        Expression.Type type();

        /** The object the path's last step is read from, or null where a step before it is null. */
        @Nullable
        Object owner(Object entity);

        double number(Object owner);

        boolean bool(Object owner);
    }

    /** The host's (see {@link #install}); plain reflection by default. */
    final class Holder
    {
        static EntityFields fields = new ReflectedEntityFields();

        private Holder()
        {
        }
    }

    static void install(EntityFields fields)
    {
        Holder.fields = fields;
    }

}

package goblinbob.mobends.core.types;

import java.util.Collection;
import java.util.Comparator;

/**
 * The precedence between entity types whose selectors all hold for the same entity (see
 * {@code misc/kumo-format.md}, "Entity types and selectors"): the higher rank first, then the
 * selector with more conditions, then the id in plain lexical order.
 */
public final class TypeOrder
{

    public interface Ranked
    {
        String getId();

        /** Set by the user; 0 unless they reordered the type. */
        int getRank();

        /** The number of conditions of the type's selector. */
        int getSpecificity();
    }

    public static final Comparator<Ranked> PRECEDENCE = Comparator
            .comparingInt((Ranked type) -> -type.getRank())
            .thenComparingInt(type -> -type.getSpecificity())
            .thenComparing(Ranked::getId);

    private TypeOrder()
    {
    }

    /** The type that applies among {@code candidates}, or null if there are none. */
    public static <T extends Ranked> T first(Collection<T> candidates)
    {
        T best = null;
        for (T candidate : candidates)
        {
            if (best == null || PRECEDENCE.compare(candidate, best) < 0)
            {
                best = candidate;
            }
        }
        return best;
    }

}

package goblinbob.mobends.core.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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

        void setRank(int rank);

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

    /**
     * Raises ranks so that {@code order} (the first one highest) is the precedence order among its
     * items, changing as few as it can: an item keeps its rank when it already comes before the
     * next one. Items can be shared by several orders (a type without an entity-type selector
     * shows up for every mob), and ranks set for another order are kept as far as possible.
     *
     * @return the items whose rank changed
     */
    public static <T extends Ranked> List<T> rankInOrder(List<T> order)
    {
        List<T> changed = new ArrayList<>();
        for (int i = order.size() - 2; i >= 0; i--)
        {
            T item = order.get(i);
            T next = order.get(i + 1);
            if (PRECEDENCE.compare(item, next) >= 0)
            {
                item.setRank(next.getRank() + 1);
                changed.add(item);
            }
        }
        return changed;
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

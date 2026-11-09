package goblinbob.mobends.core.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * The precedence between entity types whose selectors all hold for the same entity (see
 * {@code misc/kumo-format.md}, "Entity types and selectors"): the higher rank first, then the
 * selector with more conditions, then the id in plain lexical order.
 */
public final class TypeOrder {
    public interface Ranked {
        String getId();

        /** Set by the user (the Mo' Bends menu); 0 unless they reordered it. */
        int getRank();

        /** The number of conditions of the type's selector. */
        int getSpecificity();
    }

    public static final Comparator<Ranked> PRECEDENCE = Comparator
            .comparingInt((Ranked type) -> -type.getRank())
            .thenComparingInt(type -> -type.getSpecificity())
            .thenComparing(Ranked::getId);

    private TypeOrder() {
    }

    /**
     * The ranks that put {@code order} in precedence order (the first one first), changing as few
     * as it can: from the bottom up, an entry that doesn't already come before the next one gets
     * the next one's rank plus one. Each one changed is handed to {@code setRank} with its new rank,
     * which {@link Ranked#getRank()} must return from then on.
     */
    public static <T extends Ranked> void rankInOrder(List<T> order, BiConsumer<T, Integer> setRank) {
        for (int i = order.size() - 2; i >= 0; i--) {
            T item = order.get(i);
            T next = order.get(i + 1);
            if (PRECEDENCE.compare(item, next) >= 0) {
                setRank.accept(item, next.getRank() + 1);
            }
        }
    }

    /** The type that applies among {@code candidates}, or null if there are none. */
    public static <T extends Ranked> T first(Collection<T> candidates) {
        T best = null;
        for (T candidate : candidates) {
            if (best == null || PRECEDENCE.compare(candidate, best) < 0) {
                best = candidate;
            }
        }
        return best;
    }
}

package goblinbob.mobends.core.kumo.option;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The values of every option, by key ({@code <owner>#<name>}): what the user set, loaded from the
 * config and kept across reloads. A value the user never set is absent, and the option has its
 * file's default. Options are declared here ({@link #declare}), and their changes reported to the
 * {@link Listener} (the config, which saves them).
 */
public final class KumoOptions
{

    public static final KumoOptions INSTANCE = new KumoOptions();

    /** Told of every change of a value: the new one, or null when it went back to the default. */
    public interface Listener
    {
        void changed(String key, @Nullable Double value);
    }

    /** One option's value, shared by every declaration of it; NaN for the default. */
    static final class Cell
    {
        private final KumoOptions options;
        private final String key;
        volatile double value = Double.NaN;

        Cell(KumoOptions options, String key)
        {
            this.options = options;
            this.key = key;
        }

        void set(double value)
        {
            boolean wasDefault = Double.isNaN(this.value);
            if (wasDefault && Double.isNaN(value) || this.value == value)
            {
                return;
            }
            this.value = value;
            Listener listener = options.listener;
            if (listener != null)
            {
                listener.changed(key, Double.isNaN(value) ? null : value);
            }
        }
    }

    private final Map<String, Cell> cells = new HashMap<>();
    @Nullable
    private Listener listener;

    /** A registry of its own (tests); the mod uses {@link #INSTANCE}. */
    public KumoOptions()
    {
    }

    public static String key(String owner, String name)
    {
        return owner + "#" + name;
    }

    public void setListener(@Nullable Listener listener)
    {
        this.listener = listener;
    }

    /** Takes the values the user set (from the config), by key; the listener isn't told. */
    public synchronized void load(Map<String, Double> values)
    {
        for (Cell cell : cells.values())
        {
            cell.value = Double.NaN;
        }
        for (Map.Entry<String, Double> entry : values.entrySet())
        {
            cell(entry.getKey()).value = entry.getValue() == null ? Double.NaN : entry.getValue();
        }
    }

    /** The options {@code owner} declares ({@code templates}, as read from its file; null declares none). */
    public synchronized OptionSet declare(String owner, @Nullable Map<String, OptionTemplate> templates)
    {
        if (templates == null || templates.isEmpty())
        {
            return OptionSet.EMPTY;
        }
        Map<String, KumoOption> options = new LinkedHashMap<>();
        for (Map.Entry<String, OptionTemplate> entry : templates.entrySet())
        {
            String name = entry.getKey();
            options.put(name, new KumoOption(owner, name, entry.getValue(), cell(key(owner, name))));
        }
        return new OptionSet(owner, options);
    }

    /** The value the user set for the option with this key, or null if they never did. */
    @Nullable
    public synchronized Double getSaved(String key)
    {
        Cell cell = cells.get(key);
        return cell == null || Double.isNaN(cell.value) ? null : cell.value;
    }

    private Cell cell(String key)
    {
        return cells.computeIfAbsent(key, k -> new Cell(this, k));
    }

}

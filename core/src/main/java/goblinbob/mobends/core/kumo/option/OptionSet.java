package goblinbob.mobends.core.kumo.option;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The options one file declares (a model definition, a type file or an extension file), by name, in
 * the order it declares them. An animator reads them as {@code option.<name>}.
 */
public final class OptionSet
{

    public static final OptionSet EMPTY = new OptionSet("", Collections.emptyMap());

    private final String owner;
    private final Map<String, KumoOption> options;

    OptionSet(String owner, Map<String, KumoOption> options)
    {
        this.owner = owner;
        this.options = Collections.unmodifiableMap(new LinkedHashMap<>(options));
    }

    /** What declares them (see {@link KumoOption#getOwner}). */
    public String getOwner()
    {
        return owner;
    }

    @Nullable
    public KumoOption get(String name)
    {
        return options.get(name);
    }

    public List<KumoOption> all()
    {
        return new ArrayList<>(options.values());
    }

    public boolean isEmpty()
    {
        return options.isEmpty();
    }

    public int size()
    {
        return options.size();
    }

}

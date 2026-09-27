package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kinds of things addressable from animator JSON by a namespaced key ({@code "core:pose"},
 * {@code "mymod:thing"}): each key maps to the template class its JSON is read into and the
 * factory that instances the template. Addons register their own next to the built-in ones.
 *
 * @param <T> the base template class
 * @param <F> the factory type
 */
public final class TypeRegistry<T, F>
{

    private final String kind;
    private final Map<String, Entry<T, F>> entries = new ConcurrentHashMap<>();

    /** @param kind What the keys name, for error messages ("driver", "node type", ...). */
    public TypeRegistry(String kind)
    {
        this.kind = kind;
    }

    public void register(String key, Class<? extends T> templateType, F factory)
    {
        entries.put(key, new Entry<>(templateType, factory));
    }

    /** The template class the JSON of {@code key} is read into, or null if nothing is registered under it. */
    @Nullable
    public Class<? extends T> getTemplateClass(String key)
    {
        Entry<T, F> entry = entries.get(key);
        return entry == null ? null : entry.templateType;
    }

    public F getFactory(@Nullable String key) throws MalformedKumoTemplateException
    {
        if (key == null)
        {
            throw new MalformedKumoTemplateException(String.format("No %s was specified.", kind));
        }
        Entry<T, F> entry = entries.get(key);
        if (entry == null)
        {
            throw new MalformedKumoTemplateException(String.format("Unknown %s: '%s'.", kind, key));
        }
        return entry.factory;
    }

    private static final class Entry<T, F>
    {
        final Class<? extends T> templateType;
        final F factory;

        Entry(Class<? extends T> templateType, F factory)
        {
            this.templateType = templateType;
            this.factory = factory;
        }
    }

}

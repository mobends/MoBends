package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.StandardKeyframeNodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.StandardKeyframeNodeTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class KeyframeNodeRegistry
{

    public static final KeyframeNodeRegistry INSTANCE = new KeyframeNodeRegistry();

    private Map<String, KeyframeNodeRegistry.RegistryEntry<?>> registry = new HashMap<>();

    private KeyframeNodeRegistry()
    {
        register("core:standard", StandardKeyframeNodeState::new, StandardKeyframeNodeTemplate.class);
    }

    public <T extends KeyframeNodeTemplate> void register(String key, IKeyframeNodeFactory<?, T> factory, Class<T> templateType)
    {
        registry.put(key, new KeyframeNodeRegistry.RegistryEntry<T>(factory, templateType));
    }

    @Nullable
    public Type getTemplateClass(String key)
    {
        if (registry.containsKey(key))
        {
            return registry.get(key).nodeType;
        }
        return null;
    }

    public <T extends KeyframeNodeTemplate> INodeState createFromTemplate(IKumoInstancingContext context, T template) throws MalformedKumoTemplateException
    {
        final String type = template.getType();

        if (type == null)
        {
            throw new MalformedKumoTemplateException("No type was specified for KeyframeNode.");
        }

        if (registry.containsKey(type))
        {
            @SuppressWarnings("unchecked") final KeyframeNodeRegistry.RegistryEntry<T> entry = (KeyframeNodeRegistry.RegistryEntry<T>) registry.get(type);
            return createFromTemplate(context, entry, template);
        }
        else
        {
            throw new MalformedKumoTemplateException(String.format("A non-existent KeyframeNode type was specified: %s", type));
        }
    }

    private <T extends KeyframeNodeTemplate> INodeState createFromTemplate(IKumoInstancingContext context, KeyframeNodeRegistry.RegistryEntry<T> entry, T template) throws MalformedKumoTemplateException
    {
        if (!entry.nodeType.equals(template.getClass()))
        {
            throw new MalformedKumoTemplateException(String.format("The KeyframeNode registry holds a wrong entry for '%s'", template.getType()));
        }

        return entry.factory.createKeyframeNode(context, template);
    }

    private static class RegistryEntry<T extends KeyframeNodeTemplate>
    {

        public IKeyframeNodeFactory<?, T> factory;
        public Class<T> nodeType;

        RegistryEntry(IKeyframeNodeFactory<?, T> factory, Class<T> nodeType)
        {
            this.factory = factory;
            this.nodeType = nodeType;
        }

    }

}

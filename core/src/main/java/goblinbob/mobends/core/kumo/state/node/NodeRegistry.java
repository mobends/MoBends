package goblinbob.mobends.core.kumo.state.node;

import goblinbob.mobends.core.kumo.TypeRegistry;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.FallthroughNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;
import goblinbob.mobends.core.kumo.state.template.PoseNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.VanillaNodeTemplate;

import javax.annotation.Nullable;

/** Node types addressable from animator JSON by their key: {@code {"core:pose": {...}}}. */
public class NodeRegistry
{

    public static final NodeRegistry INSTANCE = new NodeRegistry();

    private final TypeRegistry<NodeTemplate, INodeFactory<?, ?>> registry = new TypeRegistry<>("node type");

    private NodeRegistry()
    {
        register("core:pose", PoseNode::createPose, PoseNodeTemplate.class);
        register("core:fallthrough", PoseNode::createFallthrough, FallthroughNodeTemplate.class);
        register("core:vanilla", PoseNode::createVanilla, VanillaNodeTemplate.class);
    }

    public <T extends NodeTemplate> void register(String key, INodeFactory<?, T> factory, Class<T> templateType)
    {
        registry.register(key, templateType, factory);
    }

    @Nullable
    public Class<? extends NodeTemplate> getTemplateClass(String key)
    {
        return registry.getTemplateClass(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends NodeTemplate> INodeState createFromTemplate(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, T template) throws MalformedKumoTemplateException
    {
        // The serializer read the template into the class registered under its type.
        INodeFactory<?, T> factory = (INodeFactory<?, T>) registry.getFactory(template.getType());
        return factory.createNode(context, skeleton, layer, template);
    }

}

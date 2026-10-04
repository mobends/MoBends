package goblinbob.mobends.core.kumo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.serializer.*;
import goblinbob.mobends.core.kumo.state.template.*;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

/**
 * Reads animators. The adapters that check or pick a template class (animators, layers and their
 * machines, nodes, pose items, trigger conditions) read the chosen class through a Gson without their own adapter,
 * so they don't recurse into themselves: hence one Gson per level.
 */
public class KumoSerializer
{

    public static final KumoSerializer INSTANCE = new KumoSerializer();

    /** Reads animators (and anything inside one), checking their format version. */
    public final Gson gson;

    /** Reads an animator's own fields, once its format version is checked: no animator adapter. */
    public final Gson animatorGson;

    /** Reads a layer's own fields: no layer adapter. */
    public final Gson layerGson;

    /** Reads the fields of nodes, pose items and conditions: none of the layer or node adapters. */
    public final Gson leafGson;

    private KumoSerializer()
    {
        leafGson = builderWithLeafAdapters().create();

        layerGson = builderWithLeafAdapters()
                .registerTypeAdapter(NodeTemplate.class, new NodeTemplateSerializer())
                .create();

        animatorGson = builderWithLeafAdapters()
                .registerTypeAdapter(LayerTemplate.class, new LayerTemplateSerializer())
                .registerTypeAdapter(NodeTemplate.class, new NodeTemplateSerializer())
                .create();

        gson = builderWithLeafAdapters()
                .registerTypeAdapter(AnimatorTemplate.class, new AnimatorTemplateSerializer())
                .registerTypeAdapter(LayerTemplate.class, new LayerTemplateSerializer())
                .registerTypeAdapter(NodeTemplate.class, new NodeTemplateSerializer())
                .create();
    }

    private static GsonBuilder builderWithLeafAdapters()
    {
        return new GsonBuilder()
                .registerTypeAdapter(BranchTemplate.class, new BranchTemplateSerializer())
                .registerTypeAdapter(ConnectionTemplate.class, new ConnectionTemplateSerializer())
                .registerTypeAdapter(DefinitionTemplate.class, new ScopeSerializers.Definition())
                .registerTypeAdapter(OnTemplate.class, new ScopeSerializers.On())
                .registerTypeAdapter(StatementTemplate.class, new ScopeSerializers.Statement())
                .registerTypeAdapter(MirrorTemplate.class, new MirrorTemplateSerializer())
                .registerTypeAdapter(PoseItemTemplate.class, new PoseItemSerializer())
                .registerTypeAdapter(ExpressionTemplate.class, new ExpressionTemplate.Deserializer())
                .registerTypeAdapter(DampingTemplate.class, new DampingTemplateSerializer())
                .registerTypeAdapter(SpaceTemplate.class, new SpaceTemplateSerializer());
    }

}

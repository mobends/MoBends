package goblinbob.mobends.core.kumo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import goblinbob.mobends.core.kumo.state.serializer.*;
import goblinbob.mobends.core.kumo.state.template.*;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

public class KumoSerializer
{

    public static final KumoSerializer INSTANCE = new KumoSerializer();

    /** The general multi-purpose gson instance (animators, bends packs). */
    public final Gson gson;

    /** For deserializing templates on the LayerTemplate level and downwards. */
    public final Gson layerGson;

    /** For deserializing templates on the KeyframeNode level and downwards. */
    public final Gson keyframeNodeGson;

    private KumoSerializer()
    {
        keyframeNodeGson = builderWithLeafAdapters().create();

        layerGson = builderWithLeafAdapters()
                .registerTypeAdapter(KeyframeNodeTemplate.class, new KeyframeNodeSerializer())
                .create();

        gson = builderWithLeafAdapters()
                .registerTypeAdapter(LayerTemplate.class, new LayerTemplateSerializer())
                .registerTypeAdapter(KeyframeNodeTemplate.class, new KeyframeNodeSerializer())
                .create();
    }

    private static GsonBuilder builderWithLeafAdapters()
    {
        return new GsonBuilder()
                .registerTypeAdapter(TriggerConditionTemplate.class, new TriggerConditionTemplateSerializer())
                .registerTypeAdapter(PoseItemTemplate.class, new PoseItemSerializer())
                .registerTypeAdapter(ValueTemplate.class, new ValueTemplateSerializer())
                .registerTypeAdapter(TimeTemplate.class, new TimeTemplateSerializer())
                .registerTypeAdapter(DampingTemplate.class, new DampingTemplateSerializer())
                .registerTypeAdapter(SpaceTemplate.class, new SpaceTemplateSerializer());
    }

}

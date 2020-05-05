package goblinbob.mobends.core.kumo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplateSerializer;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplateSerializer;

public class KumoSerializer
{

    public static final KumoSerializer INSTANCE = new KumoSerializer();

    public final Gson gson;

    private KumoSerializer()
    {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LayerTemplate.class, new LayerTemplateSerializer());
        gsonBuilder.registerTypeAdapter(TriggerConditionTemplate.class, new TriggerConditionTemplateSerializer());
        gson = gsonBuilder.create();
    }

}

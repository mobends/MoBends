package goblinbob.mobends.core.pack.state.template;

import com.google.gson.*;

import java.lang.reflect.Type;

public class LayerTemplateSerializer implements JsonSerializer<LayerTemplate>, JsonDeserializer<LayerTemplate>
{

    @Override
    public JsonElement serialize(LayerTemplate src, Type typeOfSrc, JsonSerializationContext context)
    {
        return (new Gson()).toJsonTree(src, src.getLayerType().getTemplateType());
    }

    @Override
    public LayerTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Gson gson = new Gson();
        LayerTemplate abstractLayer = gson.fromJson(json, LayerTemplate.class);
        return gson.fromJson(json, abstractLayer.getLayerType().getTemplateType());
    }

}

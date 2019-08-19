package net.gobbob.mobends.core.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.pack.state.template.LayerTemplate;
import net.gobbob.mobends.core.pack.state.template.LayerTemplateSerializer;
import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplate;
import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplateSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PackProvider
{

    public static final PackProvider instance = new PackProvider();

    private Map<IBendsPack, BendsPackData> dataMap = new HashMap<>();
    private final GsonBuilder gsonBuilder = new GsonBuilder();
    public final Gson gson;

    private PackProvider()
    {
        gsonBuilder.registerTypeAdapter(LayerTemplate.class, new LayerTemplateSerializer());
        gsonBuilder.registerTypeAdapter(TriggerConditionTemplate.class, new TriggerConditionTemplateSerializer());
        gson = gsonBuilder.create();
    }

    public void clearCache()
    {
        this.dataMap.clear();
    }

    public BendsPackData getDataForPack(IBendsPack bendsPack)
    {
        if (dataMap.containsKey(bendsPack))
        {
            return dataMap.get(bendsPack);
        }

        if (bendsPack instanceof LocalBendsPack)
        {
            try
            {
                File file = PackManager.instance.getDataFileForPack(bendsPack.getKey());
                JsonReader fileReader = new JsonReader(new FileReader(file));
                BendsPackData data = gson.fromJson(fileReader, BendsPackData.class);
                dataMap.put(bendsPack, data);
                return data;
            }
            catch (IOException ex)
            {
                Core.LOG.severe(String.format("Data for pack '%s' couldn't be fetched", bendsPack.getKey()));
            }
        }
        return null;
    }

}

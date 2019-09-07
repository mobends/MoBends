package net.gobbob.mobends.core.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.flux.Computed;
import net.gobbob.mobends.core.pack.state.template.LayerTemplate;
import net.gobbob.mobends.core.pack.state.template.LayerTemplateSerializer;
import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplate;
import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplateSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PackDataProvider
{

    public static final PackDataProvider INSTANCE = new PackDataProvider();

    private Map<IBendsPack, BendsPackData> dataMap = new HashMap<>();
    public final Gson gson;

    private Computed<BendsPackData> appliedData;

    private PackDataProvider()
    {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LayerTemplate.class, new LayerTemplateSerializer());
        gsonBuilder.registerTypeAdapter(TriggerConditionTemplate.class, new TriggerConditionTemplateSerializer());
        gson = gsonBuilder.create();

        appliedData = new Computed<>(() -> {
            Collection<IBendsPack> packs = PackManager.INSTANCE.appliedPacks.getValue();

            List<BendsPackData> dataList = new LinkedList<>();
            for (IBendsPack pack : packs)
            {
                BendsPackData data = getDataForPack(pack);
                if (data != null)
                {
                    dataList.add(data);
                }
            }

            if (dataList.size() == 0)
            {
                return null;
            }

            return PackCombiner.combineData(dataList);
        });
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
                File file = PackManager.INSTANCE.getDataFileForPack(bendsPack.getKey());
                JsonReader fileReader = new JsonReader(new FileReader(file));
                BendsPackData data = gson.fromJson(fileReader, BendsPackData.class);
                fileReader.close();
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

    public BendsPackData getAppliedData()
    {
        return appliedData.getValue();
    }

}

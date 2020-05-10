package goblinbob.mobends.core.pack;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.flux.Computed;
import goblinbob.mobends.core.kumo.KumoSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PackDataProvider
{

    public static final PackDataProvider INSTANCE = new PackDataProvider();

    private Map<IBendsPack, BendsPackData> dataMap = new HashMap<>();

    private Computed<BendsPackData> appliedData;

    private PackDataProvider()
    {
        appliedData = new Computed<>(() -> {
            final Collection<IBendsPack> packs = PackManager.INSTANCE.appliedPacks.getValue();

            final List<BendsPackData> dataList = new LinkedList<>();
            for (IBendsPack pack : packs)
            {
                final BendsPackData data = getDataForPack(pack);
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
                BendsPackData data = KumoSerializer.INSTANCE.gson.fromJson(fileReader, BendsPackData.class);
                fileReader.close();
                dataMap.put(bendsPack, data);
                return data;
            }
            catch (IOException | JsonSyntaxException ex)
            {
                ex.printStackTrace();
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

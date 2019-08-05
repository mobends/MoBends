package net.gobbob.mobends.core.pack;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.gobbob.mobends.core.Core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PackProvider
{

    public static final PackProvider instance = new PackProvider();

    private Map<IBendsPack, BendsPackData> dataMap = new HashMap<>();

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
                BendsPackData data = (new Gson()).fromJson(fileReader, BendsPackData.class);
                dataMap.put(bendsPack, data);
                return data;
            }
            catch(IOException e)
            {
                Core.LOG.severe(String.format("Data for pack '%s' couldn't be fetched", bendsPack.getKey()));
            }
        }
        return null;
    }

}

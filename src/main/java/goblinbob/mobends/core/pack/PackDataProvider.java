package goblinbob.mobends.core.pack;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PackDataProvider
{

    public static final PackDataProvider INSTANCE = new PackDataProvider();

    private Map<IBendsPack, BendsPackData> dataMap = new HashMap<>();

    private BendsPackData appliedData;

    private PackDataProvider()
    {
    }

    public void createBendsPackData(Collection<IBendsPack> packs) throws InvalidPackFormatException
    {
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
            appliedData = null;
        }

        appliedData = PackCombiner.combineData(dataList);
    }

    public void clearCache()
    {
        this.dataMap.clear();
    }

    public BendsPackData getDataForPack(IBendsPack bendsPack) throws InvalidPackFormatException
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

                validatePackData(data);

                dataMap.put(bendsPack, data);
                return data;
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                throw new InvalidPackFormatException(bendsPack.getDisplayName(), String.format("Data for pack '%s' couldn't be fetched", bendsPack.getKey()));
            }
            catch (JsonSyntaxException | MalformedKumoTemplateException ex)
            {
                ex.printStackTrace();
                throw new InvalidPackFormatException(bendsPack.getDisplayName(), String.format("The '%s' pack isn't in a correct format.", bendsPack.getKey()));
            }
        }

        return null;
    }

    public void validatePackData(BendsPackData data) throws MalformedKumoTemplateException
    {
        IKumoValidationContext context = new IKumoValidationContext() {
            @Override
            public boolean doesAnimationExist(String animationKey)
            {
                if (data.keyframeAnimations.containsKey(animationKey))
                {
                    return true;
                }

                try
                {
                    return AnimationLoader.loadFromPath(animationKey) != null;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        for (AnimatorTemplate animator : data.targets.values())
        {
            for (LayerTemplate layer : animator.layers)
            {
                layer.validate(context);
            }
        }
    }

    public BendsPackData getAppliedData()
    {
        return appliedData;
    }

}

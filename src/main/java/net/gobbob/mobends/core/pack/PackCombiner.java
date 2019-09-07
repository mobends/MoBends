package net.gobbob.mobends.core.pack;

import java.util.HashMap;
import java.util.List;

public class PackCombiner
{

    /**
     * Combining given packs into one data. The resulting data will contain
     * animations and targets from each pack. If packs share a target, the ones
     * that appear first in the array will take precedence.
     * @param packs
     * @return
     */
    public static BendsPackData combineData(List<BendsPackData> packs)
    {
        BendsPackData combinedData = new BendsPackData();
        combinedData.targets = new HashMap<>();
        combinedData.keyframeAnimations = new HashMap<>();

        for (int i = packs.size() - 1; i >= 0; --i)
        {
            BendsPackData data = packs.get(i);
            combinedData.targets.putAll(data.targets);
            combinedData.keyframeAnimations.putAll(data.keyframeAnimations);
        }

        return combinedData;
    }

}

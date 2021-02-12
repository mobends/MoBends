package goblinbob.mobends.core.mutation;

import goblinbob.mobends.core.exceptions.UnmappedPartException;

import java.util.Collection;
import java.util.Map;

public class MutationInstructions
{
    private Map<String, VanillaPart> vanillaParts;
    private Map<String, PartMutationInstructions> mutate;
    private Map<String, PartMutationInstructions> add;

    public String getFieldName(String partName) throws UnmappedPartException
    {
        if (!vanillaParts.containsKey(partName))
        {
            throw new UnmappedPartException(partName);
        }

        return vanillaParts.get(partName).getFieldName();
    }

    public String getObfuscated(String partName) throws UnmappedPartException
    {
        if (!vanillaParts.containsKey(partName))
        {
            throw new UnmappedPartException(partName);
        }

        return vanillaParts.get(partName).getObfuscated();
    }

    public Collection<Map.Entry<String, VanillaPart>> getVanillaParts()
    {
        return vanillaParts.entrySet();
    }

    public Collection<Map.Entry<String, PartMutationInstructions>> getPartMutations()
    {
        return mutate.entrySet();
    }

    public Collection<Map.Entry<String, PartMutationInstructions>> getAddedParts()
    {
        return add.entrySet();
    }
}

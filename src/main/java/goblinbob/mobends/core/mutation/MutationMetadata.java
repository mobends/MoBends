package goblinbob.mobends.core.mutation;

import goblinbob.mobends.core.exceptions.UnmappedPartException;

import java.util.Map;

public class MutationMetadata
{
    private String target;
    private Map<String, PartMetadata> parts;

    public String getTarget()
    {
        return target;
    }

    public String getFieldName(String partName) throws UnmappedPartException
    {
        if (!parts.containsKey(partName))
        {
            throw new UnmappedPartException(partName);
        }

        return parts.get(partName).getFieldName();
    }
}

package goblinbob.mobends.core.mutation;

import java.util.Collection;
import java.util.Map;

public class MutationInstructions
{
    private String id;
    private String target;
    private Map<String, PartMutationInstructions> mutate;
    private Map<String, PartMutationInstructions> add;

    public String getID()
    {
        return id;
    }

    public String getTarget()
    {
        return target;
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

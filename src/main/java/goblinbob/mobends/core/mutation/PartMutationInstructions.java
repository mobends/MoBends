package goblinbob.mobends.core.mutation;

import com.google.common.collect.ImmutableList;

public class PartMutationInstructions
{
    private String parent;
    private String extendedBy;
    private float[] position;
    private int[] textureOffset;
    private BoxDefinition[] boxes;

    public String getParent()
    {
        return parent;
    }

    public String getExtendedBy()
    {
        return extendedBy;
    }

    public float[] getPosition()
    {
        return position;
    }

    public int[] getTextureOffset()
    {
        return textureOffset;
    }

    public Iterable<BoxDefinition> getBoxes()
    {
        return ImmutableList.copyOf(boxes);
    }
}

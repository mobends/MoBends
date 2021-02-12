package goblinbob.mobends.core.mutation;

import com.google.common.collect.ImmutableList;

public class PartMutationInstructions
{
    /**
     * True if the part is rendered by the original renderer independently, and not as a child.
     */
    private boolean independent = true;
    private String parent;
    private String extendedBy;
    private float[] position;
    private int[] textureOffset;
    private BoxDefinition[] boxes;

    public boolean isIndependent()
    {
        return independent;
    }

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

package goblinbob.mobends.core.mutation;

import java.util.Map;

public class BoxDefinition
{
    float[] position;
    int[] dimensions;
    int[] textureOffset;
    float scaleFactor = 0.0F;
    Map<BoxSide, FaceDefinition> faces;

    public float[] getPosition()
    {
        return position;
    }

    public int[] getDimensions()
    {
        return dimensions;
    }

    public int[] getTextureOffset()
    {
        return textureOffset;
    }

    public float getScaleFactor()
    {
        return scaleFactor;
    }

    public FaceDefinition getFaceMutation(BoxSide boxSide)
    {
        if (faces == null)
        {
            return null;
        }

        return faces.get(boxSide);
    }
}

package goblinbob.mobends.core.mutation;

import java.util.Map;

public class BoxDefinition
{
    float[] position;
    float[] dimensions;
    Float scaleFactor = null;
    Map<BoxSide, FaceDefinition> faces;
}

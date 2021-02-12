package goblinbob.mobends.core.mutation;

import goblinbob.mobends.forge.BoxFactory;

public class FaceDefinition
{
    boolean hidden = false;
    float offsetX = 0;
    float offsetY = 0;
    FaceRotation rotate = FaceRotation.IDENTITY;

    public void applyTo(BoxFactory factory, BoxSide side)
    {
        factory.offsetTextureQuad(side, offsetX, offsetY);
        factory.rotateTextureQuad(side, rotate);

        if (hidden)
        {
            factory.hideFace(side);
        }
    }
}

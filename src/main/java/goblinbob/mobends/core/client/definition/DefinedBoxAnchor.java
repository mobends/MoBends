package goblinbob.mobends.core.client.definition;

import net.minecraft.client.model.ModelBase;
import goblinbob.mobends.core.client.model.MutatedBox;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

/**
 * What a definition puts in a vanilla model's box list for one of its parts (a bone or a segment
 * with boxes): vanilla picks a part from that list, then one of its boxes, to stick an arrow in
 * ({@code LayerArrow}). It draws nothing; its boxes are where the part's are, and {@code postRender}
 * puts them where the part is posed now.
 */
public class DefinedBoxAnchor extends ModelRenderer
{

    private final DefinedModelPart part;

    public DefinedBoxAnchor(ModelBase model, DefinedModelPart part)
    {
        // A ModelRenderer adds itself to the model's box list, which is where the anchor goes.
        super(model);
        this.part = part;
        // The part's boxes where they are: a mutated box leaves vanilla's bounds at 0, which an
        // arrow reads (sizes rounded to whole units, enough for where an arrow sticks).
        for (ModelBox box : part.cubeList)
        {
            if (box instanceof MutatedBox)
            {
                MutatedBox b = (MutatedBox) box;
                cubeList.add(new ModelBox(this, 0, 0, b.minX, b.minY, b.minZ, size(b.maxX - b.minX), size(b.maxY - b.minY), size(b.maxZ - b.minZ), 0));
            }
            else
            {
                cubeList.add(box);
            }
        }
    }

    private static int size(float extent)
    {
        return Math.max(1, Math.round(extent));
    }

    @Override
    public void render(float scale)
    {
    }

    @Override
    public void renderWithRotation(float scale)
    {
    }

    @Override
    public void postRender(float scale)
    {
        part.applyCharacterTransform(scale);
    }

}

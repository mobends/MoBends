package goblinbob.mobends.test.geometry;

import net.minecraft.client.model.ModelRenderer;

import java.util.ArrayList;
import java.util.List;

/** What a model draws, captured (see {@link Capture}). */
public final class ModelGeometry
{

    private static final float SCALE = 0.0625F;

    private ModelGeometry()
    {
    }

    /** The quads {@code parts} draw, rendered as a model renders them, in model units. */
    public static List<Capture.Vertex[]> render(ModelRenderer... parts)
    {
        Capture.reset();
        Capture.push();
        Capture.scale(16, 16, 16);
        for (ModelRenderer part : parts)
        {
            part.render(SCALE);
        }
        Capture.pop();
        return new ArrayList<>(Capture.drawn);
    }

    /** Where {@code part.postRender} puts what is attached to it (a held item, a hat): its matrix, in model units. */
    public static double[] postRender(ModelRenderer part)
    {
        Capture.reset();
        Capture.push();
        Capture.scale(16, 16, 16);
        part.postRender(SCALE);
        Capture.scale(1 / 16.0, 1 / 16.0, 1 / 16.0);
        double[] matrix = Capture.matrix();
        Capture.pop();
        return matrix;
    }

}

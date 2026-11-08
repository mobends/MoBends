package goblinbob.mobends.test.geometry;

import goblinbob.mobends.standard.client.model.armor.ArmorMesh;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Armour drawn as faces rather than vanilla boxes is cut at a limb's joint as the boxes are: each
 * face's part above the cut plane goes to the upper segment, the rest to the lower, the texture
 * following the cut.
 */
public class ArmorMeshTest
{

    /** A quad in the x/y plane from y0 to y1, u across x and v down y. */
    private static float[] quad(float y0, float y1)
    {
        return new float[] {
                0, y0, 0, 0, 0, 0, 0, -1,
                4, y0, 0, 1, 0, 0, 0, -1,
                4, y1, 0, 1, 1, 0, 0, -1,
                0, y1, 0, 0, 1, 0, 0, -1,
        };
    }

    private static float[] bounds(ArmorMesh mesh, int component)
    {
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (int i = 0; i < mesh.getVertexCount(); i++)
        {
            float value = mesh.getData()[i * ArmorMesh.STRIDE + component];
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return new float[] { min, max };
    }

    @Test
    public void aFaceAcrossTheCutIsSplitAtIt()
    {
        ArmorMesh above = new ArmorMesh(), below = new ArmorMesh();
        ArmorMesh.slice(quad(-2, 10), 4, 4, above, below, ArmorMesh.scratchFor(4));

        assertEquals(4, above.getVertexCount());
        assertEquals(4, below.getVertexCount());
        assertArrayEquals(new float[] { -2, 4 }, bounds(above, 1), 1e-6F);
        assertArrayEquals(new float[] { 4, 10 }, bounds(below, 1), 1e-6F);
        // The texture is cut where the face is: half way down it.
        assertArrayEquals(new float[] { 0, 0.5F }, bounds(above, 4), 1e-6F);
        assertArrayEquals(new float[] { 0.5F, 1 }, bounds(below, 4), 1e-6F);
        // Every vertex keeps the face's normal.
        assertEquals(-1, above.getData()[7], 0);
        assertEquals(-1, below.getData()[7], 0);
    }

    @Test
    public void aFaceOnOneSideStaysWhole()
    {
        ArmorMesh above = new ArmorMesh(), below = new ArmorMesh();
        float[] scratch = ArmorMesh.scratchFor(4);
        ArmorMesh.slice(quad(-2, 4), 4, 4, above, below, scratch);
        ArmorMesh.slice(quad(6, 10), 4, 4, above, below, scratch);

        assertEquals(4, above.getVertexCount());
        assertArrayEquals(new float[] { -2, 4 }, bounds(above, 1), 1e-6F);
        assertEquals(4, below.getVertexCount());
        assertArrayEquals(new float[] { 6, 10 }, bounds(below, 1), 1e-6F);
    }

    @Test
    public void aFaceOnTheCutGoesAboveOnly()
    {
        ArmorMesh above = new ArmorMesh(), below = new ArmorMesh();
        float[] flat = {
                0, 4, 0, 0, 0, 0, 1, 0,
                4, 4, 0, 1, 0, 0, 1, 0,
                4, 4, 4, 1, 1, 0, 1, 0,
                0, 4, 4, 0, 1, 0, 1, 0,
        };
        ArmorMesh.slice(flat, 4, 4, above, below, ArmorMesh.scratchFor(4));

        assertEquals(4, above.getVertexCount());
        assertTrue(below.isEmpty());
    }

    @Test
    public void aCornerCutOffLeavesATriangleAndAPentagon()
    {
        // A diamond: its top corner pokes above the cut.
        float[] diamond = {
                2, 0, 0, 0.5F, 0, 0, 0, -1,
                4, 2, 0, 1, 0.5F, 0, 0, -1,
                2, 4, 0, 0.5F, 1, 0, 0, -1,
                0, 2, 0, 0, 0.5F, 0, 0, -1,
        };
        ArmorMesh above = new ArmorMesh(), below = new ArmorMesh();
        ArmorMesh.slice(diamond, 4, 1, above, below, ArmorMesh.scratchFor(4));

        // A triangle is a quad with its last vertex twice; a pentagon two quads.
        assertEquals(4, above.getVertexCount());
        assertEquals(8, below.getVertexCount());
        assertArrayEquals(new float[] { 0, 1 }, bounds(above, 1), 1e-6F);
        assertArrayEquals(new float[] { 1, 3 }, bounds(above, 0), 1e-6F);
        assertArrayEquals(new float[] { 1, 4 }, bounds(below, 1), 1e-6F);
    }

}

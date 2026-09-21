package goblinbob.mobends.lab;

import goblinbob.mobends.core.definition.BoxSplitter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A leg box cut at the knee keeps its texture strips lined up and its end caps where they were. */
public class BoxSplitterTest
{
    @Test
    void legSplitsAtTheKnee()
    {
        // A biped leg: 4 x 12 x 4 at (-2, 0, -2), textured at (0, 16) on a 64 x 32 sheet.
        float[] min = { -2, 0, -2 }, max = { 2, 12, 2 };
        float[][] faces = {
                { 8, 20, 4, 12 }, { 0, 20, 4, 12 },      // left, right
                { 4, 16, 4, 4 }, { 8, 16, 4, -4 },       // top, bottom
                { 4, 20, 4, 12 }, { 12, 20, 4, 12 },     // front, back
        };
        List<BoxSplitter.Segment> segments = BoxSplitter.split(min, max, faces, 1, new float[] { 0.5F });
        assertEquals(2, segments.size());
        BoxSplitter.Segment upper = segments.get(0), lower = segments.get(1);

        assertEquals(0, upper.min[1]); assertEquals(6, upper.max[1]);
        assertEquals(0, lower.min[1]); assertEquals(6, lower.max[1], "the lower segment is in its own frame");
        assertEquals(6, lower.pivot[1], "the knee is six units below the hip");
        assertEquals(0, lower.pivot[0]); assertEquals(0, lower.pivot[2]);

        // Side strips: the lower segment continues the texture where the upper one ends.
        assertEquals(20, upper.faces[0][1]); assertEquals(6, upper.faces[0][3]);
        assertEquals(26, lower.faces[0][1]); assertEquals(6, lower.faces[0][3]);
        assertEquals(26, lower.faces[4][1]); assertEquals(6, lower.faces[5][3]);
        // The caps stay: the top on the upper segment, the bottom on the lower.
        assertTrue((upper.visibility & (1 << BoxSplitter.TOP)) != 0);
        assertTrue((upper.visibility & (1 << BoxSplitter.BOTTOM)) == 0);
        assertTrue((lower.visibility & (1 << BoxSplitter.TOP)) == 0);
        assertTrue((lower.visibility & (1 << BoxSplitter.BOTTOM)) != 0);
        // Untouched faces are copied as they were.
        assertEquals(16, upper.faces[2][1]);
    }

    @Test
    void threeSegmentsAlongZ()
    {
        float[] min = { -1, 0, 0 }, max = { 1, 2, 9 };
        float[][] faces = { { 0, 0, 9, 2 }, { 0, 0, 9, 2 }, { 0, 0, 2, 9 }, { 0, 0, 2, 9 }, { 0, 0, 2, 2 }, { 0, 0, 2, 2 } };
        List<BoxSplitter.Segment> segments = BoxSplitter.split(min, max, faces, 2, new float[] { 1F / 3, 2F / 3 });
        assertEquals(3, segments.size());
        assertEquals(3, segments.get(1).pivot[2], 1e-5);
        assertEquals(3, segments.get(2).pivot[2], 1e-5);
        assertEquals(3, segments.get(2).max[2], 1e-5);
        assertEquals(6, segments.get(2).faces[0][0], 1e-5, "the left strip's u continues");
        assertEquals(3, segments.get(2).faces[0][2], 1e-5);
    }
}

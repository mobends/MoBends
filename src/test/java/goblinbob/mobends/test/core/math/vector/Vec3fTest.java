package goblinbob.mobends.test.core.math.vector;

import static org.junit.Assert.*;
import org.junit.Test;

import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.math.vector.VectorUtils;

public class Vec3fTest
{
    @Test
    public void lengthSqTest()
    {
        Vec3f vec = new Vec3f(1, 1, 0);
        assertEquals(vec.lengthSq(), 2, 0.001F);
    }

    @Test
    public void lengthTest()
    {
        Vec3f vec = new Vec3f(1, 1, 0);
        assertEquals(vec.length(), Math.sqrt(2), 0.001F);
    }

    @Test
    public void normalizeTest()
    {
        Vec3f vec = new Vec3f(1, 0, 1);
        VectorUtils.normalize(vec);
        assertEquals(vec.x, 1 / Math.sqrt(2), 0.001F);
        assertEquals(vec.y, 0, 0.001F);
        assertEquals(vec.z, 1 / Math.sqrt(2), 0.001F);
    }
}

package goblinbob.mobends.test.core.math;

import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.state.VariableScope;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import org.junit.Test;

import static org.junit.Assert.*;

public class SmoothingTest
{

    private static Quaternion axisAngle(float x, float y, float z, float degrees)
    {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(x, y, z, (float) Math.toRadians(degrees));
        return q;
    }

    private static float angleBetween(Quaternion a, Quaternion b)
    {
        float dot = Math.abs(a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w);
        return (float) Math.toDegrees(2 * Math.acos(Math.min(1F, dot)));
    }

    @Test
    public void smoothingTakesTheShortWayToASignFlippedTarget()
    {
        SmoothOrientation orientation = new SmoothOrientation();
        orientation.snapTo(axisAngle(0, 1, 0, 10));
        Quaternion target = axisAngle(0, 1, 0, 20);
        target.set(-target.x, -target.y, -target.z, -target.w); // the same rotation
        orientation.setSmoothness(0.5F);
        orientation.target(target);
        orientation.update(1F);

        assertEquals(15F, angleBetween(new Quaternion(), orientation.getSmooth()), 0.5F);
    }

    @Test
    public void nlerpTakesTheShortWay()
    {
        Quaternion b = axisAngle(1, 0, 0, 40);
        b.set(-b.x, -b.y, -b.z, -b.w);
        Quaternion half = new Quaternion();
        PoseMath.nlerp(new Quaternion(), b, 0.5F, half);
        assertEquals(20F, angleBetween(new Quaternion(), half), 0.5F);
    }

    @Test
    public void scaleScalesTheAngle()
    {
        Quaternion scaled = new Quaternion();
        PoseMath.scale(axisAngle(0, 0, 1, 90), 0.5F, scaled);
        assertEquals(0F, angleBetween(axisAngle(0, 0, 1, 45), scaled), 0.01F);
    }

    @Test
    public void conjugateIsTheInverse()
    {
        Quaternion q = axisAngle(1, 2, 3, 70);
        q.normalise();
        Quaternion inverse = new Quaternion();
        inverse.set(q);
        inverse.conjugate();
        Quaternion product = new Quaternion();
        Quaternion.mul(q, inverse, product);
        assertEquals(0F, angleBetween(new Quaternion(), product), 0.1F);
    }

    @Test
    public void variableScopeGrowsAndKeepsValues()
    {
        VariableScope scope = new VariableScope();
        for (int i = 0; i < 10; i++)
        {
            scope.set("v" + i, i);
        }
        scope.set("v3", 30);
        assertEquals(30, scope.get("v3"), 0);
        assertEquals(9, scope.get("v9"), 0);
        assertFalse(scope.has("missing"));
        assertEquals(0, scope.get("missing"), 0);
    }

}

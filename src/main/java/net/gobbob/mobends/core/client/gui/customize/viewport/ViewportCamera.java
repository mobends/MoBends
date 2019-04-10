package net.gobbob.mobends.core.client.gui.customize.viewport;

import net.gobbob.mobends.core.math.TransformUtils;
import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.matrix.Mat4x4dBuffered;
import net.gobbob.mobends.core.math.matrix.MatrixUtils;
import net.gobbob.mobends.core.math.physics.Ray;
import net.gobbob.mobends.core.math.vector.*;
import net.gobbob.mobends.core.util.GUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class ViewportCamera
{

    /**
     * The camera's origin relative to the viewport's origin.
     */
    private Vec3f position;
    private Vec3f prevPosition;

    /**
     * These are calculated based on the current orientation of the camera.
     * Should be updated any time the orientation changes.
     */
    private Vec3f forward;
    private Vec3f up;
    private Vec3f right;

    private float prevAngleYaw,
            prevAnglePitch;
    private float angleYaw,    // around the Y axis in radians (-PI, PI)
            anglePitch;    // around the X axis in radians (-PI, PI)

    private Vec3f anchorPoint;
    private float anchorDistance = 0;
    private boolean anchored = false;

    private Mat4x4dBuffered viewMatrix;
    private Mat4x4dBuffered projectionMatrix;

    public ViewportCamera(float posX, float posY, float posZ, float angleYaw, float anglePitch)
    {
        this.position = new Vec3f(posX, posY, posZ);
        this.prevPosition = new Vec3f(this.position);
        this.anchorPoint = new Vec3f();
        this.angleYaw = this.prevAngleYaw = angleYaw;
        this.anglePitch = this.prevAnglePitch = anglePitch;

        this.forward = new Vec3f();
        this.up = new Vec3f();
        this.right = new Vec3f();
        this.viewMatrix = new Mat4x4dBuffered();
        this.projectionMatrix = new Mat4x4dBuffered();
        this.updateLocalSpace();
    }

    public void setupProjection(float fovy, float aspect, float zNear, float zFar)
    {
        float sine, cotangent, deltaZ;
        float radians = fovy / 2 * GUtil.PI / 180;

        deltaZ = zFar - zNear;
        sine = (float) Math.sin(radians);

        if ((deltaZ == 0) || (sine == 0) || (aspect == 0))
        {
            return;
        }

        cotangent = (float) Math.cos(radians) / sine;

        MatrixUtils.identity(this.projectionMatrix);
        this.projectionMatrix.set(0, 0, cotangent / aspect);
        this.projectionMatrix.set(1, 1, cotangent);
        this.projectionMatrix.set(2, 2, -(zFar + zNear) / deltaZ);
        this.projectionMatrix.set(2, 3, -1);
        this.projectionMatrix.set(3, 2, -2 * zNear * zFar / deltaZ);
        this.projectionMatrix.set(3, 3, 0);
        this.projectionMatrix.updateBuffer();
    }

    public void update()
    {
        this.prevPosition.set(this.position);
        this.prevAngleYaw = this.angleYaw;
        this.prevAnglePitch = this.anglePitch;
    }

    public void setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
    }

    /**
     * Moves the camera in world space.
     *
     * @param x The amount to move by in the x axis.
     * @param y The amount to move by in the y axis.
     * @param z The amount to move by in the z axis.
     */
    public void moveGlobal(float x, float y, float z)
    {
        this.position.add(x, y, z);
    }

    public void moveForward(float amount)
    {
        if (this.anchored)
        {
            this.anchorPoint.add(this.forward.x * amount, this.forward.y * amount, this.forward.z * amount);
            this.updateAsAnchored();
        }
    }

    public void moveSideways(float amount)
    {
        if (this.anchored)
        {
            this.anchorPoint.add(this.right.x * amount, this.right.y * amount, this.right.z * amount);
            this.updateAsAnchored();
        }
    }

    public void moveUp(float amount)
    {
        if (this.anchored)
        {
            this.anchorPoint.add(this.up.x * amount, this.up.y * amount, this.up.z * amount);
            this.updateAsAnchored();
        }
    }

    public void zoomInOrOut(float amount)
    {
        this.anchorDistance += (amount) * (1 + this.anchorDistance * 4F) * 0.04F;
        this.anchorDistance = Math.max(0.1F, this.anchorDistance);
        this.updateAsAnchored();
    }

    public void setAnchorDistance(float distance)
    {
        this.anchorDistance = distance;
    }

    public void rotateYaw(float angle)
    {
        this.angleYaw += angle;
        while (this.angleYaw >= Math.PI)
            this.angleYaw -= Math.PI * 2;
        while (this.angleYaw < -Math.PI)
            this.angleYaw += Math.PI * 2;

        if (this.anchored)
        {
            this.updateAsAnchored();
        }

        this.updateLocalSpace();
    }

    public void rotatePitch(float angle)
    {
        this.anglePitch += angle;
        if (this.anglePitch < -GUtil.PI / 2)
            this.anglePitch = -GUtil.PI / 2;
        if (this.anglePitch > GUtil.PI / 2)
            this.anglePitch = GUtil.PI / 2;

        if (this.anchored)
        {
            this.updateAsAnchored();
        }

        this.updateLocalSpace();
    }

    public void applyProjection()
    {
        GlStateManager.multMatrix(this.projectionMatrix.getBuffer());
    }

    public void applyViewTransform(float partialTicks)
    {
        this.updateViewMatrix(partialTicks);

        GlStateManager.multMatrix(this.viewMatrix.getBuffer());
    }

    public void anchorTo(float x, float y, float z, float distance)
    {
        this.anchorPoint.set(x, y, z);
        this.anchorDistance = distance;
        this.anchored = true;
        this.updateAsAnchored();
    }

    public void unanchor()
    {
        this.anchored = false;
    }

    public void updateAsAnchored()
    {
        final float cosPitch = MathHelper.cos(this.anglePitch);
        final float dX = MathHelper.sin(this.angleYaw) * cosPitch;
        final float dZ = -MathHelper.cos(this.angleYaw) * cosPitch;
        final float dY = -MathHelper.sin(this.anglePitch);

        this.position.set(
                this.anchorPoint.x - dX * this.anchorDistance,
                this.anchorPoint.y - dY * this.anchorDistance,
                this.anchorPoint.z - dZ * this.anchorDistance
        );
    }

    private void updateLocalSpace()
    {
        final float sinPitch = MathHelper.sin(this.anglePitch);
        final float cosPitch = MathHelper.cos(this.anglePitch);

        this.forward.set(
                MathHelper.sin(this.angleYaw) * cosPitch,
                -MathHelper.sin(this.anglePitch),
                -MathHelper.cos(this.angleYaw) * cosPitch
        );

        this.up.set(
                MathHelper.sin(this.angleYaw) * sinPitch,
                MathHelper.cos(this.anglePitch),
                -MathHelper.cos(this.angleYaw) * sinPitch
        );

        this.right.set(
                MathHelper.cos(this.angleYaw),
                0,
                MathHelper.sin(this.angleYaw)
        );
    }

    private void updateViewMatrix(float partialTicks)
    {
        MatrixUtils.identity(this.viewMatrix);
        float yaw = GUtil.interpolateRadians(this.angleYaw, this.prevAngleYaw, partialTicks);
        float pitch = GUtil.interpolateRadians(this.anglePitch, this.prevAnglePitch, partialTicks);

        float x = this.prevPosition.x + (this.position.x - this.prevPosition.x) * partialTicks;
        float y = this.prevPosition.y + (this.position.y - this.prevPosition.y) * partialTicks;
        float z = this.prevPosition.z + (this.position.z - this.prevPosition.z) * partialTicks;

        TransformUtils.rotate(this.viewMatrix, pitch, 1.0, 0.0, 0.0, this.viewMatrix);
        TransformUtils.rotate(this.viewMatrix, yaw, 0.0, 1.0, 0.0, this.viewMatrix);
        TransformUtils.translate(this.viewMatrix, -x, -y, -z, this.viewMatrix);
        this.viewMatrix.updateBuffer();
    }

    public void lookAt(float x, float y, float z)
    {
        float dX = this.anchorPoint.x - this.position.x;
        float dY = this.anchorPoint.y - this.position.y;
        float dZ = this.anchorPoint.z - this.position.z;

        this.lookInDirection(dX, dY, dZ);
    }

    public void lookInDirection(float x, float y, float z)
    {
        this.angleYaw = (float) MathHelper.atan2(x, z);
        float xzLen = MathHelper.sqrt(x * x + z * z);
        this.anglePitch = (float) MathHelper.atan2(xzLen, y);

        this.updateLocalSpace();
    }

    public IVec3fRead getPosition()
    {
        return this.position;
    }

    public IVec3fRead getForward()
    {
        return this.forward;
    }

    public IVec3fRead getUp()
    {
        return this.up;
    }

    public IVec3fRead getRight()
    {
        return this.right;
    }

    public IMat4x4d getViewMatrix()
    {
        return this.viewMatrix;
    }

    public IMat4x4d getProjectionMatrix()
    {
        return this.projectionMatrix;
    }

    public Ray getRayFromScreen(int mx, int my, int width, int height)
    {
        float nx = ((float) mx / (float) width) * 2 - 1;
        float ny = 1 - ((float) my / (float) height) * 2;

        // Z needs to go forward, W is by default
        Vec4d clip = new Vec4d(nx, ny, -1, 1);
        Vec4d eye = new Vec4d(0, 0, 0, 0);

        Mat4x4d inverseProj = new Mat4x4d();
        MatrixUtils.inverse(this.projectionMatrix, inverseProj);
        TransformUtils.transform(clip, inverseProj, eye);
        eye.z = -1; // Pointing out from the camera
        eye.w = 0; // It's not a point

        Mat4x4d inverseView = new Mat4x4d();
        MatrixUtils.inverse(this.viewMatrix, inverseView);
        Vec4d world = new Vec4d();
        TransformUtils.transform(eye, inverseView, world);

        double length = Math.sqrt(world.x * world.x +
                world.y * world.y +
                world.z * world.z);

        return new Ray(this.position.x, this.position.y, this.position.z,
                (float) (world.x / length),
                (float) (world.y / length),
                (float) (world.z / length));
    }

}

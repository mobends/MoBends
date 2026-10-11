package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.api.DoubleInput;
import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.EvalContext;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.PoseWriter;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.ChainIkTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PointTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/**
 * See {@link ChainIkTemplate}: {@code core:two_bone_ik} and {@code core:three_bone_ik}. The target
 * and the joints' directions are brought from the world into the frame the chain hangs from, back
 * through what the renderer does to the model (see {@code MutatedRenderer.beforeRender}): turned
 * by {@code -bodyYaw} about the entity's position, the root offset, the center rotation about half
 * the entity's height, the render rotation, the local offset, then vanilla's own (its turn by
 * {@code bodyYaw} cancels the first; the model drawn flipped in Y and Z, its origin 1.501 blocks
 * above the entity's), then the chain's parents. {@link ChainIk} solves it there.
 */
public final class ChainIkDriver implements DriverEvaluator
{

    public static final String STRETCH = "stretch";

    /** Where vanilla draws the model's origin above the entity's, in blocks. */
    private static final double ORIGIN_Y = 1.501;

    public static final KumoDriver<ChainIkTemplate> TWO_BONE = KumoDriver.of("core:two_bone_ik", ChainIkTemplate.class, (template, args) -> new ChainIkDriver(template, args, 2));
    public static final KumoDriver<ChainIkTemplate> THREE_BONE = KumoDriver.of("core:three_bone_ik", ChainIkTemplate.class, (template, args) -> new ChainIkDriver(template, args, 3));

    private final int[] bones;
    private final double[][] pivots;
    private final double[] end;
    private final float[] bends;
    /** Per segment, its joint's direction in the world, or null (and always the first's). */
    private final NumberInput[][] directions;
    private final DoubleInput targetX, targetY, targetZ;
    private final DoubleInput x, y, z;
    private final NumberInput yaw, height, weight;
    private final StateHandle stretch;
    private final int rootBone, localOffsetBone, centerRotationBone, renderRotationBone;
    private final int[] parentBones;
    private final double[][] parentPivots;
    private final double units;
    private final double scale;

    // Scratch: one evaluation at a time.
    private final Quaternion quaternion = new Quaternion();
    private final Quaternion blended = new Quaternion();
    private final Quaternion scratch = new Quaternion();
    private final Vec3f vector = new Vec3f();
    private final double[] center = new double[4], render = new double[4];
    private final double[][] parentRotations;
    private final double[][] segments, chainDirections, fallbacks, rotations, posedSoFar;

    private ChainIkDriver(ChainIkTemplate template, DriverBindArgs args, int count) throws MalformedKumoTemplateException
    {
        if (template.segments == null || template.segments.size() != count)
        {
            throw args.error("needs " + count + " 'segments', each {\"bone\": ..., \"pivot\": [x, y, z]}, from the one nearest the body.");
        }
        if (!isVector(template.end))
        {
            throw args.error("needs 'end' as [x, y, z]: the point that reaches, from the last segment's pivot.");
        }
        if (template.target == null || template.target.x == null || template.target.y == null || template.target.z == null)
        {
            throw args.error("needs 'target', {\"x\": ..., \"y\": ..., \"z\": ...}, in the world.");
        }
        if (template.unitsPerBlock <= 0 || template.modelScale <= 0)
        {
            throw args.error("'unitsPerBlock' and 'modelScale' must be positive.");
        }

        bones = new int[count];
        pivots = new double[count][];
        bends = new float[count];
        directions = new NumberInput[count][];
        for (int i = 0; i < count; i++)
        {
            ChainIkTemplate.Segment segment = template.segments.get(i);
            String field = "segments[" + i + "]";
            if (segment == null || !isVector(segment.pivot))
            {
                throw args.error("'" + field + "' needs a 'bone' and a 'pivot' as [x, y, z].");
            }
            bones[i] = args.bone(field + ".bone", segment.bone);
            pivots[i] = toDoubles(segment.pivot);
            if (i == 0)
            {
                if (segment.bendDirection != null)
                {
                    throw args.error("'" + field + "' is the first segment: it has no joint to give a 'bendDirection'.");
                }
                continue;
            }
            if (segment.bend == 0)
            {
                throw args.error("'" + field + ".bend' is 1 (a knee) or -1 (an elbow).");
            }
            bends[i] = Math.signum(segment.bend);
            directions[i] = segment.bendDirection == null ? null : vector(args, field + ".bendDirection", segment.bendDirection);
        }
        end = toDoubles(template.end);

        targetX = args.doubleNumber("target.x", template.target.x);
        targetY = args.doubleNumber("target.y", template.target.y);
        targetZ = args.doubleNumber("target.z", template.target.z);
        x = args.doubleNumber("x", template.x);
        y = args.doubleNumber("y", template.y);
        z = args.doubleNumber("z", template.z);
        yaw = args.number("yaw", template.yaw);
        height = args.number("height", template.height);
        weight = args.number("weight", template.weight, 1);
        stretch = args.outputs(template.out, STRETCH).get(STRETCH);

        rootBone = args.bone("rootBone", template.rootBone);
        localOffsetBone = args.bone("localOffset", Skeleton.LOCAL_OFFSET);
        centerRotationBone = args.bone("centerRotation", Skeleton.CENTER_ROTATION);
        renderRotationBone = args.bone("renderRotation", "renderRotation");

        int parentCount = template.parents == null ? 0 : template.parents.size();
        parentBones = new int[parentCount];
        parentPivots = new double[parentCount][];
        for (int i = 0; i < parentCount; i++)
        {
            ChainIkTemplate.Parent parent = template.parents.get(i);
            String field = "parents[" + i + "]";
            if (parent == null || !isVector(parent.pivot))
            {
                throw args.error("'" + field + "' needs a 'bone' and a 'pivot' as [x, y, z].");
            }
            parentBones[i] = args.bone(field + ".bone", parent.bone);
            parentPivots[i] = toDoubles(parent.pivot);
        }

        units = template.unitsPerBlock;
        scale = template.modelScale;
        parentRotations = new double[parentCount][4];
        segments = new double[count][];
        chainDirections = new double[count][];
        fallbacks = new double[count][];
        rotations = new double[count][4];
        posedSoFar = new double[count][4];
    }

    private static NumberInput[] vector(DriverBindArgs args, String field, PointTemplate point) throws MalformedKumoTemplateException
    {
        return new NumberInput[] {
                args.number(field + ".x", point.x, 0),
                args.number(field + ".y", point.y, 0),
                args.number(field + ".z", point.z, 0)};
    }

    private static boolean isVector(float[] v)
    {
        return v != null && v.length == 3;
    }

    private static double[] toDoubles(float[] v)
    {
        return new double[] {v[0], v[1], v[2]};
    }

    @Override
    public void evaluate(EvalContext context, PoseWriter pose)
    {
        final float w = Math.max(0F, Math.min(1F, weight.get(context)));

        // The renderer's frame, as the items before this one have posed it.
        pose.rotationSoFar(centerRotationBone, quaternion);
        toDoubles(quaternion, center);
        pose.rotationSoFar(renderRotationBone, quaternion);
        toDoubles(quaternion, render);
        final double yawRad = Math.toRadians(yaw.get(context));
        final double cos = Math.cos(yawRad), sin = Math.sin(yawRad);
        final double halfHeight = height.get(context) / 2;

        // The target, from the entity, in doubles until it's a difference.
        double[] point = toModel(targetX.get(context) - x.get(context), targetY.get(context) - y.get(context), targetZ.get(context) - z.get(context),
                cos, sin, halfHeight, pose);
        for (int i = 0; i < parentBones.length; i++)
        {
            pose.rotationSoFar(parentBones[i], quaternion);
            toDoubles(quaternion, parentRotations[i]);
            pose.offsetSoFar(parentBones[i], vector);
            point = ChainIk.rotateInverse(parentRotations[i], new double[] {
                    point[0] - parentPivots[i][0] - vector.x,
                    point[1] - parentPivots[i][1] - vector.y,
                    point[2] - parentPivots[i][2] - vector.z});
        }
        pose.offsetSoFar(bones[0], vector);
        final double[] target = {point[0] - pivots[0][0] - vector.x, point[1] - pivots[0][1] - vector.y, point[2] - pivots[0][2] - vector.z};

        // The segments, each to the next one's pivot (where it is offset to) and the last to its end.
        for (int i = 0; i < bones.length; i++)
        {
            if (i + 1 < bones.length)
            {
                pose.offsetSoFar(bones[i + 1], vector);
                segments[i] = new double[] {pivots[i + 1][0] + vector.x, pivots[i + 1][1] + vector.y, pivots[i + 1][2] + vector.z};
            }
            else
            {
                segments[i] = end;
            }
        }

        // Where each joint points: told, or as the chain is posed so far.
        final double[] soFar = {0, 0, 0, 1};
        for (int i = 0; i < bones.length; i++)
        {
            pose.rotationSoFar(bones[i], quaternion);
            toDoubles(quaternion, posedSoFar[i]);
            if (i > 0)
            {
                fallbacks[i] = ChainIk.rotate(soFar, ChainIk.protrusion(segments[i - 1], bends[i]));
                chainDirections[i] = directions[i] == null ? null : toChain(directions[i][0].get(context), directions[i][1].get(context), directions[i][2].get(context), cos, sin);
            }
            ChainIk.multiply(soFar, posedSoFar[i], soFar);
        }

        final double shortfall = ChainIk.solve(segments, bends, chainDirections, fallbacks, target, rotations);
        if (stretch != null)
        {
            stretch.set(context, (float) (shortfall * scale / units));
        }
        if (w <= 0)
        {
            return;
        }
        for (int i = 0; i < bones.length; i++)
        {
            double[] r = rotations[i];
            quaternion.set((float) r[0], (float) r[1], (float) r[2], (float) r[3]);
            if (w >= 1F)
            {
                // At full weight the bone snaps to the IK, so the end doesn't slide.
                pose.rotate(bones[i], quaternion, Pose.Space.OVERRIDE);
                pose.snapRotation(bones[i]);
            }
            else
            {
                double[] s = posedSoFar[i];
                blended.set((float) s[0], (float) s[1], (float) s[2], (float) s[3]);
                PoseMath.slerp(blended, quaternion, w, scratch);
                pose.rotate(bones[i], scratch, Pose.Space.OVERRIDE);
            }
        }
    }

    /**
     * A point relative to the entity, in blocks, into the model (model units): turned into the
     * body's frame (+Z ahead, +X left, +Y up), then back through the root offset, the center
     * rotation, the render rotation, the local offset and vanilla's flip.
     */
    private double[] toModel(double dx, double dy, double dz, double cos, double sin, double halfHeight, PoseWriter pose)
    {
        pose.vectorSoFar(rootBone, vector);
        double[] p = {dx * cos + dz * sin - vector.x / units, dy - vector.y / units - halfHeight, -dx * sin + dz * cos - vector.z / units};
        p = ChainIk.rotateInverse(center, p);
        p[1] += halfHeight;
        p = ChainIk.rotateInverse(render, p);
        pose.vectorSoFar(localOffsetBone, vector);
        return new double[] {
                (p[0] - vector.x / units) / scale * units,
                (ORIGIN_Y - (p[1] - vector.y / units) / scale) * units,
                -(p[2] - vector.z / units) / scale * units};
    }

    /** A direction in the world into the chain's frame, as {@link #toModel} turns it. */
    private double[] toChain(double dx, double dy, double dz, double cos, double sin)
    {
        double[] d = {dx * cos + dz * sin, dy, -dx * sin + dz * cos};
        d = ChainIk.rotateInverse(render, ChainIk.rotateInverse(center, d));
        d[1] = -d[1];
        d[2] = -d[2];
        for (double[] parent : parentRotations)
        {
            d = ChainIk.rotateInverse(parent, d);
        }
        return d;
    }

    private static void toDoubles(Quaternion q, double[] dest)
    {
        dest[0] = q.x;
        dest[1] = q.y;
        dest[2] = q.z;
        dest[3] = q.w;
    }

}

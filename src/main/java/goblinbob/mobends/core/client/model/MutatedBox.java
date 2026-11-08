package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A textured box Mo' Bends draws itself: a vanilla box (or a piece of one, cut by
 * {@code BoxSplitter}), or one a model definition declares. Its faces are in the mod's order, LEFT
 * (+X), RIGHT (-X), TOP (-Y), BOTTOM (+Y), FRONT (-Z), BACK (+Z), and any of them can be hidden.
 * <p>
 * The quads are laid out as vanilla's {@code ModelPart.Cube} lays out its polygons (the same corners
 * in the same order, the same texture corners, the same mirroring and normals), so a box made from a
 * vanilla one draws exactly as vanilla draws it: LEFT is vanilla's EAST polygon, RIGHT its WEST, TOP
 * its DOWN, BOTTOM its UP, FRONT its NORTH and BACK its SOUTH.
 */
public class MutatedBox {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    /** The faces in the order vanilla draws its polygons (DOWN, UP, WEST, NORTH, EAST, SOUTH). */
    private static final int[] DRAW_ORDER = { TOP, BOTTOM, RIGHT, FRONT, LEFT, BACK };
    /** Each face's outward direction before mirroring: vanilla's {@code Direction.step()} of its polygon. */
    private static final float[][] NORMALS = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 } };

    /** Where the box is, in its part's frame, in model units. */
    public final float minX, minY, minZ, maxX, maxY, maxZ;
    private final byte faceVisibilityFlag;
    /** Per face, four corners: x, y, z (model units), u, v (0 to 1). */
    private final float[][][] quads = new float[6][][];
    private final Vector3f[] normals = new Vector3f[6];

    /**
     * @param faces         the texture of each face, in texture pixels
     * @param mirrored      mirrors the box's texture along X, as a vanilla box's {@code mirror} does
     * @param textureWidth  the texture's size the faces are given in
     */
    public MutatedBox(IVec3fRead min, IVec3fRead max, BoxFactory.TextureFace[] faces, byte faceVisibilityFlag, boolean mirrored,
                      float textureWidth, float textureHeight) {
        this.faceVisibilityFlag = faceVisibilityFlag;
        this.minX = min.getX();
        this.minY = min.getY();
        this.minZ = min.getZ();
        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();

        float x0 = minX;
        float x1 = maxX;
        if (mirrored) {
            float temp = x1;
            x1 = x0;
            x0 = temp;
        }
        // Vanilla's corners (ModelPart.Cube): c7 is (x0, y0, z0), and so on.
        float[] c7 = { x0, minY, minZ };
        float[] c0 = { x1, minY, minZ };
        float[] c1 = { x1, maxY, minZ };
        float[] c2 = { x0, maxY, minZ };
        float[] c3 = { x0, minY, maxZ };
        float[] c4 = { x1, minY, maxZ };
        float[] c5 = { x1, maxY, maxZ };
        float[] c6 = { x0, maxY, maxZ };
        float[][][] corners = {
            { c4, c0, c1, c5 }, // LEFT, vanilla's EAST
            { c7, c3, c6, c2 }, // RIGHT, WEST
            { c4, c3, c7, c0 }, // TOP, DOWN
            { c1, c2, c6, c5 }, // BOTTOM, UP
            { c0, c7, c2, c1 }, // FRONT, NORTH
            { c3, c4, c5, c6 }, // BACK, SOUTH
        };
        for (int f = 0; f < 6; f++) {
            quads[f] = createQuad(corners[f], faces[f], textureWidth, textureHeight, mirrored);
            normals[f] = new Vector3f(NORMALS[f][0], NORMALS[f][1], NORMALS[f][2]);
            if (mirrored) {
                // As vanilla: the face that was on one side along X is on the other now.
                normals[f].mul(-1.0F, 1.0F, 1.0F);
            }
        }
    }

    /**
     * One face: its corners, with the texture's corners as vanilla's {@code ModelPart.Polygon} (and
     * 1.12's {@code TexturedQuad}) gives them, turned as the face asks, the order reversed if mirrored.
     */
    private static float[][] createQuad(float[][] corners, BoxFactory.TextureFace face, float textureWidth, float textureHeight, boolean mirrored) {
        float uSize = face == null ? 0 : face.uSize;
        float vSize = face == null ? 0 : face.vSize;
        FaceRotation rotation = face == null ? FaceRotation.IDENTITY : face.faceRotation;
        if (rotation == FaceRotation.CLOCKWISE || rotation == FaceRotation.COUNTER_CLOCKWISE) {
            float swap = uSize;
            uSize = vSize;
            vSize = swap;
        }
        float u1 = face == null ? 0 : face.uPos;
        float v1 = face == null ? 0 : face.vPos;
        float u2 = u1 + uSize;
        float v2 = v1 + vSize;
        float[][] uvs = {
            { u2 / textureWidth, v1 / textureHeight },
            { u1 / textureWidth, v1 / textureHeight },
            { u1 / textureWidth, v2 / textureHeight },
            { u2 / textureWidth, v2 / textureHeight },
        };
        // 1.12's ModelUtils.applyFaceRotation: each corner takes the texture corner a quarter turn
        // (or two) along.
        int offset = switch (rotation) {
            case IDENTITY -> 0;
            case CLOCKWISE -> 3;
            case COUNTER_CLOCKWISE -> 1;
            case HALF_TURN -> 2;
        };
        float[][] quad = new float[4][];
        for (int i = 0; i < 4; i++) {
            float[] uv = uvs[(i + offset) % 4];
            quad[i] = new float[] { corners[i][0], corners[i][1], corners[i][2], uv[0], uv[1] };
        }
        if (mirrored) {
            for (int i = 0; i < 2; i++) {
                float[] swap = quad[i];
                quad[i] = quad[3 - i];
                quad[3 - i] = swap;
            }
        }
        return quad;
    }

    public boolean isFaceVisible(int faceIndex) {
        return ((faceVisibilityFlag >> faceIndex) & 1) == 1;
    }

    /** Writes the visible faces, as {@code ModelPart.Cube.compile} writes a vanilla box. */
    public void render(PoseStack.Pose pose, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        Matrix4f matrix = pose.pose();
        Vector3f scratch = new Vector3f();
        for (int f : DRAW_ORDER) {
            if (!isFaceVisible(f)) {
                continue;
            }
            Vector3f normal = pose.transformNormal(normals[f], scratch);
            float nx = normal.x();
            float ny = normal.y();
            float nz = normal.z();
            for (float[] vertex : quads[f]) {
                Vector3f position = matrix.transformPosition(vertex[0] / 16.0F, vertex[1] / 16.0F, vertex[2] / 16.0F, scratch);
                consumer.addVertex(position.x(), position.y(), position.z(), color, vertex[3], vertex[4], packedOverlay, packedLight, nx, ny, nz);
            }
        }
    }
}

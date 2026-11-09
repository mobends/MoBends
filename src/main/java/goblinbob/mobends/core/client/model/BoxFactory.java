package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.geom.ModelPart;

import java.util.ArrayList;
import java.util.List;

/**
 * A box being made: where it is, the texture of each face (in texture pixels, faces in
 * {@link MutatedBox}'s order), which faces show, and whether its texture is mirrored. Read from a
 * vanilla box ({@link #of(ModelPart.Cube)}) or made from a size and a texture corner, then moved,
 * grown and cut before {@link #create()} makes the box drawn.
 */
public class BoxFactory {
    public final Vec3f min;
    public final Vec3f max;
    public final TextureFace[] faces = new TextureFace[6];
    public boolean mirrored;
    public byte faceVisibilityFlag = 0b111111;
    /** The size of the texture the faces are given in (a vanilla box has it baked into its UVs). */
    public float textureWidth = 64;
    public float textureHeight = 64;
    /**
     * For a box read from vanilla: how far vanilla grew it past its texture on each axis (its
     * {@code CubeDeformation}), already in {@link #min} and {@link #max}.
     */
    public final float[] inflation = new float[3];

    private int uvWidth;
    private int uvHeight;
    private int uvLength;
    private int textureU;
    private int textureV;
    private boolean textureUVSet;

    public BoxFactory(float x, float y, float z, int dx, int dy, int dz, float delta) {
        this.min = new Vec3f(x - delta, y - delta, z - delta);
        this.max = new Vec3f(x + dx + delta, y + dy + delta, z + dz + delta);
        this.uvWidth = dx;
        this.uvHeight = dy;
        this.uvLength = dz;
    }

    public BoxFactory(IVec3fRead min, IVec3fRead max, TextureFace[] faces) {
        this.min = new Vec3f(min);
        this.max = new Vec3f(max);
        for (int i = 0; i < faces.length; ++i) {
            this.faces[i] = faces[i] == null ? null : new TextureFace(faces[i]);
        }
        this.textureUVSet = true;
    }

    /**
     * Reads a vanilla box back: its bounds as drawn (grown as vanilla grew it, which only its
     * vertices carry), whether it's mirrored, the faces it has, and each face's texture, from the
     * polygons it writes.
     * <p>
     * The polygons are vanilla's own (private) data, so the box is asked to write itself into a
     * consumer that only records what it's given. Each polygon is four vertices; its normal says
     * which face it is. Vanilla's texture corners are (u2, v1), (u1, v1), (u1, v2), (u2, v2), in
     * reverse order when mirrored, so a face's texture starts at its second vertex and ends at its
     * fourth (its third and first, mirrored), as 1.12's {@code BoxFactory} read them.
     */
    public static BoxFactory of(ModelPart.Cube cube) {
        List<float[]> vertices = readVertices(cube);
        int polygons = vertices.size() / 4;
        float[] dimensions = { cube.maxX - cube.minX, cube.maxY - cube.minY, cube.maxZ - cube.minZ };
        boolean mirrored = isMirrored(vertices, polygons);

        // Which face each polygon is: by its normal, whose X vanilla flips on a mirrored box.
        int[] faceOf = new int[polygons];
        for (int p = 0; p < polygons; p++) {
            float[] first = vertices.get(p * 4);
            float nx = mirrored ? -first[5] : first[5];
            float ny = first[6];
            float nz = first[7];
            if (Math.abs(nx) > 0.5F) faceOf[p] = nx > 0 ? MutatedBox.LEFT : MutatedBox.RIGHT;
            else if (Math.abs(ny) > 0.5F) faceOf[p] = ny < 0 ? MutatedBox.TOP : MutatedBox.BOTTOM;
            else faceOf[p] = nz < 0 ? MutatedBox.FRONT : MutatedBox.BACK;
        }

        // The texture's size, from how much of it a face of known size spans: a face's texture is
        // as many pixels as the box is units (depth across the sides, width across the top, ...).
        float textureWidth = 0;
        float textureHeight = 0;
        for (int p = 0; p < polygons; p++) {
            float[] start = vertices.get(p * 4 + (mirrored ? 2 : 1));
            float[] end = vertices.get(p * 4 + (mirrored ? 0 : 3));
            int face = faceOf[p];
            float across = face == MutatedBox.LEFT || face == MutatedBox.RIGHT ? dimensions[2] : dimensions[0];
            float down = face == MutatedBox.TOP || face == MutatedBox.BOTTOM ? dimensions[2] : dimensions[1];
            float du = Math.abs(end[3] - start[3]);
            float dv = Math.abs(end[4] - start[4]);
            if (textureWidth == 0 && across > 0 && du > 0) textureWidth = snap(across / du);
            if (textureHeight == 0 && down > 0 && dv > 0) textureHeight = snap(down / dv);
        }
        if (textureWidth == 0) textureWidth = 64;
        if (textureHeight == 0) textureHeight = 64;

        float[] lowest = { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
        float[] highest = { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
        for (float[] vertex : vertices) {
            for (int i = 0; i < 3; i++) {
                lowest[i] = Math.min(lowest[i], vertex[i]);
                highest[i] = Math.max(highest[i], vertex[i]);
            }
        }
        if (vertices.isEmpty()) {
            lowest = new float[] { cube.minX, cube.minY, cube.minZ };
            highest = new float[] { cube.maxX, cube.maxY, cube.maxZ };
        }

        BoxFactory box = new BoxFactory(new Vec3f(lowest[0], lowest[1], lowest[2]), new Vec3f(highest[0], highest[1], highest[2]), new TextureFace[6]);
        box.mirrored = mirrored;
        box.textureWidth = textureWidth;
        box.textureHeight = textureHeight;
        box.inflation[0] = Math.max(0, cube.minX - lowest[0]);
        box.inflation[1] = Math.max(0, cube.minY - lowest[1]);
        box.inflation[2] = Math.max(0, cube.minZ - lowest[2]);
        box.faceVisibilityFlag = 0;
        for (int p = 0; p < polygons; p++) {
            float[] start = vertices.get(p * 4 + (mirrored ? 2 : 1));
            float[] end = vertices.get(p * 4 + (mirrored ? 0 : 3));
            int face = faceOf[p];
            box.faces[face] = new TextureFace(start[3] * textureWidth, start[4] * textureHeight,
                    (end[3] - start[3]) * textureWidth, (end[4] - start[4]) * textureHeight);
            box.faceVisibilityFlag |= (byte) (1 << face);
        }
        for (int f = 0; f < 6; f++) {
            // A face vanilla leaves out stays hidden; it still needs a texture to be cut.
            if (box.faces[f] == null) {
                box.faces[f] = new TextureFace(0, 0, 0, 0);
            }
        }
        return box;
    }

    /** A texture size read back from float UVs: a whole number of pixels, when it's that within rounding. */
    private static float snap(float size) {
        float rounded = Math.round(size);
        return Math.abs(size - rounded) < 0.01F ? rounded : size;
    }

    /**
     * Whether vanilla built the box mirrored. A mirrored box swaps its corners along X but not the
     * texture, so along the top, bottom and front faces the texture runs against X instead of with
     * it (along the back it runs against X unmirrored). Failing that (a box with no width), the
     * polygon vanilla writes first of the two X faces is WEST's, which faces +X once mirrored.
     */
    private static boolean isMirrored(List<float[]> vertices, int polygons) {
        for (int p = 0; p < polygons; p++) {
            float[] a = vertices.get(p * 4);
            float[] b = vertices.get(p * 4 + 1);
            float ny = a[6];
            float nz = a[7];
            if (Math.abs(ny) < 0.5F && Math.abs(nz) < 0.5F) {
                continue;
            }
            float alongX = (a[3] - b[3]) * (a[0] - b[0]);
            if (alongX == 0) {
                continue;
            }
            boolean withX = alongX > 0;
            boolean back = nz > 0.5F;
            return back == withX;
        }
        for (int p = 0; p < polygons; p++) {
            float nx = vertices.get(p * 4)[5];
            if (Math.abs(nx) > 0.5F) {
                return nx > 0;
            }
        }
        return false;
    }

    /** The cube's vertices as it writes them with no transform: x, y, z (model units), u, v, nx, ny, nz. */
    private static List<float[]> readVertices(ModelPart.Cube cube) {
        VertexRecorder recorder = new VertexRecorder();
        cube.compile(new PoseStack().last(), recorder, 0, 0, -1);
        return recorder.vertices;
    }

    public BoxFactory inflate(float dx, float dy, float dz) {
        this.min.add(-dx, -dy, -dz);
        this.max.add(dx, dy, dz);
        return this;
    }

    public BoxFactory resize(float dx, float dy, float dz) {
        this.max.set(this.min.x + dx, this.min.y + dy, this.min.z + dz);
        return this;
    }

    public BoxFactory offset(float x, float y, float z) {
        this.min.add(x, y, z);
        this.max.add(x, y, z);
        return this;
    }

    /** Textures the box as vanilla's {@code addBox} does, its texture starting at {@code (u, v)}. */
    public BoxFactory withUVs(int u, int v) {
        this.textureU = u;
        this.textureV = v;
        this.textureUVSet = true;
        this.generateTextureFaces();
        return this;
    }

    public BoxFactory withTextureSize(float width, float height) {
        this.textureWidth = width;
        this.textureHeight = height;
        return this;
    }

    public BoxFactory hideFace(BoxSide face) {
        this.faceVisibilityFlag &= (byte) ~(1 << face.faceIndex);
        return this;
    }

    public BoxFactory offsetTextureQuad(BoxSide face, float x, float y) {
        ensureTextureFaces();
        this.faces[face.faceIndex].uPos += x;
        this.faces[face.faceIndex].vPos += y;
        return this;
    }

    public BoxFactory rotateTextureQuad(BoxSide face, FaceRotation rotation) {
        ensureTextureFaces();
        this.faces[face.faceIndex].faceRotation = rotation;
        return this;
    }

    public BoxFactory flipTextureQuad(BoxSide face) {
        ensureTextureFaces();
        this.faces[face.faceIndex].flipped = !this.faces[face.faceIndex].flipped;
        return this;
    }

    public MutatedBox create() {
        ensureTextureFaces();
        return new MutatedBox(this.min, this.max, this.faces, this.faceVisibilityFlag, this.mirrored, this.textureWidth, this.textureHeight);
    }

    private void ensureTextureFaces() {
        if (!this.textureUVSet) {
            this.textureUVSet = true;
            this.generateTextureFaces();
        }
    }

    /** The faces vanilla's {@code addBox} gives a box of this size whose texture starts at its corner. */
    private void generateTextureFaces() {
        int u = this.textureU;
        int v = this.textureV;
        this.faces[0] = new TextureFace(u + uvLength + uvWidth, v + uvLength, uvLength, uvHeight);
        this.faces[1] = new TextureFace(u, v + uvLength, uvLength, uvHeight);
        this.faces[2] = new TextureFace(u + uvLength, v, uvWidth, uvLength);
        this.faces[3] = new TextureFace(u + uvLength + uvWidth, v + uvLength, uvWidth, -uvLength);
        this.faces[4] = new TextureFace(u + uvLength, v + uvLength, uvWidth, uvHeight);
        this.faces[5] = new TextureFace(u + uvLength + uvWidth + uvLength, v + uvLength, uvWidth, uvHeight);
    }

    /** A face's texture: where it starts and how far it runs (negative runs backwards), in texture pixels. */
    public static class TextureFace {
        public float uPos;
        public float vPos;
        public float uSize;
        public float vSize;
        public FaceRotation faceRotation = FaceRotation.IDENTITY;
        /** Whether the texture is mirrored left to right on the face, before {@link #faceRotation} turns it. */
        public boolean flipped;

        public TextureFace(float uPos, float vPos, float uSize, float vSize) {
            this.uPos = uPos;
            this.vPos = vPos;
            this.uSize = uSize;
            this.vSize = vSize;
        }

        public TextureFace(TextureFace face) {
            this.uPos = face.uPos;
            this.vPos = face.vPos;
            this.uSize = face.uSize;
            this.vSize = face.vSize;
            this.faceRotation = face.faceRotation;
            this.flipped = face.flipped;
        }
    }

    /** Keeps what a vanilla box writes, a vertex at a time; draws nothing. */
    private static final class VertexRecorder implements VertexConsumer {
        final List<float[]> vertices = new ArrayList<>();
        private float[] current;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            // Written divided by 16, which a power of two keeps exact.
            current = new float[] { x * 16.0F, y * 16.0F, z * 16.0F, 0, 0, 0, 0, 0 };
            vertices.add(current);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            current[3] = u;
            current[4] = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            current[5] = x;
            current[6] = y;
            current[7] = z;
            return this;
        }
    }
}

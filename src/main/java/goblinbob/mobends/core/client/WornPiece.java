package goblinbob.mobends.core.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nullable;

/**
 * Where a piece of armour that draws itself its own way (GeckoLib's) goes on a bent entity: on the
 * bone it's worn on, and, if that bone is split (an arm, a leg), cut where the bone is cut, what's
 * past the elbow or knee following the forearm or shin, as 1.12's {@code GeoArmorMesh} cut it.
 * Made by {@link BendsLayers#wornPiece}.
 * <p>
 * The armour draws as it does, in {@link #frame()}: a point given as vanilla's part has it at rest
 * (in blocks, from where vanilla's part turns, y down) lands on the bone. What it draws through
 * {@link #cut} is cut and moved onto the further pieces.
 */
public final class WornPiece {
    /** Per piece of the bone (the bone, then its further segments): its frame (see {@link #frame()}), in the space the armour is drawn in. */
    private final Matrix4f[] frames;
    /** Where each further piece starts, along {@link #axis}, in blocks in {@link #frame()}; null if the bone isn't cut. */
    @Nullable
    private final float[] cuts;
    private final int axis;

    WornPiece(Matrix4f[] frames, @Nullable float[] cuts, int axis) {
        this.frames = frames;
        this.cuts = cuts != null && cuts.length == frames.length - 1 && axis >= 0 ? cuts : null;
        this.axis = axis;
    }

    /**
     * The frame the armour is drawn in, in the space it's drawn in (the pose its layer was handed):
     * vanilla's part's, posed as the bone is.
     */
    public Matrix4fc frame() {
        return frames[0];
    }

    /**
     * What to draw the armour through, so what is past a cut follows the further piece of the bone;
     * {@code buffer} itself if the bone isn't cut. The armour is drawn as quads (four vertices each,
     * given whole, as GeckoLib gives them), in the space {@link #frame()} is in.
     */
    public VertexConsumer cut(VertexConsumer buffer) {
        return cuts == null ? buffer : new Cut(buffer, this);
    }

    /**
     * Sends quads on to the buffer underneath, each piece of a quad between two cuts moved from the
     * bone's frame into the further piece's. A cut quad becomes polygons of three to six corners,
     * drawn as quads in a fan from the first (1.12's {@code ArmorMesh}), a triangle as a quad with
     * its last corner twice. Vertices given one value at a time (not as a whole vertex) aren't cut.
     */
    public static final class Cut implements VertexConsumer {
        /** Per corner: x, y, z (where it's drawn), u, v, normal x, y, z, and where it is along the cut axis. */
        private static final int STRIDE = 9;
        private static final int AXIS = 8;

        private VertexConsumer delegate;
        /** From where it's drawn into the bone's frame, in blocks. */
        private final Matrix4f toBone;
        private final float[] cuts;
        private final int axis;
        /** Per further piece (from 1): from the bone's frame, where it's drawn, to the piece's, and its normals. */
        private final Matrix4f[] moves;
        private final Matrix3f[] normalMoves;

        private final float[] quad = new float[4 * STRIDE];
        private final int[] colors = new int[4];
        private final int[] overlays = new int[4];
        private final int[] lights = new int[4];
        private int corners;
        /** Two polygons being clipped back and forth, at most six corners each (a quad cut by two planes). */
        private final float[] polygon = new float[8 * STRIDE];
        private final float[] clipped = new float[8 * STRIDE];
        private final Vector4f position = new Vector4f();
        private final Vector3f normal = new Vector3f();

        Cut(VertexConsumer delegate, WornPiece piece) {
            this.delegate = delegate;
            this.toBone = new Matrix4f(piece.frames[0]).invert();
            this.cuts = piece.cuts;
            this.axis = piece.axis;
            this.moves = new Matrix4f[piece.frames.length];
            this.normalMoves = new Matrix3f[piece.frames.length];
            for (int k = 1; k < piece.frames.length; k++) {
                moves[k] = new Matrix4f(piece.frames[k]).mul(toBone);
                normalMoves[k] = moves[k].normal(new Matrix3f());
            }
        }

        /** The buffer it draws into. */
        public VertexConsumer delegate() {
            return delegate;
        }

        /** Draws into {@code delegate} from now on (the renderer may have to swap its buffer for a fresh one). */
        public void setDelegate(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight,
                              float normalX, float normalY, float normalZ) {
            int at = corners * STRIDE;
            quad[at] = x;
            quad[at + 1] = y;
            quad[at + 2] = z;
            quad[at + 3] = u;
            quad[at + 4] = v;
            quad[at + 5] = normalX;
            quad[at + 6] = normalY;
            quad[at + 7] = normalZ;
            position.set(x, y, z, 1.0F);
            toBone.transform(position);
            quad[at + AXIS] = axis == 0 ? position.x : axis == 1 ? position.y : position.z;
            colors[corners] = color;
            overlays[corners] = packedOverlay;
            lights[corners] = packedLight;
            if (++corners == 4) {
                corners = 0;
                drawQuad();
            }
        }

        /** The piece a point this far along the axis is on: past the last cut it reaches. */
        private int pieceAt(float along) {
            int piece = 0;
            for (int k = 0; k < cuts.length; k++) {
                if (along >= cuts[k]) {
                    piece = k + 1;
                }
            }
            return piece;
        }

        private void drawQuad() {
            int first = Integer.MAX_VALUE;
            int last = Integer.MIN_VALUE;
            for (int i = 0; i < 4; i++) {
                int piece = pieceAt(quad[i * STRIDE + AXIS]);
                first = Math.min(first, piece);
                last = Math.max(last, piece);
            }
            if (first == last) {
                System.arraycopy(quad, 0, polygon, 0, 4 * STRIDE);
                draw(polygon, 4, first);
                return;
            }
            for (int piece = first; piece <= last; piece++) {
                System.arraycopy(quad, 0, polygon, 0, 4 * STRIDE);
                int count = 4;
                if (piece > 0) {
                    // Keep what's at or past the cut the piece starts at.
                    count = clip(polygon, count, cuts[piece - 1], true);
                }
                if (piece < cuts.length && count >= 3) {
                    // Keep what's at or before the next cut.
                    count = clip(polygon, count, cuts[piece], false);
                }
                if (count >= 3) {
                    draw(polygon, count, piece);
                }
            }
        }

        /**
         * Clips the convex polygon of {@code count} corners in {@code corners} to one side of
         * {@code cut} (past it if {@code keepPast}), in place.
         *
         * @return how many corners are left
         */
        private int clip(float[] corners, int count, float cut, boolean keepPast) {
            int kept = 0;
            for (int i = 0; i < count; i++) {
                int a = i * STRIDE;
                int b = ((i + 1) % count) * STRIDE;
                float da = keepPast ? corners[a + AXIS] - cut : cut - corners[a + AXIS];
                float db = keepPast ? corners[b + AXIS] - cut : cut - corners[b + AXIS];
                if (da >= 0) {
                    System.arraycopy(corners, a, clipped, kept++ * STRIDE, STRIDE);
                }
                if ((da > 0 && db < 0) || (da < 0 && db > 0)) {
                    float t = da / (da - db);
                    int at = kept++ * STRIDE;
                    for (int k = 0; k < STRIDE; k++) {
                        clipped[at + k] = corners[a + k] + (corners[b + k] - corners[a + k]) * t;
                    }
                    clipped[at + AXIS] = cut;
                }
            }
            System.arraycopy(clipped, 0, corners, 0, kept * STRIDE);
            return kept;
        }

        /** Draws a convex polygon as quads, a fan from its first corner, moved onto further piece {@code piece} if it isn't 0. */
        private void draw(float[] corners, int count, int piece) {
            if (piece > 0) {
                Matrix4f move = moves[piece];
                Matrix3f normalMove = normalMoves[piece];
                for (int i = 0; i < count; i++) {
                    int at = i * STRIDE;
                    position.set(corners[at], corners[at + 1], corners[at + 2], 1.0F);
                    move.transform(position);
                    corners[at] = position.x;
                    corners[at + 1] = position.y;
                    corners[at + 2] = position.z;
                    normal.set(corners[at + 5], corners[at + 6], corners[at + 7]);
                    float length = normal.length();
                    normalMove.transform(normal);
                    if (normal.lengthSquared() > 0) {
                        // As long as it was: GeckoLib hands them over as its pose makes them.
                        normal.normalize(length);
                    }
                    corners[at + 5] = normal.x;
                    corners[at + 6] = normal.y;
                    corners[at + 7] = normal.z;
                }
            }
            for (int i = 1; i + 1 < count; i += 2) {
                emit(corners, 0);
                emit(corners, i);
                emit(corners, i + 1);
                emit(corners, Math.min(i + 2, count - 1));
            }
        }

        private void emit(float[] corners, int corner) {
            int at = corner * STRIDE;
            // A quad's corners share their colour, overlay and light in GeckoLib's armour.
            delegate.addVertex(corners[at], corners[at + 1], corners[at + 2], colors[0], corners[at + 3], corners[at + 4], overlays[0], lights[0],
                    corners[at + 5], corners[at + 6], corners[at + 7]);
        }

        // Vertices given one value at a time go straight through, uncut.

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}

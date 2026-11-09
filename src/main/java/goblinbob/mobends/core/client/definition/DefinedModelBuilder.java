package goblinbob.mobends.core.client.definition;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.model.BoxFactory;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.client.model.FaceRotation;
import goblinbob.mobends.core.client.model.MutatedBox;
import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.BoxSplitter;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds the parts Mo' Bends draws for one vanilla model from an {@link EntityModelDefinition}: every
 * declared vanilla part becomes a {@link DefinedModelPart} with the same boxes and texture, cut into
 * segments where the definition asks for extra joints; overlays (sleeves, trousers) are cut where
 * the bone under them is, each piece riding its segment. This is 1.12's
 * {@code DefinedMutator.createParts}, except that the vanilla model is left as it is: its parts are
 * only read, and drawn instead of it.
 * <p>
 * It also builds the parts of a model worn over another one ({@link #buildOver}: the vanilla armour
 * over the player), cut where the parts underneath are cut, as 1.12's {@code ArmorWrapper} cut the
 * armour at the elbows and knees.
 */
final class DefinedModelBuilder {
    private final EntityModelDefinition definition;
    /** The model, whose fields a mod's model's bones may name ({@link ModelPartLookup}). */
    private final Object model;
    private final ModelPart root;
    /** The parts this model is worn over, whose cuts and frames its boxes follow; null for an entity's own model. */
    @Nullable
    private final DefinedModel over;
    /** Per bone: the vanilla part it takes over. */
    private final Map<String, ModelPart> vanillaParts = new LinkedHashMap<>();
    private final Map<String, DefinedModelPart> parts = new LinkedHashMap<>();
    /** The positions the data adopts: per bone and segment, where it sits in its parent's frame. */
    private final Map<String, float[]> positions = new LinkedHashMap<>();
    /** Per bone: its segments' origins in its frame. */
    private final Map<String, List<float[]>> originsOf = new HashMap<>();
    /** Per bone, in model units: how far its boxes move into its frame (from where the vanilla part turned). */
    private final Map<String, float[]> shifts = new HashMap<>();
    /** Per split bone: the axis it's split along. */
    private final Map<String, Integer> splitAxes = new HashMap<>();
    private final List<DefinedModelPart> roots = new ArrayList<>();

    private DefinedModelBuilder(EntityModelDefinition definition, Object model, ModelPart root, @Nullable DefinedModel over) {
        this.definition = definition;
        this.model = model;
        this.root = root;
        this.over = over;
    }

    /** The parts of the model whose part tree starts at {@code root}. */
    static DefinedModel build(EntityModelDefinition definition, Object model, ModelPart root) {
        return new DefinedModelBuilder(definition, model, root, null).build();
    }

    /**
     * The parts of a model worn over {@code over} (armour), whose part tree starts at {@code root}.
     * They are drawn posed by the same data, so they don't read positions of their own: each box
     * goes into the frame of the bone it is worn on as {@code over} has it, and a split bone's boxes
     * are cut where {@code over}'s are, at the joints of its segments, open there, as the sleeves
     * are. A bone whose vanilla part this model lacks (the sleeves, the jacket) is left empty.
     */
    static DefinedModel buildOver(EntityModelDefinition definition, Object model, ModelPart root, DefinedModel over) {
        return new DefinedModelBuilder(definition, model, root, over).build();
    }

    private DefinedModel build() {
        resolve();
        createParts();
        return new DefinedModel(roots, parts, positions, shifts, originsOf, splitAxes, over);
    }

    // --- resolving the vanilla parts ---------------------------------------------------------------

    private void resolve() {
        for (BoneDefinition bone : definition.bones) {
            if (bone.vanilla == null) {
                continue;
            }
            ModelPart part = ModelPartLookup.find(definition, bone.vanilla, root, model);
            if (part == null) {
                if (over != null) {
                    // Worn over a model with more parts (armour has no sleeves): nothing to draw there.
                    continue;
                }
                MoBends.LOGGER.warn("Model definition {}: cannot find the vanilla part of bone '{}'.", definition.location, bone.name);
                continue;
            }
            vanillaParts.put(bone.name, part);
        }
    }

    // --- building -------------------------------------------------------------------------------

    private void createParts() {
        // Per bone, in model units: where it turns, and how far its boxes move into its frame (from
        // where the vanilla part turned), and its segments.
        Map<String, float[]> pivots = new HashMap<>();
        Map<String, List<DefinedModelPart>> segmentsOf = new HashMap<>();
        for (BoneDefinition bone : definition.bones) {
            if (bone.overlay != null) {
                createOverlay(bone, pivots, segmentsOf);
                continue;
            }
            ModelPart vanilla = vanillaParts.get(bone.name);
            DefinedModelPart parent = bone.parent == null ? null : parts.get(bone.parent);
            // Where vanilla's part turns as the model is made: setupAnim moves it about each frame
            // (a crouch lowers the body), which the definition's pose replaces.
            float[] turnsAt = new float[3];
            if (vanilla != null) {
                PartPose rest = vanilla.getInitialPose();
                turnsAt = new float[] { rest.x, rest.y, rest.z };
            }
            float[] pivot;
            if (bone.position != null) {
                pivot = parent == null ? bone.position.clone() : add(pivots.get(bone.parent), bone.position);
            } else {
                pivot = bone.pivot != null ? bone.pivot.clone() : turnsAt;
            }
            float[] shift = bone.pivot != null && bone.position == null ? subtract(turnsAt, bone.pivot) : new float[3];
            if (over != null && over.shifts.get(bone.name) != null) {
                // Vanilla draws the worn model's part where the part underneath is (copyPropertiesTo),
                // so its boxes go where that part's would.
                shift = over.shifts.get(bone.name);
            }
            pivots.put(bone.name, pivot);
            shifts.put(bone.name, shift);

            DefinedModelPart part = new DefinedModelPart(bone.name, parent, vanilla);
            float[] position = parent == null ? pivot : subtract(pivot, pivots.get(bone.parent));
            part.position.set(position[0], position[1], position[2]);
            positions.put(bone.name, position);
            if (bone.restRotation != null) {
                part.restRotation = eulerDegrees(bone.restRotation);
            }
            if (parent == null) {
                roots.add(part);
            } else if (bone.drawnAlone) {
                part.drawnAlone = true;
                roots.add(part);
            } else {
                parent.children.add(part);
            }
            parts.put(bone.name, part);

            // Geometry: the vanilla boxes, split into segments where asked.
            List<DefinedModelPart> segments = new ArrayList<>();
            segments.add(part);
            if (bone.split != null) {
                splitAxes.put(bone.name, bone.split.axisIndex());
                DefinedModelPart previous = part;
                for (String name : bone.split.names) {
                    // A segment shows with its bone.
                    DefinedModelPart segment = new DefinedModelPart(name, previous, vanilla);
                    previous.children.add(segment);
                    part.segments.add(segment);
                    parts.put(name, segment);
                    segments.add(segment);
                    previous = segment;
                }
            }
            segmentsOf.put(bone.name, segments);
            List<float[]> origins = new ArrayList<>();
            origins.add(new float[3]);
            if (bone.boxes != null) {
                // Boxes the definition declares stand for the vanilla part's, on the model's texture.
                // A bone without a vanilla part (the wolf's shins, nose, ears) reads the texture's
                // size off its nearest parent's (1.12 had it from the model, which this version's
                // only keeps in its boxes). On a model worn over the entity's (the wolf's armour:
                // the same model, grown a little), they are grown as much as its vanilla boxes are.
                ModelPart grownAs = vanillaPartOfOrAbove(bone);
                float[] grow = over == null ? null : inflationOf(grownAs);
                if (over == null || grow != null) {
                    float[] textureSize = textureSizeOf(grownAs != null ? grownAs : root);
                    for (BoneDefinition.BoxDefinition box : bone.boxes) {
                        part.boxes.add(createBox(box, textureSize, grow));
                    }
                }
            } else if (vanilla != null) {
                for (ModelPart.Cube cube : cubesOf(vanilla)) {
                    BoxFactory source = sourceBox(cube, shift);
                    if (bone.split == null) {
                        inflate(source, bone, 0);
                        part.boxes.add(source.create());
                        continue;
                    }
                    List<float[]> cuts = over == null ? null : over.segmentOrigins.get(bone.name);
                    if (cuts != null && cuts.size() == segments.size()) {
                        // Cut where the bone underneath is, and open at the joints, as 1.12's armour was.
                        origins = addSplitBoxes(source, bone, bone.split, segments, cuts, false);
                    } else {
                        origins = addSplitBoxes(source, bone, bone.split, segments, null, bone.split.caps);
                    }
                }
            }
            if (vanilla != null) {
                // A child the definition declares is a bone of its own (the villager's nose). Declared
                // boxes stand for the children too: the wolf's head and tail draw theirs from a child
                // now (real_head, real_tail), which 1.12's model drew as the part's own.
                if (bone.boxes == null) {
                    // A split bone's children ride the segment they hang from (a gauntlet the
                    // forearm), as 1.12's armour had them: by where they turn, against the cuts.
                    List<float[]> cuts = over != null ? over.segmentOrigins.get(bone.name) : origins;
                    boolean byCuts = bone.split != null && cuts != null && cuts.size() == segments.size();
                    int axis = bone.split == null ? 0 : bone.split.axisIndex();
                    for (ModelPart child : childrenOf(vanilla)) {
                        if (vanillaParts.containsValue(child)) {
                            continue;
                        }
                        int k = 0;
                        if (byCuts) {
                            PartPose at = child.getInitialPose();
                            float along = (axis == 0 ? at.x : axis == 1 ? at.y : at.z) + shift[axis];
                            for (int i = 1; i < cuts.size(); i++) {
                                if (along >= cuts.get(i)[axis]) {
                                    k = i;
                                }
                            }
                        }
                        DefinedModelPart segment = segments.get(k);
                        segment.vanillaChildren.add(child);
                        if (k > 0) {
                            float[] origin = cuts.get(k);
                            segment.vanillaChildrenShift.set(shift[0] - origin[0], shift[1] - origin[1], shift[2] - origin[2]);
                        }
                    }
                }
                part.vanillaChildrenShift.set(shift[0], shift[1], shift[2]);
            }
            originsOf.put(bone.name, origins);
            if (bone.split != null && positions.get(bone.split.names.get(0)) == null) {
                // A split of a bone without boxes: the segments sit on the bone.
                for (String name : bone.split.names) {
                    positions.put(name, new float[] { 0, 0, 0 });
                }
            }
        }
    }

    /**
     * A bone laid over another: its boxes, in the frame of the bone it lies over (as vanilla turns
     * them both about that bone's point), split as that bone is, each piece riding its segment.
     */
    private void createOverlay(BoneDefinition bone, Map<String, float[]> pivots, Map<String, List<DefinedModelPart>> segmentsOf) {
        BoneDefinition under = definition.bone(bone.overlay);
        List<DefinedModelPart> underSegments = segmentsOf.get(bone.overlay);
        if (under == null || underSegments == null) {
            MoBends.LOGGER.warn("Model definition {}: bone '{}' lies over '{}', which isn't a bone declared before it.", definition.location, bone.name, bone.overlay);
            return;
        }
        ModelPart vanilla = vanillaParts.get(bone.name);
        float[] shift = shifts.get(bone.overlay);
        pivots.put(bone.name, pivots.get(bone.overlay));
        shifts.put(bone.name, shift);

        // One piece per segment underneath, each its child; only the first is posed by the data.
        List<DefinedModelPart> pieces = new ArrayList<>();
        for (DefinedModelPart segment : underSegments) {
            DefinedModelPart piece = new DefinedModelPart(pieces.isEmpty() ? bone.name : null, segment, vanilla);
            segment.children.add(piece);
            pieces.add(piece);
        }
        parts.put(bone.name, pieces.get(0));
        positions.put(bone.name, new float[3]);
        if (vanilla == null) {
            return;
        }
        for (ModelPart.Cube cube : cubesOf(vanilla)) {
            BoxFactory source = sourceBox(cube, shift);
            if (under.split == null) {
                inflate(source, bone, 0);
                pieces.get(0).boxes.add(source.create());
                continue;
            }
            // A sleeve stays open at the joint it lies over.
            addSplitBoxes(source, bone, under.split, pieces, originsOf.get(bone.overlay), false);
        }
    }

    /**
     * A box the definition declares itself (see {@link BoneDefinition.BoxDefinition}), grown by
     * {@code grow} (x, y, z, in model units) if it isn't null, as vanilla grows a worn model's.
     */
    private static MutatedBox createBox(BoneDefinition.BoxDefinition box, float[] textureSize, @Nullable float[] grow) {
        BoxFactory factory = new BoxFactory(box.from[0], box.from[1], box.from[2], box.size[0], box.size[1], box.size[2], 0)
                .withUVs(box.uv[0], box.uv[1])
                .withTextureSize(textureSize[0], textureSize[1]);
        if (box.drawnSize != null) {
            factory.resize(box.drawnSize[0], box.drawnSize[1], box.drawnSize[2]);
        }
        if (box.offset != null) {
            factory.offset(box.offset[0], box.offset[1], box.offset[2]);
        }
        if (box.inflate != null) {
            factory.inflate(box.inflate[0], box.inflate[1], box.inflate[2]);
        }
        if (grow != null) {
            factory.inflate(grow[0], grow[1], grow[2]);
        }
        if (box.hide != null) {
            for (String face : box.hide) {
                factory.hideFace(side(face));
            }
        }
        if (box.faces != null) {
            for (Map.Entry<String, BoneDefinition.FaceDefinition> face : box.faces.entrySet()) {
                if (face.getValue().uvOffset != null) {
                    factory.offsetTextureQuad(side(face.getKey()), face.getValue().uvOffset[0], face.getValue().uvOffset[1]);
                }
                if (face.getValue().flip) {
                    factory.flipTextureQuad(side(face.getKey()));
                }
                if (face.getValue().rotate != null) {
                    factory.rotateTextureQuad(side(face.getKey()), FaceRotation.valueOf(face.getValue().rotate.toUpperCase(Locale.ROOT)));
                }
            }
        }
        factory.mirrored = box.mirror;
        return factory.create();
    }

    private static BoxSide side(String face) {
        return BoxSide.valueOf(face.toUpperCase(Locale.ROOT));
    }

    /**
     * The texture size of a vanilla part, which this version only keeps in its boxes' UVs (the
     * player's skin, 64 by 64, when there is no box to read it from). A part that draws no box of
     * its own (the wolf's head) is read off the first box under it.
     */
    private static float[] textureSizeOf(@Nullable ModelPart vanilla) {
        ModelPart.Cube cube = vanilla == null ? null : firstCubeUnder(vanilla);
        if (cube != null) {
            BoxFactory read = BoxFactory.of(cube);
            return new float[] { read.textureWidth, read.textureHeight };
        }
        return new float[] { 64, 64 };
    }

    /** The vanilla part of {@code bone}, else of its nearest parent that has one; null if none has. */
    @Nullable
    private ModelPart vanillaPartOfOrAbove(BoneDefinition bone) {
        for (BoneDefinition at = bone; at != null; at = at.parent == null ? null : definition.bone(at.parent)) {
            ModelPart part = vanillaParts.get(at.name);
            if (part != null) {
                return part;
            }
        }
        return null;
    }

    /** How much vanilla grew the first box of a part (or under it), or null if it draws none. */
    @Nullable
    private static float[] inflationOf(@Nullable ModelPart vanilla) {
        ModelPart.Cube cube = vanilla == null ? null : firstCubeUnder(vanilla);
        return cube == null ? null : BoxFactory.of(cube).inflation.clone();
    }

    /** The part's first box, else the first of its children's, through {@code ModelPart.visit}; null if it draws none. */
    @Nullable
    private static ModelPart.Cube firstCubeUnder(ModelPart part) {
        List<ModelPart.Cube> cubes = new ArrayList<>();
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (cubes.isEmpty()) {
                cubes.add(cube);
            }
        });
        return cubes.isEmpty() ? null : cubes.get(0);
    }

    /** A vanilla box, inflated as vanilla has it, moved {@code shift} into the bone's frame. */
    private static BoxFactory sourceBox(ModelPart.Cube cube, float[] shift) {
        BoxFactory source = BoxFactory.of(cube);
        source.offset(shift[0], shift[1], shift[2]);
        return source;
    }

    private static void inflate(BoxFactory box, BoneDefinition bone, int segment) {
        if (bone.inflate != null && segment < bone.inflate.length) {
            float[] by = bone.inflate[segment];
            box.inflate(by[0], by[1], by[2]);
        }
    }

    /** The part's own boxes (not its children's), read through {@code ModelPart.visit}, which hands each over. */
    private static List<ModelPart.Cube> cubesOf(ModelPart part) {
        List<ModelPart.Cube> cubes = new ArrayList<>();
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            // The part's own boxes come with the path "", its children's with "/<name>...".
            if (path.isEmpty()) {
                cubes.add(cube);
            }
        });
        return cubes;
    }

    /**
     * The part's direct children that draw anything, found through the paths {@code ModelPart.visit}
     * gives their boxes ({@code /<child>/<grandchild>...}): vanilla keeps them private.
     */
    private static List<ModelPart> childrenOf(ModelPart part) {
        Set<String> names = new LinkedHashSet<>();
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (!path.isEmpty()) {
                int end = path.indexOf('/', 1);
                names.add(end < 0 ? path.substring(1) : path.substring(1, end));
            }
        });
        List<ModelPart> children = new ArrayList<>();
        for (String name : names) {
            children.add(part.getChild(name));
        }
        return children;
    }

    private static float[] add(float[] a, float[] b) {
        return new float[] { a[0] + b[0], a[1] + b[1], a[2] + b[2] };
    }

    private static float[] subtract(float[] a, float[] b) {
        return new float[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
    }

    /**
     * Cuts {@code source} as {@code split} says into {@code segments}, one piece each. With
     * {@code origins} (an overlay's), the box is cut where the segments of the bone it lies over
     * start, and each piece goes into the frame of the segment it lies over: a box that ends before
     * a cut (or starts after it), but for its inflation, isn't cut there. Else the box is cut at the
     * split's fractions, and the segments are placed at the cuts.
     *
     * @return the segments' origins in the bone's frame
     */
    private List<float[]> addSplitBoxes(BoxFactory source, BoneDefinition bone, BoneDefinition.SplitDefinition split,
                                        List<DefinedModelPart> segments, @Nullable List<float[]> origins, boolean caps) {
        float[] min = { source.min.x, source.min.y, source.min.z };
        float[] max = { source.max.x, source.max.y, source.max.z };
        float[][] faces = new float[6][4];
        for (int f = 0; f < 6; f++) {
            BoxFactory.TextureFace face = source.faces[f];
            faces[f] = new float[] { face.uPos, face.vPos, face.uSize, face.vSize };
        }
        int axis = split.axisIndex();
        // How far vanilla grew the box past its texture along the cut axis.
        float inflation = source.inflation[axis];
        float[] fractions = split.at;
        // Per piece, the segment it goes into.
        int[] segmentOf = new int[segments.size()];
        for (int k = 0; k < segmentOf.length; k++) {
            segmentOf[k] = k;
        }
        if (origins != null && origins.size() == segments.size()) {
            float length = max[axis] - min[axis];
            List<Float> kept = new ArrayList<>();
            int count = 1;
            segmentOf[0] = 0;
            for (int k = 1; k < segments.size(); k++) {
                float cut = origins.get(k)[axis];
                if (cut <= min[axis] + inflation) {
                    segmentOf[0] = k;
                } else if (cut < max[axis] - inflation) {
                    kept.add((cut - min[axis]) / length);
                    segmentOf[count++] = k;
                }
            }
            fractions = new float[kept.size()];
            for (int i = 0; i < fractions.length; i++) {
                fractions[i] = kept.get(i);
            }
        }
        List<BoxSplitter.Segment> pieces = BoxSplitter.split(min, max, faces, axis, fractions, split.hingeAxis(), split.hingeSide(), caps);
        List<float[]> own = new ArrayList<>();
        float[] origin = new float[3];
        for (int k = 0; k < pieces.size(); k++) {
            BoxSplitter.Segment piece = pieces.get(k);
            origin = add(origin, piece.pivot);
            own.add(origin);
            // Where the piece's frame is in the frame of the segment it goes into.
            float[] into = origins == null ? new float[3] : subtract(origin, origins.get(segmentOf[k]));
            DefinedModelPart target = segments.get(segmentOf[k]);
            BoxFactory.TextureFace[] pieceFaces = new BoxFactory.TextureFace[6];
            for (int f = 0; f < 6; f++) {
                // Whole texture pixels, as 1.12 cut them.
                pieceFaces[f] = new BoxFactory.TextureFace(Math.round(piece.faces[f][0]), Math.round(piece.faces[f][1]),
                        Math.round(piece.faces[f][2]), Math.round(piece.faces[f][3]));
                pieceFaces[f].faceRotation = source.faces[f].faceRotation;
                pieceFaces[f].flipped = source.faces[f].flipped;
            }
            BoxFactory factory = new BoxFactory(new Vec3f(piece.min[0] + into[0], piece.min[1] + into[1], piece.min[2] + into[2]),
                    new Vec3f(piece.max[0] + into[0], piece.max[1] + into[1], piece.max[2] + into[2]), pieceFaces);
            // A face vanilla left out of the box stays out of every piece.
            factory.faceVisibilityFlag = (byte) (piece.visibility & source.faceVisibilityFlag);
            factory.mirrored = source.mirrored;
            factory.withTextureSize(source.textureWidth, source.textureHeight);
            inflate(factory, bone, segmentOf[k]);
            target.boxes.add(factory.create());
            if (k > 0 && origins == null) {
                String name = split.names.get(k - 1);
                if (positions.get(name) == null) {
                    target.position.set(piece.pivot[0], piece.pivot[1], piece.pivot[2]);
                    positions.put(name, new float[] { piece.pivot[0], piece.pivot[1], piece.pivot[2] });
                }
            }
        }
        return origins != null ? origins : own;
    }

    /** X, then Y, then Z, each in the frame the previous left (as 1.12's {@code DefinedMutator.eulerDegrees}). */
    private static Quaternion eulerDegrees(float[] xyz) {
        Quaternion q = new Quaternion();
        q.setIdentity();
        Quaternion axis = new Quaternion();
        float[][] axes = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
        for (int i = 2; i >= 0; i--) {
            if (xyz[i] == 0) continue;
            axis.setFromAxisAngle(axes[i][0], axes[i][1], axes[i][2], (float) Math.toRadians(xyz[i]));
            Quaternion.mul(q, axis, q);
        }
        return q;
    }
}

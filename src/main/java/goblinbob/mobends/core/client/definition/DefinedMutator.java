package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.model.BoxFactory;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.BoxSplitter;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.DefinedFields;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.mutators.Mutator;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Mutates any vanilla model from an {@link EntityModelDefinition}: every declared vanilla part is
 * replaced (in whichever field or array slot holds it, found by identity, so obfuscation does not
 * matter) by a bends part carrying the same boxes and texture, cut into segments where the
 * definition asks for extra joints.
 */
public class DefinedMutator<E extends EntityLivingBase> extends Mutator<DefinedEntityData<E>, E, ModelBase>
{

    private final EntityModelDefinition definition;
    private final Map<String, ModelPart> parts = new LinkedHashMap<>();
    private final Map<String, float[]> positions = new LinkedHashMap<>();
    /** Per bone: the vanilla part, and every place in the model that referenced it. */
    private final Map<String, ModelRenderer> vanillaParts = new LinkedHashMap<>();
    private final List<Slot> slots = new ArrayList<>();
    private boolean resolved;

    private static class Slot
    {
        final Field field;
        final Object holder;
        final int index;
        final ModelRenderer vanilla;

        Slot(Field field, Object holder, int index, ModelRenderer vanilla)
        {
            this.field = field;
            this.holder = holder;
            this.index = index;
            this.vanilla = vanilla;
        }

        void set(Object value)
        {
            try
            {
                if (index < 0) field.set(holder, value);
                else Array.set(field.get(holder), index, value);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    public DefinedMutator(EntityModelDefinition definition)
    {
        this.definition = definition;
    }

    // --- resolving the vanilla parts ---------------------------------------------------------------

    private void resolve(ModelBase model)
    {
        if (resolved)
        {
            return;
        }
        resolved = true;
        for (BoneDefinition bone : definition.bones)
        {
            if (bone.vanilla == null)
            {
                continue;
            }
            ModelRenderer part = findVanillaPart(model, bone.vanilla);
            if (part == null)
            {
                Core.LOG.log(Level.WARNING, "Model definition for " + definition.entity + ": cannot find the vanilla part of bone '" + bone.name + "'");
                continue;
            }
            vanillaParts.put(bone.name, part);
            for (Slot slot : slotsHolding(model, part))
            {
                slots.add(slot);
            }
        }
    }

    private static ModelRenderer findVanillaPart(ModelBase model, BoneDefinition.VanillaPart ref)
    {
        Function<Object, Object> field = ref.field == null ? null : DefinedFields.part(model.getClass(), ref.field);
        Object value = field == null ? null : field.apply(model);
        if (value instanceof ModelRenderer) return (ModelRenderer) value;
        if (value != null && value.getClass().isArray() && ref.element >= 0 && ref.element < Array.getLength(value))
        {
            Object element = Array.get(value, ref.element);
            if (element instanceof ModelRenderer) return (ModelRenderer) element;
        }
        if (ref.index >= 0 && ref.index < model.boxList.size())
        {
            return model.boxList.get(ref.index);
        }
        return null;
    }

    private static List<Slot> slotsHolding(ModelBase model, ModelRenderer part)
    {
        List<Slot> found = new ArrayList<>();
        for (Class<?> c = model.getClass(); c != null && c != Object.class; c = c.getSuperclass())
        {
            for (Field field : c.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()))
                {
                    continue;
                }
                try
                {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value == part)
                    {
                        found.add(new Slot(field, model, -1, part));
                    }
                    else if (value != null && value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive())
                    {
                        int length = Array.getLength(value);
                        for (int i = 0; i < length; i++)
                        {
                            if (Array.get(value, i) == part)
                            {
                                found.add(new Slot(field, model, i, part));
                            }
                        }
                    }
                }
                catch (IllegalAccessException | SecurityException ignored)
                {
                    // A field that can't be read can't hold the part for us either.
                }
            }
        }
        return found;
    }

    // --- the mutator contract -----------------------------------------------------------------

    @Override
    public void swapLayer(RenderLivingBase<? extends E> renderer, int index)
    {
    }

    @Override
    public boolean createParts(ModelBase model)
    {
        resolve(model);
        parts.clear();
        positions.clear();
        // Per bone, in model units: where it turns, and how far its boxes move into its frame
        // (from where the vanilla part turned), and its segments' origins in its own frame.
        Map<String, float[]> pivots = new HashMap<>();
        Map<String, float[]> shifts = new HashMap<>();
        Map<String, List<DefinedModelPart>> segmentsOf = new HashMap<>();
        Map<String, List<float[]>> originsOf = new HashMap<>();
        for (BoneDefinition bone : definition.bones)
        {
            if (bone.overlay != null)
            {
                createOverlay(model, bone, pivots, shifts, segmentsOf, originsOf);
                continue;
            }
            ModelRenderer vanilla = vanillaParts.get(bone.name);
            DefinedModelPart parent = bone.parent == null ? null : (DefinedModelPart) parts.get(bone.parent);
            float[] turnsAt = vanilla != null ? new float[] { vanilla.rotationPointX, vanilla.rotationPointY, vanilla.rotationPointZ } : new float[3];
            float[] pivot;
            if (bone.position != null)
            {
                pivot = parent == null ? bone.position.clone() : add(pivots.get(bone.parent), bone.position);
            }
            else
            {
                pivot = bone.pivot != null ? bone.pivot.clone() : turnsAt;
            }
            float[] shift = bone.pivot != null && bone.position == null ? subtract(turnsAt, bone.pivot) : new float[3];
            pivots.put(bone.name, pivot);
            shifts.put(bone.name, shift);

            DefinedModelPart part = new DefinedModelPart(model);
            copyLook(vanilla, part, true);
            float[] position = parent == null ? pivot : subtract(pivot, pivots.get(bone.parent));
            part.setPosition(position[0], position[1], position[2]);
            part.setPostShift(shift[0], shift[1], shift[2]);
            positions.put(bone.name, position);
            if (bone.restRotation != null)
            {
                part.setRestRotation(eulerDegrees(bone.restRotation));
            }
            if (parent != null)
            {
                part.setParent(parent);
                parent.addChild(part);
            }
            parts.put(bone.name, part);

            // Geometry: the vanilla boxes, split into segments where asked.
            List<DefinedModelPart> segments = new ArrayList<>();
            segments.add(part);
            if (bone.split != null)
            {
                DefinedModelPart previous = part;
                for (String name : bone.split.names)
                {
                    DefinedModelPart segment = new DefinedModelPart(model);
                    copyLook(vanilla, segment, false);
                    segment.setParent(previous);
                    previous.addChild(segment);
                    part.addSegment(segment);
                    parts.put(name, segment);
                    segments.add(segment);
                    previous = segment;
                }
            }
            segmentsOf.put(bone.name, segments);
            List<float[]> origins = new ArrayList<>();
            origins.add(new float[3]);
            if (vanilla != null)
            {
                for (ModelBox box : vanilla.cubeList)
                {
                    BoxFactory source = sourceBox(vanilla, box, shift);
                    if (bone.split == null)
                    {
                        inflate(source, bone, 0);
                        part.addBox(source.create(part));
                        continue;
                    }
                    origins = addSplitBoxes(source, bone, bone.split, segments, null, null);
                }
                if (vanilla.childModels != null)
                {
                    for (ModelRenderer child : vanilla.childModels)
                    {
                        part.addChild(child);
                    }
                }
            }
            originsOf.put(bone.name, origins);
            if (bone.split != null && positions.get(bone.split.names.get(0)) == null)
            {
                // A split of a bone without boxes: the segments sit on the bone.
                for (String name : bone.split.names)
                {
                    positions.put(name, new float[] { 0, 0, 0 });
                }
            }
        }

        // Hand the parts to the vanilla model. A bone rendered inside another leaves a stand-in where
        // the vanilla renderer looks, so it is not drawn twice, but is still reached through it.
        for (Slot slot : slots)
        {
            String bone = boneOf(slot.vanilla);
            DefinedModelPart part = bone == null ? null : (DefinedModelPart) parts.get(bone);
            if (part == null)
            {
                continue;
            }
            BoneDefinition definitionOf = definition.bone(bone);
            if (definitionOf != null && (definitionOf.parent != null || definitionOf.overlay != null))
            {
                slot.set(new DefinedStandIn(model, part));
            }
            else
            {
                slot.set(part);
            }
        }
        return true;
    }

    /**
     * A bone laid over another: its boxes, in the frame of the bone it lies over (as vanilla turns
     * them both about that bone's point), split as that bone is, each piece riding its segment.
     */
    private void createOverlay(ModelBase model, BoneDefinition bone, Map<String, float[]> pivots, Map<String, float[]> shifts,
                               Map<String, List<DefinedModelPart>> segmentsOf, Map<String, List<float[]>> originsOf)
    {
        BoneDefinition under = definition.bone(bone.overlay);
        ModelRenderer vanilla = vanillaParts.get(bone.name);
        List<DefinedModelPart> underSegments = segmentsOf.get(bone.overlay);
        float[] shift = shifts.get(bone.overlay);
        pivots.put(bone.name, pivots.get(bone.overlay));
        shifts.put(bone.name, shift);

        List<DefinedModelPart> pieces = new ArrayList<>();
        for (DefinedModelPart segment : underSegments)
        {
            DefinedModelPart piece = new DefinedModelPart(model);
            copyLook(vanilla, piece, pieces.isEmpty());
            piece.setParent(segment);
            segment.addChild(piece);
            if (!pieces.isEmpty())
            {
                pieces.get(0).addSegment(piece);
            }
            pieces.add(piece);
        }
        DefinedModelPart first = pieces.get(0);
        first.setPostShift(shift[0], shift[1], shift[2]);
        parts.put(bone.name, first);
        positions.put(bone.name, new float[3]);
        if (vanilla == null)
        {
            return;
        }
        for (ModelBox box : vanilla.cubeList)
        {
            BoxFactory source = sourceBox(vanilla, box, shift);
            if (under.split == null)
            {
                inflate(source, bone, 0);
                first.addBox(source.create(first));
                continue;
            }
            addSplitBoxes(source, bone, under.split, pieces, originsOf.get(bone.overlay), first);
        }
    }

    /** A vanilla box, inflated as vanilla has it, moved {@code shift} into the bone's frame. */
    private static BoxFactory sourceBox(ModelRenderer vanilla, ModelBox box, float[] shift)
    {
        BoxFactory source = new BoxFactory(vanilla, box);
        // Keep the vanilla inflation (a villager's robe is its body box, scaled up by half a unit).
        float inflation = BoxFactory.inflationOf(box);
        source.inflate(inflation, inflation, inflation);
        source.min.add(shift[0], shift[1], shift[2]);
        source.max.add(shift[0], shift[1], shift[2]);
        return source;
    }

    private static void inflate(BoxFactory box, BoneDefinition bone, int segment)
    {
        if (bone.inflate != null && segment < bone.inflate.length)
        {
            float[] by = bone.inflate[segment];
            box.inflate(by[0], by[1], by[2]);
        }
    }

    private static void copyLook(ModelRenderer vanilla, DefinedModelPart part, boolean withOffset)
    {
        if (vanilla == null)
        {
            return;
        }
        part.mirror = vanilla.mirror;
        part.textureWidth = vanilla.textureWidth;
        part.textureHeight = vanilla.textureHeight;
        if (withOffset)
        {
            part.offsetX = vanilla.offsetX;
            part.offsetY = vanilla.offsetY;
            part.offsetZ = vanilla.offsetZ;
        }
    }

    private static float[] add(float[] a, float[] b)
    {
        return new float[] { a[0] + b[0], a[1] + b[1], a[2] + b[2] };
    }

    private static float[] subtract(float[] a, float[] b)
    {
        return new float[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
    }

    /**
     * Cuts {@code source} as {@code split} says into {@code segments}, one piece each. With
     * {@code origins} (an overlay's), the pieces go into the frames of the segments of the bone it
     * lies over, whose origins those are; else the segments are placed at the cuts.
     *
     * @return the segments' origins in the bone's frame
     */
    private List<float[]> addSplitBoxes(BoxFactory source, BoneDefinition bone, BoneDefinition.SplitDefinition split, List<DefinedModelPart> segments,
                                        List<float[]> origins, DefinedModelPart overlay)
    {
        float[] min = { source.min.x, source.min.y, source.min.z };
        float[] max = { source.max.x, source.max.y, source.max.z };
        float[][] faces = new float[6][4];
        for (int f = 0; f < 6; f++)
        {
            BoxFactory.TextureFace face = source.faces[f];
            faces[f] = new float[] { face.uPos, face.vPos, face.uSize, face.vSize };
        }
        List<BoxSplitter.Segment> pieces = BoxSplitter.split(min, max, faces, split.axisIndex(), split.at, split.hingeAxis(), split.hingeSide(),
                // A sleeve stays open at the joint it lies over.
                split.caps && origins == null);
        List<float[]> own = new ArrayList<>();
        float[] origin = new float[3];
        for (int k = 0; k < pieces.size(); k++)
        {
            BoxSplitter.Segment piece = pieces.get(k);
            origin = add(origin, piece.pivot);
            own.add(origin);
            // Where the piece's frame is in the frame of the segment it goes into.
            float[] into = origins == null ? new float[3] : subtract(origin, origins.get(k));
            DefinedModelPart target = segments.get(k);
            BoxFactory.TextureFace[] pieceFaces = new BoxFactory.TextureFace[6];
            for (int f = 0; f < 6; f++)
            {
                BoxFactory.TextureFace face = new BoxFactory.TextureFace(source.faces[f]);
                face.uPos = Math.round(piece.faces[f][0]);
                face.vPos = Math.round(piece.faces[f][1]);
                face.uSize = Math.round(piece.faces[f][2]);
                face.vSize = Math.round(piece.faces[f][3]);
                pieceFaces[f] = face;
            }
            BoxFactory factory = new BoxFactory(new Vec3f(piece.min[0] + into[0], piece.min[1] + into[1], piece.min[2] + into[2]),
                    new Vec3f(piece.max[0] + into[0], piece.max[1] + into[1], piece.max[2] + into[2]), pieceFaces);
            factory.faceVisibilityFlag = (byte) piece.visibility;
            factory.mirrored = source.mirrored;
            inflate(factory, bone, k);
            target.addBox(factory.create(target));
            if (k > 0 && origins == null)
            {
                String name = split.names.get(k - 1);
                if (positions.get(name) == null)
                {
                    target.setPosition(piece.pivot[0], piece.pivot[1], piece.pivot[2]);
                    positions.put(name, new float[] { piece.pivot[0], piece.pivot[1], piece.pivot[2] });
                }
            }
        }
        return own;
    }

    private String boneOf(ModelRenderer vanilla)
    {
        for (Map.Entry<String, ModelRenderer> entry : vanillaParts.entrySet())
        {
            if (entry.getValue() == vanilla) return entry.getKey();
        }
        return null;
    }

    private static Quaternion eulerDegrees(float[] xyz)
    {
        Quaternion q = new Quaternion();
        q.setIdentity();
        Quaternion axis = new Quaternion();
        // X, then Y, then Z, each in the frame the previous left (the vanilla rotateAngle order is Z, Y, X outermost-first)
        float[][] axes = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
        for (int i = 2; i >= 0; i--)
        {
            if (xyz[i] == 0) continue;
            axis.setFromAxisAngle(axes[i][0], axes[i][1], axes[i][2], (float) Math.toRadians(xyz[i]));
            Quaternion.mul(q, axis, q);
        }
        return q;
    }

    @Override
    public void syncUpWithData(DefinedEntityData<E> data)
    {
        if (!data.hasAdoptedPositions())
        {
            data.adoptPositions(positions);
        }
        for (Map.Entry<String, ModelPart> entry : parts.entrySet())
        {
            entry.getValue().syncUp(data.getPart(entry.getKey()));
        }
    }

    @Override
    public boolean shouldModelBeSkipped(ModelBase model)
    {
        if (definition.model == null)
        {
            return false;
        }
        for (Class<?> c = model.getClass(); c != null; c = c.getSuperclass())
        {
            if (c.getName().equals(definition.model)) return false;
        }
        return true;
    }

}

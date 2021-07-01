package goblinbob.mobends.forge;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.exceptions.MissingPartException;
import goblinbob.mobends.core.exceptions.UnmappedPartException;
import goblinbob.mobends.core.mutation.*;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Mutator
{
    private final EntityBender<ForgeMutationContext, BenderResources> bender;
    private final MutationMetadata metadata;
    private final EntityModel<?> model;

    private final VanillaContainer vanillaContainer = new VanillaContainer();
    private final Map<String, IForgeModelPart> partMap = new HashMap<>();

    public Mutator(EntityBender<ForgeMutationContext, BenderResources> bender, EntityModel<?> model)
    {
        this.bender = bender;
        this.metadata = bender.getBenderResources().getMutationMetadata();
        this.model = model;
    }

    public Iterable<Map.Entry<String, IForgeModelPart>> getParts()
    {
        return partMap.entrySet();
    }

    public void demutate() throws InvalidMutationException
    {
        MutatedModels.markVanilla(model);
        for (VanillaContainer.Entry entry : vanillaContainer.getEntries())
        {
            try
            {
                entry.field.set(model, entry.originalPart);
            }
            catch (IllegalAccessException e)
            {
                throw new InvalidMutationException(String.format("Illegal access to '%s' during demutation.", entry.field.getName()), bender);
            }
        }
        vanillaContainer.clear();
        partMap.clear();
    }

    public void mutate()
    {
        if (MutatedModels.isMutated(model))
        {
            // This model has already been mutated.
            return;
        }

        if (model == null)
        {
            throw new InvalidMutationException("Couldn't mutate model because it's null", bender);
        }

        // This is going to be unmarked in case something wrong happens during the mutation by the 'demutate' method.
        MutatedModels.markMutated(model);

        try
        {
            this.createParts();
        }
        catch(InvalidMutationException ex)
        {
            // Something went wrong during the mutation, rollback.
            this.demutate();
            throw ex;
        }
    }

    private void createParts()
    {
        MutationInstructions instructions = bender.getBenderResources().getMutationInstructions();

        for (Map.Entry<String, PartMutationInstructions> entry : instructions.getPartMutations())
        {
            createMutationPart(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, PartMutationInstructions> entry : instructions.getAddedParts())
        {
            createAdditionalPart(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, PartMutationInstructions> entry : instructions.getPartMutations())
        {
            resolveParents(entry.getKey(), entry.getValue(), true);
        }

        for (Map.Entry<String, PartMutationInstructions> entry : instructions.getAddedParts())
        {
            resolveParents(entry.getKey(), entry.getValue(), false);
        }
    }

    private void createMutationPart(String partName, PartMutationInstructions partInstructions) throws InvalidMutationException
    {
        Class<?> modelClass = model.getClass();

        // Fetching the field names
        String fieldName;

        try
        {
            fieldName = metadata.getFieldName(partName);
        }
        catch (UnmappedPartException e)
        {
            throw new InvalidMutationException(String.format("No field name mappings for part name: '%s'", partName), bender);
        }

        // Fetching the model renderer field
        Field field = fetchField(bender, modelClass, partName, fieldName);
        field.setAccessible(true);

        try
        {
            ModelRenderer originalRenderer = (ModelRenderer) field.get(model);
            ModelPart modelPart = createPart(partName, partInstructions, originalRenderer.xTexOffs, originalRenderer.yTexOffs);

            field.set(model, modelPart);
            vanillaContainer.store(field, originalRenderer);
        }
        catch (ClassCastException e)
        {
            throw new InvalidMutationException(String.format("Field '%s' expected to be a ModelRenderer, found %s", field.getName(), field.getDeclaringClass().getName()), bender);
        }
        catch (IllegalAccessException e)
        {
            throw new InvalidMutationException(String.format("Illegal access to '%s'", field.getName()), bender);
        }
    }

    private void createAdditionalPart(String partName, PartMutationInstructions partInstructions)
    {
        int[] textureOffset = partInstructions.getTextureOffset();

        if (textureOffset != null)
        {
            if (textureOffset.length != 2)
            {
                throw new InvalidMutationException(String.format("Invalid textureOffset for '%s'", partName), bender);
            }

            createPart(partName, partInstructions, textureOffset[0], textureOffset[1]);
        }
        else
        {
            createVirtualPart(partName, partInstructions);
        }
    }

    /**
     * Creates a "virtual" part, which doesn't have boxes or a visual representation associated with it.
     * Examples of parts like this could be root bones.
     * @param partName The name of the part
     * @param instructions How the part should be constructed
     */
    private void createVirtualPart(String partName, PartMutationInstructions instructions)
    {
        ForgeModelPartTransform modelPart = new ForgeModelPartTransform();

        float[] positionValues = instructions.getPosition();
        modelPart.position.set(positionValues[0], positionValues[1], positionValues[2]);

        partMap.put(partName, modelPart);
    }

    private ModelPart createPart(String partName, PartMutationInstructions partInstructions, int textureOffsetX, int textureOffsetY)
    {
        ModelPart modelPart = new ModelPart(model, textureOffsetX, textureOffsetY);

        float[] positionValues = partInstructions.getPosition();
        modelPart.position.set(positionValues[0], positionValues[1], positionValues[2]);

        for (BoxDefinition boxDefinition : partInstructions.getBoxes())
        {
            float[] position = boxDefinition.getPosition();
            int[] dimensions = boxDefinition.getDimensions();
            BoxFactory factory = modelPart.developBox(position[0], position[1], position[2], dimensions[0], dimensions[1], dimensions[2], boxDefinition.getScaleFactor());

            int[] boxTextureOffset = boxDefinition.getTextureOffset();
            if (boxTextureOffset != null)
            {
                if (boxTextureOffset.length != 2)
                {
                    throw new InvalidMutationException(String.format("One of the '%s' box's textureOffset is not of length 2", partName), bender);
                }

                factory.withUVs(boxTextureOffset[0], boxTextureOffset[1]);
            }

            for (int i = 0; i < 6; ++i)
            {
                BoxSide side = BoxSide.values()[i];
                FaceDefinition face = boxDefinition.getFaceMutation(side);

                if (face != null)
                {
                    face.applyTo(factory, side);
                }
            }

            factory.create();
        }

        partMap.put(partName, modelPart);

        return modelPart;
    }

    /**
     *
     * @param partName The name of the part to resolve parents of
     * @param partInstructions The instructions of the part to resolve parents of
     * @param independent True if the part is rendered by the original renderer independently, and not as a child.
     * @throws InvalidMutationException
     */
    private void resolveParents(String partName, PartMutationInstructions partInstructions, boolean independent) throws InvalidMutationException
    {
        String parentName = partInstructions.getParent();

        if (parentName == null)
        {
            return;
        }

        IForgeModelPart part = getModelPart(partName);
        IForgeModelPart parent = getModelPart(parentName);

        if (independent)
        {
            part.setParent(parent);
        }
        else
        {
            try
            {
                parent.addChild((ModelPart) part);
            }
            catch(ClassCastException ex)
            {
                throw new InvalidMutationException(String.format("Cannot add a dependent virtual part '%s' as child of '%s'", partName, parentName), this.bender);
            }
            catch(IllegalStateException ex)
            {
                throw new InvalidMutationException(ex.getMessage() + String.format(" (Parent=%s, Child=%s)", parentName, partName), this.bender);
            }
        }
    }

    private IForgeModelPart getModelPart(String partName)
    {
        IForgeModelPart part = this.partMap.get(partName);
        if (part == null)
        {
            throw new InvalidMutationException(String.format("Couldn't find part '%s'", partName), bender);
        }
        return part;
    }

    private static Field fetchField(EntityBender<ForgeMutationContext, BenderResources> bender, Class<?> modelClass, String partName, String fieldName) throws MissingPartException
    {
        // Trying to fetch the field.
        try
        {
            return modelClass.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            throw new MissingPartException(bender, partName, fieldName);
        }
    }
}

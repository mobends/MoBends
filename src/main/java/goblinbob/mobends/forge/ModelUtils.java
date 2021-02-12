package goblinbob.mobends.forge;

import goblinbob.mobends.core.mutation.FaceRotation;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ModelRenderer.TexturedQuad;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collection;

public class ModelUtils
{

    public static AxisAlignedBB getBounds(ModelRenderer modelRenderer)
    {
        return getBounds(modelRenderer, new Vector3f(0, 0, 0), new AxisAlignedBB(0, 0, 0, 0, 0, 0));
    }

    public static AxisAlignedBB getBounds(ModelRenderer modelRenderer, Vector3f position, AxisAlignedBB oldBounds)
    {
        double minX = oldBounds.minX;
        double minY = oldBounds.minY;
        double minZ = oldBounds.minZ;
        double maxX = oldBounds.maxX;
        double maxY = oldBounds.maxY;
        double maxZ = oldBounds.maxZ;

        float x = modelRenderer.rotationPointX + position.getX();
        float y = modelRenderer.rotationPointY + position.getY();
        float z = modelRenderer.rotationPointZ + position.getZ();

        if (modelRenderer.cubeList != null)
        {
            for (ModelRenderer.ModelBox box : modelRenderer.cubeList)
            {
                if (x + box.posX1 < minX)
                    minX = x + box.posX1;
                if (y + box.posY1 < minY)
                    minY = y + box.posY1;
                if (z + box.posZ1 < minZ)
                    minZ = z + box.posZ1;
                if (x + box.posX2 > maxX)
                    maxX = x + box.posX2;
                if (y + box.posY2 > maxY)
                    maxY = y + box.posY2;
                if (z + box.posZ2 > maxZ)
                    maxZ = z + box.posZ2;
            }
        }

        AxisAlignedBB newBounds = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        if (modelRenderer.childModels != null)
        {
            for (ModelRenderer child : modelRenderer.childModels)
            {
                newBounds = getBounds(child, new Vector3f(x, y, z), newBounds);
            }
        }

        return newBounds;
    }

    /**
     * This method iterates through all parts looking for the parent of the specified part. It then runs itself with
     * that parent as the next part, and it does that until the part is not null.
     */
    public static ModelRenderer getRootParent(final ModelRenderer partIn, final Collection<ModelRenderer> partsIn)
    {
        for (ModelRenderer possibleParent : partsIn)
        {
            if (possibleParent != null && possibleParent.childModels != null && possibleParent.childModels.contains(partIn))
            {
                ModelRenderer nextParent = getRootParent(possibleParent, partsIn);
                if (nextParent != null)
                    return nextParent;
                else
                    return possibleParent;
            }
        }
        return null;
    }

    public static Collection<ModelRenderer> getParentsList(ModelRenderer partIn, Collection<ModelRenderer> possibleParents, Collection<ModelRenderer> parentsList)
    {
        for (ModelRenderer possibleParent : possibleParents)
        {
            if (possibleParent != null && possibleParent.childModels != null && possibleParent.childModels.contains(partIn))
            {
                parentsList.add(possibleParent);
                getParentsList(possibleParent, possibleParents, parentsList);
            }
        }

        return parentsList;
    }

    public static Collection<ModelRenderer> getParentsList(ModelRenderer partIn, Collection<ModelRenderer> possibleParents)
    {
        return getParentsList(partIn, possibleParents, new ArrayList<>());
    }

    public static Vector3f getGlobalOrigin(ModelRenderer partIn, Collection<ModelRenderer> possibleParents)
    {
        Vector3f origin = new Vector3f(partIn.rotationPointX, partIn.rotationPointY, partIn.rotationPointZ);

        Collection<ModelRenderer> parentsList = getParentsList(partIn, possibleParents);
        for (ModelRenderer parent : parentsList)
        {
            origin.add(parent.rotationPointX, parent.rotationPointY, parent.rotationPointZ);
        }

        return origin;
    }

    public static TexturedQuad createQuad(ModelRenderer.PositionTextureVertex[] positions, BoxFactory.TextureFace face, float textureWidth, float textureHeight, boolean flipped, Direction faceDirection)
    {
        int uSize = face.uSize;
        int vSize = face.vSize;

        if (face.faceRotation == FaceRotation.CLOCKWISE || face.faceRotation == FaceRotation.COUNTER_CLOCKWISE)
        {
            uSize = face.vSize;
            vSize = face.uSize;
        }

        TexturedQuad quad = new TexturedQuad(
                positions,
                face.uPos, face.vPos, face.uPos + uSize, face.vPos + vSize, textureWidth, textureHeight,
                flipped,
                faceDirection
        );

        applyFaceRotation(quad, face.faceRotation);

        return quad;
    }

    public static void applyFaceRotation(TexturedQuad quad, FaceRotation rotation)
    {
        if (rotation == FaceRotation.IDENTITY)
            return;

        float[] uCoords = new float[] {
                quad.vertexPositions[0].textureU,
                quad.vertexPositions[1].textureU,
                quad.vertexPositions[2].textureU,
                quad.vertexPositions[3].textureU,
        };

        float[] vCoords = new float[] {
                quad.vertexPositions[0].textureV,
                quad.vertexPositions[1].textureV,
                quad.vertexPositions[2].textureV,
                quad.vertexPositions[3].textureV,
        };

        int offset = 2;
        if (rotation == FaceRotation.CLOCKWISE)
            offset = 3;
        else if (rotation == FaceRotation.COUNTER_CLOCKWISE)
            offset = 1;

        for (int i = 0; i < 4; ++i)
        {
            quad.vertexPositions[i].textureU = uCoords[(i + offset) % 4];
            quad.vertexPositions[i].textureV = vCoords[(i + offset) % 4];
        }
    }

}

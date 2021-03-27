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

        float x = modelRenderer.x + position.x();
        float y = modelRenderer.y + position.y();
        float z = modelRenderer.z + position.z();

        if (modelRenderer.cubes != null)
        {
            for (ModelRenderer.ModelBox box : modelRenderer.cubes)
            {
                if (x + box.minX < minX)
                    minX = x + box.minX;
                if (y + box.minY < minY)
                    minY = y + box.minY;
                if (z + box.minZ < minZ)
                    minZ = z + box.minZ;
                if (x + box.maxX > maxX)
                    maxX = x + box.maxX;
                if (y + box.maxY > maxY)
                    maxY = y + box.maxY;
                if (z + box.maxZ > maxZ)
                    maxZ = z + box.maxZ;
            }
        }

        AxisAlignedBB newBounds = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        if (modelRenderer.children != null)
        {
            for (ModelRenderer child : modelRenderer.children)
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
            if (possibleParent != null && possibleParent.children != null && possibleParent.children.contains(partIn))
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
            if (possibleParent != null && possibleParent.children != null && possibleParent.children.contains(partIn))
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
        Vector3f origin = new Vector3f(partIn.x, partIn.y, partIn.z);

        Collection<ModelRenderer> parentsList = getParentsList(partIn, possibleParents);
        for (ModelRenderer parent : parentsList)
        {
            origin.add(parent.x, parent.y, parent.z);
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
                quad.vertices[0].u,
                quad.vertices[1].u,
                quad.vertices[2].u,
                quad.vertices[3].u,
        };

        float[] vCoords = new float[] {
                quad.vertices[0].v,
                quad.vertices[1].v,
                quad.vertices[2].v,
                quad.vertices[3].v,
        };

        int offset = 2;
        if (rotation == FaceRotation.CLOCKWISE)
            offset = 3;
        else if (rotation == FaceRotation.COUNTER_CLOCKWISE)
            offset = 1;

        for (int i = 0; i < 4; ++i)
        {
            quad.vertices[i].u = uCoords[(i + offset) % 4];
            quad.vertices[i].v = vCoords[(i + offset) % 4];
        }
    }

}

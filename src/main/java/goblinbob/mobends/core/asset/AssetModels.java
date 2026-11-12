package goblinbob.mobends.core.asset;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AssetModels
{
    public static final AssetModels INSTANCE = new AssetModels();
    private static final Logger LOGGER = LogManager.getLogger();

    private final FaceBakery faceBakery = new FaceBakery();
    private final Map<AssetLocation, SimpleBakedModel> bakedModelMap = new HashMap<>();

    public SimpleBakedModel register(AssetLocation location) throws IOException
    {
        ModelBlock model;
        try (InputStream stream = new FileInputStream(AssetsModule.INSTANCE.getAssetFile(location)))
        {
            model = ModelBlock.deserialize(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        }

        SimpleBakedModel bakedModel = bakeModel(model, ModelRotation.X0_Y0, false);
        bakedModelMap.put(location, bakedModel);

        return bakedModel;
    }

    public void clearCache()
    {
        bakedModelMap.clear();
    }

    public SimpleBakedModel getModel(AssetLocation location)
    {
        if (!bakedModelMap.containsKey(location))
        {
            try
            {
                SimpleBakedModel bakedModel = register(location);
                bakedModelMap.put(location, bakedModel);
                return bakedModel;
            }
            catch(IOException e)
            {
                LOGGER.warn("Failed to bake asset model: {}", location.toString(), e);
                bakedModelMap.put(location, null);
                return null;
            }
        }

        return bakedModelMap.get(location);
    }

    private AssetLocation resolveTextureName(String name)
    {
        if (name.equals("missingno"))
        {
            return null;
        }

        return new AssetLocation("textures/" + name);
    }

    protected BakedQuad makeBakedQuad(BlockPart part, BlockPartFace face, TextureAtlasSprite sprite, EnumFacing facing, ModelRotation rotation, boolean uvLocked)
    {
        return this.faceBakery.makeBakedQuad(part.positionFrom, part.positionTo, face, sprite, facing, rotation, part.partRotation, uvLocked, part.shade);
    }

    private SimpleBakedModel bakeModel(ModelBlock modelBlock, ModelRotation modelRotationIn, boolean uvLocked) throws IOException
    {
        SimpleBakedModel.Builder modelBuilder = new SimpleBakedModel.Builder(modelBlock);
        boolean particleTextureSet = false;

        if (modelBlock.getElements().isEmpty())
        {
            return null;
        }
        else
        {
            for (BlockPart blockpart : modelBlock.getElements())
            {
                for (EnumFacing enumfacing : blockpart.mapFaces.keySet())
                {
                    BlockPartFace blockpartface = blockpart.mapFaces.get(enumfacing);
                    AssetLocation textureLocation = resolveTextureName(modelBlock.resolveTextureName(blockpartface.texture));

                    if (textureLocation == null)
                    {
                        continue;
                    }

                    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(textureLocation);
                    final int[] size;
                    try (InputStream stream = new FileInputStream(AssetsModule.INSTANCE.getAssetFile(textureLocation)))
                    {
                        size = pngSize(stream);
                    }
                    sprite.setIconWidth(size[0]);
                    sprite.setIconHeight(size[1]);
                    sprite.initSprite(size[0], size[1], 0, 0, false);

                    if (!particleTextureSet)
                    {
                        modelBuilder.setTexture(sprite);
                        particleTextureSet = true;
                    }

                    if (blockpartface.cullFace == null)
                    {
                        modelBuilder.addGeneralQuad(this.makeBakedQuad(blockpart, blockpartface, sprite, enumfacing, modelRotationIn, uvLocked));
                    }
                    else
                    {
                        modelBuilder.addFaceQuad(modelRotationIn.rotateFace(blockpartface.cullFace), this.makeBakedQuad(blockpart, blockpartface, sprite, enumfacing, modelRotationIn, uvLocked));
                    }
                }
            }

            return (SimpleBakedModel) modelBuilder.makeBakedModel();
        }
    }

    /** The width and height of a PNG, from its header (as 1.12's PngSizeInfo reads it). */
    private static int[] pngSize(InputStream stream) throws IOException
    {
        DataInputStream data = new DataInputStream(stream);
        if (data.readLong() != 0x89504E470D0A1A0AL)
            throw new IOException("Not a PNG");
        data.readInt(); // the IHDR chunk's length
        if (data.readInt() != 0x49484452)
            throw new IOException("The PNG doesn't begin with IHDR");
        return new int[] {data.readInt(), data.readInt()};
    }
}

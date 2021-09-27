package goblinbob.mobends.core.asset;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
        InputStream stream = new FileInputStream(AssetsModule.INSTANCE.getAssetFile(location));
        ModelBlock model = ModelBlock.deserialize(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));

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

    protected BakedQuad makeBakedQuad(BlockPart p_177589_1_, BlockPartFace p_177589_2_, TextureAtlasSprite p_177589_3_, EnumFacing p_177589_4_, net.minecraftforge.common.model.ITransformation p_177589_5_, boolean p_177589_6_)
    {
        return this.faceBakery.makeBakedQuad(p_177589_1_.positionFrom, p_177589_1_.positionTo, p_177589_2_, p_177589_3_, p_177589_4_, p_177589_5_, p_177589_1_.partRotation, p_177589_6_, p_177589_1_.shade);
    }

    private SimpleBakedModel bakeModel(ModelBlock modelBlock, net.minecraftforge.common.model.ITransformation modelRotationIn, boolean uvLocked) throws IOException
    {
        SimpleBakedModel.Builder modelBuilder = (new SimpleBakedModel.Builder(modelBlock, modelBlock.createOverrides()));
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
                    PngSizeInfo sizeInfo = new PngSizeInfo(new FileInputStream(AssetsModule.INSTANCE.getAssetFile(textureLocation)));
                    sprite.loadSprite(sizeInfo, true);
                    sprite.initSprite(sizeInfo.pngWidth, sizeInfo.pngHeight, 0, 0, false);

                    if (!particleTextureSet)
                    {
                        modelBuilder.setTexture(sprite);
                        particleTextureSet = true;
                    }

                    if (blockpartface.cullFace == null || !net.minecraftforge.common.model.TRSRTransformation.isInteger(modelRotationIn.getMatrix()))
                    {
                        modelBuilder.addGeneralQuad(this.makeBakedQuad(blockpart, blockpartface, sprite, enumfacing, modelRotationIn, uvLocked));
                    }
                    else
                    {
                        modelBuilder.addFaceQuad(modelRotationIn.rotate(blockpartface.cullFace), this.makeBakedQuad(blockpart, blockpartface, sprite, enumfacing, modelRotationIn, uvLocked));
                    }
                }
            }

            return (SimpleBakedModel) modelBuilder.makeBakedModel();
        }
    }
}

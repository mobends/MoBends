package goblinbob.mobends.standard.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class LayerPlayerAccessories implements LayerRenderer<AbstractClientPlayer>
{
//    ModelResourceLocation modelResourceLocation = new ModelResourceLocation("mobends:accessories/accessory_rulers_crown#normal");
    ModelResourceLocation modelResourceLocation = new ModelResourceLocation("brown_concrete#normal");
    private final RenderItem itemRenderer;
    private final ModelManager modelManager;
    private final ItemStack emptyItemStack = new ItemStack(Items.AIR);

    public LayerPlayerAccessories()
    {
        Minecraft mc = Minecraft.getMinecraft();
        this.itemRenderer = mc.getRenderItem();
        this.modelManager = mc.getRenderItem().getItemModelMesher().getModelManager();
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        IBakedModel model = this.modelManager.getModel(new ModelResourceLocation("diamond_block#inventory"));

        GlStateManager.pushMatrix();

        model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.NONE, false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, enumfacing, 0L), 0xffffffff, emptyItemStack);
        }

        itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, null, 0L), 0xffffffff, emptyItemStack);
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }
}

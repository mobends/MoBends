package net.gobbob.mobends.client.renderer.entity.layers;

import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBendsSkeletonType implements LayerRenderer<EntitySkeleton>
{
    private static final ResourceLocation field_190092_a = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
    private final RenderLivingBase<?> renderer;
    private ModelBendsSkeleton model;

    public LayerBendsSkeletonType(RenderLivingBase<?> p_i47131_1_)
    {
        this.renderer = p_i47131_1_;
        this.model = new ModelBendsSkeleton(0.25F, true);
    }

    public void doRenderLayer(EntitySkeleton entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (entitylivingbaseIn.func_189771_df() == SkeletonType.STRAY)
        {
            this.model.setModelAttributes(this.renderer.getMainModel());
            this.model.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
            this.renderer.bindTexture(field_190092_a);
            this.model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }
}
package goblinbob.mobends.standard.compat.geckolib;

import goblinbob.mobends.standard.client.model.armor.ArmorWrapper;
import goblinbob.mobends.standard.client.model.armor.IArmorGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Draws GeckoLib armour (a {@link GeoArmorRenderer}) as the wearer's animation poses it, in place
 * of the renderer's own render, which only copies the vanilla parts' rotations to its bones. The
 * armour's GeckoLib animation still runs, and moves the bones below the armour bones.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GeoArmorGeometry implements IArmorGeometry
{
    private final GeoArmorRenderer renderer;
    /** Made with the first render, once the wrapper has its parts. */
    private GeoArmorMesh mesh;

    private GeoArmorGeometry(GeoArmorRenderer renderer)
    {
        this.renderer = renderer;
    }

    @Nullable
    static IArmorGeometry createFor(ModelBiped model)
    {
        return model instanceof GeoArmorRenderer ? new GeoArmorGeometry((GeoArmorRenderer) model) : null;
    }

    @Override
    public void render(ArmorWrapper wrapper, EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, float scale)
    {
        final Item item = stack.getItem();
        if (!(item instanceof IAnimatable))
        {
            return;
        }

        if (mesh == null)
        {
            mesh = new GeoArmorMesh(wrapper);
        }

        // What GeoArmorRenderer.render does before posing its bones: GeckoLib's own animation of the armour.
        final AnimatedGeoModel provider = renderer.getGeoModelProvider();
        final GeoModel model = provider.getModel(provider.getModelLocation(item));
        final AnimationEvent event = new AnimationEvent((IAnimatable) item, 0, 0, 0, false, Arrays.asList(stack, entity, slot));
        provider.setLivingAnimations((IAnimatable) item, renderer.getUniqueID(item), event);

        mesh.build(model, renderer);

        final float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        final Color color = renderer.getRenderColor(item, partialTicks);
        final float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F, alpha = color.getAlpha() / 255F;

        Minecraft.getMinecraft().renderEngine.bindTexture(renderer.getTextureLocation(item));

        // As IGeoRenderer.render draws a model.
        GlStateManager.disableCull();
        GlStateManager.enableRescaleNormal();
        renderer.renderEarly(item, partialTicks, red, green, blue, alpha);
        renderer.renderLate(item, partialTicks, red, green, blue, alpha);
        mesh.draw(scale, red, green, blue, alpha);
        renderer.renderAfter(item, partialTicks, red, green, blue, alpha);
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableCull();
    }
}

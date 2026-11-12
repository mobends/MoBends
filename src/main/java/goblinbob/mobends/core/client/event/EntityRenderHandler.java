package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.compat.RenderPartialTicks;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.RendererState;
import goblinbob.mobends.core.client.preview.PreviewWorld;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Puts the entity's mutation in place on the renderer before it's drawn, and the vanilla model back
 * after. Entities that share a renderer can be animated by different types, or not at all.
 */
public class EntityRenderHandler
{

    /** The renders in progress (a render can happen inside another one). */
    private final Deque<Render> renders = new ArrayDeque<>();

    private static class Render
    {
        final EntityLivingBase entity;
        final EntityBender<EntityLivingBase> bender;

        Render(EntityLivingBase entity, EntityBender<EntityLivingBase> bender)
        {
            this.entity = entity;
            this.bender = bender;
        }
    }

    /** Runs last, so a handler that cancels the render does it before anything is mutated. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SuppressWarnings("unchecked")
    public void beforeLivingRender(RenderLivingEvent.Pre<? extends EntityLivingBase> event)
    {
        final EntityLivingBase living = event.entity;
        final RendererLivingEntity<EntityLivingBase> renderer = (RendererLivingEntity<EntityLivingBase>) event.renderer;

        // A renderer the game doesn't draw this entity with belongs to another mod that draws it its
        // own way (Idō draws swimming and crawling players with a renderer of its own, made each
        // frame), so it's left as it is.
        if (renderer.getRenderManager().<EntityLivingBase>getEntityRenderObject(living) != renderer)
        {
            return;
        }

        final EntityTypeRegistry.Selection selection = EntityBenderRegistry.instance.getSelection(living);

        if (selection == null || !selection.bender.isAnimated())
        {
            RendererState.restoreVanilla(renderer);
            return;
        }

        final EntityBender<EntityLivingBase> entityBender = (EntityBender<EntityLivingBase>) selection.bender;
        final float pt = RenderPartialTicks.get();

        GlStateManager.pushMatrix();
        renders.push(new Render(living, entityBender));

        if (entityBender.applyMutation(renderer, living, (IEntityDataFactory<EntityLivingBase>) selection.dataFactory, pt))
        {
            final LivingEntityData<EntityLivingBase> data = EntityDatabase.instance.get(living);
            entityBender.beforeRender(data, living, pt);
        }
    }

    /** The previews of the Settings window are drawn without the name above them. */
    @SubscribeEvent
    public void beforeLivingSpecials(RenderLivingEvent.Specials.Pre<? extends EntityLivingBase> event)
    {
        if (event.entity.worldObj instanceof PreviewWorld)
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void afterLivingRender(RenderLivingEvent.Post<? extends EntityLivingBase> event)
    {
        // Renders whose Post never came (cancelled after this handler) are dropped on the way.
        while (!renders.isEmpty())
        {
            Render render = renders.pop();
            if (render.entity == event.entity)
            {
                GlStateManager.popMatrix();
                break;
            }
        }

        RendererState.restoreVanilla(event.renderer);
    }
}

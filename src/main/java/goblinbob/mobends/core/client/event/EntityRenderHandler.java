package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.RendererState;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
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
        final EntityLivingBase living = event.getEntity();
        final RenderLivingBase<EntityLivingBase> renderer = (RenderLivingBase<EntityLivingBase>) event.getRenderer();
        final EntityTypeRegistry.Selection selection = EntityBenderRegistry.instance.getSelection(living);

        if (selection == null || !selection.bender.isAnimated())
        {
            RendererState.restoreVanilla(renderer);
            return;
        }

        final EntityBender<EntityLivingBase> entityBender = (EntityBender<EntityLivingBase>) selection.bender;
        final float pt = event.getPartialRenderTick();

        GlStateManager.pushMatrix();
        renders.push(new Render(living, entityBender));

        if (entityBender.applyMutation(renderer, living, (IEntityDataFactory<EntityLivingBase>) selection.dataFactory, pt))
        {
            final LivingEntityData<EntityLivingBase> data = EntityDatabase.instance.get(living);
            entityBender.beforeRender(data, living, pt);
        }
    }

    @SubscribeEvent
    public void afterLivingRender(RenderLivingEvent.Post<? extends EntityLivingBase> event)
    {
        // Renders whose Post never came (cancelled after this handler) are dropped on the way.
        while (!renders.isEmpty())
        {
            Render render = renders.pop();
            if (render.entity == event.getEntity())
            {
                render.bender.afterRender(event.getEntity(), event.getPartialRenderTick());
                GlStateManager.popMatrix();
                break;
            }
        }

        RendererState.restoreVanilla(event.getRenderer());
    }
}

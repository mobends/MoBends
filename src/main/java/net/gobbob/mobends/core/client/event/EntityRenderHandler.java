package net.gobbob.mobends.core.client.event;

import net.gobbob.mobends.core.bender.EntityBender;
import net.gobbob.mobends.core.bender.EntityBenderRegistry;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.mutators.Mutator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityRenderHandler
{

    @SubscribeEvent
    public void beforeLivingRender(RenderLivingEvent.Pre<? extends EntityLivingBase> event)
    {
        final EntityLivingBase living = event.getEntity();
        final EntityBender<EntityLivingBase> entityBender = EntityBenderRegistry.instance.getForEntity(living);

        if (entityBender == null)
            return;

        final RenderLivingBase<?> renderer = event.getRenderer();
        final float pt = event.getPartialRenderTick();

        GlStateManager.pushMatrix();

        if (entityBender.isAnimated())
        {
            if (entityBender.applyMutation(renderer, living, pt))
            {
                Mutator mutator = entityBender.getMutator(renderer);
                LivingEntityData<EntityLivingBase> data = mutator.getData(living);
                entityBender.beforeRender(data, living, pt);
            }
        }
        else
        {
            entityBender.deapplyMutation(event.getRenderer(), living);
        }
    }

    @SubscribeEvent
    public void afterLivingRender(RenderLivingEvent.Post<? extends EntityLivingBase> event)
    {
        final EntityBender<EntityLivingBase> entityBender = EntityBenderRegistry.instance.getForEntity(event.getEntity());

        if (entityBender == null)
            return;

        entityBender.afterRender(event.getEntity(), event.getPartialRenderTick());

        GlStateManager.popMatrix();
    }

}

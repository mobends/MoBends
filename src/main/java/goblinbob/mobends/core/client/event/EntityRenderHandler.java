package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
                final Mutator mutator = entityBender.getMutator(renderer);
                final LivingEntityData<EntityLivingBase> data = mutator.getData(living);

                if(!living.isInWater())
                {
                    entityBender.beforeRender(data, living, pt);
                }
                else
                {
                    //in water
                    if(!ModConfig.swimModule && living instanceof EntityPlayer)
                    {
                        entityBender.deapplyMutation(event.getRenderer(), living);
                    }else
                    {
                        entityBender.beforeRender(data, living, pt);
                    }

                }

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

package net.gobbob.mobends.core.mutators;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.LivingEntityData;
import net.gobbob.mobends.standard.client.mutators.PlayerMutator;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class MutationContainer<D extends LivingEntityData<E>, E extends EntityLivingBase, M extends Mutator<D, E, ?>, R extends RenderLivingBase>
{
	
	private HashMap<R, M> mutatorMap = new HashMap<R, M>();
	private IMutatorFactory<M> mutatorFactory;
	
	public MutationContainer(IMutatorFactory<M> mutatorFactory)
	{
		this.mutatorFactory = mutatorFactory;
	}
	
	/*
	 * Used to apply the effect of the mutation, or just to update the model
	 * if it was already mutated.
	 * Called from AnimatedEntity.
	 */
	public boolean apply(R renderer, E entity, float partialTicks)
	{
		M mutator = mutatorMap.get(renderer);
		if (!mutatorMap.containsKey(renderer))
		{
			mutator = mutatorFactory.createMutator();
			if (!mutator.mutate(entity, renderer))
			{
				return false;
			}
			
			mutatorMap.put(renderer, mutator);
		}

		mutator.updateModel(entity, renderer, partialTicks);
		D data = mutator.getOrMakeData(entity);
		mutator.performAnimations(data, renderer, partialTicks);
		mutator.syncUpWithData(data);
		
		return true;
	}
	
	/*
	 * Used to reverse the effect of the mutation.
	 * Called from AnimatedEntity.
	 */
	public void deapply(R renderer, E entity)
	{
		if (mutatorMap.containsKey(renderer))
		{
			M mutator = mutatorMap.get(renderer);
			mutator.demutate(entity, renderer);
			mutatorMap.remove(renderer);
		}
	}

	/*
	 * Used to refresh the mutators in case of real-time changes during development.
	 */
	public void refresh()
	{
		for (Map.Entry<R, M> mutator : mutatorMap.entrySet())
		{
			mutator.getValue().mutate(null, mutator.getKey());
			mutator.getValue().postRefresh();
		}
	}
	
	public M getForRenderer(R renderer)
	{
		return mutatorMap.get(renderer);
	}
	
}

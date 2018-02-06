package net.gobbob.mobends.client.mutators;

import net.gobbob.mobends.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class BoxMutator
{
	protected ModelBox target;
	
	public BoxMutator(ModelBox target)
	{
		this.target = target;
	}
	
	public static BoxMutator createFrom(ModelRenderer modelRenderer, net.minecraft.client.model.ModelBox original)
	{
		ModelBox target = new ModelBox(modelRenderer, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		return new BoxMutator(target);
	}

	public ModelBox produce()
	{
		// TODO Make magic happen here.
		return null;
	}
}

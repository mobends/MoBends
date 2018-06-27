package net.gobbob.mobends.client.mutators;

import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public abstract class Mutator<M extends ModelBase>
{
	protected M vanillaModel;
	protected float headYaw, headPitch, limbSwing, limbSwingAmount;
	
	protected List<LayerRenderer<EntityLivingBase>> layerRenderers;
}

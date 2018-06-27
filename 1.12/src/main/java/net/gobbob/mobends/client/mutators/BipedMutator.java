package net.gobbob.mobends.client.mutators;

import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.client.model.IBendsModel;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartExtended;
import net.gobbob.mobends.client.model.ModelPartPostOffset;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomBipedArmor;
import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomHeldItem;
import net.gobbob.mobends.util.FieldMiner;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public abstract class BipedMutator<T extends EntityLivingBase> implements IBendsModel
{
	protected ModelPartPostOffset body;
	protected ModelPartChild head;
	protected ModelPartChild headwear;
	protected ModelPartChildExtended leftArm;
	protected ModelPartChildExtended rightArm;
	protected ModelPartChildExtended leftForeArm;
	protected ModelPartChildExtended rightForeArm;
	protected ModelPartExtended leftLeg;
	protected ModelPartExtended rightLeg;
	protected ModelPartChild leftForeLeg;
	protected ModelPartChild rightForeLeg;
	
	protected ModelPartTransform leftItemTransform;
	protected ModelPartTransform rightItemTransform;
	protected float headYaw, headPitch, limbSwing, limbSwingAmount;
	
	protected List<LayerRenderer<EntityLivingBase>> layerRenderers;
	protected LayerBipedArmor vanillaLayerArmor;
	protected LayerHeldItem vanillaLayerHeldItem;
	protected LayerCustomBipedArmor layerArmor;
	protected LayerCustomHeldItem layerHeldItem;
	
	protected HashMap<String, IModelPart> nameToPartMap;
	
	public BipedMutator()
	{
		this.nameToPartMap = new HashMap<String, IModelPart>();
	}
	
	public void fetchFields(RenderLivingBase<T> renderer)
	{
		// Getting the layer renderers
		this.layerRenderers = FieldMiner.getObfuscatedValue(renderer, "layerRenderers", "field_177097_h");
	}
	
	@Override
	public Object getPartForName(String name)
	{
		return nameToPartMap.get(name);
	}
}

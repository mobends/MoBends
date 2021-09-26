package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.client.model.ModelPartExtended;
import goblinbob.mobends.core.client.model.ModelPartPostOffset;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomCape;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomElytra;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerPlayerAccessories;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

/**
 * Instantiated once per RenderPlayer
 */
public class PlayerMutator extends BipedMutator<PlayerData, AbstractClientPlayer, ModelPlayer>
{
	protected ModelPart bodywear;
	protected ModelPart leftArmwear;
	protected ModelPart rightArmwear;
	protected ModelPart leftForeArmwear;
	protected ModelPart rightForeArmwear;
	protected ModelPart leftLegwear;
	protected ModelPart rightLegwear;
	protected ModelPart leftForeLegwear;
	protected ModelPart rightForeLegwear;

	protected boolean smallArms;

	protected LayerCustomCape layerCape;
	protected LayerCape layerCapeVanilla;
	protected LayerCustomElytra layerElytra;
	protected LayerElytra layerElytraVanilla;
	protected LayerPlayerAccessories layerPlayerAccessories;

	public PlayerMutator(IEntityDataFactory<AbstractClientPlayer> dataFactory)
	{
		super(dataFactory);
	}
	
	public boolean hasSmallArms()
	{
		return this.smallArms;
	}

	@Override
	public boolean mutate(RenderLivingBase<? extends AbstractClientPlayer> renderer)
	{
		if (super.mutate(renderer))
		{
			this.layerPlayerAccessories = new LayerPlayerAccessories();
			layerRenderers.add(layerPlayerAccessories);
			return true;
		}

		return false;
	}

	@Override
	public void demutate(RenderLivingBase<? extends AbstractClientPlayer> renderer)
	{
		super.demutate(renderer);

		layerRenderers.remove(layerPlayerAccessories);
	}

	@Override
	public void fetchFields(RenderLivingBase<? extends AbstractClientPlayer> renderer)
	{
		super.fetchFields(renderer);

		// Does the renderer have Small Arms?
		this.smallArms = ((RenderPlayer) renderer).smallArms;
	}

	@Override
	public void storeVanillaModel(ModelPlayer model)
	{
		ModelPlayer vanillaModel = new ModelPlayer(0.0F, this.smallArms);
		this.vanillaModel = vanillaModel;
		
		// Calling the super method here, since it
		// requires the vanillaModel property to be
		// set.
		super.storeVanillaModel(model);
		
		vanillaModel.bipedBodyWear = model.bipedBodyWear;
		vanillaModel.bipedLeftArmwear = model.bipedLeftArmwear;
		vanillaModel.bipedLeftLegwear = model.bipedLeftLegwear;
		vanillaModel.bipedRightArmwear = model.bipedRightArmwear;
		vanillaModel.bipedRightLegwear = model.bipedRightLegwear;
	}
	
	@Override
	public void applyVanillaModel(ModelPlayer model)
	{
		super.applyVanillaModel(model);
		
		model.bipedBodyWear = vanillaModel.bipedBodyWear;
		model.bipedLeftArmwear = vanillaModel.bipedLeftArmwear;
		model.bipedLeftLegwear = vanillaModel.bipedLeftLegwear;
		model.bipedRightArmwear = vanillaModel.bipedRightArmwear;
		model.bipedRightLegwear = vanillaModel.bipedRightLegwear;
	}

	@Override
	public void swapLayer(RenderLivingBase<? extends AbstractClientPlayer> renderer, int index, boolean isModelVanilla)
	{
		super.swapLayer(renderer, index, isModelVanilla);

		final LayerRenderer<?> layer = layerRenderers.get(index);
		if (layer instanceof LayerCape)
		{
			this.layerCape = new LayerCustomCape((RenderPlayer) renderer);
			if (isModelVanilla)
				this.layerCapeVanilla = (LayerCape) layer;
			layerRenderers.set(index, this.layerCape);
		}

		if (layer instanceof LayerElytra)
		{
			this.layerElytra = new LayerCustomElytra((RenderPlayer) renderer);
			if (isModelVanilla)
				this.layerElytraVanilla = (LayerElytra) layer;
			layerRenderers.set(index, this.layerElytra);
		}
	}

	@Override
	public void deswapLayer(RenderLivingBase<? extends AbstractClientPlayer> renderer, int index)
	{
		super.deswapLayer(renderer, index);

		final LayerRenderer<? extends EntityLivingBase> layer = layerRenderers.get(index);
		if (layer instanceof LayerCustomCape)
		{
			layerRenderers.set(index, this.layerCapeVanilla);
		}

		if (layer instanceof LayerCustomElytra)
		{
			layerRenderers.set(index, this.layerElytraVanilla);
		}

		layerRenderers.remove(layerPlayerAccessories);
	}

	@Override
	public boolean createParts(ModelPlayer original, float scaleFactor)
	{
		super.createParts(original, scaleFactor);

		// Arms
		int armWidth = this.smallArms ? 3 : 4;
		float armY = this.smallArms ? -9.5F : -10F;
		
		original.bipedLeftArm = this.leftArm = new ModelPartExtended(original, 32, 48);
		this.leftArm
				.setParent(body)
				.setPosition(5.0F, armY, 0.0F)
				.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
				.inflate(0.01F, 0F, 0.01F)
				.hideFace(BoxSide.BOTTOM)
				.create();
		
		original.bipedRightArm = this.rightArm = new ModelPartExtended(original, 40, 16);
		this.rightArm
				.setParent(body)
				.setPosition(-5.0F, armY, 0.0F)
				.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
				.inflate(0.01F, 0F, 0.01F)
				.hideFace(BoxSide.BOTTOM)
				.create();
		
		this.leftForeArm = new ModelPartPostOffset(original, 32, 48 + 6)
				.setPostOffset(0, -4F, -2F);
		this.leftForeArm
				.setPosition(0.0F, 4.0F, 2.0F)
				.setParent(leftArm)
				.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.leftArm.setExtension(this.leftForeArm);
		
		this.rightForeArm = new ModelPartPostOffset(original, 40, 16 + 6)
				.setPostOffset(0, -4F, -2F);
		this.rightForeArm
				.setPosition(0.0F, 4.0F, 2.0F)
				.setParent(rightArm)
				.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.rightArm.setExtension(this.rightForeArm);

		original.bipedLeftLeg = leftLeg = (ModelPartExtended) new ModelPartExtended(original, 16, 48)
				.setPosition(0.0F, 12.0F, 0.0F);
		leftLeg.addBox(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
		leftLeg.setExtension(leftForeLeg);

		// Wear
		original.bipedBodyWear = bodywear = new ModelPart(original, 16, 32);
		this.bodywear.setParent(body);
		this.bodywear.addBox(-4F, -12F, -2F, 8, 12, 4, scaleFactor + 0.25F);
		
		// this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
		// this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
		// this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
		// this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
		// this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
		// this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);

		float limbWearHeight = (6F + 2 * scaleFactor + 0.5F) - 0.25F;

		original.bipedLeftArmwear = leftArmwear = new ModelPart(original, 48, 48);
		this.leftArmwear
				.setParent(leftArm)
				.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.inflate(0.0025F, 0F, 0.0025F)
				.hideFace(BoxSide.BOTTOM)
				.create();
		
		original.bipedRightArmwear = rightArmwear = new ModelPart(original, 40, 32);
		this.rightArmwear
				.setParent(rightArm)
				.developBox(-armWidth + 1, -2F, -2F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.inflate(0.0025F, 0F, 0.0025F)
				.hideFace(BoxSide.BOTTOM)
				.create();

		this.leftForeArmwear = new ModelPart(original, 48, 48 + 6);
		this.leftForeArmwear
				.developBox(-1F, 0F, -4F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.inflate(0.005F, 0F, 0.005F)
				.offset(0F, 0.25F, 0F)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.leftForeArm.addChild(this.leftForeArmwear);
		
		this.rightForeArmwear = new ModelPart(original, 40, 32 + 6);
		this.rightForeArmwear
				.developBox(-armWidth + 1, 0F, -4F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.inflate(0.005F, 0F, 0.005F)
				.offset(0F, 0.25F, 0F)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.rightForeArm.addChild(this.rightForeArmwear);

		original.bipedLeftLegwear = leftLegwear = new ModelPart(original, 0, 48);
		this.leftLegwear.setParent(leftLeg)
				.developBox(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.hideFace(BoxSide.BOTTOM)
				.create();
		original.bipedRightLegwear = rightLegwear = new ModelPart(original, 0, 32);
		this.rightLegwear
				.setParent(rightLeg)
				.developBox(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.hideFace(BoxSide.BOTTOM)
				.create();

		this.leftForeLegwear = new ModelPart(original, 0, 48 + 6);
		this.leftForeLegwear
				.developBox(-0.1F, 0F, 0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.inflate(0.005F, 0F, 0.005F)
				.offset(0F, 0.25F, 0F)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.leftForeLeg.addChild(this.leftForeLegwear);
		
		this.rightForeLegwear = new ModelPart(original, 0, 32 + 6);
		this.rightForeLegwear
				.developBox(-3.9F, 0F, 0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(limbWearHeight)
				.inflate(0.005F, 0, 0.005F)
				.offset(0F, 0.25F, 0F)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.rightForeLeg.addChild(this.rightForeLegwear);
		
		return true;
	}

	@Override
	public void performAnimations(PlayerData data, String animatedEntityKey, RenderLivingBase<? extends AbstractClientPlayer> renderer, float partialTicks)
	{
		leftForeArmwear.setVisible(leftArmwear.isShowing());
		rightForeArmwear.setVisible(rightArmwear.isShowing());
		leftForeLegwear.setVisible(leftLegwear.isShowing());
		rightForeLegwear.setVisible(rightLegwear.isShowing());
		
		super.performAnimations(data, animatedEntityKey, renderer, partialTicks);
	}
	
	@Override
	public void postRefresh()
	{
		if (this.layerArmor != null)
			this.layerArmor.initArmor();
	}
	
	/**
	 * Called before the first person hand is rendered, so the mutator can pose it
	 * in any way.
	 */
	public void poseForFirstPersonView()
	{
		this.body.rotation.identity();
		this.rightArm.rotation.identity();
		this.rightForeArm.rotation.identity();
		this.leftArm.rotation.identity();
		this.leftForeArm.rotation.identity();
	}

	@Override
	public boolean isModelVanilla(ModelPlayer model)
	{
		return !(model.bipedBody instanceof IModelPart);
	}

	@Override
	public boolean shouldModelBeSkipped(ModelBase model)
	{
		return !(model instanceof ModelPlayer);
	}

	@Override
	public PlayerData getData(AbstractClientPlayer entity)
	{
		return PlayerPreviewer.isPreviewInProgress() ? PlayerPreviewer.getPreviewData() : super.getData(entity);
	}

	@Override
	public PlayerData getOrMakeData(AbstractClientPlayer entity)
	{
		return PlayerPreviewer.isPreviewInProgress() ? PlayerPreviewer.getPreviewData() : super.getOrMakeData(entity);
	}

}

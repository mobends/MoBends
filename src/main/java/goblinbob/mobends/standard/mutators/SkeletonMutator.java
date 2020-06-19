package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.*;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.SkeletonData;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;

/**
 * Instantiated once per RenderSkeleton
 *
 * @author Iwo Plaza
 *
 */
public class SkeletonMutator extends BipedMutator<SkeletonData, EntitySkeleton, ModelSkeleton>
{

	protected boolean boneLimbs = false;

	public SkeletonMutator(IEntityDataFactory<EntitySkeleton> dataCreationFunction)
	{
		super(dataCreationFunction);
	}

	@Override
	public void fetchFields(RenderLivingBase<? extends EntitySkeleton> renderer)
	{
		super.fetchFields(renderer);

		if (renderer.getMainModel() instanceof ModelSkeleton)
		{
			ModelSkeleton model = (ModelSkeleton) renderer.getMainModel();

			this.boneLimbs = ((int) model.bipedRightArm.cubeList.get(0).posX1) == -1;
		}
	}

	@Override
	public void storeVanillaModel(ModelSkeleton model)
	{
		this.vanillaModel = new ModelSkeleton(0.0F, !this.boneLimbs);

		// Calling the super method here, since it
		// requires the vanillaModel property to be
		// set.
		super.storeVanillaModel(model);
	}

	@Override
	public boolean createParts(ModelSkeleton original, float scaleFactor)
	{
		super.createParts(original, scaleFactor);

		if (this.boneLimbs)
		{
//			this.bipedRightArm = new ModelRenderer(this, 40, 16);
//			this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
//			this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
//			this.bipedLeftArm = new ModelRenderer(this, 40, 16);
//			this.bipedLeftArm.mirror = true;
//			this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
//			this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
//			this.bipedRightLeg = new ModelRenderer(this, 0, 16);
//			this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
//			this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
//			this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
//			this.bipedLeftLeg.mirror = true;
//			this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
//			this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);

			// Arms
			original.bipedRightArm = this.rightArm = new ModelPartExtended(original, 40, 16);
			this.rightArm
					.setParent(body)
					.setPosition(-5.0F, 2.0F, 0.0F)
					.developBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor)
					.inflate(0.01F, 0F, 0.01F)
					.hideFace(BoxSide.BOTTOM)
					.create();

			original.bipedLeftArm = this.leftArm = new ModelPartExtended(original, 40, 16);
			this.leftArm
					.setParent(body)
					.setPosition(5.0F, 2.0F, 0.0F)
					.developBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor)
					.inflate(0.01F, 0F, 0.01F)
					.hideFace(BoxSide.BOTTOM)
					.create();

			this.rightForeArm = new ModelPartPostOffset(original, 40, 16 + 6)
					.setPostOffset(0, -4F, -1F);
			this.rightForeArm
					.setPosition(0.0F, 4.0F, 1.0F)
					.setParent(rightArm)
					.developBox(-1.0F, 0.0F, -2.0F, 2, 6, 2, scaleFactor)
					.hideFace(BoxSide.TOP)
					.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
					.create();
			this.rightArm.setExtension(this.rightForeArm);

			this.leftForeArm = new ModelPartPostOffset(original, 40, 16 + 6)
					.setPostOffset(0, -4F, -1F);
			this.leftForeArm
					.setPosition(0.0F, 4.0F, 1.0F)
					.setParent(leftArm)
					.developBox(-1.0F, 0.0F, -2.0F, 2, 6, 2, scaleFactor)
					.hideFace(BoxSide.TOP)
					.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
					.create();
			this.leftArm.setExtension(this.leftForeArm);

			//			this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
			//			this.bipedLeftLeg.mirror = true;
			//			this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
			//			this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);

			// Legs
			original.bipedRightLeg = rightLeg = (ModelPartExtended) new ModelPartExtended(original, 0, 16)
					.setPosition(-2.0F, 12.0F, 0.0F);
			rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);
			original.bipedLeftLeg = leftLeg = (ModelPartExtended) new ModelPartExtended(original, 0, 16)
					.setPosition(2.0F, 12.0F, 0.0F)
					.setMirror(true);
			leftLeg.addBox(-1F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);

			leftForeLeg = new ModelPart(original, 0, 16 + 6)
					.setParent(leftLeg)
					.setPosition(0, 6.0F, -2.0F)
					.setMirror(true);
			leftForeLeg.developBox(-1.0F, 0.0F, 0.0F, 2, 6, 2, scaleFactor)
					.inflate(0.01F, 0, 0.01F)
					.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
					.create();
			leftLeg.setExtension(leftForeLeg);

			rightForeLeg = new ModelPart(original, 0, 16 + 6)
					.setParent(rightLeg)
					.setPosition(0, 6.0F, -2.0F);
			rightForeLeg.developBox(-1F, 0.0F, 0.0F, 2, 6, 2, scaleFactor)
					.inflate(0.01F, 0, 0.01F)
					.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
					.create();
			rightLeg.setExtension(rightForeLeg);
		}

		return true;
	}

	@Override
	public boolean shouldModelBeSkipped(ModelBase model)
	{
		return !(model instanceof ModelSkeleton);
	}

}

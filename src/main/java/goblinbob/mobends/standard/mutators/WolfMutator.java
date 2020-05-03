package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.client.model.FaceRotation;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.data.WolfData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.passive.EntityWolf;

public class WolfMutator extends Mutator<WolfData, EntityWolf, ModelWolf>
{

    /** main box for the wolf head */
    public ModelPart wolfHeadMain;
    /** The wolf's body */
    public ModelPart wolfBody;
    /** Wolf'se first leg */
    public ModelPart wolfLeg1;
    /** Wolf's second leg */
    public ModelPart wolfLeg2;
    /** Wolf's third leg */
    public ModelPart wolfLeg3;
    /** Wolf's fourth leg */
    public ModelPart wolfLeg4;
    /** The wolf's tail */
    public ModelPart wolfTail;
    /** The wolf's mane */
    public ModelPart wolfMane;

    public ModelPart nose;
    public ModelPart mouth;
    public ModelPart foreLeg1;
    public ModelPart foreLeg2;
    public ModelPart foreLeg3;
    public ModelPart foreLeg4;

    public WolfMutator(IEntityDataFactory<EntityWolf> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(ModelWolf model)
    {
        this.vanillaModel = new ModelWolf();

        this.vanillaModel.wolfHeadMain = model.wolfHeadMain;
        this.vanillaModel.wolfBody = model.wolfBody;
        this.vanillaModel.wolfLeg1 = model.wolfLeg1;
        this.vanillaModel.wolfLeg2 = model.wolfLeg2;
        this.vanillaModel.wolfLeg3 = model.wolfLeg3;
        this.vanillaModel.wolfLeg4 = model.wolfLeg4;
        this.vanillaModel.wolfTail = model.wolfTail;
        this.vanillaModel.wolfMane = model.wolfMane;
    }

    @Override
    public void applyVanillaModel(ModelWolf model)
    {
        model.wolfHeadMain = this.vanillaModel.wolfHeadMain;
        model.wolfBody = this.vanillaModel.wolfBody;
        model.wolfLeg1 = this.vanillaModel.wolfLeg1;
        model.wolfLeg2 = this.vanillaModel.wolfLeg2;
        model.wolfLeg3 = this.vanillaModel.wolfLeg3;
        model.wolfLeg4 = this.vanillaModel.wolfLeg4;
        model.wolfTail = this.vanillaModel.wolfTail;
        model.wolfMane = this.vanillaModel.wolfMane;
    }

    @Override
    public void swapLayer(RenderLivingBase<? extends EntityWolf> renderer, int index, boolean isModelVanilla)
    {

    }

    @Override
    public void deswapLayer(RenderLivingBase<? extends EntityWolf> renderer, int index)
    {

    }

    @Override
    public boolean createParts(ModelWolf original, float scaleFactor)
    {
        float f = 0.0F;
        float f1 = 13.5F;

        // Body
        original.wolfBody = wolfBody = new ModelPart(original, 18, 14)
                .setPosition(-1.0F, 13.0F, 2.0F);
        //wolfBody.addBox(-3.0F, -1.0F, -3.0F, 6, 9, 6, scaleFactor);
        wolfBody.developBox(-3.0F, -1.0F, -3.0F, 6, 6, 9, scaleFactor)
                .offsetTextureQuad(BoxSide.TOP, 9.0F, 6.0F)
                .rotateTextureQuad(BoxSide.TOP, FaceRotation.HALF_TURN)
                .offsetTextureQuad(BoxSide.BACK, -12F, -9F)
                .rotateTextureQuad(BoxSide.BOTTOM, FaceRotation.HALF_TURN)
                .offsetTextureQuad(BoxSide.BOTTOM, -8F, 6F)
                .rotateTextureQuad(BoxSide.LEFT, FaceRotation.CLOCKWISE)
                .offsetTextureQuad(BoxSide.LEFT, -3F, -3F)
                .rotateTextureQuad(BoxSide.RIGHT, FaceRotation.COUNTER_CLOCKWISE)
                .offsetTextureQuad(BoxSide.RIGHT, 0F, -3F)
                .create();

        // Head
        original.wolfHeadMain = wolfHeadMain = new ModelPart(original, 0, 0)
                .setParent(wolfBody)
                .setPosition(-1.0F, 0.5F, -7.0F);
        wolfHeadMain.addBox(-2.0F, -3.0F, -2.0F, 6, 6, 4, scaleFactor);

        // Mane
        original.wolfMane = wolfMane = new ModelPart(original, 21, 0)
                .setParent(wolfBody)
                .setPosition(0.0F, 0.0F, -7.0F);
        wolfMane.developBox(-4.0F, -3.5F, -2.0F, 8, 7, 6, scaleFactor)
                .offsetTextureQuad(BoxSide.TOP, 1.0F, 7.0F)
                .rotateTextureQuad(BoxSide.TOP, FaceRotation.HALF_TURN)
                .offsetTextureQuad(BoxSide.BACK, -5F, -6F)
                .offsetTextureQuad(BoxSide.BOTTOM, 8F, 7F)
                .rotateTextureQuad(BoxSide.BOTTOM, FaceRotation.HALF_TURN)
                .rotateTextureQuad(BoxSide.LEFT, FaceRotation.CLOCKWISE)
                .offsetTextureQuad(BoxSide.LEFT, -14F, 1F)
                .rotateTextureQuad(BoxSide.RIGHT, FaceRotation.COUNTER_CLOCKWISE)
                .offsetTextureQuad(BoxSide.RIGHT, 15F, 1F)
                .offsetTextureQuad(BoxSide.FRONT, 1F, -6F)
                .create();

        // Leg1
        original.wolfLeg1 = wolfLeg1 = new ModelPart(original, 0, 18)
                .setPosition(-2.5F, 16.0F, 7.0F);
        wolfLeg1.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Leg2
        original.wolfLeg2 = wolfLeg2 = new ModelPart(original, 0, 18)
                .setPosition(0.5F, 16.0F, 7.0F);
        wolfLeg2.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Leg3
        original.wolfLeg3 = wolfLeg3 = new ModelPart(original, 0, 18)
                .setPosition(-2.5F, 16.0F, -4.0F);
        wolfLeg3.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Leg4
        original.wolfLeg4 = wolfLeg4 = new ModelPart(original, 0, 18)
                .setPosition(0.5F, 16.0F, -4.0F);
        wolfLeg4.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Tail
        original.wolfTail = wolfTail = new ModelPart(original, 9, 18)
                .setParent(wolfBody)
                .setPosition(-1.0F, 12.0F, 8.0F);
        wolfTail.addBox(0.0F, 0.0F, -2.0F, 2, 8, 2, scaleFactor);

        wolfHeadMain.setTextureOffset(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F);
        wolfHeadMain.setTextureOffset(16, 14).addBox(2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F);
        wolfHeadMain.setTextureOffset(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3, 3, 4, 0.0F);

        return false;
    }

    @Override
    public void syncUpWithData(WolfData data)
    {
        wolfHeadMain.syncUp(data.head);
        wolfBody.syncUp(data.body);
        wolfLeg1.syncUp(data.leg1);
        wolfLeg2.syncUp(data.leg2);
        wolfLeg3.syncUp(data.leg3);
        wolfLeg4.syncUp(data.leg4);
        wolfTail.syncUp(data.tail);
        wolfMane.syncUp(data.mane);
    }

    @Override
    public boolean isModelVanilla(ModelWolf model)
    {
        return !(model.wolfBody instanceof IModelPart);
    }

    @Override
    public boolean shouldModelBeSkipped(ModelBase model)
    {
        return !(model instanceof ModelWolf);
    }

}

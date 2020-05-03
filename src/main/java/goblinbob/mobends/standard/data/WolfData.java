package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.standard.animation.controller.WolfController;
import net.minecraft.entity.passive.EntityWolf;

public class WolfData extends LivingEntityData<EntityWolf>
{

    /** main box for the wolf head */
    public ModelPartTransform wolfHeadMain;
    /** The wolf's body */
    public ModelPartTransform wolfBody;
    /** Wolf'se first leg */
    public ModelPartTransform wolfLeg1;
    /** Wolf's second leg */
    public ModelPartTransform wolfLeg2;
    /** Wolf's third leg */
    public ModelPartTransform wolfLeg3;
    /** Wolf's fourth leg */
    public ModelPartTransform wolfLeg4;
    /** The wolf's tail */
    public ModelPartTransform wolfTail;
    /** The wolf's mane */
    public ModelPartTransform wolfMane;

    private final WolfController controller = new WolfController();

    public WolfData(EntityWolf entity)
    {
        super(entity);
    }

    @Override
    public IAnimationController<?> getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
        // No behaviour
    }

    @Override
    public void initModelPose()
    {
        super.initModelPose();

        nameToPartMap.put("wolfHeadMain", wolfHeadMain = new ModelPartTransform());
        nameToPartMap.put("wolfBody", wolfBody = new ModelPartTransform());
        nameToPartMap.put("wolfLeg1", wolfLeg1 = new ModelPartTransform());
        nameToPartMap.put("wolfLeg2", wolfLeg2 = new ModelPartTransform());
        nameToPartMap.put("wolfLeg3", wolfLeg3 = new ModelPartTransform());
        nameToPartMap.put("wolfLeg4", wolfLeg4 = new ModelPartTransform());
        nameToPartMap.put("wolfTail", wolfTail = new ModelPartTransform());
        nameToPartMap.put("wolfMane", wolfMane = new ModelPartTransform());

        wolfHeadMain.position.set(-1.0F, 13.5F, -7.0F);
        wolfBody.position.set(0.0F, 14.0F, 2.0F);
        wolfMane.position.set(-1.0F, 14.0F, 2.0F);
        wolfLeg1.position.set(-2.5F, 16.0F, 7.0F);
        wolfLeg2.position.set(0.5F, 16.0F, 7.0F);
        wolfLeg3.position.set(-2.5F, 16.0F, -4.0F);
        wolfLeg4.position.set(0.5F, 16.0F, -4.0F);
        wolfTail.position.set(-1.0F, 12.0F, 8.0F);
    }

    @Override
    public void updateParts(float ticksPerFrame)
    {
        super.updateParts(ticksPerFrame);

        wolfHeadMain.update(ticksPerFrame);
        wolfBody.update(ticksPerFrame);
        wolfLeg1.update(ticksPerFrame);
        wolfLeg2.update(ticksPerFrame);
        wolfLeg3.update(ticksPerFrame);
        wolfLeg4.update(ticksPerFrame);
        wolfTail.update(ticksPerFrame);
        wolfMane.update(ticksPerFrame);
    }

}

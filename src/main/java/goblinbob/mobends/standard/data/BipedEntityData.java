package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3fReadonly;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import net.minecraft.entity.EntityLivingBase;

public abstract class BipedEntityData<E extends EntityLivingBase> extends LivingEntityData<E>
{
	/*
	 * These models need to be represented only
	 * as transforms, because that's the only thing
	 * that needs to persist between frames.
	 */

	private static final IVec3fRead ROOT_OFFSET = new Vec3fReadonly(0, 0F, 0);

	public ModelPartTransform root;
	public ModelPartTransform head;
    public ModelPartTransform body;
    public ModelPartTransform rightArm;
    public ModelPartTransform leftArm;
    public ModelPartTransform rightLeg;
    public ModelPartTransform leftLeg;
    public ModelPartTransform rightForearm;
    public ModelPartTransform leftForearm;
    public ModelPartTransform rightShin;
    public ModelPartTransform leftShin;
	
    public ModelPartTransform rightHeldItem;
    public ModelPartTransform leftHeldItem;
	
    public SwordTrail swordTrail;
    
	public BipedEntityData(E entity)
	{
		super(entity);
	}

	@Override
	public void initModelPose()
	{
		this.root = new ModelPartTransform();
		this.body = new ModelPartTransform(this.root);
		this.head = new ModelPartTransform(this.body);
		this.rightArm = new ModelPartTransform(this.body);
		this.leftArm = new ModelPartTransform(this.body);
		this.rightLeg = new ModelPartTransform(this.root);
		this.leftLeg = new ModelPartTransform(this.root);
		this.rightForearm = new ModelPartTransform(this.rightArm);
		this.leftForearm = new ModelPartTransform(this.leftArm);
		this.rightShin = new ModelPartTransform(this.rightLeg);
		this.leftShin = new ModelPartTransform(this.leftLeg);
		this.rightHeldItem = new ModelPartTransform(this.rightForearm);
		this.leftHeldItem = new ModelPartTransform(this.leftForearm);
		
		this.swordTrail = new SwordTrail(() -> SupporterContent.getTrailColorFor(this.entity));

		this.nameToPartMap.put("root", root);
		this.nameToPartMap.put("body", body);
		this.nameToPartMap.put("head", head);
		this.nameToPartMap.put("leftArm", leftArm);
		this.nameToPartMap.put("rightArm", rightArm);
		this.nameToPartMap.put("leftLeg", leftLeg);
		this.nameToPartMap.put("rightLeg", rightLeg);
		this.nameToPartMap.put("leftForearm", leftForearm);
        this.nameToPartMap.put("rightForearm", rightForearm);
        this.nameToPartMap.put("leftShin", leftShin);
        this.nameToPartMap.put("rightShin", rightShin);
        this.nameToPartMap.put("rightHeldItem", rightHeldItem);
        this.nameToPartMap.put("leftHeldItem", leftHeldItem);

		this.root.position.set(0F, 0F, 0F);
		this.body.position.set(0F, 12F, 0F);
		this.head.position.set(0F, -12F, 0F);
		this.rightArm.position.set(-5F, -10F, 0F);
		this.leftArm.position.set(5F, -10f, 0f);
		this.rightLeg.position.set(0F, 12.0F, 0.0F);
		this.leftLeg.position.set(0F, 12.0F, 0.0F);
		this.rightForearm.position.set(0F, 4F, 2F);
		this.leftForearm.position.set(0F, 4F, 2F);
		this.leftShin.position.set(0, 6.0F, -2.0F);
		this.rightShin.position.set(0, 6.0F, -2.0F);
	}

	@Override
	public void updateParts(float ticksPerFrame)
	{
		this.root.update(ticksPerFrame);
		this.head.update(ticksPerFrame);
		this.body.update(ticksPerFrame);
		this.rightArm.update(ticksPerFrame);
		this.leftArm.update(ticksPerFrame);
		this.rightLeg.update(ticksPerFrame);
		this.leftLeg.update(ticksPerFrame);
		this.rightForearm.update(ticksPerFrame);
		this.leftForearm.update(ticksPerFrame);
		this.rightShin.update(ticksPerFrame);
		this.leftShin.update(ticksPerFrame);
		
		this.rightHeldItem.update(ticksPerFrame);
		this.leftHeldItem.update(ticksPerFrame);
		
		this.swordTrail.update(ticksPerFrame);
	}
	
	@Override
	public E getEntity()
	{
		return this.entity;
	}

	@Override
	public IVec3fRead getRootOffset()
	{
		return ROOT_OFFSET;
	}
}

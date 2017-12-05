package net.gobbob.mobends.data;

import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.util.SmoothVector3f;

public class DataBiped extends EntityData
{
	public ModelPart head;
    public ModelPart headwear;
    public ModelPart body;
    public ModelPart rightArm;
    public ModelPart leftArm;
    public ModelPart rightLeg;
    public ModelPart leftLeg;
    public ModelPart rightForeArm;
    public ModelPart leftForeArm;
    public ModelPart rightForeLeg;
    public ModelPart leftForeLeg;
	
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
	
	public DataBiped(int entityId)
	{
		super(entityId);
	}

	@Override
	public void initModelPose()
	{
		
	}
}

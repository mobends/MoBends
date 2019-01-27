package net.gobbob.mobends.core.client.model;

import net.minecraft.client.model.ModelBase;

public class ModelPartChild extends ModelPart
{
	
	protected boolean hideLikeParent = false;
	
	public ModelPartChild(ModelBase model, boolean register, int texOffsetX, int texOffsetY)
	{
		super(model, register, texOffsetX, texOffsetY);
	}
	
	public ModelPartChild(ModelBase model, boolean register)
	{
		this(model, register, 0, 0);
	}
	
	public ModelPartChild(ModelBase model, int texOffsetX, int texOffsetY)
    {
		this(model, true, texOffsetX, texOffsetY);
    }
	
	public ModelPartChild setHideLikeParent(boolean flag)
	{
		this.hideLikeParent = flag;
		return this;
	}
	
	@Override
    public void applyStandaloneTransform(float scale)
    {
		if(this.parent != null)
    		this.parent.applyStandaloneTransform(scale);
		
		super.applyStandaloneTransform(scale);
    }
	
}

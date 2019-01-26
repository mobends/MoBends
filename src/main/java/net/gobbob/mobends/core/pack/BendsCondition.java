package net.gobbob.mobends.core.pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.gobbob.mobends.core.client.model.IBendsModel;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.core.util.EnumAxis;
import net.gobbob.mobends.core.util.IVec3f;
import net.gobbob.mobends.core.util.SmoothOrientation;
import net.gobbob.mobends.core.util.SmoothVector3f;
import net.gobbob.mobends.core.util.Vec3f;

public class BendsCondition
{
	private String animationName;
	private List<BendsAction> actions;

	public BendsCondition(String animationName)
	{
		this.actions = new ArrayList<BendsAction>();
		this.animationName = animationName;
	}

	public void applyToModel(Object object, String anim, String model)
	{
		for (BendsAction action : this.actions)
		{
			if (!action.part.equalsIgnoreCase(model))
			{
				continue;
			}
			
			applyToPart(object, action);
		}
	}

	public void applyToModel(IBendsModel model)
	{
		for (BendsAction action : this.actions)
		{
			Object object = model.getPartForName(action.part);
			
			if(object != null)
			{
				applyToPart(object, action);
			}
		}
	}
	
	public void applyToPart(Object part, BendsAction action)
	{
		if (part instanceof SmoothVector3f)
		{
			SmoothVector3f vector = (SmoothVector3f) part;
			if (action.property == EnumBoxProperty.ROT)
			{
				vector.slideTo(action.axis,
							   action.getNumber(vector.getEnd(action.axis)),
							   action.smooth);
			}
		}
		else if (part instanceof IModelPart)
		{
			IModelPart box = (IModelPart) part;
			if (action.property == EnumBoxProperty.ROT ||
				action.property == EnumBoxProperty.PREROT)
			{
				SmoothOrientation rotation = box.getRotation();
				
				if (action.doesOverride())
				{
					rotation.setSmoothness(action.smooth).orient(action.axis, action.getNumber());
				}
				else
				{
					if (action.property == EnumBoxProperty.ROT)
						rotation.setSmoothness(action.smooth).rotate(action.axis, action.getNumber());
					else if (action.property == EnumBoxProperty.PREROT)
						rotation.setSmoothness(action.smooth).localRotate(action.axis, action.getNumber());
				}
			}
			else if (action.property == EnumBoxProperty.SCALE)
			{
				IVec3f scale = box.getScale();
				
				if (action.axis == null || action.axis == EnumAxis.X)
					scale.setX(action.getNumber(scale.getX()));
				if (action.axis == null || action.axis == EnumAxis.Y)
					scale.setY(action.getNumber(scale.getY()));
				if (action.axis == null || action.axis == EnumAxis.Z)
					scale.setZ(action.getNumber(scale.getZ()));
			}
		}
	}

	public void addAction(BendsAction action)
	{
		this.actions.add(action);
	}

	public String getAnimationName()
	{
		return this.animationName;
	}

	public int getActionAmount()
	{
		return this.actions.size();
	}

	public BendsAction getAction(int a)
	{
		return this.actions.get(a);
	}
	
	public List<BendsAction> getActions()
	{
		return Collections.unmodifiableList(this.actions);
	}
}

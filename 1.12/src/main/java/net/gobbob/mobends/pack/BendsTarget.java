package net.gobbob.mobends.pack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.client.model.IBendsModel;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.util.EnumAxis;
import net.gobbob.mobends.util.SmoothVector3f;

public class BendsTarget
{
	public String name;
	public HashMap<String, BendsCondition> conditions = new HashMap<String, BendsCondition>();

	public BendsTarget(String name)
	{
		this.name = name;
	}

	public void applyToModel(Object object, String anim, String model)
	{
		if (this.conditions.containsKey(anim))
			((BendsCondition) this.conditions.get(anim)).applyToModel(object, anim, model);

		if (this.conditions.containsKey("all"))
			((BendsCondition) this.conditions.get("all")).applyToModel(object, anim, model);
	}

	public void applyToModel(IBendsModel model, String anim)
	{
		if (this.conditions.containsKey(anim))
			((BendsCondition) this.conditions.get(anim)).applyToModel(model);

		if (this.conditions.containsKey("all"))
			((BendsCondition) this.conditions.get("all")).applyToModel(model);
	}
	
	public void applyToModel(IBendsModel model, Collection<String> animations)
	{
		for (Map.Entry<String, BendsCondition> entry : this.conditions.entrySet())
		{
			if (animations.contains(entry.getKey()) || entry.getKey().equalsIgnoreCase("all"))
			{
				entry.getValue().applyToModel(model);
			}
		}
	}
	
	public BendsCondition getCondition(String anim)
	{
		return (BendsCondition) this.conditions.get(anim);
	}

	public AnimatedEntity getAnimatedEntity()
	{
		return AnimatedEntity.get(name);
	}
}

package net.gobbob.mobends.pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.util.EnumAxis;
import net.gobbob.mobends.util.SmoothVector3f;

public class BendsTarget {
	public String mob;
	public HashMap<String, BendsCondition> conditions = new HashMap<String, BendsCondition>();
	public float visual_DeletePopUp;
	
	public BendsTarget(String argMob){
		this.mob = argMob;
		this.visual_DeletePopUp = 0;
	}
	
	public void applyToModel(Object object, String anim, String model){
		if(this.conditions.containsKey(anim))
			((BendsCondition)this.conditions.get(anim)).applyToModel(object, anim, model);
		
		if(this.conditions.containsKey("all"))
			((BendsCondition)this.conditions.get("all")).applyToModel(object, anim, model);
	}

	public BendsCondition getCondition(String anim) {
		return (BendsCondition) this.conditions.get(anim);
	}

	public void applyToModel(IBendsModel model, String anim) {
		if(this.conditions.containsKey(anim))
			((BendsCondition)this.conditions.get(anim)).applyToModel(model);
		
		if(this.conditions.containsKey("all"))
			((BendsCondition)this.conditions.get("all")).applyToModel(model);
	}

	public AnimatedEntity getAnimatedEntity() {
		return AnimatedEntity.get(mob);
	}
}

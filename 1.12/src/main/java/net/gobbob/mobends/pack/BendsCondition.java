package net.gobbob.mobends.pack;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.util.EnumAxis;
import net.gobbob.mobends.util.SmoothVector3f;

public class BendsCondition {
	private String animationName;
	private List<BendsAction> actions;
	
	public BendsCondition(String animationName) {
		this.actions = new ArrayList<BendsAction>();
		this.animationName = animationName;
	}

	public void applyToModel(Object object, String anim, String model){
		for(int i = 0;i < actions.size();i++){
			if(actions.get(i).model.equalsIgnoreCase(model)){
				if(object instanceof SmoothVector3f){
					SmoothVector3f vector = (SmoothVector3f) object;
					if(actions.get(i).property == EnumBoxProperty.ROT){
						vector.setSmooth(actions.get(i).axis,actions.get(i).getNumber((actions.get(i).axis == EnumAxis.X ? vector.vFinal.x : actions.get(i).axis == EnumAxis.Y ? vector.vFinal.y : vector.vFinal.z)),actions.get(i).smooth);
					}
				}else if(object instanceof ModelPart){
					ModelPart box = (ModelPart) object;
					if(actions.get(i).property == EnumBoxProperty.ROT){
						box.rotation.setSmooth(actions.get(i).axis,actions.get(i).getNumber((actions.get(i).axis == EnumAxis.X ? box.rotation.vFinal.x : actions.get(i).axis == EnumAxis.Y ? box.rotation.vFinal.y : box.rotation.vFinal.z)),actions.get(i).smooth);
					}else if(actions.get(i).property == EnumBoxProperty.PREROT){
						box.pre_rotation.setSmooth(actions.get(i).axis,actions.get(i).getNumber((actions.get(i).axis == EnumAxis.X ? box.pre_rotation.vFinal.x : actions.get(i).axis == EnumAxis.Y ? box.pre_rotation.vFinal.y : box.pre_rotation.vFinal.z)),actions.get(i).smooth);
					}else if(actions.get(i).property == EnumBoxProperty.SCALE){
						if(actions.get(i).axis == null | actions.get(i).axis == EnumAxis.X) box.scaleX = actions.get(i).getNumber(box.scaleX);
						if(actions.get(i).axis == null | actions.get(i).axis == EnumAxis.Y) box.scaleY = actions.get(i).getNumber(box.scaleY);
						if(actions.get(i).axis == null | actions.get(i).axis == EnumAxis.Z) box.scaleZ = actions.get(i).getNumber(box.scaleZ);
					}
				}
			}
		}
	}
	
	public void applyToModel(IBendsModel model) {
		for(int i = 0;i < actions.size();i++){
			Object object = model.getPartForName(actions.get(i).model);
			if(object != null){
				if(object instanceof SmoothVector3f){
					SmoothVector3f vector = (SmoothVector3f) object;
					if(actions.get(i).property == EnumBoxProperty.ROT){
						vector.setSmooth(actions.get(i).axis,actions.get(i).getNumber((actions.get(i).axis == EnumAxis.X ? vector.vFinal.x : actions.get(i).axis == EnumAxis.Y ? vector.vFinal.y : vector.vFinal.z)),actions.get(i).smooth);
					}
				}else if(object instanceof ModelPart){
					ModelPart box = (ModelPart) object;
					if(actions.get(i).property == EnumBoxProperty.ROT){
						box.rotation.setSmooth(actions.get(i).axis,actions.get(i).getNumber((actions.get(i).axis == EnumAxis.X ? box.rotation.vFinal.x : actions.get(i).axis == EnumAxis.Y ? box.rotation.vFinal.y : box.rotation.vFinal.z)),actions.get(i).smooth);
					}else if(actions.get(i).property == EnumBoxProperty.PREROT){
						box.pre_rotation.setSmooth(actions.get(i).axis,actions.get(i).getNumber((actions.get(i).axis == EnumAxis.X ? box.pre_rotation.vFinal.x : actions.get(i).axis == EnumAxis.Y ? box.pre_rotation.vFinal.y : box.pre_rotation.vFinal.z)),actions.get(i).smooth);
					}else if(actions.get(i).property == EnumBoxProperty.SCALE){
						if(actions.get(i).axis == null | actions.get(i).axis == EnumAxis.X) box.scaleX = actions.get(i).getNumber(box.scaleX);
						if(actions.get(i).axis == null | actions.get(i).axis == EnumAxis.Y) box.scaleY = actions.get(i).getNumber(box.scaleY);
						if(actions.get(i).axis == null | actions.get(i).axis == EnumAxis.Z) box.scaleZ = actions.get(i).getNumber(box.scaleZ);
					}
				}
			}
		}
	}
	
	public void addAction(BendsAction action) {
		this.actions.add(action);
	}
	
	public String getAnimationName() {
		return this.animationName;
	}

	public int getActionAmount() {
		return this.actions.size();
	}
	
	public BendsAction getAction(int a) {
		return this.actions.get(a);
	}
}

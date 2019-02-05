package net.gobbob.mobends.core.animation.controller;

import net.gobbob.mobends.core.data.EntityData;

/*
 * This interface is responsible for updating animation for each
 * instance of an AnimatedEntity.
 * 
 * It's a member of an EntityData instance, and it holds all
 * the information about the current animation's state.
 * */
public interface IAnimationController<T extends EntityData<?>>
{
	
	void perform(T entityData);
	
}

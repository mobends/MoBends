package net.gobbob.mobends.core.animation.controller;

import net.gobbob.mobends.core.EntityData;

/*
 * This class is responsible for updating animation for each
 * instance of an AnimatedEntity.
 * 
 * It's a member of an EntityData instance, and it holds all
 * the information about the current animation's state.
 * */
public abstract class Controller<T extends EntityData>
{
	public abstract void perform(T entityData);
}

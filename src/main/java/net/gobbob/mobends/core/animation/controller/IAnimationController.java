package net.gobbob.mobends.core.animation.controller;

import net.gobbob.mobends.core.data.EntityData;

import javax.annotation.Nullable;
import java.util.Collection;

/*
 * This interface is responsible for updating animation for each
 * instance of an AnimatedEntity.
 * 
 * It's a member of an EntityData instance, and it holds all
 * the information about the current animation's state.
 * */
public interface IAnimationController<T extends EntityData<?>>
{

	@Nullable
	Collection<String> perform(T entityData);
	
}

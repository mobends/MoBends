package net.gobbob.mobends.core.client.gui;

import java.util.LinkedList;
import java.util.List;

/**
 * Makes whatever implements it potentially observable, if properly implemented
 * (i.e. You can listen to changes on that object, and take action accordingly).
 * 
 * @author Iwo Plaza
 */
public interface IObservable
{
	/**
	 * These listeners will respond, when a change has occurred in this object.
	 */
	List<IChangeListener> getChangeListeners();
	
	/**
	 * Adds a listener to the list of listeners.
	 * @param listener The listener to add.
	 */
	default void addListener(IChangeListener listener)
	{
		getChangeListeners().add(listener);
	}
	
	/**
	 * This method notifies all listeners that a change 
	 */
	default void notifyChanged()
	{
		for (IChangeListener listener : getChangeListeners())
		{
			listener.handleChange(this);
		}
	}
}

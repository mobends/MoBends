package net.gobbob.mobends.client.gui;

import java.util.LinkedList;

/**
 * Makes whatever implements it potentially observable, if properly implemented
 * (i.e. You can listen to changes on that object, and take action accordingly).
 * 
 * @author Iwo Plaza
 */
public class Observable
{
	/**
	 * These listeners will respond, when a change has occured in this object.
	 */
	private LinkedList<IChangeListener> changeListeners = new LinkedList<IChangeListener>();
	
	/**
	 * Adds a listener to the list of listeners.
	 * @param listener The listener to add.
	 */
	public void addListener(IChangeListener listener)
	{
		this.changeListeners.add(listener);
	}
	
	/**
	 * This method notifies all listeners that a change 
	 */
	protected void notifyChanged()
	{
		for (IChangeListener listener : this.changeListeners)
		{
			listener.handleChange(this);
		}
	}
}

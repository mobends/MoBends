package goblinbob.mobends.core.client.gui;

/**
 * Allows the implementation to listen for changes of observable objects.
 * 
 * @author Iwo Plaza
 */
public interface IChangeListener
{
	/**
	 * Called when a change has occurred on the object that this listener
	 * was bound to.
	 * @param objectChanged The object that was changed.
	 */
	void handleChange(IObservable objectChanged);
}

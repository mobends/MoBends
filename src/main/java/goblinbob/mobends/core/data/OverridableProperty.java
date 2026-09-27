package goblinbob.mobends.core.data;

public class OverridableProperty<T>
{
	
	private T value;
	
	public OverridableProperty(T value)
	{
		this.value = value;
	}
	
	public T get()
	{
		return this.value;
	}
	
	public void set(T value)
	{
		this.value = value;
	}
}

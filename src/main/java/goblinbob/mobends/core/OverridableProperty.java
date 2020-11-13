package goblinbob.mobends.core;

public class OverridableProperty<T>
{
	private T value;
	private T override;
	
	public OverridableProperty(T value)
	{
		this.value = value;
		this.override = null;
	}
	
	public void override(T overrideValue)
	{
		this.override = overrideValue;
	}
	
	public void unsetOverride()
	{
		this.override = null;
	}
	
	public T get()
	{
		return this.override != null ? this.override : this.value;
	}
	
	public void set(T value)
	{
		this.value = value;
	}
}

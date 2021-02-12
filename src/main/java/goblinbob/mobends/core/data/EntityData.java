package goblinbob.mobends.core.data;

import goblinbob.mobends.core.exceptions.UnknownPropertyException;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityData
{
    private Map<String, IProperty> propertyMap = new HashMap<>();

    public EntityData()
    {

    }

    public void registerProperty(String key, PropertyGetter getter)
    {
        this.propertyMap.put(key, new ComputedProperty(getter));
    }

    public void setProperty(String key, Object value) throws UnknownPropertyException
    {
        IProperty property = propertyMap.get(key);
        if (property != null)
        {
            if (property instanceof StoredProperty)
            {
                ((StoredProperty) property).setValue(value);
            }
            else
            {
                throw new UnknownPropertyException(key, String.format("Tried to set a computed property '%s'", key));
            }
        }
        else
        {
            propertyMap.put(key, new StoredProperty(value));
        }
    }

    private Object getProperty(String key) throws UnknownPropertyException
    {
        IProperty property = propertyMap.get(key);
        if (property == null)
            throw new UnknownPropertyException(key, String.format("Tried to retrieve unknown entity property '%s'", key));

        return property.getValue();
    }

    public boolean doesPropertyExist(String key)
    {
        return propertyMap.containsKey(key);
    }

    public boolean getBooleanProperty(String key) throws UnknownPropertyException
    {
        return (boolean) this.getProperty(key);
    }

    public int getIntegerProperty(String key) throws UnknownPropertyException
    {
        return (int) this.getProperty(key);
    }

    public double getDoubleProperty(String key) throws UnknownPropertyException
    {
        return (double) this.getProperty(key);
    }

    public abstract void update();

    /**
     * This is a container for a singular entity attribute.
     * For example, the animations can use these as variables.
     */
    private interface IProperty
    {
        Object getValue();
    }

    /**
     * This property is computed when necessary.
     */
    private static class ComputedProperty implements IProperty
    {
        private PropertyGetter getter;

        public ComputedProperty(PropertyGetter getter)
        {
            this.getter = getter;
        }

        @Override
        public Object getValue()
        {
            return getter.getProperty();
        }
    }

    /**
     * This property's value is stored and updated.
     */
    private static class StoredProperty implements IProperty
    {
        private Object value;

        public StoredProperty(Object defaultValue)
        {
            this.value = defaultValue;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        @Override
        public Object getValue()
        {
            return this.value;
        }
    }

    @FunctionalInterface
    private interface PropertyGetter
    {
        Object getProperty();
    }
}

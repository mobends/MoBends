package net.minecraft.inventory;

public enum EntityEquipmentSlot
{
    MAINHAND("mainhand"), OFFHAND("offhand"), FEET("feet"), LEGS("legs"), CHEST("chest"), HEAD("head");

    private final String name;

    EntityEquipmentSlot(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static EntityEquipmentSlot fromString(String name)
    {
        for (EntityEquipmentSlot slot : values())
        {
            if (slot.name.equals(name)) return slot;
        }
        throw new IllegalArgumentException("Invalid slot '" + name + "'");
    }
}

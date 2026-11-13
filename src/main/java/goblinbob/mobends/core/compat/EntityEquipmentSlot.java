package goblinbob.mobends.core.compat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * The equipment slots of 1.9 and later, by the names animators use. 1.8.9 numbers them instead
 * ({@link EntityLivingBase#getEquipmentInSlot}: 0 the held item, then the feet up to the head), and has no off hand.
 */
public enum EntityEquipmentSlot
{
    MAINHAND(Type.HAND, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, "offhand"),
    FEET(Type.ARMOR, 0, "feet"),
    LEGS(Type.ARMOR, 1, "legs"),
    CHEST(Type.ARMOR, 2, "chest"),
    HEAD(Type.ARMOR, 3, "head");

    public enum Type
    {
        HAND,
        ARMOR
    }

    private final Type type;
    private final int index;
    private final String name;

    EntityEquipmentSlot(Type type, int index, String name)
    {
        this.type = type;
        this.index = index;
        this.name = name;
    }

    public Type getSlotType()
    {
        return type;
    }

    /** The index within its type: the hand (0 main, 1 off) or the armour slot (0 feet to 3 head, as in {@code armorInventory}). */
    public int getIndex()
    {
        return index;
    }

    public String getName()
    {
        return name;
    }

    /** What the entity holds or wears in this slot, or null. */
    public ItemStack getStack(EntityLivingBase entity)
    {
        if (this == OFFHAND) return null;
        return entity.getEquipmentInSlot(this == MAINHAND ? 0 : index + 1);
    }

    /** The armour slot of {@code armorInventory} index {@code index} (0 feet to 3 head). */
    public static EntityEquipmentSlot fromArmorIndex(int index)
    {
        switch (index)
        {
            case 0: return FEET;
            case 1: return LEGS;
            case 2: return CHEST;
            case 3: return HEAD;
            default: throw new IllegalArgumentException("Invalid armour slot index " + index);
        }
    }

    public static EntityEquipmentSlot fromString(String targetName)
    {
        for (EntityEquipmentSlot slot : values())
        {
            if (slot.getName().equals(targetName)) return slot;
        }
        throw new IllegalArgumentException("Invalid slot '" + targetName + "'");
    }
}

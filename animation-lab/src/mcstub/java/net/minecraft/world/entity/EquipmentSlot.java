package net.minecraft.world.entity;

/** Where an entity holds or wears an item, by the names packs write. */
public enum EquipmentSlot {
    MAINHAND("mainhand"),
    OFFHAND("offhand"),
    FEET("feet"),
    LEGS("legs"),
    CHEST("chest"),
    HEAD("head"),
    BODY("body");

    private final String name;

    EquipmentSlot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EquipmentSlot byName(String name) {
        for (EquipmentSlot slot : values()) {
            if (slot.name.equals(name)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("Invalid slot '" + name + "'");
    }
}

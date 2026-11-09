package net.minecraft.world.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

public final class ItemStack {
    public static final ItemStack EMPTY = new ItemStack(Items.AIR);

    private final Item item;

    public ItemStack(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public boolean isEmpty() {
        return item == Items.AIR;
    }

    public UseAnim getUseAnimation() {
        return item.getUseAnimation(this);
    }

    /** Whether it carries the component: here only {@link DataComponents#FOOD}, which food has. */
    public boolean has(DataComponentType<?> component) {
        return component == DataComponents.FOOD && item.isFood();
    }

    @Override
    public String toString() {
        return "1 " + item;
    }
}

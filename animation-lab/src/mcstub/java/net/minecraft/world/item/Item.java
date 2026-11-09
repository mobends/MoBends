package net.minecraft.world.item;

import net.minecraft.core.registries.BuiltInRegistries;

/** Stand-in for the game's Item; the lab's are in {@link Items}. */
public class Item {
    public static class Properties {
        boolean food;

        /** It's eaten (the game takes how nourishing it is). */
        public Properties food() {
            this.food = true;
            return this;
        }
    }

    private final boolean food;

    public Item(Properties properties) {
        this.food = properties.food;
    }

    /** Lab: what its stacks carry (the game keeps that in the stacks' components). */
    boolean isFood() {
        return food;
    }

    /** How it's used: eaten if it's food, as the game has it; its kinds of item say otherwise. */
    public UseAnim getUseAnimation(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) ? UseAnim.EAT : UseAnim.NONE;
    }

    @Override
    public String toString() {
        return BuiltInRegistries.ITEM.getKey(this).getPath();
    }
}

package net.minecraft.world.item;

public class BowItem extends Item {
    public BowItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}

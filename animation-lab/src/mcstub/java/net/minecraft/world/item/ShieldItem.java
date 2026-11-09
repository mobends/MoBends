package net.minecraft.world.item;

public class ShieldItem extends Item {
    public ShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }
}

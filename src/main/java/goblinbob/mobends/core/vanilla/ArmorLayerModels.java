package goblinbob.mobends.core.vanilla;

import net.minecraft.client.model.HumanoidModel;

/**
 * A vanilla armour layer's own two armour models ({@code HumanoidArmorLayer}, through
 * {@code HumanoidArmorLayerMixin}), which vanilla keeps private: Mo' Bends bends these, and leaves
 * a model another mod hands the layer instead as it is.
 */
public interface ArmorLayerModels {
    /** The model the leggings are drawn with. */
    HumanoidModel<?> mobends$innerModel();

    /** The model the helmet, chestplate and boots are drawn with. */
    HumanoidModel<?> mobends$outerModel();
}

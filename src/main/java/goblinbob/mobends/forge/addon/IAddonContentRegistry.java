package goblinbob.mobends.forge.addon;

import goblinbob.mobends.core.client.gui.IAnimationEditor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public interface IAddonContentRegistry
{
    <T extends LivingEntity> void registerEntity(EntityType<T> entityType, ResourceLocation instructionsPath, ResourceLocation animatorPath);

    void registerAnimationEditor(IAnimationEditor editor);
}

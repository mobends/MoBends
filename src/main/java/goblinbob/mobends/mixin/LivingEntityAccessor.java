package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/^*
 * Where 26.x keeps a living entity's arm swing, so a preview of the Mo' Bends menu, which nothing else
 * ticks, carries its swing through (the entity's own base tick would also burn, drown and hurt it).
 ^/
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("swingState")
    LivingEntity.SwingState mobends$getSwingState();
}
*///?}

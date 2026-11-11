package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/^*
 * The adult and baby models of a renderer that draws babies with a model of their own (26.x), which
 * it picks only as it submits the entity ({@code LivingEntityRendererMixin}).
 ^/
@Mixin(AgeableMobRenderer.class)
public interface AgeableMobRendererAccessor {
    @Accessor("adultModel")
    EntityModel<?> mobends$adultModel();

    @Accessor("babyModel")
    EntityModel<?> mobends$babyModel();
}
*///?}

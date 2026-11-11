package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/^*
 * What the GUI draws this frame, to add what its own calls can't draw to it: the Mo' Bends menu's
 * slanted quads ({@code GuiQuads}). Named in full: the GUI's class is GuiGraphics in this source.
 ^/
@Mixin(net.minecraft.client.gui.GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
    @Accessor("guiRenderState")
    GuiRenderState mobends$getGuiRenderState();
}
*///?}

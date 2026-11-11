package goblinbob.mobends.mixin;

//? if >=26.1 && fabric {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/^*
 * Draws each entity the GUI shows in a frame into a texture of its own. 26.x draws a GUI entity into
 * a texture later in the frame, and has one for them all: drawn into one after another, every entity
 * would show the last one. The first still has vanilla's; the rest have Mo' Bends', made as needed
 * (the Settings window's mobs, one a row). NeoForge's GUI keeps renderers enough for them already.
 ^/
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow
    @Final
    private GuiRenderState renderState;
    @Shadow
    @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Unique
    private final List<GuiEntityRenderer> mobends$entityRenderers = new ArrayList<>();
    /^* How many entities have been drawn this frame. ^/
    @Unique
    private int mobends$entitiesDrawn;

    @Inject(method = "preparePictureInPicture", at = @At("HEAD"))
    private void mobends$startFrame(CallbackInfo ci) {
        mobends$entitiesDrawn = 0;
    }

    @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
    private void mobends$prepareEntity(PictureInPictureRenderState state, int guiScale, CallbackInfo ci) {
        if (!(state instanceof GuiEntityRenderState entityState) || mobends$entitiesDrawn++ == 0) {
            return;
        }
        int index = mobends$entitiesDrawn - 2;
        if (index == mobends$entityRenderers.size()) {
            mobends$entityRenderers.add(new GuiEntityRenderer(Minecraft.getInstance().getEntityRenderDispatcher()));
        }
        mobends$entityRenderers.get(index).prepare(entityState, renderState, featureRenderDispatcher, guiScale);
        ci.cancel();
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void mobends$close(CallbackInfo ci) {
        mobends$entityRenderers.forEach(GuiEntityRenderer::close);
        mobends$entityRenderers.clear();
    }
}
*///?}

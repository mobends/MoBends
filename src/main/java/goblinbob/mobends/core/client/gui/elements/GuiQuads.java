package goblinbob.mobends.core.client.gui.elements;

//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import goblinbob.mobends.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import java.util.Arrays;
*///?} else {
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.VanillaDraw;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
//?}
import net.minecraft.client.gui.GuiGraphics;

/**
 * Quads of any shape, in solid colours, on the GUI: what the GUI's own calls only draw as rectangles
 * (the Mo' Bends menu's slanted sections). Four {@link #vertex vertices} a quad, placed by the pose
 * at the start, then {@link #draw}.
 * <p>
 * Clipped to a rectangle given here: 26.x's GUI extracts what's drawn, each element taking its clip
 * along, and the clip the GUI's own calls take (the scissor) can't be read from outside. Earlier
 * versions draw at once, in the scissor the caller sets.
 */
public final class GuiQuads {
    private final GuiGraphics graphics;
    //? if >=26.1 {
    /*private final Matrix3x2f pose;
    @Nullable
    private final ScreenRectangle clip;
    private float[] positions = new float[2 * 8];
    private int[] colors = new int[8];
    private int count;
    *///?} else {
    private final VertexConsumer buffer;
    private final Matrix4f pose;
    //?}

    /** Clipped to the rectangle from ({@code left}, {@code top}) to ({@code right}, {@code bottom}), as the pose places it. */
    public GuiQuads(GuiGraphics graphics, int left, int top, int right, int bottom) {
        this.graphics = graphics;
        //? if >=26.1 {
        /*this.pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle rectangle = new ScreenRectangle(left, top, right - left, bottom - top).transformAxisAligned(pose);
        this.clip = rectangle.intersection(new ScreenRectangle(0, 0, graphics.guiWidth(), graphics.guiHeight()));
        *///?} else {
        this.buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        this.pose = graphics.pose().last().pose();
        //?}
    }

    public void vertex(float x, float y, float red, float green, float blue, float alpha) {
        //? if >=26.1 {
        /*if (count == colors.length) {
            positions = Arrays.copyOf(positions, 2 * positions.length);
            colors = Arrays.copyOf(colors, 2 * colors.length);
        }
        positions[2 * count] = x;
        positions[2 * count + 1] = y;
        colors[count] = channel(alpha) << 24 | channel(red) << 16 | channel(green) << 8 | channel(blue);
        count++;
        *///?} else {
        VanillaDraw.vertex(buffer, pose, x, y, 0, red, green, blue, alpha);
        //?}
    }

    public void draw() {
        //? if >=26.1 {
        /*int quads = count / 4;
        if (quads > 0 && clip != null) {
            Quads state = Quads.of(pose, Arrays.copyOf(positions, 8 * quads), Arrays.copyOf(colors, 4 * quads), clip);
            if (state.bounds() != null) {
                ((GuiGraphicsExtractorAccessor) graphics).mobends$getGuiRenderState().addGuiElement(state);
            }
        }
        count = 0;
        *///?} else {
        graphics.flush();
        //?}
    }

    //? if >=26.1 {
    /*private static int channel(float value) {
        return Math.round(Math.max(0F, Math.min(1F, value)) * 255F);
    }

    /^* The quads as 26.x's GUI draws them, later in the frame. ^/
    private record Quads(Matrix3x2fc pose, float[] positions, int[] colors, ScreenRectangle scissorArea,
                         @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
        /^* With their bounds: where they reach, in the clip (null if they're out of it). ^/
        static Quads of(Matrix3x2fc pose, float[] positions, int[] colors, ScreenRectangle clip) {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            Vector2f point = new Vector2f();
            for (int i = 0; i < positions.length; i += 2) {
                pose.transformPosition(positions[i], positions[i + 1], point);
                minX = Math.min(minX, point.x);
                minY = Math.min(minY, point.y);
                maxX = Math.max(maxX, point.x);
                maxY = Math.max(maxY, point.y);
            }
            int left = (int) Math.floor(minX);
            int top = (int) Math.floor(minY);
            ScreenRectangle reach = new ScreenRectangle(left, top, (int) Math.ceil(maxX) - left, (int) Math.ceil(maxY) - top);
            return new Quads(pose, positions, colors, clip, clip.intersection(reach));
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (int i = 0; i < colors.length; i++) {
                consumer.addVertexWith2DPose(pose, positions[2 * i], positions[2 * i + 1]).setColor(colors[i]);
            }
        }

        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }
    }
    *///?}
}

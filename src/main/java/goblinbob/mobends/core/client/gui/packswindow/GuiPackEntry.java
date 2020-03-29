package goblinbob.mobends.core.client.gui.packswindow;

import goblinbob.mobends.core.client.gui.IGuiDraggable;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class GuiPackEntry implements IGuiListElement, IGuiDraggable
{

    private static final int HEIGHT = 31;

    protected final FontRenderer fontRenderer;
    protected String name;
    protected String author;
    protected String description;
    private String displayName;
    private ResourceLocation thumbnailLocation;

    private int x;
    private int y;
    private int smoothX;
    private int smoothY;
    private int dragX;
    private int dragY;
    private int dragPivotX;
    private int dragPivotY;
    private int order;
    private GuiPackList parentList;
    private boolean firstInit = false;
    private boolean hover;
    private boolean selected;
    private boolean dragged;

    public GuiPackEntry(IBendsPack pack)
    {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;

        this.name = pack.getKey();
        this.displayName = pack.getDisplayName();
        this.author = pack.getAuthor();
        this.description = pack.getDescription();
        this.thumbnailLocation = pack.getThumbnail();

        this.dragX = this.dragY = 0;
        this.dragPivotX = this.dragPivotY = 0;
        this.hover = false;
        this.selected = false;
    }

    @Override
    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;

        if (!firstInit)
        {
            firstInit = true;
            smoothX = x;
            smoothY = y;
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        this.hover = mouseX >= x && mouseX <= x + 102 &&
                     mouseY >= y && mouseY <= y + HEIGHT;

        this.smoothX += (this.x - this.smoothX) * 0.7F;
        this.smoothY += (this.y - this.smoothY) * 0.7F;
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int state)
    {
        this.update(mouseX, mouseY);
        this.dragPivotX = mouseX - this.x;
        this.dragPivotY = mouseY - this.y;
        return this.hover;
    }

    @Override
    public void dragTo(int x, int y)
    {
        this.dragX = x;
        this.dragY = y;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public void setDragged(boolean dragged)
    {
        this.dragged = dragged;
    }

    @Override
    public void setOrder(int order)
    {
        this.order = order;
    }

    public void setParentList(GuiPackList parentList)
    {
        this.parentList = parentList;
    }

    @Override
    public boolean isDragged()
    {
        return dragged;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    public int getDragX()
    {
        return dragX;
    }

    public int getDragY()
    {
        return dragY;
    }

    public int getDragPivotX()
    {
        return dragPivotX;
    }

    public int getDragPivotY()
    {
        return dragPivotY;
    }

    @Override
    public int getOrder()
    {
        return order;
    }

    public GuiPackList getParentList()
    {
        return parentList;
    }

    @Override
    public int getHeight()
    {
        return HEIGHT;
    }

    @Override
    public void draw(float partialTicks)
    {
        final int viewX = dragged ? dragX - dragPivotX : smoothX;
        final int viewY = dragged ? dragY - dragPivotY : smoothY;

        final Minecraft mc = Minecraft.getMinecraft();
        final TextureManager textureManager = mc.getTextureManager();

        textureManager.bindTexture(GuiPacksWindow.BACKGROUND_TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        final int SELECTED_TEXTURE_Y = 62;
        final int HOVER_TEXTURE_Y = 31;
        final int NEUTRAL_TEXTURE_Y = 0;
        final int textureY = selected ? SELECTED_TEXTURE_Y : hover ? HOVER_TEXTURE_Y : NEUTRAL_TEXTURE_Y;
        Draw.texturedModalRect(viewX - 1, viewY - (selected ? 1 : 0), 0, textureY, 102, HEIGHT);

        textureManager.bindTexture(thumbnailLocation);
        GlStateManager.color(1, 1, 1, 1);
        Draw.texturedRectangle(viewX + 2, viewY + 2, 25, 25, 0, 0, 25F / 32F, 25F / 32F);

        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(this.displayName, 70), viewX + 32, viewY + 1, 0xffffff);
        Draw.rectangleHorizontalGradient(viewX + 101 - 40, viewY + 1, 39, 9, 0x004e4e4e, 0xff4e4e4e);
    }

}
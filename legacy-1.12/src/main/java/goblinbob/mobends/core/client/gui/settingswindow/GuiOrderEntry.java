package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/** An entry of an order list: its id, a description, and buttons to move it up or down. */
public class GuiOrderEntry implements IGuiListElement
{

    private static final int BUTTON_SIZE = 20;

    private final String id;
    private final String description;
    private final int position;
    private final Runnable onMoveUp;
    private final Runnable onMoveDown;
    private final Minecraft mc;
    private final GuiCustomButton upButton;
    private final GuiCustomButton downButton;

    private int x, y;
    private int mouseX, mouseY;
    private int listOrder;

    public GuiOrderEntry(String id, String description, int position, int count, Runnable onMoveUp, Runnable onMoveDown)
    {
        this.id = id;
        this.description = description;
        this.position = position;
        this.onMoveUp = onMoveUp;
        this.onMoveDown = onMoveDown;
        this.mc = Minecraft.getMinecraft();
        this.upButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, "▲");
        this.downButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, "▼");
        this.upButton.enabled = position > 0;
        this.downButton.enabled = position < count - 1;
    }

    @Override
    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.downButton.setPosition(x + 232, y + 5);
        this.upButton.setPosition(x + 232 - BUTTON_SIZE - 2, y + 5);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int state)
    {
        if (state != 0)
            return false;

        if (upButton.mousePressed(mouseX, mouseY))
        {
            onMoveUp.run();
            return true;
        }
        if (downButton.mousePressed(mouseX, mouseY))
        {
            onMoveDown.run();
            return true;
        }
        return false;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public void draw(float partialTicks)
    {
        GlStateManager.color(1F, 1F, 1F);

        mc.fontRenderer.drawStringWithShadow((position + 1) + ".", this.x + 4, this.y + 6, 0xaaaaaa);
        mc.fontRenderer.drawStringWithShadow(mc.fontRenderer.trimStringToWidth(id, 170), this.x + 20, this.y + 6, 0xffffff);
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(description, 170), this.x + 20, this.y + 18, 0x9a9a9a);

        upButton.drawButton(mouseX, mouseY, partialTicks);
        downButton.drawButton(mouseX, mouseY, partialTicks);
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

    @Override
    public int getHeight()
    {
        return 30;
    }

    @Override
    public int getOrder()
    {
        return listOrder;
    }

    @Override
    public void setOrder(int order)
    {
        listOrder = order;
    }

}

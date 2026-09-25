package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.client.gui.elements.GuiSmallToggleButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiBenderSettings implements IGuiListElement
{

    private final EntityBender<?> bender;
    private final Minecraft mc;
    private final GuiSmallToggleButton toggleButton;
    /** Opens the order of the entity's types; only there when more than one can animate it. */
    private final GuiCustomButton orderButton;
    private final Runnable onOpenOrder;

    private int x, y;
    private int mouseX, mouseY;
    private int listOrder;

    public GuiBenderSettings(EntityBender<?> bender, int typeCount, Runnable onOpenOrder)
    {
        this.bender = bender;
        this.mc = Minecraft.getMinecraft();
        this.toggleButton = new GuiSmallToggleButton();
        this.toggleButton.setToggleState(bender.isAnimated());
        this.orderButton = typeCount > 1 ? new GuiCustomButton(70, 20, I18n.format("mobends.gui.typeorder.button", typeCount)) : null;
        this.onOpenOrder = onOpenOrder;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.toggleButton.initGui(x + 4, y + 4);
        if (orderButton != null)
            this.orderButton.setPosition(x + 184, y + 4);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (toggleButton.mouseClicked(mouseX, mouseY, mouseButton))
        {
            bender.setAnimate(toggleButton.getToggleState());
            return true;
        }

        if (orderButton != null && mouseButton == 0 && orderButton.mousePressed(mouseX, mouseY))
        {
            onOpenOrder.run();
            return true;
        }

        return false;
    }

    public void update(int mouseX, int mouseY)
    {
        toggleButton.update(mouseX, mouseY);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void draw(float partialTicks)
    {
        GlStateManager.color(1F, 1F, 1F);

//        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSettingsWindow.BACKGROUND_TEXTURE);
//
//        // Container
//        Draw.borderBox(x + 4, y + 4, 100, getHeight(), 4, 36, 126);

        mc.fontRenderer.drawStringWithShadow(mc.fontRenderer.trimStringToWidth(bender.getLocalizedName(), 140), this.x + 38, this.y + 10, 0xffffff);

        toggleButton.draw();
        if (orderButton != null)
            orderButton.drawButton(mouseX, mouseY, partialTicks);
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
        return 20;
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

package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.client.gui.packswindow.GuiPacksWindow;
import goblinbob.mobends.core.types.EntityType;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The types that can animate one kind of entity, in precedence order. When several apply to an
 * entity, the highest one wins; moving a type ranks the whole list (like resource packs), and
 * Reset puts every rank back to 0.
 */
public class GuiTypeOrderWindow extends GuiScreen
{

    private static final int COMPONENT_BUTTON_BACK = 0;
    private static final int COMPONENT_BUTTON_RESET = 1;

    private final EntityBender<?> bender;
    private final TypeList typeList = new TypeList(0, 0, GuiSettingsWindow.EDITOR_WIDTH - 10, GuiSettingsWindow.EDITOR_HEIGHT - 10 - 20);
    private List<EntityType> types;

    private int x, y;

    private static class TypeList extends GuiList<GuiTypeOrderEntry>
    {
        private final LinkedList<GuiTypeOrderEntry> elements = new LinkedList<>();

        TypeList(int x, int y, int width, int height)
        {
            super(x, y, width, height, 5, 3, 3, 15);
        }

        @Override
        protected void drawBackground(float partialTicks)
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPacksWindow.BACKGROUND_TEXTURE);
            Draw.borderBox(0, 0, this.width, this.height, 4, 36, 117);
        }

        @Override
        public LinkedList<GuiTypeOrderEntry> getListElements()
        {
            return elements;
        }

        @Override
        protected int getScrollSpeed()
        {
            return 20;
        }
    }

    public GuiTypeOrderWindow(EntityBender<?> bender)
    {
        this.bender = bender;
        fetchTypes();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.x = (this.width - GuiSettingsWindow.EDITOR_WIDTH) / 2;
        this.y = (this.height - GuiSettingsWindow.EDITOR_HEIGHT) / 2;

        buttonList.clear();
        buttonList.add(new GuiButton(COMPONENT_BUTTON_BACK, 10, height - 30, 60, 20, I18n.format("mobends.gui.back")));
        buttonList.add(new GuiButton(COMPONENT_BUTTON_RESET, width - 70, height - 30, 60, 20, I18n.format("mobends.gui.reset")));
        typeList.initGui(this.x + 9, this.y + 9 + 20);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        mc.getTextureManager().bindTexture(GuiSettingsWindow.BACKGROUND_TEXTURE);
        // Container
        Draw.borderBox(x + 4, y + 4, GuiSettingsWindow.EDITOR_WIDTH, GuiSettingsWindow.EDITOR_HEIGHT, 4, 36, 126);
        // Title background
        Draw.texturedModalRect(x, y - 13, 101, 0, 4, 16);
        Draw.texturedModalRect(x + 4, y - 13, GuiSettingsWindow.EDITOR_WIDTH - 16, 16, 105, 0, 1, 16);
        Draw.texturedModalRect(x + GuiSettingsWindow.EDITOR_WIDTH - 17, y - 13, 106, 0, 19, 16);

        typeList.draw(DataUpdateHandler.partialTicks);

        fontRenderer.drawStringWithShadow(I18n.format("mobends.gui.typeorder.title", bender.getLocalizedName()), this.x + 6, this.y - 9, 0xffffff);
        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(I18n.format("mobends.gui.typeorder.hint"), GuiSettingsWindow.EDITOR_WIDTH - 12), this.x + 9, this.y + 13, 0xcccccc);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        final int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        final int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        typeList.update(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        typeList.handleMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseReleased(mouseX, mouseY, mouseButton);

        typeList.handleMouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        typeList.handleMouseInput();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (keyCode == 1)
        {
            Core.saveConfiguration();
            GuiHelper.closeGui();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        switch (button.id)
        {
            case COMPONENT_BUTTON_BACK:
                Core.saveConfiguration();
                this.mc.displayGuiScreen(new GuiSettingsWindow());
                break;
            case COMPONENT_BUTTON_RESET:
                EntityTypeRegistry.INSTANCE.resetRanks(types);
                fetchTypes();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void move(int index, int offset)
    {
        int target = index + offset;
        if (target < 0 || target >= types.size())
            return;

        types.add(target, types.remove(index));
        EntityTypeRegistry.INSTANCE.setOrder(types);
        fetchTypes();
    }

    private void fetchTypes()
    {
        types = EntityTypeRegistry.INSTANCE.getTypesFor(bender);
        typeList.clearElements();
        for (int i = 0; i < types.size(); i++)
        {
            final int index = i;
            typeList.addElement(new GuiTypeOrderEntry(types.get(i), i, types.size(), () -> move(index, -1), () -> move(index, 1)));
        }
    }

}

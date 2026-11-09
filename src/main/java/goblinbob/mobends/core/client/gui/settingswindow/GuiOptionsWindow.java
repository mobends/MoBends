package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.kumo.option.KumoOption;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.types.EntityType;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.types.Extension;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The options of one kind of entity: those of its model definition, of the types that can animate
 * it and of their extensions. A change applies at once; Reset puts every one back to its default.
 */
public class GuiOptionsWindow extends GuiScreen
{

    /** An option, with where it comes from. */
    public static final class Entry
    {
        public final KumoOption option;
        public final String source;

        Entry(KumoOption option, String source)
        {
            this.option = option;
            this.source = source;
        }
    }

    /** The options of the entities {@code bender} animates: its model definition's, then its types' and their extensions', in precedence order. */
    public static List<Entry> optionsOf(EntityBender<?> bender)
    {
        List<Entry> entries = new ArrayList<>();
        add(entries, bender.getOptions(), I18n.format("mobends.gui.options.model"));
        for (EntityType type : EntityTypeRegistry.INSTANCE.getTypesFor(bender))
        {
            add(entries, type.getOptions(), I18n.format("mobends.gui.options.type", type.getId()));
        }
        for (Extension extension : EntityTypeRegistry.INSTANCE.getExtensionsFor(bender))
        {
            add(entries, extension.getOptions(), I18n.format("mobends.gui.options.extension", extension.getId()));
        }
        return entries;
    }

    private static void add(List<Entry> entries, OptionSet options, String source)
    {
        for (KumoOption option : options.all())
        {
            entries.add(new Entry(option, source));
        }
    }

    private static final int COMPONENT_BUTTON_BACK = 0;
    private static final int COMPONENT_BUTTON_RESET = 1;

    private final EntityBender<?> bender;
    /** Where leaving the Mo' Bends menus goes: the game (null), or the Mods list it was opened from. */
    @Nullable
    private final GuiScreen exit;
    private final List<Entry> entries;
    private final EntryList entryList = new EntryList(0, 0, GuiSettingsWindow.EDITOR_WIDTH - 10, GuiSettingsWindow.EDITOR_HEIGHT - 10);

    private int x, y;

    private static class EntryList extends GuiList<GuiOptionEntry>
    {
        private final LinkedList<GuiOptionEntry> elements = new LinkedList<>();

        EntryList(int x, int y, int width, int height)
        {
            super(x, y, width, height, 5, 3, 3, 15);
        }

        @Override
        protected void drawBackground(float partialTicks)
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBenderList.LIST_TEXTURE);
            Draw.borderBox(0, 0, this.width, this.height, 4, 36, 117);
        }

        @Override
        public LinkedList<GuiOptionEntry> getListElements()
        {
            return elements;
        }
    }

    public GuiOptionsWindow(EntityBender<?> bender, @Nullable GuiScreen exit)
    {
        this.bender = bender;
        this.exit = exit;
        this.entries = optionsOf(bender);
        for (Entry entry : entries)
        {
            entryList.addElement(new GuiOptionEntry(entry.option, entry.source));
        }
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
        entryList.initGui(this.x + 9, this.y + 9);
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

        entryList.draw(DataUpdateHandler.partialTicks);

        String title = I18n.format("mobends.gui.options.title", bender.getLocalizedName());
        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(title, GuiSettingsWindow.EDITOR_WIDTH - 24), this.x + 6, this.y - 9, 0xffffff);
        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(I18n.format("mobends.gui.options.hint"), GuiSettingsWindow.EDITOR_WIDTH - 12),
                this.x + 9, this.y + GuiSettingsWindow.EDITOR_HEIGHT + 12, 0xcccccc);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        final int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        final int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        entryList.update(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        entryList.handleMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseReleased(mouseX, mouseY, mouseButton);

        entryList.handleMouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        entryList.handleMouseInput();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (keyCode == 1)
        {
            Core.saveConfiguration();
            this.mc.displayGuiScreen(exit);
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
                this.mc.displayGuiScreen(new GuiSettingsWindow(exit));
                break;
            case COMPONENT_BUTTON_RESET:
                for (Entry entry : entries)
                {
                    entry.option.reset();
                }
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
}

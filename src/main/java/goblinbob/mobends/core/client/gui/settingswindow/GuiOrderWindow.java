package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.types.EntityType;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.types.Extension;
import goblinbob.mobends.core.types.TypeOrder;
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
 * A list the user orders like resource packs: the types that can animate one kind of entity (when
 * several apply, the highest one wins), or their extensions (the highest one goes on top). Moving
 * an entry ranks the whole list, and Reset puts every rank back to 0.
 */
public class GuiOrderWindow<T extends TypeOrder.Ranked> extends GuiScreen
{

    /** What is being ordered. */
    public interface Source<T>
    {
        String getTitle();

        String getHint();

        /** The entries, in their current order. */
        List<T> fetch();

        String describe(T entry);

        /** Ranks the entries in this order, the first one highest. */
        void setOrder(List<T> order);

        void reset(List<T> entries);
    }

    public static GuiOrderWindow<EntityType> forTypes(EntityBender<?> bender)
    {
        return new GuiOrderWindow<>(new Source<EntityType>()
        {
            @Override public String getTitle() { return I18n.format("mobends.gui.typeorder.title", bender.getLocalizedName()); }
            @Override public String getHint() { return I18n.format("mobends.gui.typeorder.hint"); }
            @Override public List<EntityType> fetch() { return EntityTypeRegistry.INSTANCE.getTypesFor(bender); }
            @Override public void setOrder(List<EntityType> order) { EntityTypeRegistry.INSTANCE.setOrder(order); }
            @Override public void reset(List<EntityType> entries) { EntityTypeRegistry.INSTANCE.resetRanks(entries); }

            @Override
            public String describe(EntityType type)
            {
                String conditions = I18n.format("mobends.gui.typeorder.conditions", type.getSpecificity());
                return type.getSource() + " · " + conditions + describeRank(type);
            }
        });
    }

    public static GuiOrderWindow<Extension> forExtensions(EntityBender<?> bender)
    {
        return new GuiOrderWindow<>(new Source<Extension>()
        {
            @Override public String getTitle() { return I18n.format("mobends.gui.extensionorder.title", bender.getLocalizedName()); }
            @Override public String getHint() { return I18n.format("mobends.gui.extensionorder.hint"); }
            @Override public List<Extension> fetch() { return EntityTypeRegistry.INSTANCE.getExtensionsFor(bender); }
            @Override public void setOrder(List<Extension> order) { EntityTypeRegistry.INSTANCE.setExtensionOrder(order); }
            @Override public void reset(List<Extension> entries) { EntityTypeRegistry.INSTANCE.resetExtensionRanks(entries); }

            @Override
            public String describe(Extension extension)
            {
                String extended = I18n.format("mobends.gui.extensionorder.extends", extension.getTypeId());
                return extension.getSource() + " · " + extended + describeRank(extension);
            }
        });
    }

    private static String describeRank(TypeOrder.Ranked entry)
    {
        return entry.getRank() != 0 ? " · " + I18n.format("mobends.gui.typeorder.rank", entry.getRank()) : "";
    }

    private static final int COMPONENT_BUTTON_BACK = 0;
    private static final int COMPONENT_BUTTON_RESET = 1;

    private final Source<T> source;
    private final EntryList entryList = new EntryList(0, 0, GuiSettingsWindow.EDITOR_WIDTH - 10, GuiSettingsWindow.EDITOR_HEIGHT - 10 - 20);
    private List<T> entries;

    private int x, y;

    private static class EntryList extends GuiList<GuiOrderEntry>
    {
        private final LinkedList<GuiOrderEntry> elements = new LinkedList<>();

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
        public LinkedList<GuiOrderEntry> getListElements()
        {
            return elements;
        }

        @Override
        protected int getScrollSpeed()
        {
            return 20;
        }
    }

    public GuiOrderWindow(Source<T> source)
    {
        this.source = source;
        fetchEntries();
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
        entryList.initGui(this.x + 9, this.y + 9 + 20);
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

        fontRenderer.drawStringWithShadow(source.getTitle(), this.x + 6, this.y - 9, 0xffffff);
        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(source.getHint(), GuiSettingsWindow.EDITOR_WIDTH - 12), this.x + 9, this.y + 13, 0xcccccc);

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
                source.reset(entries);
                fetchEntries();
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
        if (target < 0 || target >= entries.size())
            return;

        entries.add(target, entries.remove(index));
        source.setOrder(entries);
        fetchEntries();
    }

    private void fetchEntries()
    {
        entries = source.fetch();
        entryList.clearElements();
        for (int i = 0; i < entries.size(); i++)
        {
            final int index = i;
            T entry = entries.get(i);
            entryList.addElement(new GuiOrderEntry(entry.getId(), source.describe(entry), i, entries.size(), () -> move(index, -1), () -> move(index, 1)));
        }
    }

}

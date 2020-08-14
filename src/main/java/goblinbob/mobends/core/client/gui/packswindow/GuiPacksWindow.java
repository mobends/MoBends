package goblinbob.mobends.core.client.gui.packswindow;

import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.pack.InvalidPackFormatException;
import goblinbob.mobends.core.pack.PackManager;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.core.util.Timer;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiPacksWindow extends GuiScreen
{

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ModStatics.MODID,
            "textures/gui/pack_window.png");
    public static final int EDITOR_WIDTH = 280;
    public static final int EDITOR_HEIGHT = 177;
    private static final int BUTTON_BACK = 0;

    private int x;
    private int y;

    private final GuiTabNavigation tabNavigation;
    private final GuiPackTab localPacksTab;
    private final GuiPackTab publicPacksTab;

    private GuiLocalPacks localPacks;

    private Timer timer;

    public GuiPacksWindow()
    {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;

        this.localPacks = new GuiLocalPacks();
        this.tabNavigation = new GuiTabNavigation();
        this.localPacksTab = this.tabNavigation.addTab("mobends.gui.localpacks", 0);
        this.publicPacksTab = this.tabNavigation.addTab("mobends.gui.publicpacks", 1);
        this.tabNavigation.selectTab(0);

        this.timer = new Timer();

        // Initializing local packs from disk.
        try
        {
            PackManager.INSTANCE.initLocalPacks();
        }
        catch (InvalidPackFormatException e)
        {
            // Some of the packs were in an invalid format.
            e.printStackTrace();
            ErrorReporter.showErrorToPlayer(e);
        }
    }

    @Override
    public void onGuiClosed()
    {
        this.localPacks.dispose();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.x = (this.width - EDITOR_WIDTH) / 2;
        this.y = (this.height - EDITOR_HEIGHT) / 2;

        this.tabNavigation.initGui(this.x + 5, this.y);

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(BUTTON_BACK, 10, height - 30, 60, 20, I18n.format("mobends.gui.back")));
        this.localPacks.initGui(this.x, this.y);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        this.localPacks.update(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, button);

        this.tabNavigation.mouseClicked(mouseX, mouseY, button);
        this.localPacks.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        this.localPacks.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        this.localPacks.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        float delta = this.timer.tick();

        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        // Container
        Draw.borderBox(x + 4, y + 4, EDITOR_WIDTH, EDITOR_HEIGHT, 4, 36, 126);
        // Title background
        Draw.texturedModalRect(x, y - 13, 101, 0, 4, 16);
        Draw.texturedModalRect(x + 4, y - 13, EDITOR_WIDTH - 16, 16, 105, 0, 1, 16);
        Draw.texturedModalRect(x + EDITOR_WIDTH - 17, y - 13, 106, 0, 19, 16);

        this.tabNavigation.draw(mouseX, mouseY);
        if (this.tabNavigation.getSelectedTab() == this.localPacksTab)
        {
            this.localPacks.draw(partialTicks);
        }
        else if (this.tabNavigation.getSelectedTab() == this.publicPacksTab)
        {
            fontRenderer.drawStringWithShadow("Coming soon...", x + EDITOR_WIDTH / 2 - fontRenderer.getStringWidth("Coming soon...") / 2, y + EDITOR_HEIGHT / 2 - 10,
                    0xffffff);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {

        switch (keyCode)
        {
            case 1:
                GuiHelper.closeGui();
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        switch (button.id)
        {
            case BUTTON_BACK:
                goBack();
                break;
        }
    }

    private void goBack()
    {
        this.mc.displayGuiScreen(new GuiBendsMenu());
    }

}
package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GuiHelper;
import net.gobbob.mobends.core.util.Timer;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
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

    private int x, y;

    private final GuiTabNavigation tabNavigation;
    private final GuiPackTab localPacksTab;
    private final GuiPackTab publicPacksTab;

    private GuiLocalPacks localPacks;

    private float publicJumbotronTransition = 1F;
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
        this.buttonList.add(new GuiButton(BUTTON_BACK, 10, height - 30, 60, 20, "Back"));
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

        if (this.publicJumbotronTransition > 0)
            this.publicJumbotronTransition = this.publicJumbotronTransition - delta * 0.001F;
        if (this.publicJumbotronTransition < 0)
            this.publicJumbotronTransition = 0;

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

        super.drawScreen(mouseX, mouseY, partialTicks);

//        GuiPackEntry entry = packList.getSelectedEntry();
//        if (true)
//        {
//            if (entry != null)
//            {
//                String packName = entry.getDisplayName();
//                String packAuthor = "by " + entry.author;
//                fontRenderer.drawStringWithShadow(packName, x + 184 - fontRenderer.getStringWidth(packName) / 2, y + 7,
//                        0xffffff);
//                fontRenderer.drawString(packAuthor, x + 184 - fontRenderer.getStringWidth(packAuthor) / 2, y + 17,
//                        0x444444);
//                int yOffset = y + 29;
//                for (String line : GUtil.squashText(fontRenderer, entry.description, 130))
//                {
//                    fontRenderer.drawStringWithShadow(line, x + 120, yOffset, 0xffffff);
//                    yOffset += 12;
//                }
//            }
//            else
//            {
//                GlStateManager.color(1, 1, 1, 1);
//                Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
//                Draw.texturedModalRect(x + EDITOR_WIDTH / 2 - 4, y + 6, 0, 195, 130, 61);
//
//                float alpha = GUtil.clamp(this.publicJumbotronTransition * 2, 0, 1);
//                int color = 0x00ffffff;
//                color |= (((int) (alpha * 255) & 255) << 24);
//                Draw.rectangle(x + EDITOR_WIDTH / 2 - 4, y + 6, 130, 61, color);
//
//                int yOffset = y + 77;
//                for (String line : GUtil.squashText(fontRenderer, publicInfoText, 130))
//                {
//                    fontRenderer.drawString(line, x + 121, yOffset, 0x444444);
//                    yOffset += 12;
//                }
//                fontRenderer.drawStringWithShadow("gobbobminecraft@gmail.com", x + 118, y + 110, 0xffffff);
//            }
//        }
//        else
//        {
//            this.openFolderButton.drawButton(mouseX, mouseY, partialTicks);
//            if (entry != null)
//            {
//                String text = entry.getDisplayName();
//                fontRenderer.drawStringWithShadow(text, x + 184 - fontRenderer.getStringWidth(text) / 2, y + 7,
//                        0xffffff);
//                int yOffset = y + 27;
//                for (String line : GUtil.squashText(fontRenderer, entry.description, 80))
//                {
//                    fontRenderer.drawStringWithShadow(line, x + 120, yOffset, 0xffffff);
//                    yOffset += 12;
//                }
//            }
//            else
//            {
//                String text = I18n.format("mobends.gui.selectpack");
//                fontRenderer.drawStringWithShadow(text, x + 184 - fontRenderer.getStringWidth(text) / 2,
//                        y + EDITOR_HEIGHT / 2, 0xffffff);
//            }
//        }
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
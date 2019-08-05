package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.gui.elements.GuiCompactTextField;
import net.gobbob.mobends.core.client.gui.elements.GuiCustomButton;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.core.util.BendsPackHelper;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.GuiHelper;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiPacksWindow extends GuiScreen
{

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ModStatics.MODID,
            "textures/gui/pack_editor.png");
    private static final int EDITOR_WIDTH = 248;
    private static final int EDITOR_HEIGHT = 177;
    private static final String publicInfoText = "Want your creation to be featured? Contact me at:";

    private static final int BUTTON_BACK = 0;
    private static final int BUTTON_USE = 1;
    private static final int BUTTON_EDIT = 2;
    private static final int BUTTON_DONE = 3;
    private static final int BUTTON_OPEN_FOLDER = 4;

    private int x, y;
    private GuiCompactTextField titleTextField;
    private GuiCustomButton openFolderButton;
    private GuiCustomButton useButton;
    private GuiCustomButton editButton;
    private GuiCustomButton doneButton;
    private GuiPackList packList;
    private boolean editMode;
    private String generatedName;

    private float publicJumbotronTransition = 1;
    private long time, lastTime;

    public GuiPacksWindow()
    {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.titleTextField = new GuiCompactTextField(this.fontRenderer, 120, 14);
        this.titleTextField.setVisible(false);
        this.openFolderButton = new GuiCustomButton(BUTTON_OPEN_FOLDER, 130, 20);
        this.useButton = new GuiCustomButton(BUTTON_USE, 60, 20);
        this.editButton = new GuiCustomButton(BUTTON_EDIT, 60, 20);
        this.doneButton = new GuiCustomButton(BUTTON_DONE, 60, 20);
        this.useButton.visible = false;
        this.doneButton.visible = false;
        this.editButton.visible = false;
        this.packList = new GuiPackList(this);
        this.populate();

        this.time = System.nanoTime() / 1000;
        this.lastTime = System.nanoTime() / 1000;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.x = (this.width - EDITOR_WIDTH) / 2;
        this.y = (this.height - EDITOR_HEIGHT) / 2;
        this.titleTextField.setPosition(this.x + 123, this.y + 39);
        this.packList.initGui(this.x + 5, this.y + 19);

        this.useButton.setPosition(this.x + EDITOR_WIDTH - 57, this.y + EDITOR_HEIGHT - 17);
        this.editButton.setPosition(this.x + EDITOR_WIDTH - 128, this.y + EDITOR_HEIGHT - 17);
        this.doneButton.setPosition(this.x + EDITOR_WIDTH - 57, this.y + EDITOR_HEIGHT - 17);
        this.openFolderButton.setPosition(this.x + EDITOR_WIDTH - 128, this.y + EDITOR_HEIGHT - 17);
        this.updateButtonLabels();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(BUTTON_BACK, 10, height - 30, 60, 20, "Back"));
        this.buttonList.add(this.useButton);
        this.buttonList.add(this.editButton);
        this.buttonList.add(this.doneButton);
        this.buttonList.add(this.openFolderButton);
    }

    private void updateButtonLabels()
    {
        String useButtonTitle = "mobends.gui.use";
        if (packList.getSelectedEntry() != null && packList.getSelectedEntry().isApplied())
            useButtonTitle = "mobends.gui.disable";

        this.useButton.setText(I18n.format(useButtonTitle));
        this.editButton.setText(I18n.format("mobends.gui.edit"));
        this.doneButton.setText(I18n.format("mobends.gui.done"));
        this.openFolderButton.setText(I18n.format("mobends.gui.openpacksfolder"));
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        if (this.titleTextField.isFocused())
            this.titleTextField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, button);

        this.titleTextField.mouseClicked(mouseX, mouseY, button);
        this.packList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        packList.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.time = System.nanoTime() / 1000;
        float delta = (time - lastTime) / 1000;
        this.lastTime = time;

        if (this.publicJumbotronTransition > 0)
            this.publicJumbotronTransition = this.publicJumbotronTransition - delta * 0.001f;
        if (this.publicJumbotronTransition < 0)
            this.publicJumbotronTransition = 0;

        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        Draw.borderBox(x + 4, y + 4, EDITOR_WIDTH, EDITOR_HEIGHT, 4, 102, 13);
        Draw.texturedModalRect(x + 4 + EDITOR_WIDTH / 2 - 75, y - 13, 106, 0, 150, 13);

        this.drawCenteredString(fontRenderer, I18n.format("mobends.gui.packs", new Object[0]),
                (int) this.x + 4 + EDITOR_WIDTH / 2, y - 9, 0xffffff);

        this.packList.display(mouseX, mouseY);
        this.titleTextField.drawTextBox();
        this.openFolderButton.drawButton(mouseX, mouseY, partialTicks);
        this.useButton.drawButton(mouseX, mouseY, partialTicks);
        this.editButton.drawButton(mouseX, mouseY, partialTicks);
        this.doneButton.drawButton(mouseX, mouseY, partialTicks);

        GuiPackEntry entry = packList.getSelectedEntry();
        if (packList.getSelectedTab().index == GuiPackList.TAB_PUBLIC)
        {
            if (entry != null)
            {
                String packName = entry.getDisplayName();
                String packAuthor = "by " + entry.author;
                fontRenderer.drawStringWithShadow(packName, x + 184 - fontRenderer.getStringWidth(packName) / 2, y + 7,
                        0xffffff);
                fontRenderer.drawString(packAuthor, x + 184 - fontRenderer.getStringWidth(packAuthor) / 2, y + 17,
                        0x444444);
                int yOffset = y + 29;
                for (String line : GUtil.squashText(fontRenderer, entry.description, 130))
                {
                    fontRenderer.drawStringWithShadow(line, x + 120, yOffset, 0xffffff);
                    yOffset += 12;
                }
            }
            else
            {
                GlStateManager.color(1, 1, 1, 1);
                Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
                Draw.texturedModalRect(x + EDITOR_WIDTH / 2 - 4, y + 6, 0, 195, 130, 61);

                float alpha = GUtil.clamp(this.publicJumbotronTransition * 2, 0, 1);
                int color = 0x00ffffff;
                color |= (((int) (alpha * 255) & 255) << 24);
                Draw.rectangle(x + EDITOR_WIDTH / 2 - 4, y + 6, 130, 61, color);

                int yOffset = y + 77;
                for (String line : GUtil.squashText(fontRenderer, publicInfoText, 130))
                {
                    fontRenderer.drawString(line, x + 121, yOffset, 0x444444);
                    yOffset += 12;
                }
                fontRenderer.drawStringWithShadow("gobbobminecraft@gmail.com", x + 118, y + 110, 0xffffff);
            }
        }
        else
        {
            this.openFolderButton.drawButton(mouseX, mouseY, partialTicks);
            if (!editMode)
            {
                if (entry != null)
                {
                    String text = entry.getDisplayName();
                    fontRenderer.drawStringWithShadow(text, x + 184 - fontRenderer.getStringWidth(text) / 2, y + 7,
                            0xffffff);
                    int yOffset = y + 27;
                    for (String line : GUtil.squashText(fontRenderer, entry.description, 80))
                    {
                        fontRenderer.drawStringWithShadow(line, x + 120, yOffset, 0xffffff);
                        yOffset += 12;
                    }
                }
                else
                {
                    String text = I18n.format("mobends.gui.selectpack", new Object[0]);
                    fontRenderer.drawStringWithShadow(text, x + 184 - fontRenderer.getStringWidth(text) / 2,
                            y + EDITOR_HEIGHT / 2, 0xffffff);
                }
            }
            else
            {
                fontRenderer.drawStringWithShadow(I18n.format("mobends.gui.entername", new Object[0]), x + 125, y + 25,
                        0xffffff);
                fontRenderer.drawStringWithShadow(I18n.format("mobends.gui.filename", new Object[0]) + ": ", x + 125,
                        y + 58, 0xffffff);
                fontRenderer.drawString(this.generatedName, x + 125, y + 70, 0x777777);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {

        switch (keyCode)
        {
            case 1:
                GuiHelper.closeGui();
                return;
        }

        if (this.titleTextField.textboxKeyTyped(typedChar, keyCode))
        {
            this.generatedName = BendsPackHelper.constructPackName(this.titleTextField.getText()) + ".bends";
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
            case BUTTON_OPEN_FOLDER:
                OpenGlHelper.openFile(PackManager.instance.getLocalDirectory());
                break;
            case BUTTON_USE:
                if (this.packList.getSelectedEntry().isApplied())
                    this.packList.getSelectedEntry().setApplied(false);
                else
                {
                    this.packList.applySelectedEntry();
                    PackManager.instance.choose(PackManager.instance.getLocal(this.packList.getSelectedEntry().name));
                }

                this.updateButtonLabels();

                break;
            case BUTTON_EDIT:
                this.enableEditMode();
                break;
            case BUTTON_DONE:
                this.applyChangesToName();
                break;
        }
    }

    private void goBack()
    {
        this.mc.displayGuiScreen(new GuiBendsMenu());
    }

    private void populate()
    {
        if (!PackManager.instance.arePublicPacksLoaded())
            PackManager.instance.updatePublicDatabase();
        this.packList.populate();
    }

    private void enableEditMode()
    {
        if (this.editMode)
            return;

        this.editMode = true;
        this.titleTextField.setVisible(true);
        this.titleTextField.setText(this.packList.getSelectedEntry().getDisplayName());
        this.editButton.visible = false;
        this.useButton.visible = false;
        this.doneButton.visible = true;
    }

    private void applyChangesToName()
    {
        if (!this.editMode)
            return;

        this.editMode = false;
        this.titleTextField.setVisible(false);
        this.editButton.visible = true;
        this.useButton.visible = true;
        this.doneButton.visible = false;
        this.packList.getSelectedEntry().updateName(this.titleTextField.getText());
    }

    public void onEntrySelected(GuiPackEntry entry)
    {
        if (entry != null)
        {
            useButton.visible = true;
            switch (packList.tabId)
            {
                case GuiPackList.TAB_PUBLIC:
                    editButton.visible = false;
                    break;
                case GuiPackList.TAB_LOCAL:
                    editButton.visible = true;
                    break;
            }
            openFolderButton.visible = false;
        }
        else
        {
            useButton.visible = false;
            editButton.visible = false;
            openFolderButton.visible = true;
        }

        applyChangesToName();
        updateButtonLabels();
    }

    public GuiPackList getPackList()
    {
        return packList;
    }

    public void onOpened()
    {
        publicJumbotronTransition = 1;
    }

}
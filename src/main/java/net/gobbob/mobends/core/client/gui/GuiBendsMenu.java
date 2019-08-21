package net.gobbob.mobends.core.client.gui;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.client.gui.addonswindow.GuiAddonsWindow;
import net.gobbob.mobends.editor.gui.GuiCustomizeWindow;
import net.gobbob.mobends.core.client.gui.elements.GuiSectionButton;
import net.gobbob.mobends.core.client.gui.packswindow.GuiPacksWindow;
import net.gobbob.mobends.core.client.gui.popup.GuiPopUp;
import net.gobbob.mobends.core.client.gui.popup.GuiPopUpCreatePack;
import net.gobbob.mobends.core.client.gui.popup.GuiPopUpHelp;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GuiHelper;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiBendsMenu extends GuiScreen
{
	
	/*--VISUAL--*/
	public static final ResourceLocation MENU_TITLE_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/title.png");
	public static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/icons.png");

	/*--TECHNICAL--*/
	public static final int POPUP_CHANGETARGET = 0;
	public static final int POPUP_GOBACK = 1;
	public static final int POPUP_HELP = 2;
	public static final int POPUP_EXIT = 3;
	public static final int POPUP_CREATEPACK = 4;

	private GuiSectionButton customizeButton;
	private GuiSectionButton packsButton;
	private GuiSectionButton addonsButton;
	private GuiPopUp popUp;
	private GuiAddonsWindow addonsWindow;

	public GuiBendsMenu()
	{
		Keyboard.enableRepeatEvents(true);

		this.customizeButton = new GuiSectionButton(I18n.format("mobends.gui.section.customize"), 0xFFDA3A00)
				.setLeftIcon(0, 43, 19, 19).setRightIcon(19, 43, 19, 19);
		this.packsButton = new GuiSectionButton(I18n.format("mobends.gui.section.packs"), 0xFF4577DE)
				.setLeftIcon(38, 43, 23, 20).setRightIcon(38, 43, 23, 20);
		this.addonsButton = new GuiSectionButton(I18n.format("mobends.gui.section.addons"), 0xFFFFE565)
				.setLeftIcon(61, 43, 19, 18).setRightIcon(61, 43, 19, 18);

		this.addonsWindow = new GuiAddonsWindow();
		this.popUp = null;
	}

	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		this.addonsWindow.initGui(this.width / 2, this.height / 2);
		
		if (this.popUp != null)
			this.popUp.initGui(this.width / 2, this.height / 2);

		int startY = height / 2 - 32;
		int distance = 49;
		this.customizeButton.initGui((this.width - 318) / 2, startY);
		this.packsButton.initGui((this.width - 318) / 2, startY + distance);
		this.addonsButton.initGui((this.width - 318) / 2, startY + distance * 2);
	}

	protected void keyTyped(char typedChar, int keyCode)
	{
		if (popUp != null)
		{
			popUp.keyTyped(typedChar, keyCode);
			return;
		}

		switch (keyCode)
		{
			case 1:
				GuiHelper.closeGui();
				break;
		}
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		Core.saveConfiguration();
	}

	@Override
	public void updateScreen()
	{
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		if (this.popUp != null)
		{
			this.popUp.update(mouseX, mouseY);
			return;
		}

		this.customizeButton.update(mouseX, mouseY);
		this.packsButton.update(mouseX, mouseY);
		this.addonsButton.update(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int state)
	{
		if (this.popUp != null)
		{
			int button = popUp.mouseClicked(x, y, state);
			switch (popUp.getAfterAction())
			{
				case POPUP_CHANGETARGET:
					switch (button)
					{
						case 0: // Yes
							//selectAlterEntry((Integer) customizeWindow.getTargetList().getSelectedValue());
							break;
						case 1: // No
							break;
					}
					break;
				case POPUP_HELP:
					if (button == 0)
						popUp = null;
					break;
				case POPUP_EXIT:
					switch (button)
					{
						case 0: // Yes
							GuiHelper.closeGui();
							break;
						case 1: // No
							break;
					}
					break;
			}
			if (button != -1)
			{
				popUp = null;
			}
			return;
		}

		if (this.customizeButton.mouseClicked(x, y, state))
		{
			this.mc.displayGuiScreen(new GuiCustomizeWindow());
		}
		else if (this.packsButton.mouseClicked(x, y, state))
		{
			this.mc.displayGuiScreen(new GuiPacksWindow());
		}
		else if (this.addonsButton.mouseClicked(x, y, state))
		{
			this.mc.displayGuiScreen(new GuiCustomizeWindow());
		}

		try
		{
			super.mouseClicked(x, y, state);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		this.customizeButton.mouseReleased(mouseX, mouseY, state);
		this.packsButton.mouseReleased(mouseX, mouseY, state);
		this.addonsWindow.mouseReleased(mouseX, mouseY, state);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(MENU_TITLE_TEXTURE);
		int titleWidth = 167 * 2;
		int titleHeight = 37 * 2;

		Draw.texturedRectangle((width - titleWidth) / 2, (height - titleHeight) / 2 - 70, titleWidth, titleHeight, 0, 0, 1, 1);

		this.customizeButton.display();
		this.packsButton.display();
		this.addonsButton.display();

		super.drawScreen(mouseX, mouseY, partialTicks);

		if (this.popUp != null)
		{
			GlStateManager.disableDepth();
			this.drawDefaultBackground();
			this.popUp.display(mouseX, mouseY, partialTicks);
			GlStateManager.enableDepth();
		}
	}

	public boolean doesGuiPauseGame()
	{
		return false;
	}

	public void popUpDiscardChanges(int afterAction)
	{
		this.popUp = new GuiPopUp(I18n.format("mobends.gui.discardchanges"), afterAction,
				new String[] { "Yes", "No" });
		initGui();
	}

	public void popUpHelp()
	{
		this.popUp = new GuiPopUpHelp(POPUP_HELP);
		initGui();
	}

	public void popUpCreatePack()
	{
		this.popUp = new GuiPopUpCreatePack(POPUP_CREATEPACK);
		initGui();
	}
}
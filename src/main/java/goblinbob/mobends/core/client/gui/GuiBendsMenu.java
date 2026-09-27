package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.client.gui.elements.GuiSectionButton;
import goblinbob.mobends.core.client.gui.popup.GuiPopUp;
import goblinbob.mobends.core.client.gui.settingswindow.GuiSettingsWindow;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.core.ModStatics;
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
	
	private static final ResourceLocation MENU_TITLE_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/title.png");
	public static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/icons.png");

	private GuiSectionButton settingsButton;
	private GuiPopUp popUp;

	public GuiBendsMenu()
	{
		Keyboard.enableRepeatEvents(true);

		this.settingsButton = new GuiSectionButton(I18n.format("mobends.gui.section.settings"), 0xFFDA3A00)
				.setLeftIcon(0, 43, 19, 19).setRightIcon(19, 43, 19, 19);

		this.popUp = null;
	}

	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();

		if (this.popUp != null)
			this.popUp.initGui(this.width / 2, this.height / 2);

		this.settingsButton.initGui((this.width - 318) / 2, height / 2 - 32);
	}

	protected void keyTyped(char typedChar, int keyCode)
	{
		if (popUp != null)
		{
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

		this.settingsButton.update(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int state)
	{
		if (popUp != null)
		{
			popUp.mouseClicked(x, y, state);
			return;
		}

		if (settingsButton.mouseClicked(x, y, state))
		{
			mc.displayGuiScreen(new GuiSettingsWindow());
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
		this.settingsButton.mouseReleased(mouseX, mouseY, state);
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

		this.settingsButton.display();

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

	private void closePopUp()
	{
		this.popUp = null;
		this.initGui();
	}

	private void openPopUp(GuiPopUp popUp)
	{
		this.popUp = popUp;
		this.initGui();
	}

}
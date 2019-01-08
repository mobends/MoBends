package net.gobbob.mobends.core.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.client.gui.addonswindow.GuiAddonsWindow;
import net.gobbob.mobends.core.client.gui.customize.GuiCustomizeWindow;
import net.gobbob.mobends.core.client.gui.elements.GuiSectionButton;
import net.gobbob.mobends.core.client.gui.packeditor.GuiPackEditor;
import net.gobbob.mobends.core.client.gui.packeditor.GuiPackList;
import net.gobbob.mobends.core.client.gui.popup.GuiPopUp;
import net.gobbob.mobends.core.client.gui.popup.GuiPopUpCreatePack;
import net.gobbob.mobends.core.client.gui.popup.GuiPopUpHelp;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.BendsTarget;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiBendsMenu extends GuiScreen
{
	/*--VISUAL--*/
	public static final ResourceLocation MENU_TITLE_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/title.png");
	public static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/icons.png");

	/*--TECHNICAL--*/
	int guiTab = 0;
	static final int TAB_MAIN = 0;
	static final int TAB_SETTINGS = 1;
	static final int TAB_CUSTOMIZE = 2;
	static final int TAB_PACKS = 3;
	static final int TAB_ADDONS = 4;

	public static final int POPUP_CHANGETARGET = 0;
	public static final int POPUP_GOBACK = 1;
	public static final int POPUP_HELP = 2;
	public static final int POPUP_EXIT = 3;
	public static final int POPUP_CREATEPACK = 4;
	
	
	public int currentAlterEntry = 0;

	private GuiSectionButton customizeButton;
	private GuiSectionButton packsButton;
	private GuiSectionButton addonsButton;
	private GuiCustomizeWindow customizeWindow;
	private GuiPopUp popUp;
	private GuiPackEditor packEditor;
	private GuiAddonsWindow addonsWindow;
	private int guiLeft;
	private int guiTop;
	protected int xSize = 230;
	protected int ySize = 232;

	public GuiBendsMenu()
	{
		Keyboard.enableRepeatEvents(true);

		this.customizeButton = new GuiSectionButton(I18n.format("mobends.gui.section.customize"), 0xFFDA3A00)
				.setLeftIcon(0, 43, 19, 19).setRightIcon(19, 43, 19, 19);
		this.packsButton = new GuiSectionButton(I18n.format("mobends.gui.section.packs"), 0xFF4577DE)
				.setLeftIcon(38, 43, 23, 20).setRightIcon(38, 43, 23, 20);
		this.addonsButton = new GuiSectionButton(I18n.format("mobends.gui.section.addons"), 0xFFFFE565)
				.setLeftIcon(61, 43, 19, 18).setRightIcon(61, 43, 19, 18);
		
		this.customizeWindow = new GuiCustomizeWindow(this);
		this.packEditor = new GuiPackEditor();
		this.addonsWindow = new GuiAddonsWindow();
		this.popUp = null;
	}

	public void initGui()
	{
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		this.buttonList.clear();
		this.customizeWindow.initGui(this.guiLeft, this.guiTop);
		this.packEditor.initGui(this.width / 2, this.height / 2);
		this.addonsWindow.initGui(this.width / 2, this.height / 2);
		
		if (this.popUp != null)
			this.popUp.initGui(this.width / 2, this.height / 2);

		if (this.guiTab == TAB_MAIN)
		{
			int startY = height / 2 - 32;
			int distance = 49;
			this.customizeButton.initGui((this.width - 318) / 2, startY);
			this.packsButton.initGui((this.width - 318) / 2, startY + distance);
			this.addonsButton.initGui((this.width - 318) / 2, startY + distance*2);
		}
		else
		{
			this.buttonList.add(new GuiButton(0, 10, height - 30, 60, 20, "Back"));
		}

		if (this.guiTab == TAB_CUSTOMIZE)
		{
			/*if (PackManager.isCurrentPackLocal())
			{
				GuiButton applyButton = new GuiButton(4, guiLeft + xSize - 50 - 7, guiTop + 77 - 4, 50, 20,
						I18n.format("mobends.gui.apply", new Object[0]));
				applyButton.enabled = customizeWindow.areChangesUnapplied();
				this.buttonList.add(applyButton);
			}
			else
			{
				String buttonTitle = I18n.format("mobends.gui.selectpack", new Object[0]);
				this.buttonList.add(new GuiButton(5, (width - fontRenderer.getStringWidth(buttonTitle) - 20) / 2,
						guiTop + 150, fontRenderer.getStringWidth(buttonTitle) + 20, 20, buttonTitle));
			}*/
		}
	}

	public void openTab(int tab)
	{
		this.guiTab = tab;
		switch (this.guiTab)
		{
			case TAB_CUSTOMIZE:
				this.customizeWindow.initGui(this.guiLeft + 7, this.guiTop + 97);
				break;
			case TAB_PACKS:
				this.packEditor.onOpened();
				break;
			case TAB_ADDONS:
				this.addonsWindow.onOpened();
				break;
		}
		this.initGui();
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
				/*if (guiTab == TAB_CUSTOMIZE && customizeWindow.areChangesUnapplied())
					popUpDiscardChanges(POPUP_EXIT);
				else if (guiTab == TAB_PACKS)
				{
					packEditor.apply();
					close();
				}
				else
				{*/
					close();
				//}
				break;
		}

		switch (guiTab)
		{
			case TAB_CUSTOMIZE:
				this.customizeWindow.keyTyped(typedChar, keyCode);
				break;
			case TAB_PACKS:
				this.packEditor.keyTyped(typedChar, keyCode);
				break;
			/*case TAB_ADDONS:
				this.addonsWindow.keyTyped(typedChar, keyCode);
				break;*/
		}
	}

	public void close()
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		Core.saveConfiguration();
		switch (guiTab)
		{
			case TAB_CUSTOMIZE:
				if (PackManager.getCurrentPack() != null)
				{
					try
					{
						PackManager.getCurrentPack().save();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				break;
		}
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

		switch (guiTab)
		{
			case TAB_MAIN:
				this.customizeButton.update(mouseX, mouseY);
				this.packsButton.update(mouseX, mouseY);
				this.addonsButton.update(mouseX, mouseY);
				break;
			case TAB_CUSTOMIZE:
				this.customizeWindow.update(mouseX, mouseY);
				break;
			case TAB_PACKS:
				this.packEditor.update(mouseX, mouseY);
				break;
			case TAB_ADDONS:
				this.addonsWindow.update(mouseX, mouseY);
				break;
		}
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
				case POPUP_GOBACK:
					switch (button)
					{
						case 0: // Yes
							this.openTab(TAB_MAIN);
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
							close();
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

		switch (guiTab)
		{
			case TAB_MAIN:
				if (this.customizeButton.mouseClicked(x, y, state))
				{
					this.openTab(TAB_CUSTOMIZE);
				}
				else if (this.packsButton.mouseClicked(x, y, state))
				{
					this.openTab(TAB_PACKS);
				}
				else if (this.addonsButton.mouseClicked(x, y, state))
				{
					this.openTab(TAB_ADDONS);
				}
				
				break;
			case TAB_CUSTOMIZE:
				this.customizeWindow.mouseClicked(x, y, state);
				break;
			case TAB_PACKS:
				this.packEditor.mouseClicked(x, y, state);
				break;
			case TAB_ADDONS:
				this.addonsWindow.mouseClicked(x, y, state);
				break;
		}

		try
		{
			super.mouseClicked(x, y, state);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		this.customizeButton.mouseReleased(mouseX, mouseY, state);
		this.packsButton.mouseReleased(mouseX, mouseY, state);
		this.customizeWindow.mouseReleased(mouseX, mouseY, state);
		this.addonsWindow.mouseReleased(mouseX, mouseY, state);
	}

	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();

		if (this.popUp != null)
		{
			return;
		}

		switch (guiTab)
		{
			case TAB_CUSTOMIZE:
				customizeWindow.handleMouseInput();
				break;
			case TAB_PACKS:
				packEditor.handleMouseInput();
				break;
		}
	}

	public void createANewPack(String string)
	{
		BendsPack newPack = new BendsPack(null, string, Minecraft.getMinecraft().player.getName(),
				"A custom pack made by " + Minecraft.getMinecraft().player.getName() + ".");
		PackManager.addLocal(newPack);
		PackManager.choose(newPack);
	}

	protected void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.id >= 100)
		{
			this.currentAlterEntry = par1GuiButton.id - 100;
			this.openTab(TAB_CUSTOMIZE);
		}
		else if (par1GuiButton.id >= 10)
		{
			initGui();
		}

		switch (par1GuiButton.id)
		{
			case 0: // Back
				/*if (guiTab == TAB_CUSTOMIZE && customizeWindow.areChangesUnapplied())
					this.popUpDiscardChanges(POPUP_GOBACK);
				else */if (guiTab == TAB_PACKS)
				{
					packEditor.apply();
					this.openTab(TAB_MAIN);
				}
				else
					this.openTab(TAB_MAIN);
				break;
			case 4: // Apply
				/*BendsTarget target = BendsPack.getTarget(getCurrentAlterEntry().getKey());
				if (target == null)
					target = BendsPack.createTarget(getCurrentAlterEntry().getKey());*/
				//this.customizeWindow.applyChanges(target);
				try
				{
					PackManager.getCurrentPack().save();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				initGui();
				break;
			case 5:
				this.openTab(TAB_PACKS);
				packEditor.getPackList().selectTab(GuiPackList.TAB_LOCAL, true);
				break;
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);

		switch (guiTab)
		{
			case TAB_MAIN:
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				this.mc.renderEngine.bindTexture(MENU_TITLE_TEXTURE);
				int titleWidth = 167 * 2;
				int titleHeight = 37 * 2;
				Draw.rectangle((width - titleWidth) / 2, (height - titleHeight) / 2 - 70, titleWidth, titleHeight);

				this.customizeButton.display();
				this.packsButton.display();
				this.addonsButton.display();
				break;
			case TAB_CUSTOMIZE:
				this.customizeWindow.display(mouseX, mouseY, partialTicks);
				break;
			case TAB_PACKS:
				this.packEditor.display(mouseX, mouseY, partialTicks);
				break;
			case TAB_ADDONS:
				this.addonsWindow.display(mouseX, mouseY, partialTicks);
				break;
		}

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
		this.popUp = new GuiPopUp(I18n.format("mobends.gui.discardchanges", new Object[0]), afterAction,
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
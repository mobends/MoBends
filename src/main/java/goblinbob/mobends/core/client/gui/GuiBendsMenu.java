package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.client.gui.elements.GuiSectionButton;
import goblinbob.mobends.core.client.gui.popup.GuiPopUp;
import goblinbob.mobends.core.client.gui.settingswindow.GuiSettingsWindow;
import goblinbob.mobends.core.client.gui.supporterswindow.GuiSupportersWindow;
import goblinbob.mobends.core.connection.MoBendsAccount;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

import java.io.IOException;

public class GuiBendsMenu extends GuiScreen
{
	
	private static final ResourceLocation MENU_TITLE_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/title.png");
	public static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/icons.png");
	private static final int COMPONENT_BUTTON_LINK_ACCOUNT = 0;
	private static final int COMPONENT_BUTTON_ACCOUNT = 1;
	private static final int COMPONENT_BUTTON_LOG_OUT = 2;
	private static final int COMPONENT_BUTTON_REFRESH = 3;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 4;
	/** The title texture's size: its pixel art is drawn at twice the size of a pixel. */
	private static final int TITLE_TEXTURE_WIDTH = 334;
	private static final int TITLE_TEXTURE_HEIGHT = 74;
	/**
	 * The title and the sections share three columns: the title, with the account's buttons under
	 * it, takes up the first two, the sections, one above the other, the third.
	 */
	private static final int MAX_CONTENT_WIDTH = 378;
	private static final int COLUMN_GAP = 6;
	private static final int SECTION_GAP = 2;
	private static final int SECTION_COUNT = 3;
	private static final int SCREEN_MARGIN = 10;
	private GuiSectionButton settingsButton;
	private GuiSectionButton customizeButton;
	private GuiSectionButton supportersButton;
	private GuiPopUp popUp;
	/** Where leaving the Mo' Bends menus goes: the game (null), or the Mods list it was opened from. */
	@Nullable
	private final GuiScreen exit;

	public GuiBendsMenu()
	{
		this(null);
	}

	public GuiBendsMenu(@Nullable GuiScreen exit)
	{
		this.exit = exit;
		Keyboard.enableRepeatEvents(true);

		this.settingsButton = new GuiSectionButton(I18n.format("mobends.gui.section.settings"), 0xFFDA3A00)
				.setIcon(120, 43, 14, 14);
		this.customizeButton = new GuiSectionButton(I18n.format("mobends.gui.section.customize"), 0xFF26DAA3)
				.setIcon(80, 43, 19, 14);
		this.supportersButton = new GuiSectionButton(I18n.format("mobends.gui.section.supporters"), 0xFFD8306A)
				.setIcon(100, 43, 18, 16);

		this.popUp = null;

		// The account may have been unlinked or renamed on the dashboard.
		MoBendsAccount.verify(() -> {
			if (Minecraft.getMinecraft().currentScreen == this)
			{
				this.initGui();
			}
		});
	}

	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();

		if (this.popUp != null)
			this.popUp.initGui(this.width / 2, this.height / 2);

		Layout layout = new Layout();
		GuiSectionButton[] sections = { this.settingsButton, this.customizeButton, this.supportersButton };
		for (int i = 0; i < sections.length; ++i)
		{
			sections[i].initGui(layout.sectionsX, layout.sectionsY + i * (GuiSectionButton.HEIGHT + SECTION_GAP), layout.column);
		}
		addAccountButtons(layout);

		// Refresh reloads the animation and refreshes the addons and modules (see CoreClient#refresh()).
		String refresh = I18n.format("mobends.gui.refresh");
		int refreshWidth = this.fontRendererObj.getStringWidth(refresh) + 20;
		this.buttonList.add(new GuiButton(COMPONENT_BUTTON_REFRESH, 10, this.height - 30, refreshWidth, BUTTON_HEIGHT, refresh));
	}

	/**
	 * Under the title: Link account, half as wide and centered, or once linked, across its width,
	 * the Mo' Bends account's name (opening the dashboard) and Log out.
	 */
	private void addAccountButtons(Layout layout)
	{
		int x = layout.titleX;
		int y = layout.buttonsY;
		int width = layout.titleWidth;

		MoBendsAccount.Session session = MoBendsAccount.getCurrent();
		if (session == null)
		{
			int linkWidth = width / 2;
			this.buttonList.add(new GuiButton(COMPONENT_BUTTON_LINK_ACCOUNT, x + (width - linkWidth) / 2, y, linkWidth, BUTTON_HEIGHT,
					I18n.format("mobends.gui.link.button")));
			return;
		}

		String logOut = I18n.format("mobends.gui.link.log_out");
		int logOutWidth = this.fontRendererObj.getStringWidth(logOut) + 20;
		int nameWidth = width - BUTTON_GAP - logOutWidth;
		this.buttonList.add(new GuiButton(COMPONENT_BUTTON_LOG_OUT, x + nameWidth + BUTTON_GAP, y, logOutWidth, BUTTON_HEIGHT, logOut));

		String name = session.getDisplayName();
		int maxNameWidth = nameWidth - 8;
		if (this.fontRendererObj.getStringWidth(name) > maxNameWidth)
		{
			name = this.fontRendererObj.trimStringToWidth(name, maxNameWidth - this.fontRendererObj.getStringWidth("...")) + "...";
		}
		this.buttonList.add(new GuiButton(COMPONENT_BUTTON_ACCOUNT, x, y, nameWidth, BUTTON_HEIGHT, name));
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (popUp != null)
		{
			return;
		}

		switch (button.id)
		{
			case COMPONENT_BUTTON_LINK_ACCOUNT:
				mc.displayGuiScreen(new GuiLinkAccount(this));
				break;
			case COMPONENT_BUTTON_ACCOUNT:
				WebLink.ACCOUNT.open();
				break;
			case COMPONENT_BUTTON_LOG_OUT:
				MoBendsAccount.logOut();
				initGui();
				break;
			case COMPONENT_BUTTON_REFRESH:
				CoreClient.refresh();
				// The account is read again from disk.
				initGui();
				break;
			default:
				break;
		}
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
				mc.displayGuiScreen(exit);
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
		this.customizeButton.update(mouseX, mouseY);
		this.supportersButton.update(mouseX, mouseY);
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
			mc.displayGuiScreen(new GuiSettingsWindow(exit));
		}
		else if (customizeButton.mouseClicked(x, y, state))
		{
			// Opens the link the static API names: meant to become the web animation editor (see TODO.md).
			WebLink.EDITOR.open();
		}
		else if (supportersButton.mouseClicked(x, y, state))
		{
			mc.displayGuiScreen(new GuiSupportersWindow(exit));
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
		this.customizeButton.mouseReleased(mouseX, mouseY, state);
		this.supportersButton.mouseReleased(mouseX, mouseY, state);
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
		Layout layout = new Layout();
		Draw.texturedRectangle(layout.titleX, layout.titleY, layout.titleWidth, layout.titleHeight, 0, 0, 1, 1);

		this.settingsButton.display();
		this.customizeButton.display();
		this.supportersButton.display();

		super.drawScreen(mouseX, mouseY, partialTicks);

		if (this.popUp != null)
		{
			GlStateManager.disableDepth();
			this.drawDefaultBackground();
			this.popUp.display(mouseX, mouseY, partialTicks);
			GlStateManager.enableDepth();
		}
	}

	/**
	 * Where the title and the sections go: three columns, as wide as fit on the screen, centered a
	 * little above the middle. The title, with its buttons under it, fills the first two and the
	 * sections stack in the third, the bottoms of both lined up.
	 */
	private class Layout
	{
		final int column;
		final int titleX, titleY, titleWidth, titleHeight;
		final int buttonsY;
		final int sectionsX, sectionsY;

		Layout()
		{
			int contentWidth = Math.min(MAX_CONTENT_WIDTH, width - 2 * SCREEN_MARGIN);
			column = (contentWidth - 2 * COLUMN_GAP) / 3;
			titleWidth = 2 * column + COLUMN_GAP;
			titleHeight = Math.round((float) titleWidth * TITLE_TEXTURE_HEIGHT / TITLE_TEXTURE_WIDTH);
			int titleColumnHeight = titleHeight + BUTTON_GAP + BUTTON_HEIGHT;
			int sectionsHeight = SECTION_COUNT * GuiSectionButton.HEIGHT + (SECTION_COUNT - 1) * SECTION_GAP;
			int contentHeight = Math.max(titleColumnHeight, sectionsHeight);
			int left = (width - (3 * column + 2 * COLUMN_GAP)) / 2;
			int top = (height - contentHeight) / 2 - 10;

			// Both columns end at the same line: the buttons under the title line up with the last section.
			int bottom = top + contentHeight;
			titleX = left;
			buttonsY = bottom - BUTTON_HEIGHT;
			titleY = buttonsY - BUTTON_GAP - titleHeight;
			sectionsX = left + titleWidth + COLUMN_GAP;
			sectionsY = bottom - sectionsHeight;
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
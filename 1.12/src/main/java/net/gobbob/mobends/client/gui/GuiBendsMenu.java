package net.gobbob.mobends.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.client.gui.elements.GuiPortraitDisplay;
import net.gobbob.mobends.client.gui.nodeeditor.GuiNodeEditor;
import net.gobbob.mobends.client.gui.packeditor.GuiPackEditor;
import net.gobbob.mobends.client.gui.packeditor.GuiPackList;
import net.gobbob.mobends.client.gui.popup.GuiPopUp;
import net.gobbob.mobends.client.gui.popup.GuiPopUpCreatePack;
import net.gobbob.mobends.client.gui.popup.GuiPopUpHelp;
import net.gobbob.mobends.main.ModBase;
import net.gobbob.mobends.main.ModStatics;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.pack.PackManager;
import net.gobbob.mobends.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiBendsMenu extends GuiScreen
{
	/*--VISUAL--*/
	public static final ResourceLocation menuTitleTexture = new ResourceLocation(ModStatics.MODID,
			"textures/gui/logo.png");
	public static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/icons.png");

	public float titleTransitionState = 0.0f;
	public boolean titleTransition = true;

	/*--TECHNICAL--*/
	int guiTab = 0;
	public static final int TAB_MAIN = 0;
	public static final int TAB_SETTINGS = 1;
	public static final int TAB_CUSTOMIZE = 2;
	public static final int TAB_PACKS = 3;

	public static final int POPUP_CHANGETARGET = 0;
	public static final int POPUP_GOBACK = 1;
	public static final int POPUP_HELP = 2;
	public static final int POPUP_EXIT = 3;
	public static final int POPUP_CREATEPACK = 4;

	public List<AlterEntry> alterEntries = new ArrayList<AlterEntry>();
	public int currentAlterEntry = 0;

	private GuiNodeEditor nodeEditor;
	private GuiPortraitDisplay portraitDisplay;
	private GuiPopUp popUp;
	private GuiPackEditor packEditor;
	private int guiLeft;
	private int guiTop;
	protected int xSize = 230;
	protected int ySize = 232;

	public GuiBendsMenu()
	{
		Keyboard.enableRepeatEvents(true);
		this.titleTransition = true;
		this.titleTransitionState = 0.0f;

		for (AnimatedEntity animatedEntity : AnimatedEntity.animatedEntities.values())
		{
			this.alterEntries.addAll(animatedEntity.getAlredEntries());
		}

		this.nodeEditor = new GuiNodeEditor(this);
		this.portraitDisplay = new GuiPortraitDisplay();
		this.popUp = null;
		this.packEditor = new GuiPackEditor();
	}

	public void initGui()
	{
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		this.buttonList.clear();
		this.nodeEditor.initGui(this.guiLeft + 7, this.guiTop + 97, this.guiLeft + this.xSize, this.guiTop + 70);
		this.portraitDisplay.initGui(this.guiLeft + 8, this.guiTop + 21);
		this.packEditor.initGui(this.width / 2, this.height / 2);
		if (this.popUp != null)
			this.popUp.initGui(this.width / 2, this.height / 2);

		if (this.guiTab == TAB_MAIN)
		{
			this.buttonList.add(new GuiButton(1, (this.width - 80) / 2, height / 2 - 30, 80, 20, "Customize"));
			this.buttonList.add(new GuiButton(3, (this.width - 80) / 2, height / 2 + 30, 80, 20, "Packs"));
		} else
		{
			this.buttonList.add(new GuiButton(0, 10, height - 30, 60, 20, "Back"));
		}

		if (this.guiTab == TAB_CUSTOMIZE)
		{
			if (PackManager.isCurrentPackLocal())
			{
				GuiButton applyButton = new GuiButton(4, guiLeft + xSize - 50 - 7, guiTop + 77 - 4, 50, 20,
						I18n.format("mobends.gui.apply", new Object[0]));
				applyButton.enabled = nodeEditor.areChangesUnapplied();
				this.buttonList.add(applyButton);
			} else
			{
				String buttonTitle = I18n.format("mobends.gui.selectpack", new Object[0]);
				this.buttonList.add(new GuiButton(5, (width - fontRenderer.getStringWidth(buttonTitle) - 20) / 2,
						guiTop + 150, fontRenderer.getStringWidth(buttonTitle) + 20, 20, buttonTitle));
			}
		}
	}

	public void openTab(int tab)
	{
		this.guiTab = tab;
		switch (this.guiTab)
		{
			case TAB_CUSTOMIZE:
				this.nodeEditor.initGui(this.guiLeft + 7, this.guiTop + 97, this.guiLeft + this.xSize,
						this.guiTop + 70);
				this.nodeEditor.populate(getCurrentAlterEntry());
				this.portraitDisplay.setViewEntity(this.getCurrentAlterEntry().getName().equalsIgnoreCase("player")
						? Minecraft.getMinecraft().player
						: this.getCurrentAlterEntry().getEntity());
				this.portraitDisplay.setValue(getCurrentAlterEntry().isAnimated());
				break;
			case TAB_PACKS:
				this.packEditor.onOpened();
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
				if (guiTab == TAB_CUSTOMIZE && nodeEditor.areChangesUnapplied())
					popUpDiscardChanges(POPUP_EXIT);
				else if (guiTab == TAB_PACKS)
				{
					packEditor.apply();
					close();
				} else
				{
					close();
				}
				break;
		}

		switch (guiTab)
		{
			case TAB_CUSTOMIZE:
				this.nodeEditor.keyTyped(typedChar, keyCode);
				break;
			case TAB_PACKS:
				this.packEditor.keyTyped(typedChar, keyCode);
				break;
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
		ModBase.instance.configuration.save();
		switch (guiTab)
		{
			case TAB_CUSTOMIZE:
				if (PackManager.getCurrentPack() != null)
				{
					try
					{
						PackManager.getCurrentPack().save();
					} catch (IOException e)
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
			case TAB_CUSTOMIZE:
				this.nodeEditor.update(mouseX, mouseY);
				this.portraitDisplay.update(mouseX, mouseY);
				break;
			case TAB_PACKS:
				this.packEditor.update(mouseX, mouseY);
				break;
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int p_73864_3_)
	{
		if (this.popUp != null)
		{
			int button = popUp.mouseClicked(x, y, p_73864_3_);
			switch (popUp.getAfterAction())
			{
				case POPUP_CHANGETARGET:
					switch (button)
					{
						case 0: // Yes
							selectAlterEntry((Integer) nodeEditor.getTargetList().getSelectedValue());
							break;
						case 1: // No
							nodeEditor.getTargetList().selectValue(currentAlterEntry);
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
							nodeEditor.getTargetList().selectValue(currentAlterEntry);
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
							nodeEditor.getTargetList().selectValue(currentAlterEntry);
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
			case TAB_CUSTOMIZE:
				this.nodeEditor.mouseClicked(x, y, p_73864_3_);
				if (this.portraitDisplay.mouseClicked(x, y, p_73864_3_))
				{
					getCurrentAlterEntry().setAnimate(this.portraitDisplay.getValue());
				}
				break;
			case TAB_PACKS:
				this.packEditor.mouseClicked(x, y, p_73864_3_);
				break;
		}

		try
		{
			super.mouseClicked(x, y, p_73864_3_);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		this.nodeEditor.mouseReleased(mouseX, mouseY, state);
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
				nodeEditor.handleMouseInput();
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
		} else if (par1GuiButton.id >= 10)
		{
			initGui();
		}

		switch (par1GuiButton.id)
		{
			case 0: // Back
				if (guiTab == TAB_CUSTOMIZE && nodeEditor.areChangesUnapplied())
					this.popUpDiscardChanges(POPUP_GOBACK);
				else if (guiTab == TAB_PACKS)
				{
					packEditor.apply();
					this.openTab(TAB_MAIN);
				} else
					this.openTab(TAB_MAIN);
				break;
			case 1:
				this.openTab(TAB_CUSTOMIZE);
				break;
			case 2:
				this.openTab(TAB_SETTINGS);
				break;
			case 3:
				this.openTab(TAB_PACKS);
				break;
			case 4: // Apply
				BendsTarget target = BendsPack.getTarget(getCurrentAlterEntry().getName());
				if (target == null)
					target = BendsPack.createTarget(getCurrentAlterEntry().getName());
				this.nodeEditor.applyChanges(target);
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

		if (this.titleTransition)
		{
			this.titleTransitionState += (128 - this.titleTransitionState) / 5;
		} else
		{
			this.titleTransitionState += (100 - this.titleTransitionState) / 7;
		}

		if (this.titleTransitionState > 126)
		{
			this.titleTransition = false;
		}

		switch (guiTab)
		{
			case TAB_MAIN:
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				this.mc.renderEngine.bindTexture(menuTitleTexture);
				Draw.rectangle(width / 2 - 64, titleTransitionState - 100, 128, 64);
				break;
			case TAB_CUSTOMIZE:
				displayCustomizeWindow(mouseX, mouseY, partialTicks);
				break;
			case TAB_PACKS:
				this.packEditor.display(mouseX, mouseY, partialTicks);
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
		return true;
	}

	public void displayCustomizeWindow(int mouseX, int mouseY, float partialTicks)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiNodeEditor.BACKGROUND_TEXTURE);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		BendsTarget bendsTarget = BendsPack.getTarget(getCurrentAlterEntry().getName());

		this.drawCenteredString(this.fontRenderer, I18n.format("mobends.gui.customize", new Object[0]),
				(int) (this.width / 2), this.guiTop + 4, 0xffffff);

		this.nodeEditor.display(mouseX, mouseY, partialTicks);
		if (!PackManager.isCurrentPackLocal())
		{
			this.drawCenteredString(fontRenderer, I18n.format("mobends.gui.chooseapacktoedit", new Object[0]),
					this.width / 2, this.guiTop + 135, 0xffffff);
		}
		this.portraitDisplay.display(partialTicks);

		GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		ScaledResolution res = new ScaledResolution(mc);
		GL11.glOrtho(0, res.getScaledWidth(), res.getScaledHeight(), 0, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public AlterEntry getCurrentAlterEntry()
	{
		return this.alterEntries.get(this.currentAlterEntry);
	}

	public void onNodeEditorChange()
	{
		initGui();
	}

	public void selectAlterEntry(int id)
	{
		this.currentAlterEntry = id;
		openTab(TAB_CUSTOMIZE);
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
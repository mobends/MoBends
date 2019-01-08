package net.gobbob.mobends.core.client.gui.customize;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.IObservable;
import net.gobbob.mobends.core.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.core.client.gui.elements.GuiIconButton;
import net.gobbob.mobends.core.client.gui.elements.GuiPortraitDisplay;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.pack.BendsTarget;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import scala.actors.threadpool.Arrays;

public class GuiCustomizeWindow extends Gui
{
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/node_editor.png");
	static final int WIDTH = 230;
	static final int HEIGHT = 232;
	
	/*
	 * Used to remember which AlterEntry was used last, so
	 * the GUI can show that as default the next time it
	 * opens up.
	 */
	protected static AlterEntry lastAlterEntryViewed = null;
	
	private int x, y;
	private final FontRenderer fontRenderer;
	
	private final GuiBendsMenu mainMenu;
	final List<AlterEntry<?>> alterEntries = new ArrayList<>();
	AlterEntry currentAlterEntry;

	private final ViewportLayer viewportLayer;
	private final HeaderLayer headerLayer;
	private final List<IGuiLayer> layers = new ArrayList<>();
	private final List<IEditorAction> actionHistory = new ArrayList<>();
	private int actionHistoryPointer = 0;
	private boolean changesMade = false;
	
	public GuiCustomizeWindow(GuiBendsMenu mainMenu)
	{
		this.mainMenu = mainMenu;
		
		for (AnimatedEntity<?> animatedEntity : AnimatedEntityRegistry.getRegistered())
		{
			this.alterEntries.addAll(animatedEntity.getAlterEntries());
		}
		
		// Showing the AlterEntry viewed the last time
		// this gui was open.
		this.currentAlterEntry = lastAlterEntryViewed;
		if (this.currentAlterEntry == null)
			this.currentAlterEntry = this.alterEntries.get(0);

		this.viewportLayer = new ViewportLayer();
		this.headerLayer = new HeaderLayer(this);
		this.layers.add(this.viewportLayer);
		this.layers.add(this.headerLayer);
		
		this.viewportLayer.showAlterEntry(this.currentAlterEntry);
		
		this.x = 0;
		this.y = 0;
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
	}

	public void initGui(int x, int y)
	{
		this.x = x;
		this.y = y;
		this.viewportLayer.initGui(x, y);
		this.headerLayer.initGui(x, y);
	}

	public void display(int mouseX, int mouseY, float partialTicks)
	{
		//Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCustomizeWindow.BACKGROUND_TEXTURE);
		//this.drawTexturedModalRect(this.x, this.y, 0, 0, WIDTH, HEIGHT);

		this.drawCenteredString(this.fontRenderer, I18n.format("mobends.gui.customize"),
				(int) (this.x + WIDTH/2), this.y + 4, 0xFFFFFF);
		
		for (IGuiLayer layer : this.layers)
		{
			layer.draw();
		}

		if (!PackManager.isCurrentPackLocal())
		{
			this.drawCenteredString(fontRenderer, I18n.format("mobends.gui.chooseapacktoedit"),
					this.x + WIDTH / 2, this.y + 135, 0xffffff);
			
			return;
		}
	}

	public void update(int mouseX, int mouseY)
	{
		for (IGuiLayer layer : this.layers)
		{
			layer.update(mouseX, mouseY);
		}
	}

	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleMouseClicked(mouseX, mouseY, button))
				break;
		}
	}

	public void mouseReleased(int mouseX, int mouseY, int button)
	{
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleMouseReleased(mouseX, mouseY, button))
				break;
		}
	}

	public void handleMouseInput()
	{
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleMouseInput())
				break;
		}
	}
	
	public void keyTyped(char typedChar, int keyCode)
	{
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleKeyTyped(typedChar, keyCode))
				break;
		}
		
		/*if (!PackManager.isCurrentPackLocal())
			return;*/
	}

	public boolean areChangesUnapplied()
	{
		return this.changesMade;
	}
	
	public String[] getAlterableParts()
	{
		if (this.currentAlterEntry != null)
			return this.currentAlterEntry.getOwner().getAlterableParts();
		else
			return null;
	}
	
	public void showAlterEntry(AlterEntry alterEntry)
	{
		if (this.currentAlterEntry == alterEntry)
			return;
		
		if (areChangesUnapplied())
		{
			mainMenu.popUpDiscardChanges(GuiBendsMenu.POPUP_CHANGETARGET);
		}
		else
		{
			this.currentAlterEntry = alterEntry;
			this.viewportLayer.showAlterEntry(this.currentAlterEntry);
			lastAlterEntryViewed = alterEntry;
		}
	}
	
	public void performAction(IEditorAction<GuiCustomizeWindow> action)
	{
		action.perform(this);
		this.actionHistory.add(action);
	}
}

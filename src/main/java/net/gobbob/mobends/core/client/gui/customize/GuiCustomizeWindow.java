package net.gobbob.mobends.core.client.gui.customize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.client.gui.customize.viewport.ViewportLayer;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiCustomizeWindow extends GuiScreen
{
	
	/*
	 * Used to remember which AlterEntry was used last, so
	 * the GUI can show that as default the next time it
	 * opens up.
	 */
	protected static AlterEntry lastAlterEntryViewed = null;
	
	private GuiScreen previousScreen;
	
	final List<AlterEntry<?>> alterEntries = new ArrayList<>();
	AlterEntry currentAlterEntry;

	private final ViewportLayer viewportLayer;
	private final HeaderLayer headerLayer;
	private final List<IGuiLayer> layers = new ArrayList<>();
	private final List<IEditorAction> actionHistory = new ArrayList<>();
	private int actionHistoryPointer = 0;
	private boolean changesMade = false;
	
	public GuiCustomizeWindow(GuiScreen previousScreen)
	{
		this.previousScreen = previousScreen;
		
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
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		
		this.headerLayer.initGui(0, 0);
		this.viewportLayer.initGui();
		
		for (IGuiLayer layer : this.layers)
		{
			layer.handleResize(this.width, this.height);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		//Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCustomizeWindow.BACKGROUND_TEXTURE);
		//this.drawTexturedModalRect(this.x, this.y, 0, 0, WIDTH, HEIGHT);

		/*this.drawCenteredString(this.fontRenderer, I18n.format("mobends.gui.customize"),
				(int) (this.x + WIDTH/2), this.y + 4, 0xFFFFFF);*/
		
		for (IGuiLayer layer : this.layers)
		{
			layer.draw();
		}

		/*if (!PackManager.isCurrentPackLocal())
		{
			this.drawCenteredString(fontRenderer, I18n.format("mobends.gui.chooseapacktoedit"),
					this.x + WIDTH / 2, this.y + 135, 0xffffff);
			
			return;
		}*/
	}

	@Override
	public void updateScreen()
	{
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		
		for (IGuiLayer layer : this.layers)
		{
			layer.update(mouseX, mouseY);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleMouseClicked(mouseX, mouseY, button))
				break;
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int button)
	{
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleMouseReleased(mouseX, mouseY, button))
				break;
		}
	}

	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleMouseInput())
				break;
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode)
	{
		boolean eventHandled = false;
		
		for (int i = this.layers.size() - 1; i >= 0; --i)
		{
			if (this.layers.get(i).handleKeyTyped(typedChar, keyCode))
			{
				eventHandled |= true;
				break;
			}
		}
		
		if (!eventHandled && keyCode == Keyboard.KEY_ESCAPE)
		{
			goBack();
			eventHandled = true;
		}
		
		/*if (!PackManager.isCurrentPackLocal())
			return;*/
	}

	@Override
	public boolean doesGuiPauseGame()
    {
        return false;
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
			//mainMenu.popUpDiscardChanges(GuiBendsMenu.POPUP_CHANGETARGET);
		}
		else
		{
			this.currentAlterEntry = alterEntry;
			this.viewportLayer.showAlterEntry(this.currentAlterEntry);
			lastAlterEntryViewed = alterEntry;
		}
	}
	
	public void goBack()
	{
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
		
		this.mc.displayGuiScreen(this.previousScreen);
	}
	
	public void performAction(IEditorAction<GuiCustomizeWindow> action)
	{
		action.perform(this);
		this.actionHistory.add(action);
	}

}

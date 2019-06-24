package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
import net.gobbob.mobends.core.client.gui.customize.viewport.ViewportLayer;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiCustomizeWindow extends GuiScreen implements GuiPartList.IPartListListener
{
	
	/*
	 * Used to remember which AlterEntry was used last, so
	 * the GUI can show that as default the next time it
	 * opens up.
	 */
	protected static AlterEntry lastAlterEntryViewed = null;
	
	final List<AlterEntry<?>> alterEntries = new ArrayList<>();
	AlterEntry currentAlterEntry;
	private AlterEntryRig rig;

	private final ViewportLayer viewportLayer;
	private final HeaderLayer headerLayer;
	private final List<IGuiLayer> layers = new ArrayList<>();
	private final List<IEditorAction> actionHistory = new ArrayList<>();
	private int actionHistoryPointer = 0;
	private boolean changesMade = false;
	
	public GuiCustomizeWindow()
	{
		for (AnimatedEntity<?> animatedEntity : AnimatedEntityRegistry.getRegistered())
		{
			this.alterEntries.addAll(animatedEntity.getAlterEntries());
		}

		this.viewportLayer = new ViewportLayer(this);
		this.headerLayer = new HeaderLayer(this);
		this.layers.add(this.viewportLayer);
		this.layers.add(this.headerLayer);

		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;

		// Showing the AlterEntry viewed the last time
		// this gui was open.
		if (lastAlterEntryViewed != null)
		{
			this.showAlterEntry(lastAlterEntryViewed);
		}
		else
		{
			this.showAlterEntry(this.alterEntries.get(0));
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();
		
		this.headerLayer.initGui();
		this.viewportLayer.initGui();
		
		for (IGuiLayer layer : this.layers)
		{
			layer.handleResize(this.width, this.height);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
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

	@Override
	public void onPartSelectedFromList(String name, AlterEntryRig.Bone bone)
	{
		this.rig.select(bone);
	}

	public boolean areChangesUnapplied()
	{
		return this.changesMade;
	}

	public void selectBone(AlterEntryRig.Bone bone)
	{
		this.headerLayer.hierarchy.selectBone(bone);
		this.rig.select(bone);
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

		IPreviewer previewer = alterEntry.getPreviewer();
		if (previewer != null)
		{
			this.rig = new AlterEntryRig(alterEntry);
		}
		else
		{
			this.rig = null;
		}

		if (areChangesUnapplied())
		{
			//mainMenu.popUpDiscardChanges(GuiBendsMenu.POPUP_CHANGETARGET);
		}
		else
		{
			this.currentAlterEntry = alterEntry;
			this.viewportLayer.showAlterEntry(this.currentAlterEntry, this.rig);
			this.headerLayer.showAlterEntry(this.currentAlterEntry, this.rig);
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

		this.mc.displayGuiScreen(new GuiBendsMenu());
	}
	
	public void performAction(IEditorAction<GuiCustomizeWindow> action)
	{
		action.perform(this);
		this.actionHistory.add(action);
	}

}

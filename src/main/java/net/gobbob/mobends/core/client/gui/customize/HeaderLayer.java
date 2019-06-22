package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.IObservable;
import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
import net.gobbob.mobends.core.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.core.client.gui.elements.GuiToggleButton;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.renderer.GlStateManager;

public class HeaderLayer implements IGuiLayer, IChangeListener
{

	private final GuiCustomizeWindow customizeWindow;
	private int screenWidth;
	private int screenHeight;
	private final GuiDropDownList<AlterEntry<?>> targetList;
	private final GuiToggleButton toggleButton;
	final GuiPartHierarchy hierarchy;
	
	private AlterEntry<?> alterEntryToView;
	
	public HeaderLayer(GuiCustomizeWindow customizeWindow)
	{
		this.customizeWindow = customizeWindow;
		this.screenWidth = this.customizeWindow.width;
		this.screenHeight = this.customizeWindow.height;
		this.targetList = new GuiDropDownList().forbidNoValue();
		this.targetList.addListener(this);
		this.toggleButton = new GuiToggleButton("Animated", 64);
		this.hierarchy = new GuiPartHierarchy(customizeWindow);
		
		for (AlterEntry alterEntry : customizeWindow.alterEntries)
		{
			this.targetList.addEntry(alterEntry.getLocalizedName(), alterEntry);
		}
		
		this.targetList.selectValue(customizeWindow.currentAlterEntry);
	}
	
	public void initGui()
	{
		this.targetList.setPosition(2, 2);
		this.toggleButton.initGui(10, 30);
		this.hierarchy.initGui();
		if (this.alterEntryToView != null)
			this.toggleButton.setToggleState(this.alterEntryToView.isAnimated());
	}

	@Override
	public void handleResize(int width, int height)
	{
		this.screenWidth = width;
		this.screenHeight = height;
	}

	public void showAlterEntry(AlterEntry<?> alterEntry, AlterEntryRig rig)
	{
		if (this.alterEntryToView != alterEntry)
		{
			this.toggleButton.setToggleState(alterEntry.isAnimated());
			if (rig != null)
			{
				this.hierarchy.setParts(alterEntry.getOwner().getAlterableParts(), rig);
			}
			else
			{
				this.hierarchy.clearParts();
			}
		}
		
		this.alterEntryToView = alterEntry;
	}
	
	@Override
	public void update(int mouseX, int mouseY)
	{
		this.targetList.update(mouseX, mouseY);
		this.hierarchy.update(mouseX, mouseY);
		this.toggleButton.update(mouseX, mouseY);
	}
	
	@Override
	public void draw()
	{
		GlStateManager.disableTexture2D();
		Draw.rectangle(0, 0, this.screenWidth, 20, 0xff00528a);
		Draw.rectangle(0, 20, this.screenWidth, 2, 0xff00406b);

		GlStateManager.enableTexture2D();
		this.toggleButton.draw();
		this.hierarchy.draw();
		this.targetList.display();
	}
	
	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int button)
	{
		boolean eventHandled = false;
		
		eventHandled |= this.targetList.mouseClicked(mouseX, mouseY, button);
		
		if (!eventHandled && this.toggleButton.mouseClicked(mouseX, mouseY, button))
		{
			if (this.alterEntryToView != null)
				this.alterEntryToView.setAnimate(this.toggleButton.getToggleState());
			
			eventHandled = true;
		}

		if (!eventHandled && this.hierarchy.mouseClicked(mouseX, mouseY, button))
		{
			eventHandled = true;
		}
		
		return eventHandled;
	}

	@Override
	public boolean handleMouseReleased(int mouseX, int mouseY, int button)
	{
		this.targetList.mouseReleased(mouseX, mouseY, button);
		this.hierarchy.mouseReleased(mouseX, mouseY, button);

		return false;
	}

	@Override
	public boolean handleMouseInput()
	{
		boolean eventHandled = false;

		eventHandled |= this.targetList.handleMouseInput();
		if (!eventHandled)
			eventHandled |= this.hierarchy.handleMouseInput();

		return eventHandled;
	}

	@Override
	public void handleChange(IObservable objectChanged)
	{
		if (objectChanged == this.targetList)
		{
			this.customizeWindow.showAlterEntry(this.targetList.getSelectedValue());
		}
	}
	
	public GuiDropDownList<AlterEntry<?>> getTargetList()
	{
		return targetList;
	}
	
}

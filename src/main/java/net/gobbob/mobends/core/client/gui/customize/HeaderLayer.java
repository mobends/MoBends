package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.IObservable;
import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
import net.gobbob.mobends.core.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.core.client.gui.elements.GuiToggleButton;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.renderer.GlStateManager;

public class HeaderLayer implements IGuiLayer, IChangeListener
{

	private final GuiCustomizeWindow customizeWindow;
	private final GuiDropDownList<AlterEntry<?>> targetList;
	private final GuiToggleButton toggleButton;
	
	private AlterEntry<?> alterEntryToView;
	
	public HeaderLayer(GuiCustomizeWindow customizeWindow)
	{
		this.customizeWindow = customizeWindow;
		this.targetList = new GuiDropDownList().forbidNoValue();
		this.targetList.addListener(this);
		this.toggleButton = new GuiToggleButton("Animated", 64);
		
		for (AlterEntry alterEntry : customizeWindow.alterEntries)
		{
			this.targetList.addEntry(alterEntry.getLocalizedName(), alterEntry);
		}
		
		this.targetList.selectValue(customizeWindow.currentAlterEntry);
	}
	
	public void initGui(int x, int y)
	{
		this.targetList.setPosition(10, 10);
		this.toggleButton.initGui(10, 30);
		if (this.alterEntryToView != null)
			this.toggleButton.setToggleState(this.alterEntryToView.isAnimated());
	}
	
	public void showAlterEntry(AlterEntry<?> alterEntry)
	{
		if (this.alterEntryToView != alterEntry)
		{
			this.toggleButton.setToggleState(alterEntry.isAnimated());
		}
		
		this.alterEntryToView = alterEntry;
	}
	
	@Override
	public void update(int mouseX, int mouseY)
	{
		this.targetList.update(mouseX, mouseY);
		this.toggleButton.update(mouseX, mouseY);
	}
	
	@Override
	public void draw()
	{
		GlStateManager.enableTexture2D();
		this.toggleButton.draw();
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
			
			eventHandled |= true;
		}
		
		return eventHandled;
	}
	
	@Override
	public boolean handleMouseInput()
	{
		return this.targetList.handleMouseInput();
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

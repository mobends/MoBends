package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.IObservable;
import net.gobbob.mobends.core.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;

public class HeaderLayer implements IGuiLayer, IChangeListener
{

	private final GuiCustomizeWindow customizeWindow;
	private final GuiDropDownList<AlterEntry> targetList;
	
	public HeaderLayer(GuiCustomizeWindow customizeWindow)
	{
		this.customizeWindow = customizeWindow;
		this.targetList = new GuiDropDownList().forbidNoValue();
		this.targetList.addListener(this);
		
		for (AlterEntry alterEntry : customizeWindow.alterEntries)
		{
			this.targetList.addEntry(alterEntry.getLocalizedName(), alterEntry);
		}
		
		this.targetList.selectValue(customizeWindow.currentAlterEntry);
	}
	
	@Override
	public void update(int mouseX, int mouseY)
	{
		this.targetList.update(mouseX, mouseY);
	}
	
	@Override
	public void draw()
	{
		this.targetList.display();
	}
	
	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int button)
	{
		boolean eventHandled = false;
		
		eventHandled |= this.targetList.mouseClicked(mouseX, mouseY, button);
		
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
	
	public GuiDropDownList<AlterEntry> getTargetList()
	{
		return targetList;
	}

	public void initGui(int x, int y)
	{
		this.targetList.setPosition(10, 10);
	}
	
}

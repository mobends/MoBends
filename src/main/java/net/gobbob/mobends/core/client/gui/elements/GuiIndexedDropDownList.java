package net.gobbob.mobends.core.client.gui.elements;

public class GuiIndexedDropDownList extends GuiDropDownList<Integer>
{
	public GuiDropDownList addEntry(String label)
	{
		int index = this.entries.size();
		return this.addEntry(label, index);
	}
}

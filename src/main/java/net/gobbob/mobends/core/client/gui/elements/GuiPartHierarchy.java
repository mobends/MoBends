package net.gobbob.mobends.core.client.gui.elements;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiPartHierarchy
{
	
	private int x, y;
	private List<String> partList;
	
	public GuiPartHierarchy()
	{
		this.x = 10;
		this.y = 60;
		this.partList = new ArrayList<String>();
	}
	
	public void addPart(String part)
	{
		this.partList.add(part);
	}
	
	public void setParts(String[] parts)
	{
		this.partList.clear();
		for (String part : parts)
		{
			this.partList.add(part);
		}
	}
	
	public void draw()
	{
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		
		for (int i = 0; i < this.partList.size(); ++i)
		{
			fontRenderer.drawString(this.partList.get(i), this.x + 5, this.y + 5 + i * 10, 0xffffffff);
		}
	}
	
}

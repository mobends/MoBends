package net.gobbob.mobends.core.client.gui.elements;

import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.gui.Observable;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;

public class GuiRadio extends Observable
{
	private int x, y;
	private int buttonX, buttonY, buttonWidth, buttonHeight;
	public int elementX, elementY, elementWidth, elementHeight;
	public int offsetX, offsetY;
	public int padding, elementOffset;
	public int bgX, bgY, bgWidth, bgHeight;
	public int numberOfElements = 5;
	/*
	 * Used to indicate which operator is selected/hovered over. The value of -1
	 * represents that nothing is hovered over. Values between 0 and (n-1) represent
	 * different operators
	 */
	public int selectedId;
	public int hoveredId;
	public boolean enabled;

	public GuiRadio()
	{
		this.x = this.y = 0;
		this.enabled = true;
	}

	public void initGui(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public void display()
	{
		if (!isEnabled())
			return;

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
		Draw.texturedModalRect(x, y, bgX, bgY, bgWidth, bgHeight);

		for (int i = 0; i < numberOfElements; i++)
		{
			if (i == selectedId)
			{
				Draw.texturedModalRect(x + offsetX + i * buttonWidth, y + offsetY, buttonX + buttonWidth * 2, buttonY,
						buttonWidth, buttonHeight);
			}
			else if (i == hoveredId)
			{
				Draw.texturedModalRect(x + offsetX + i * buttonWidth, y + offsetY, buttonX + buttonWidth, buttonY,
						buttonWidth, buttonHeight);
			}
			else
			{
				Draw.texturedModalRect(x + offsetX + i * buttonWidth, y + offsetY, buttonX, buttonY, buttonWidth,
						buttonHeight);
			}

			Draw.texturedModalRect(x + i * buttonWidth + elementOffset, y + elementOffset, elementX + i * elementWidth,
					elementY, elementWidth, elementHeight);
		}
	}

	public void update(int mouseX, int mouseY)
	{
		if (!isEnabled())
			return;

		if (areCoordinatesInBounds(mouseX, mouseY))
		{
			hoveredId = Math.min((mouseX - (x + 2)) / buttonWidth, numberOfElements - 1);
		}
		else
		{
			hoveredId = -1;
		}
	}

	public boolean onMousePressed(int mouseX, int mouseY, int event)
	{
		if (!isEnabled())
			return false;

		int newSelectedId = selectedId;

		if (hoveredId != -1)
		{
			newSelectedId = hoveredId;
		}

		if (newSelectedId != selectedId)
		{
			selectedId = newSelectedId;
			this.notifyChanged();
			return true;
		}

		return false;
	}

	public boolean areCoordinatesInBounds(int cX, int cY)
	{
		return cX >= x + offsetX && cX < x + bgWidth && cY >= y + offsetY && cY < y + bgHeight;
	}

	public void choose(int id)
	{
		this.selectedId = id;
	}

	public void enable()
	{
		this.enabled = true;
	}

	public void disable()
	{
		this.enabled = false;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public GuiRadio setButton(int x, int y, int width, int height)
	{
		buttonX = x;
		buttonY = y;
		buttonWidth = width;
		buttonHeight = height;
		return this;
	}

	public GuiRadio setElement(int x, int y, int width, int height)
	{
		elementX = x;
		elementY = y;
		elementWidth = width;
		elementHeight = height;
		return this;
	}

	public GuiRadio setNumberOfElements(int a)
	{
		numberOfElements = a;
		return this;
	}

	public GuiRadio setBackground(int x, int y, int width, int height)
	{
		bgX = x;
		bgY = y;
		bgWidth = width;
		bgHeight = height;
		return this;
	}

	public GuiRadio setOffset(int x, int y)
	{
		offsetX = x;
		offsetY = y;
		return this;
	}

	public GuiRadio setPadding(int padding, int offset)
	{
		this.padding = padding;
		this.elementOffset = offset;
		return this;
	}
}

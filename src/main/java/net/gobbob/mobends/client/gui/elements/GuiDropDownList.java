package net.gobbob.mobends.client.gui.elements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class GuiDropDownList
{
	public static final int HEIGHT = 16;
	public static final int LIST_HEIGHT = 90;
	public static final int ELEMENT_HEIGHT = 11;
	public static final int SCROLLBAR_WIDTH = 5;

	private int x, y;
	private int width;
	private List<Entry> entries;
	private int selectedId = 0;
	private boolean valueChangeUnhandled = false;
	private int entryAmount = 0;
	private boolean noValueAllowed = true;
	private final FontRenderer fontRenderer;
	private boolean enabled = true;
	private boolean hovered = false;
	private boolean listHovered = false;
	private boolean scrollBarHovered = false;
	private boolean scrollBarGrabbed = false;
	private boolean dropped = false;
	private int hoveredEntryId = -1;
	private float scrollAmount;
	private int scrollBarY;
	private int scrollBarHeight;
	private int scrollBarGrabY;

	private LinkedList<ChangeListener> changeListeners;

	public GuiDropDownList()
	{
		this.entries = new ArrayList<Entry>();
		this.width = 94;
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
		this.scrollAmount = 0;

		this.changeListeners = new LinkedList<ChangeListener>();
	}

	public GuiDropDownList init()
	{
		this.entries = new ArrayList<Entry>();
		this.selectedId = 0;
		this.scrollAmount = 0;
		this.enabled = true;
		return this;
	}

	public GuiDropDownList setPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
		return this;
	}

	public GuiDropDownList addEntry(String displayName)
	{
		this.entries.add(new Entry(this.entries.size(), displayName, entries.size()));
		updateDimensions();
		return this;
	}

	public GuiDropDownList addEntry(String displayName, Object value)
	{
		this.entries.add(new Entry(this.entries.size(), displayName, value));
		updateDimensions();
		return this;
	}

	public GuiDropDownList setEntryAmount(int entryAmount)
	{
		this.entryAmount = entryAmount;
		updateDimensions();
		return this;
	}

	public void update(int mouseX, int mouseY)
	{
		if (!isEnabled())
			return;

		hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEIGHT;

		listHovered = false;
		hoveredEntryId = -1;
		scrollBarHovered = false;

		if (scrollBarGrabbed)
		{
			scroll(((float) (mouseY - y - HEIGHT - scrollBarY - scrollBarGrabY)) / ELEMENT_HEIGHT);
		}
		else if (mouseX >= x && mouseX <= x + getWidth() && mouseY >= y + HEIGHT && mouseY <= y + getVisualHeight())
		{
			if (mouseX >= x + getWidth() - SCROLLBAR_WIDTH - 1)
			{
				if (mouseY >= y + HEIGHT + scrollBarY && mouseY <= y + HEIGHT + scrollBarY + scrollBarHeight)
				{
					scrollBarHovered = true;
				}
			}
			else
			{
				hoveredEntryId = getScrollInEntries() + (mouseY - y - HEIGHT) / ELEMENT_HEIGHT;
			}
			listHovered = true;
		}
	}

	public boolean mouseClicked(int mouseX, int mouseY, int event)
	{
		if (!isEnabled())
			return false;

		scrollBarGrabbed = false;

		if (hovered)
			dropped = !dropped;
		else if (dropped)
		{
			if (mouseX >= x + getWidth() - SCROLLBAR_WIDTH - 1)
			{
				scrollBarGrabbed = true;
				if (scrollBarHovered)
				{
					scrollBarGrabY = mouseY - y - HEIGHT - scrollBarY;
				}
				else
				{
					scrollBarGrabY = scrollBarHeight / 2;
				}
			}
			else
			{
				if (hoveredEntryId >= 0 && hoveredEntryId < entries.size() + (noValueAllowed ? 1 : 0))
				{
					if (hoveredEntryId != selectedId)
					{
						selectedId = hoveredEntryId;
						this.notifyChanged();
						valueChangeUnhandled = true;
					}
				}
				dropped = false;
			}
			return true;
		}

		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + getVisualHeight();
	}

	public void onMouseReleased(int mouseX, int mouseY, int event)
	{
		if (!isEnabled())
			return;

		this.scrollBarGrabbed = false;
	}

	public boolean handleMouseInput()
	{
		if (!isEnabled())
			return false;

		if (dropped && listHovered)
		{
			int i2 = Mouse.getEventDWheel();

			if (i2 != 0)
			{
				if (i2 > 0)
				{
					i2 = -1;
				}
				else if (i2 < 0)
				{
					i2 = 1;
				}

				scroll((float) (i2 * 0.5));
			}
			return true;
		}
		else if (hovered)
		{
			int i2 = Mouse.getEventDWheel();

			if (i2 != 0)
			{
				if (i2 > 0)
				{
					i2 = -1;
				}
				else if (i2 < 0)
				{
					i2 = 1;
				}
				selectedId = (int) GUtil.clamp(selectedId + i2, 0, entries.size() + (noValueAllowed ? 0 : -1));
				valueChangeUnhandled = true;
			}
			return true;
		}
		else
		{
			hoveredEntryId = -1;
		}

		return false;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public GuiDropDownList enable()
	{
		enabled = true;
		return this;
	}

	public GuiDropDownList disable()
	{
		enabled = false;
		return this;
	}

	public void display()
	{
		if (!isEnabled())
			return;

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);

		Draw.rectangle(this.x, this.y, this.width, this.HEIGHT, -6250336);
		Draw.rectangle(this.x + 1, this.y + 1, this.width - 2, this.HEIGHT - 2,
				hovered || dropped ? 0xff222222 : -16777216);
		GlStateManager.color(1, 1, 1, 1);

		boolean noValue = noValueAllowed && selectedId == 0;
		String text = noValue ? "None"
				: this.fontRenderer.trimStringToWidth(getSelectedEntry().getDisplayName(), this.getWidth() - 20);
		this.fontRenderer.drawStringWithShadow(text, x + 5, y + 4, noValue ? 0x999999 : 0xe2e2e2);

		Draw.rectangle_xgradient(x + width - 40, y + 1, 27, HEIGHT - 2, 0x00000000,
				hovered || dropped ? 0xff222222 : -16777216);

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
		Draw.texturedModalRect(x + width - 12, y + 3, 94, 24 + (hovered || dropped ? 10 : 0), 10, 10);

		if (dropped)
		{
			int elements = getNumberOfElements();

			Draw.rectangle(x, y + HEIGHT, width, elements * ELEMENT_HEIGHT + 3, 0xff888888);
			Draw.rectangle(x + 1, y + HEIGHT, width - 2, elements * ELEMENT_HEIGHT + 2, 0xff000000);

			for (int i = 0; i < elements; i++)
			{
				int entryID = getScrollInEntries() + i - (noValueAllowed ? 1 : 0);
				noValue = noValueAllowed && i == 0;

				if (hoveredEntryId == getScrollInEntries() + i)
					Draw.rectangle(x + 1, y + HEIGHT + i * ELEMENT_HEIGHT, width - 2, ELEMENT_HEIGHT, 0xff22333f);
				else if (selectedId == getScrollInEntries() + i)
					Draw.rectangle(x + 1, y + HEIGHT + i * ELEMENT_HEIGHT, width - 2, ELEMENT_HEIGHT, 0xff151525);
				String name = noValue ? "None"
						: this.fontRenderer.trimStringToWidth(entries.get(entryID).getDisplayName(), getWidth());
				this.fontRenderer.drawStringWithShadow(name, x + 3, y + HEIGHT + i * ELEMENT_HEIGHT + 2,
						noValue ? 0x999999 : 0xe2e2e2);
			}

			if (showScrollBar())
			{
				Draw.rectangle(x + width - SCROLLBAR_WIDTH - 1, y + HEIGHT, SCROLLBAR_WIDTH,
						elements * ELEMENT_HEIGHT + 2, 0xff222222);
				Draw.rectangle(x + width - SCROLLBAR_WIDTH - 1, y + HEIGHT + scrollBarY, SCROLLBAR_WIDTH,
						scrollBarHeight, scrollBarHovered ? 0xff666666 : 0xff444444);
			}
		}
	}

	public int getWidth()
	{
		return width;
	}

	public int getVisualHeight()
	{
		return dropped ? HEIGHT + getNumberOfElements() * ELEMENT_HEIGHT + 3 : HEIGHT;
	}

	public class Entry
	{
		private String displayName;
		private Object value;
		private int ordinal;

		public Entry(int ordinal)
		{
			this.displayName = "";
			this.value = ordinal;
			this.ordinal = ordinal;
		}

		public Entry(int ordinal, String displayName, Object value)
		{
			this.displayName = displayName;
			this.value = value;
		}

		public String getDisplayName()
		{
			return displayName;
		}

		public Object getValue()
		{
			return value;
		}
	}

	public Entry getSelectedEntry()
	{
		if (selectedId < 0 || selectedId >= entries.size() + (noValueAllowed ? 1 : 0))
			return null;
		if (noValueAllowed && selectedId == 0)
			return null;
		return entries.get(selectedId - (noValueAllowed ? 1 : 0));
	}

	public int getNumberOfElements()
	{
		int elements = entries.size() + (noValueAllowed ? 1 : 0);
		if (entryAmount > 0)
			elements = Math.min(elements, entryAmount);
		return elements;
	}

	public GuiDropDownList allowNoValue()
	{
		noValueAllowed = true;
		return this;
	}

	public GuiDropDownList forbidNoValue()
	{
		noValueAllowed = false;
		return this;
	}

	public boolean showScrollBar()
	{
		return entries.size() > getNumberOfElements();
	}

	public float getScrollPercentage()
	{
		return (float) this.scrollAmount / (entries.size() - getNumberOfElements());
	}

	public int getScrollInEntries()
	{
		return (int) Math.max(0, scrollAmount);
	}

	public void scroll(float amount)
	{
		scrollAmount = GUtil.clamp(scrollAmount + amount, 0, entries.size() - getNumberOfElements());
		scrollBarY = (int) (getScrollPercentage() * (getNumberOfElements() * ELEMENT_HEIGHT + 2 - scrollBarHeight));
	}

	public void selectValue(Object value)
	{
		for (int i = 0; i < entries.size(); i++)
		{
			if (entries.get(i).value.equals(value))
			{
				selectedId = i + (noValueAllowed ? 1 : 0);
				return;
			}
		}
		selectedId = 0;
	}

	public Object getSelectedValue()
	{
		if (getSelectedEntry() == null)
			return null;
		return getSelectedEntry().getValue();
	}

	public void updateDimensions()
	{
		scrollBarHeight = (int) ((float) getNumberOfElements() / entries.size()
				* (getNumberOfElements() * ELEMENT_HEIGHT + 2));
		scrollBarY = (int) (getScrollPercentage() * (getNumberOfElements() * ELEMENT_HEIGHT + 2 - scrollBarHeight));
	}

	public void setChangesHandled()
	{
		valueChangeUnhandled = false;
	}

	public boolean areChangedUnhandled()
	{
		return valueChangeUnhandled;
	}

	/*-- LISTENERS --*/
	
	public void addListener(ChangeListener listener)
	{
		this.changeListeners.add(listener);
	}
	
	private void notifyChanged()
	{
		for (ChangeListener listener : this.changeListeners)
		{
			listener.onDropDownListChanged();
		}
	}
	
	public static interface ChangeListener
	{
		void onDropDownListChanged();
	}
}

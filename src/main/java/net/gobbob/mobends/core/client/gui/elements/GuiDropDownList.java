package net.gobbob.mobends.core.client.gui.elements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.IObservable;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class GuiDropDownList<T> implements IObservable
{
	public static final int HEIGHT = 16;
	public static final int LIST_HEIGHT = 90;
	public static final int ELEMENT_HEIGHT = 11;
	public static final int SCROLLBAR_WIDTH = 5;

	private final FontRenderer fontRenderer;
	protected final List<Entry<T>> entries;
	private int x, y;
	private int width;
	private int selectedIndex = 0;
	/**
	 * The amount of entries that will be shown at a time.
	 * If the list exceeds that amount, a scroll bar will appear.
	 */
	private int amountOfEntriesShown = 5;
	private boolean noValueAllowed = true;
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

	/**
	 * These listeners will respond, when a change has occurred in this object.
	 */
	private List<IChangeListener> changeListeners = new LinkedList<>();
	
	public List<IChangeListener> getChangeListeners()
	{
		return this.changeListeners;
	}
	
	public GuiDropDownList()
	{
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
		this.entries = new ArrayList<>();
		this.selectedIndex = 0;
		this.width = 94;
		this.scrollAmount = 0;
	}

	public GuiDropDownList init()
	{
		this.entries.clear();
		this.selectedIndex = 0;
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

	public GuiDropDownList addEntry(String label, T value)
	{
		this.entries.add(new Entry<T>(label, value));
		updateDimensions();
		return this;
	}

	public GuiDropDownList setAmountOfEntriesShown(int amount)
	{
		this.amountOfEntriesShown = amount;
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
		{
			dropped = !dropped;
		}
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
					if (hoveredEntryId != this.selectedIndex)
					{
						this.selectedIndex = hoveredEntryId;
						this.notifyChanged();
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

		int mouseWheelRoll = -Mouse.getEventDWheel();
		if (dropped && listHovered)
		{
			if (mouseWheelRoll != 0)
			{
				mouseWheelRoll = mouseWheelRoll > 0 ? 1 : -1;
				scroll(mouseWheelRoll * 0.5F);
			}
			return true;
		}
		else if (hovered)
		{
			if (mouseWheelRoll != 0)
			{
				mouseWheelRoll = mouseWheelRoll > 0 ? 1 : -1;
				selectedIndex = (int) GUtil.clamp(selectedIndex + mouseWheelRoll, 0, entries.size() + (noValueAllowed ? 0 : -1));
				this.notifyChanged();
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

		Draw.rectangle(this.x, this.y, this.width, HEIGHT, -6250336);
		Draw.rectangle(this.x + 1, this.y + 1, this.width - 2, HEIGHT - 2,
				hovered || dropped ? 0xff222222 : -16777216);
		GlStateManager.color(1, 1, 1, 1);

		boolean noValue = noValueAllowed && selectedIndex == 0;
		String text = noValue ? "None"
				: this.fontRenderer.trimStringToWidth(getSelectedEntry().getLabel(), this.getWidth() - 20);
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
				else if (selectedIndex == getScrollInEntries() + i)
					Draw.rectangle(x + 1, y + HEIGHT + i * ELEMENT_HEIGHT, width - 2, ELEMENT_HEIGHT, 0xff151525);
				String name = noValue ? "None"
						: this.fontRenderer.trimStringToWidth(entries.get(entryID).getLabel(), getWidth());
				this.fontRenderer.drawStringWithShadow(name, x + 3, y + HEIGHT + i * ELEMENT_HEIGHT + 2,
						noValue ? 0x999999 : 0xe2e2e2);
			}

			if (shouldShowScrollBar())
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

	public static class Entry<T>
	{
		private String label;
		private T value;

		public Entry(String label, T value)
		{
			this.label = label;
			this.value = value;
		}

		public String getLabel()
		{
			return label;
		}

		public T getValue()
		{
			return value;
		}
	}

	public Entry<T> getSelectedEntry()
	{
		if (selectedIndex < 0 || selectedIndex >= entries.size() + (noValueAllowed ? 1 : 0))
			return null;
		if (noValueAllowed && selectedIndex == 0)
			return null;
		return entries.get(selectedIndex - (noValueAllowed ? 1 : 0));
	}

	public int getNumberOfElements()
	{
		int elements = entries.size() + (noValueAllowed ? 1 : 0);
		if (amountOfEntriesShown > 0)
			elements = Math.min(elements, amountOfEntriesShown);
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

	public boolean shouldShowScrollBar()
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

	public void selectValue(T value)
	{
		int previouslySelected = this.selectedIndex;
		for (int i = 0; i < entries.size(); i++)
		{
			if (entries.get(i).value.equals(value))
			{
				this.selectedIndex = i + (noValueAllowed ? 1 : 0);
				if (previouslySelected != this.selectedIndex)
					this.notifyChanged();
				return;
			}
		}
		this.selectedIndex = 0;
		
		if (previouslySelected != this.selectedIndex)
			this.notifyChanged();
	}

	public T getSelectedValue()
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
}

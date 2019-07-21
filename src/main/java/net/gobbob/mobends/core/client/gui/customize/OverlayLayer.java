package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.IObservable;
import net.gobbob.mobends.core.client.gui.customize.store.CustomizeStore;
import net.gobbob.mobends.core.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.core.client.gui.elements.GuiToggleButton;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.store.ISubscriber;
import net.gobbob.mobends.core.store.Subscription;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedList;
import java.util.List;

import static net.gobbob.mobends.core.client.gui.customize.store.CustomizeMutations.SHOW_ALTER_ENTRY;

public class OverlayLayer implements IGuiLayer, IChangeListener, ISubscriber
{

	private int screenWidth;
	private int screenHeight;
	private final GuiDropDownList<AlterEntry<?>> targetList;
	private final GuiToggleButton toggleButton;
	private final GuiPartHierarchy hierarchy;
	private final GuiPartProperties properties;

	public OverlayLayer(GuiCustomizeWindow customizeWindow)
	{
		this.screenWidth = customizeWindow.width;
		this.screenHeight = customizeWindow.height;
		this.targetList = new GuiDropDownList().forbidNoValue();
		this.toggleButton = new GuiToggleButton("Animated", 64);
		this.hierarchy = new GuiPartHierarchy();
		this.properties = new GuiPartProperties();

		for (AlterEntry alterEntry : customizeWindow.alterEntries)
		{
			this.targetList.addEntry(alterEntry.getLocalizedName(), alterEntry);
		}

		this.targetList.addListener(this);
		this.targetList.selectValue(CustomizeStore.getCurrentAlterEntry());

		this.trackSubscription(CustomizeStore.observeAlterEntry((AlterEntry<?> alterEntry) ->
		{
			this.targetList.selectValue(alterEntry);
			this.toggleButton.setToggleState(alterEntry.isAnimated());
		}));
	}

	private List<Subscription<?>> subscriptions = new LinkedList<>();

	@Override
	public List<Subscription<?>> getSubscriptions() { return this.subscriptions; }

	@Override
	public void cleanUp()
	{
		this.hierarchy.cleanUp();
		this.properties.cleanUp();
		this.removeSubscriptions();
	}

	public void initGui()
	{
		this.targetList.setPosition(2, 2);
		this.toggleButton.initGui(10, 30);
		this.hierarchy.initGui();
		this.properties.initGui();

		AlterEntry<?> alterEntry = CustomizeStore.getCurrentAlterEntry();
		if (alterEntry != null)
			this.toggleButton.setToggleState(alterEntry.isAnimated());
	}

	@Override
	public void handleResize(int width, int height)
	{
		this.screenWidth = width;
		this.screenHeight = height;
	}
	
	@Override
	public void update(int mouseX, int mouseY)
	{
		this.targetList.update(mouseX, mouseY);
		this.hierarchy.update(mouseX, mouseY);
		this.properties.update(mouseX, mouseY);
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
		this.properties.draw();
		this.targetList.display();
	}
	
	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int button)
	{
		boolean eventHandled = false;
		
		eventHandled |= this.targetList.mouseClicked(mouseX, mouseY, button);

		if (!eventHandled && this.toggleButton.mouseClicked(mouseX, mouseY, button))
		{
			AlterEntry<?> alterEntry = CustomizeStore.getCurrentAlterEntry();
			if (alterEntry != null)
				alterEntry.setAnimate(this.toggleButton.getToggleState());
			
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
		{
			eventHandled |= this.hierarchy.handleMouseInput();
		}

		return eventHandled;
	}

	@Override
	public void handleChange(IObservable objectChanged)
	{
		if (objectChanged == this.targetList)
		{
			CustomizeStore.instance.commit(SHOW_ALTER_ENTRY, this.targetList.getSelectedValue());
		}
	}
	
}

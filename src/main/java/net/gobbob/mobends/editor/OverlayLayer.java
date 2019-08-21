package net.gobbob.mobends.editor;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.IObservable;
import net.gobbob.mobends.core.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.core.client.gui.elements.GuiToggleButton;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.store.ISubscriber;
import net.gobbob.mobends.core.store.Subscription;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.editor.gui.GuiCustomizeWindow;
import net.gobbob.mobends.editor.gui.GuiPartHierarchy;
import net.gobbob.mobends.editor.gui.GuiPartProperties;
import net.gobbob.mobends.editor.gui.GuiStateEditor;
import net.gobbob.mobends.editor.project.BendsPackProject;
import net.gobbob.mobends.editor.store.CustomizeStore;
import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedList;
import java.util.List;

import static net.gobbob.mobends.editor.store.CustomizeMutations.SHOW_ANIMATED_ENTITY;

public class OverlayLayer implements IGuiLayer, IChangeListener, ISubscriber
{

	private int screenWidth;
	private int screenHeight;
	private final GuiDropDownList<AnimatedEntity<?>> targetList;
	private final GuiToggleButton toggleButton;
	private final GuiPartHierarchy hierarchy;
	private final GuiPartProperties properties;
	private final GuiStateEditor stateEditor;

	public OverlayLayer(GuiCustomizeWindow customizeWindow)
	{
		this.screenWidth = customizeWindow.width;
		this.screenHeight = customizeWindow.height;
		this.targetList = new GuiDropDownList().forbidNoValue();
		this.toggleButton = new GuiToggleButton("Animated", 64);
		this.hierarchy = new GuiPartHierarchy();
		this.properties = new GuiPartProperties();
		this.stateEditor = new GuiStateEditor(new BendsPackProject());

		for (AnimatedEntity animatedEntity : customizeWindow.animatedEntities)
		{
			this.targetList.addEntry(animatedEntity.getLocalizedName(), animatedEntity);
		}

		this.targetList.addListener(this);
		this.targetList.selectValue(CustomizeStore.getCurrentAnimatedEntity());

		this.trackSubscription(CustomizeStore.observeAnimatedEntity((AnimatedEntity<?> animatedEntity) ->
		{
			this.targetList.selectValue(animatedEntity);
			this.toggleButton.setToggleState(animatedEntity.isAnimated());
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
		this.stateEditor.initGui();

		AnimatedEntity<?> animatedEntity = CustomizeStore.getCurrentAnimatedEntity();
		if (animatedEntity != null)
			this.toggleButton.setToggleState(animatedEntity.isAnimated());
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
		this.stateEditor.update(mouseX, mouseY);
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
		this.stateEditor.draw();
		this.targetList.display();
	}
	
	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int button)
	{
		boolean eventHandled = false;

		eventHandled |= this.targetList.mouseClicked(mouseX, mouseY, button);

		if (!eventHandled && this.stateEditor.handleMouseClicked(mouseX, mouseY, button))
		{
			eventHandled = true;
		}

		if (!eventHandled && this.toggleButton.mouseClicked(mouseX, mouseY, button))
		{
			AnimatedEntity<?> animatedEntity = CustomizeStore.getCurrentAnimatedEntity();
			if (animatedEntity != null)
				animatedEntity.setAnimate(this.toggleButton.getToggleState());
			
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
			CustomizeStore.instance.commit(SHOW_ANIMATED_ENTITY, this.targetList.getSelectedValue());
		}
	}
	
}

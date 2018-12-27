package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.gui.IChangeListener;
import net.gobbob.mobends.core.client.gui.Observable;
import net.gobbob.mobends.core.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.core.client.gui.elements.GuiIconButton;
import net.gobbob.mobends.core.client.gui.elements.GuiPortraitDisplay;
import net.gobbob.mobends.core.main.ModStatics;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.BendsTarget;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.core.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class GuiCustomizeWindow extends Gui implements IChangeListener
{
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/node_editor.png");
	static final int WIDTH = 230;
	static final int HEIGHT = 232;
	
	private int x, y;
	private FontRenderer fontRenderer;
	
	private GuiNodeEditor nodeEditor;
	private GuiIconButton helpButton;
	private GuiIconButton bigModeButton;
	private GuiParameterEditor parameterEditor;
	private GuiPortraitDisplay portraitDisplay;
	private GuiBendsMenu mainMenu;
	private GuiDropDownList targetList;
	private AlterEntry currentAlterEntry;

	public GuiCustomizeWindow(GuiBendsMenu mainMenu)
	{
		this.x = 0;
		this.y = 0;
		this.nodeEditor = new GuiNodeEditor(this, mainMenu);
		this.helpButton = new GuiIconButton(247, 0, 9, 13);
		this.bigModeButton = new GuiIconButton(234, 0, 13, 13);
		this.parameterEditor = new GuiParameterEditor();
		this.parameterEditor.addListener(this);
		this.portraitDisplay = new GuiPortraitDisplay();
		this.mainMenu = mainMenu;
		
		this.targetList = new GuiDropDownList().forbidNoValue();
		this.targetList.addListener(this);
		for (AlterEntry alterEntry : mainMenu.alterEntries)
		{
			this.targetList.addEntry(alterEntry.getDisplayName());
		}
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
	}

	public void initGui(int x, int y, int pX, int pY)
	{
		this.x = x;
		this.y = y;
		this.nodeEditor.initGui(x + 7, y + 97);
		this.helpButton.initGui(x + 203, y + 20);
		this.bigModeButton.initGui(x + 151, y + 73);
		this.parameterEditor.initGui(pX, pY);
		this.portraitDisplay.initGui(x + 8, y + 21);
		this.targetList.setPosition(x + 63, y + 20);
	}

	public void display(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCustomizeWindow.BACKGROUND_TEXTURE);
		this.drawTexturedModalRect(this.x, this.y, 0, 0, this.WIDTH, this.HEIGHT);
		BendsTarget bendsTarget = BendsPack.getTarget(this.currentAlterEntry.getName());

		this.drawCenteredString(this.fontRenderer, Lang.format("mobends.gui.customize"),
				(int) (this.x + this.WIDTH/2), this.y + 4, 0xFFFFFF);
		
		this.targetList.display();
		this.portraitDisplay.display(partialTicks);

		if (!PackManager.isCurrentPackLocal())
		{
			this.drawCenteredString(fontRenderer, Lang.format("mobends.gui.chooseapacktoedit"),
					this.x + this.WIDTH / 2, this.y + 135, 0xffffff);
			
			return;
		}
		
		this.nodeEditor.display(mouseX, mouseY, partialTicks);
		this.parameterEditor.display(mouseX, mouseY, partialTicks);
		this.helpButton.display();
		this.bigModeButton.display();
	}

	public void populate(AlterEntry alterEntry)
	{
		BendsTarget target = BendsPack.getTarget(alterEntry.getName());
		
		this.currentAlterEntry = alterEntry;
		this.nodeEditor.populate(alterEntry);
		this.parameterEditor.populate(alterEntry); 
		this.parameterEditor.deselect();
		this.targetList.selectValue(mainMenu.currentAlterEntry);
		
		this.portraitDisplay.setViewEntity(alterEntry.getName().equalsIgnoreCase("player")
				? Minecraft.getMinecraft().player
				: alterEntry.getEntity());
		this.portraitDisplay.setValue(alterEntry.isAnimated());
	}

	public void update(int mouseX, int mouseY)
	{
		this.nodeEditor.update(mouseX, mouseY);
		this.parameterEditor.update(mouseX, mouseY);
		this.portraitDisplay.update(mouseX, mouseY);
		this.targetList.update(mouseX, mouseY);
		this.helpButton.update(mouseX, mouseY);
		this.bigModeButton.update(mouseX, mouseY);
	}

	public void mouseClicked(int mouseX, int mouseY, int state)
	{
		if (state != 0)
			return;

		boolean lastUnappliedChanged = this.nodeEditor.areChangesUnapplied();

		boolean pressed = false;

		if (this.targetList.mouseClicked(mouseX, mouseY, state))
		{
			applyTargetListChanges(lastUnappliedChanged);
			pressed = true;
		}

		if (this.portraitDisplay.mouseClicked(mouseX, mouseY, state))
		{
			this.mainMenu.getCurrentAlterEntry().setAnimate(this.portraitDisplay.getValue());
		}
		
		if (!PackManager.isCurrentPackLocal())
			return;

		if (!pressed && this.parameterEditor.mouseClicked(mouseX, mouseY, state))
			pressed = true;
		else
		{
			if (this.nodeEditor.mouseClicked(mouseX, mouseY, state))
			{
				pressed = true;
				GuiConditionSection section = this.parameterEditor.selectedSection;
				if (section != null)
				{
					this.portraitDisplay.setAnimationToPreview(section.getAnimationName());
				}
			}
		}

		if (this.helpButton.mouseClicked(mouseX, mouseY, state))
		{
			mainMenu.popUpHelp();
		}

		if (!pressed)
		{
			this.parameterEditor.deselect();
		}
	}

	public void mouseReleased(int mouseX, int mouseY, int event)
	{
		this.nodeEditor.mouseReleased(mouseX, mouseY, event);
		this.parameterEditor.mouseReleased(mouseX, mouseY, event);
	}

	public void handleMouseInput()
	{
		if (targetList.handleMouseInput())
		{
			this.applyTargetListChanges(this.nodeEditor.areChangesUnapplied());
			return;
		}

		if (!this.parameterEditor.handleMouseInput())
		{
			this.nodeEditor.handleMouseInput();
		}
	}

	public void applyTargetListChanges(boolean unappliedChanged)
	{
		if (targetList.areChangedUnhandled())
		{
			if (unappliedChanged)
			{
				mainMenu.popUpDiscardChanges(mainMenu.POPUP_CHANGETARGET);
			}
			else
			{
				mainMenu.selectAlterEntry((Integer) targetList.getSelectedValue());
			}
			targetList.setChangesHandled();
		}
	}

	public void onNodesChange()
	{
		this.nodeEditor.onChange();
	}
	
	public void applyChanges(BendsTarget target)
	{
		this.nodeEditor.applyChanges(target);
	}
	
	public void keyTyped(char typedChar, int keyCode)
	{
		if (!PackManager.isCurrentPackLocal())
			return;

		this.parameterEditor.keyTyped(typedChar, keyCode);
	}

	public boolean areChangesUnapplied()
	{
		return this.nodeEditor.areChangesUnapplied();
	}
	
	public GuiNodeEditor getNodeEditor()
	{
		return this.nodeEditor;
	}
	
	public GuiParameterEditor getParameterEditor()
	{
		return this.parameterEditor;
	}

	public GuiDropDownList getTargetList()
	{
		return targetList;
	}
	
	public String[] getAlterableParts()
	{
		if (this.currentAlterEntry != null)
			return this.currentAlterEntry.getOwner().getAlterableParts();
		else
			return null;
	}
	
	@Override
	public void handleChange(Observable changedObject)
	{
		this.onNodesChange();
	}
}

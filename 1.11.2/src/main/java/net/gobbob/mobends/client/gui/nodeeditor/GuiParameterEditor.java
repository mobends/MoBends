package net.gobbob.mobends.client.gui.nodeeditor;

import java.util.HashMap;
import java.util.function.BiConsumer;

import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.client.gui.elements.GuiCompactTextField;
import net.gobbob.mobends.client.gui.elements.GuiCustomButton;
import net.gobbob.mobends.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.client.gui.elements.GuiRadio;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.pack.BendsAction.EnumModifier;
import net.gobbob.mobends.pack.BendsAction.EnumOperator;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiParameterEditor {
	public GuiNodeEditor nodeEditor;
	public GuiAnimationSection selectedSection;
	public GuiAnimationNode selectedNode;
	public GuiCalculation selectedCalculation;
	
	public int x;
	public int y;
	public int width;
	public int height;
	public EnumChanged changedState;
	
	private String title;
	private FontRenderer fontRenderer;
	public GuiCompactTextField textField;
	public GuiRadio radioField;
	public GuiRadio radioField2;
	public GuiCustomButton removeButton;
	public GuiDropDownList dropDownList;
	public GuiDropDownList modifierList;
	private HashMap partMap;
	
	public GuiParameterEditor(GuiNodeEditor nodeEditor) {
		this.nodeEditor = nodeEditor;
		this.x = 0;
		this.y = 0;
		this.width = 100;
		this.height = 130;
		this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		this.textField = new GuiCompactTextField(0, Minecraft.getMinecraft().fontRendererObj, x+4, y+14, width-8, 14);
		this.radioField = new GuiRadio(nodeEditor);
		this.radioField2 = new GuiRadio(nodeEditor);
		this.radioField2.setButton(0, 76, 10, 12).setElement(36, 64, 6, 8).setNumberOfElements(4)
		  				.setOffset(2, 2).setPadding(0, 4).setBackground(42, 48, 46, 16);
		this.removeButton = new GuiCustomButton(60, 20);
		this.dropDownList = new GuiDropDownList(nodeEditor).setEntryAmount(5);
		this.modifierList = new GuiDropDownList(nodeEditor);
		for(int i = 0; i < EnumModifier.values().length; i++) {
			this.modifierList.addEntry(EnumModifier.values()[i].getDisplayName());
		}
		this.title = "";
	}
	
	public void initGui(int x, int y) {
		this.x = x;
		this.y = y;
		radioField.initGui(x+3, y+30);
		radioField2.initGui(x+46, y+30);
		textField.setPosition(x+4, y+14);
		removeButton.setPosition(x+4, y+height-24);
		modifierList.setPosition(x+3, y+50);
		
		if(changedState != null) {
			switch(changedState) {
				case CALCULATION:
					dropDownList.setPosition(x+3, y+50);
					break;
				case CONDITION:
					break;
				case NODE:
					dropDownList.setPosition(x+3, y+13);
					break;
				default:
					break;
			}
		}
	}
	
	public void display(int mouseX, int mouseY) {
		if(!isEnabled()) return;
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
		/* Top 			*/ Draw.texturedModalRect(x, y-4, width, 4, 64, 82, 1, 4);
		/* Top-Edge 	*/ Draw.texturedModalRect(x+width, y-4, 4, 4, 65, 82, 4, 4);
		/* Side 		*/ Draw.texturedModalRect(x+width, y, 4, height, 65, 86, 4, 1);
		/* Bottom-Edge 	*/ Draw.texturedModalRect(x+width, y+height, 4, 4, 65, 87, 4, 4);
		/* Bottom		*/ Draw.texturedModalRect(x, y+height, width, 4, 64, 87, 1, 4);
		/* Inside		*/ Draw.texturedModalRect(x, y, width, height, 64, 86, 1, 1);
		
		if(changedState != null) {
			this.removeButton.drawButton(mouseX, mouseY);
			fontRenderer.drawStringWithShadow(title, x+(width-fontRenderer.getStringWidth(title))/2,y+3, 0xffffff);
		}
		
		textField.drawTextBox();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		radioField.display();
		radioField2.display();
		modifierList.display();
		dropDownList.display();
	}
	
	public void select(GuiAnimationSection section) {
		selectedSection = section;
		selectedNode = null;
		selectedCalculation = null;
		changedState = EnumChanged.CONDITION;
		
		title = I18n.format("mobends.gui.condition", new Object[0]);
		textField.setVisible(true);
		textField.setPlaceholderText("Animation");
		textField.setText(String.valueOf(section.getAnimationName()));
		radioField.disable();
		radioField2.disable();
		removeButton.setText("Remove");
		removeButton.width = width-8;
		dropDownList.disable();
		modifierList.disable();
	}
	
	public void select(GuiAnimationNode node) {
		selectedSection = null;
		selectedNode = node;
		selectedCalculation = null;
		changedState = EnumChanged.NODE;
		
		title = I18n.format("mobends.gui.node", new Object[0]);
		textField.setVisible(false);
		radioField.enable();
		radioField.setButton(0, 64, 12, 12).setElement(81, 0, 10, 10).setNumberOfElements(3)
		  		  .setOffset(2, 2).setPadding(0, 3).setBackground(0, 48, 42, 16);
		radioField2.enable();
		radioField.choose(node.getProperty().ordinal());
		radioField2.choose(node.getAxis() == null ? 0 : node.getAxis().ordinal()+1);
		removeButton.setText("Remove");
		removeButton.width = width-8;
		
		dropDownList.init().setEntryAmount(5).forbidNoValue();
		dropDownList.setPosition(x+3, y+13);
		if(nodeEditor.getAlterableParts() != null) {
			for(String part : nodeEditor.getAlterableParts()) {
				dropDownList.addEntry(part, part);
			}
		}
		dropDownList.selectValue(selectedNode.getModel());
		modifierList.enable();
		modifierList.selectValue(node.getModifier() == null ? null : node.getModifier().ordinal());
	}
	
	public void select(GuiCalculation calculation) {
		selectedSection = null;
		selectedNode = null;
		selectedCalculation = calculation;
		calculation.setSelected(true);
		changedState = EnumChanged.CALCULATION;
		
		title = I18n.format("mobends.gui.calculation", new Object[0]);
		textField.setVisible(true);
		textField.setPlaceholderText("Box");
		textField.setText(String.valueOf(calculation.getValue()));
		radioField.enable();
		radioField.setButton(51, 0, 10, 10).setElement(21, 6, 6, 6).setNumberOfElements(5)
				  .setOffset(2, 2).setPadding(0, 4).setBackground(51, 10, 57, 14);
		radioField2.disable();
		radioField.choose(calculation.operator.ordinal());
		removeButton.setText("Remove");
		removeButton.width = width-8;
		
		dropDownList.init().setEntryAmount(5).allowNoValue();
		dropDownList.setPosition(x+3, y+50);
		BendsVariable.variables.forEach(new BiConsumer() {
			@Override
			public void accept(Object key, Object value) {
				dropDownList.addEntry(((BendsVariable) value).getDisplayName(), key);
			}
		});
		dropDownList.selectValue(selectedCalculation.globalVar);
		modifierList.disable();
		textField.setEnabled(selectedCalculation.getGlobalVariable() == null);
	}
	
	public void deselect() {
		if(changedState != null) {
			switch(changedState) {
				case CALCULATION:
					selectedCalculation.setSelected(false);
					break;
				case CONDITION:
					selectedSection.setSelected(false);
					break;
				case NODE:
					selectedNode.setSelected(false);
					break;
				default:
					break;
			}
		}
		
		selectedSection = null;
		selectedNode = null;
		selectedCalculation = null;
		changedState = null;
	}
	
	public boolean isEnabled() {
		return changedState != null;
	}
	
	public void keyTyped(char typedChar, int keyCode) {
		if(!isEnabled()) return;
		
		if (textField.isFocused())
        {
            textField.textboxKeyTyped(typedChar, keyCode);
            if(changedState != null) {
            	nodeEditor.onChange();
    			switch(changedState) {
    				case CALCULATION:
    					try {
    						selectedCalculation.setValue(Float.parseFloat(textField.getText()));
    					}catch(java.lang.NumberFormatException e) {
    						selectedCalculation.setValue(0);
    					}
    					selectedCalculation.getParentNode().updateAddNodeX();
    					break;
    				case CONDITION:
    					selectedSection.setAnimationName(textField.getText());
    					break;
    				case NODE:
    					selectedNode.setModel(textField.getText());
    					break;
    				default:
    					break;
    			}
    		}
        }
	}
	
	public enum EnumChanged {
		CONDITION, NODE, CALCULATION
	}

	public void update(int mouseX, int mouseY) {
		if(!isEnabled()) return;
		
		textField.updateCursorCounter();
		radioField.update(mouseX, mouseY);
		radioField2.update(mouseX, mouseY);
		dropDownList.update(mouseX, mouseY);
		modifierList.update(mouseX, mouseY);
	}

	public boolean mouseClicked(int mouseX, int mouseY, int event) {
		if(!isEnabled()) return false;
		
		if(dropDownList.mouseClicked(mouseX, mouseY, event)) {
			updateDropListChoice();
		}else if(modifierList.mouseClicked(mouseX, mouseY, event)) {
			selectedNode.setModifier(modifierList.getSelectedValue() == null ? null : EnumModifier.values()[(Integer)modifierList.getSelectedValue()]);
		}else{
			textField.mouseClicked(mouseX, mouseY, event);
			if(radioField.onMousePressed(mouseX, mouseY, event))
				updateRadioChoice();
			if(radioField2.onMousePressed(mouseX, mouseY, event))
				updateAxisChoice();
		
			if(changedState != null) {
				if(removeButton.mousePressed(mouseX, mouseY))
					removeSelected();
			}
		}
		
		return mouseX >= x && mouseX <= x+width &&
			   mouseY >= y && mouseY <= y+height;
	}
	
	public void mouseReleased(int mouseX, int mouseY, int event) {
		dropDownList.onMouseReleased(mouseX, mouseY, event);
	}
	
	public boolean handleMouseInput() {
		if(dropDownList.handleMouseInput()) {
			updateDropListChoice();
			return true;
		}else if(modifierList.handleMouseInput()) {
			selectedNode.setModifier(modifierList.getSelectedValue() == null ? null : EnumModifier.values()[(Integer)modifierList.getSelectedValue()]);
			return true;
		}
		return false;
	}

	public void updateDropListChoice() {
		if(changedState != null) {
			switch(changedState) {
				case CALCULATION:
					this.selectedCalculation.setGlobalVariable((String)dropDownList.getSelectedValue());
					this.textField.setEnabled(selectedCalculation.getGlobalVariable() == null);
					this.selectedCalculation.getParentNode().updateAddNodeX();
				break;
				case NODE:
					this.selectedNode.setModel((String)dropDownList.getSelectedValue());
				break;
				default:
				break;
			}
		}
	}

	public void updateRadioChoice() {
		if(changedState != null) {
			switch(changedState) {
				case CALCULATION:
					this.selectedCalculation.operator = EnumOperator.values()[radioField.selectedId];
				break;
				case NODE:
					this.selectedNode.setProperty(EnumBoxProperty.values()[radioField.selectedId]);
				break;
				default:
				break;
			}
		}
	}
	
	public void updateAxisChoice() {
		if(changedState == EnumChanged.NODE) {
			selectedNode.setAxis(radioField2.selectedId == 0 ? null : EnumAxis.values()[radioField2.selectedId-1]);
		}
	}
	
	public void removeSelected() {
		if(changedState == null) return;
		
		switch(this.changedState) {
			case CONDITION:
				selectedSection.remove();
				break;
			case NODE:
				selectedNode.remove();	
				break;
			case CALCULATION:
				selectedCalculation.remove();
				break;
			default:
				break;
		}
		
		deselect();
	}
}

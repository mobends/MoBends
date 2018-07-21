package net.gobbob.mobends.client.gui.customize;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.pack.BendsAction;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.pack.BendsAction.EnumModifier;
import net.gobbob.mobends.pack.BendsCondition;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.client.Minecraft;

public class GuiAnimationNode {
	public static final int HEIGHT = 15;
	public static int ADD_NODE_WIDTH = 20;
	
	private GuiConditionSection parentSection;
	private GuiParameterEditor parameterEditor;
	private List<GuiCalculation> calculations;
	private EnumAxis axis;
	private EnumBoxProperty property;
	private EnumModifier modifier;
	private String model;
	private int width = 0;
	public boolean hover;
	private boolean selected;
	private int addNodeX;
	private boolean addNodeHover;
	private int modifierWidth;
	
	public GuiAnimationNode(GuiConditionSection parentSection) {
		this.parentSection = parentSection;
		this.calculations = new ArrayList<GuiCalculation>();
		this.parameterEditor = this.parentSection.getCustomizeWindow().getParameterEditor();
		this.axis = null;
		this.property = EnumBoxProperty.ROT;
		this.addNodeX = 0;
		this.addNodeHover = false;
	}

	public GuiAnimationNode(GuiConditionSection parentSection, BendsAction action) {
		this(parentSection);
		this.axis = action.axis;
		this.property = action.property;
		for(int i = 0; i < action.calculations.size(); i++) {
			this.calculations.add(new GuiCalculation(this, action.calculations.get(i)));
		}
		this.setModel(action.model);
		this.setModifier(action.modifier);
	}

	public int display(int x, int y) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
        int textureX = this.selected ? 28 : this.hover ? 14 : 0;
        Draw.texturedModalRect(x+16, y, textureX, 16, 3, 16);
        Draw.texturedModalRect(x+16+3, y, width, 16, textureX+3, 16, 1, 16);
        Draw.texturedModalRect(x+16+3+width, y, textureX+4, 16, 1, 16);
        Draw.texturedModalRect(x+16+3+width+1, y, 26, 16, textureX+3, 16, 1, 16);
        Draw.texturedModalRect(x+16+3+width+1+26, y, textureX+5, 16, 9, 16);
        
        int propertyTextureX = 81+this.property.ordinal()*10;
        Draw.texturedModalRect(x+16+3+width+4, y+2, propertyTextureX, 0, 10, 10);
        
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(getModel(), x+20,y+3, 0xffffff);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(getAxisString(), x+16+3+width+18,y+3, 0xffffff);
        
        if(modifier != null) {
        	Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
            int modifierTextureX = this.selected ? 34 : this.hover ? 17 : 0;
            Draw.thsPuzzle(x+width+46, y, 7, modifierWidth, 9, 16, modifierTextureX, 88);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(getModifier().name(), x+width+56,y+3, 0xffffff);
        }
        
        int xOffset = x+this.getVisualWidth();
        for(int i = 0; i < this.calculations.size(); i++) {
        	xOffset = this.calculations.get(i).display(xOffset, y);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
        Draw.texturedModalRect(x+addNodeX, y, addNodeHover ? 74 : 54, 24, 20, 16); //Add Node
        
        y+=HEIGHT;
		return y;
	}

	private String getAxisString() {
		return axis == null ? "?" : axis.toString();
	}

	public void setModel(String model) {
		this.model = model;
		this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(model)+5;
		this.updateAddNodeX();
	}
	
	public String getModel() {
		return model;
	}

	public int getVisualWidth() {
		return this.width+48 + (modifier != null ? modifierWidth+7 : 0);
	}
	
	public void update(int x, int y, int mouseX, int mouseY) {
		int xOffset = x+this.getVisualWidth();
		for(int i = 0; i < this.calculations.size(); i++) {
			this.calculations.get(i).update(xOffset, y, mouseX, mouseY);
			xOffset += this.calculations.get(i).getVisualWidth();
		}
		
		if(mouseX >= x+16 && mouseX < x+this.getVisualWidth() &&
		   mouseY >= y && mouseY < y+GuiAnimationNode.HEIGHT) {
			this.hover = true;
		}else{
			this.hover = false;
		}
		
		if(mouseX >= x+addNodeX && mouseX < x+addNodeX+ADD_NODE_WIDTH &&
		   mouseY >= y && mouseY < y+GuiAnimationNode.HEIGHT) {
			this.addNodeHover = true;
		}else{
			this.addNodeHover = false;
		}
	}
	
	public GuiConditionSection getParentSection() {
		return this.parentSection;
	}

	public boolean onMousePressed(int x, int y, int event) {
		boolean pressed = false;
		
		this.selected = this.hover;
		if(this.selected) {
			this.getParameterEditor().select(this);
		}
		for(int i = 0; i < this.calculations.size(); i++) {
			if(this.calculations.get(i).onMousePressed(x, y, event))
				pressed = true;
		}
		if(addNodeHover) {
			getParameterEditor().select(addCalculation());
			pressed = true;
		}
		return pressed || selected;
	}

	private GuiParameterEditor getParameterEditor() {
		return this.parameterEditor;
	}

	public void onMouseReleased(int x, int y, int event) {
	}

	public void applyChanges(BendsTarget target, BendsCondition condition) {
		BendsAction action = new BendsAction(this.model, this.property, this.axis, 1);
		action.setModifier(modifier);
		condition.addAction(action);
		for(int i = 0; i < this.calculations.size(); i++) {
			this.calculations.get(i).applyChanges(target, action);
		}
	}

	public void setProperty(EnumBoxProperty property) {
		this.property = property;
	}

	public EnumBoxProperty getProperty() {
		return property;
	}
	
	public void setAxis(EnumAxis axis) {
		this.axis = axis;
	}
	
	public EnumAxis getAxis() {
		return axis;
	}
	
	public void setModifier(EnumModifier modifier) {
		this.modifier = modifier;
		if(modifier != null) this.modifierWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(modifier.name())+7;
		updateAddNodeX();
	}
	
	public EnumModifier getModifier() {
		return modifier;
	}
	
	public GuiCalculation addCalculation() {
		GuiCalculation calculation = new GuiCalculation(this);
		calculations.add(calculation);
		this.addNodeX += calculation.getVisualWidth();
		getParentSection().getCustomizeWindow().onNodesChange();
		return calculation;
	}
	
	public void removeCalculation(GuiCalculation guiCalculation) {
		if(calculations.remove(guiCalculation))
			this.addNodeX -= guiCalculation.getVisualWidth();
		parentSection.getCustomizeWindow().onNodesChange();
	}

	public void remove(){
		parentSection.removeNode(this);
	}
	
	public void updateAddNodeX() {
		addNodeX = getVisualWidth();
		for(int i = 0; i < calculations.size(); i++) {
			addNodeX += calculations.get(i).getVisualWidth();
		}
	}
	
	public int getGlobalWidth() {
		int globalWidth = 32 + this.getVisualWidth();
		for(int i = 0; i < calculations.size(); i++) {
			globalWidth += calculations.get(i).getVisualWidth();
		}
		return globalWidth;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
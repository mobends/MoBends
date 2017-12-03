package net.gobbob.mobends.client.gui.nodeeditor;

import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.pack.BendsAction;
import net.gobbob.mobends.pack.BendsAction.Calculation;
import net.gobbob.mobends.pack.BendsAction.EnumOperator;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.util.Draw;
import net.minecraft.client.Minecraft;

public class GuiCalculation {
	public EnumOperator operator;
	public float number;
	public String globalVar = null;
	public int width = 0;
	public String displayedText;
	public boolean hover;
	private boolean selected;
	private GuiAnimationNode parentNode;
	private GuiParameterEditor parameterEditor;
	
	public GuiCalculation(GuiAnimationNode parentNode) {
		this.parentNode = parentNode;
		this.parameterEditor = parentNode.getParentSection().getNodeEditor().getParameterEditor();
		this.operator = EnumOperator.SET;
		this.number = 0.0F;
		this.globalVar = null;
		this.setDisplayedText(String.valueOf(this.number));
	}
	
	public GuiCalculation(GuiAnimationNode parentNode, Calculation calculation) {
		this(parentNode);
		this.operator = calculation.operator;
		this.number = calculation.number;
		this.globalVar = calculation.globalVar;
		if(globalVar != null) {
			this.setDisplayedText(globalVar+"");
		}else{
			this.setDisplayedText(String.valueOf(this.number));
		}
	}

	public int display(int x, int y) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
        int textureX = this.isSelected() ? 36 : this.hover ? 18 : 0;
        Draw.thsPuzzle(x, y, 8, width, 9, 16, textureX, 32);
        
        int operatorTextureX = 21+this.operator.ordinal()*6;
        Draw.texturedModalRect(x+6, y+4, operatorTextureX, 0, 6, 6);
        
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.displayedText, x+17,y+3, 0xffffff);
        
		return x+this.getVisualWidth();
	}

	public void update(int x, int y, int mouseX, int mouseY) {
		if(mouseX >= x && mouseX < x+this.getVisualWidth() &&
		   mouseY >= y && mouseY < y+GuiAnimationNode.HEIGHT) {
			this.hover = true;
		}else{
			this.hover = false;
		}
	}
	
	public void setValue(float f) {
		this.number = f;
		this.globalVar = null;
		this.setDisplayedText(String.valueOf(this.number));
	}
	
	public float getValue() {
		return this.number;
	}
	
	public void setGlobalVariable(String arg0) {
		this.globalVar = arg0;
		if(this.globalVar != null) {
			this.setDisplayedText(globalVar+"");
		}else{
			this.setDisplayedText(String.valueOf(this.number));
		}
	}
	
	public String getGlobalVariable() {
		return globalVar;
	}
	
	public void setDisplayedText(String arg0) {
		this.displayedText = arg0;
		this.width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.displayedText)+12;
	}
	
	public int getVisualWidth() {
		return this.width+10;
	}
	
	public GuiParameterEditor getParameterEditor() {
		return this.parameterEditor;
	}

	public boolean onMousePressed(int x, int y, int event) {
		this.setSelected(this.hover);
		if(this.isSelected()) {
			this.getParameterEditor().select(this);
		}
		return this.isSelected();
	}

	public void onMouseReleased(int x, int y, int event) {
	}

	public GuiAnimationNode getParentNode() {
		return this.parentNode;
	}

	public void applyChanges(BendsTarget target, BendsAction action) {
		action.calculations.add(new Calculation(operator, number).setGlobalVar(globalVar));
	}
	
	public void remove() {
		parentNode.removeCalculation(this);
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}

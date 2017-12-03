package net.gobbob.mobends.client.gui.popup;

import net.gobbob.mobends.client.gui.elements.GuiCompactTextField;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.resources.I18n;

public class GuiPopUpCreatePack extends GuiPopUp {
	protected GuiCompactTextField titleTextField;
	
	public GuiPopUpCreatePack(int action) {
		super(I18n.format("mobends.gui.createpack", new Object[0]), action, new String[]{"Cancel", "Create"});
	}
	
	public void initGui(int x, int y) {
		super.initGui(x, y);
		titleTextField.setPosition(this.x+5, this.y+39);
	}
	
	public void display(int mouseX, int mouseY, float partialTicks) {
		super.display(mouseX, mouseY, partialTicks);
		titleTextField.drawTextBox();
	}
	
	public void update(int mouseX, int mouseY) {
		super.update(mouseX, mouseY);
		if(titleTextField.isFocused())
			titleTextField.updateCursorCounter();
	}
	
	public int mouseClicked(int mouseX, int mouseY, int button) {
		titleTextField.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public void keyTyped(char typedChar, int keyCode) {
		titleTextField.textboxKeyTyped(typedChar, keyCode);
	}
}

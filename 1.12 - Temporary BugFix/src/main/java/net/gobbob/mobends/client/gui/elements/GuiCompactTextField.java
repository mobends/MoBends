package net.gobbob.mobends.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiCompactTextField extends GuiTextField {
	String placeholderText;
	
	public GuiCompactTextField(FontRenderer fontrendererObj, int width, int height) {
		super(0, fontrendererObj, 0, 0, width, height);
	}
	
	public GuiCompactTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width,
			int height) {
		super(componentId, fontrendererObj, x, y, width, height);
	}
	
	public void drawTextBox()
    {
		super.drawTextBox();
		if(!this.isFocused() && this.getText().length() == 0)
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.placeholderText, this.x + 4, this.y + (this.height - 8) / 2, 7368816);
    }
	
	public GuiCompactTextField setPlaceholderText(String placeholderText) {
		this.placeholderText = placeholderText;
		return this;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

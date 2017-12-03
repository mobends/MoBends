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
			Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.placeholderText, this.xPosition + 4, this.yPosition + (this.height - 8) / 2, 7368816);
    }
	
	public GuiCompactTextField setPlaceholderText(String placeholderText) {
		this.placeholderText = placeholderText;
		return this;
	}
	
	public void setPosition(int x, int y) {
		this.xPosition = x;
		this.yPosition = y;
	}
}

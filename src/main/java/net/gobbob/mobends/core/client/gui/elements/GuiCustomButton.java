package net.gobbob.mobends.core.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiCustomButton extends GuiButton{
	public GuiCustomButton(int width, int height) {
		super(0, 0, 0, width, height, "");
	}
	
	public GuiCustomButton(int width, int height, String text) {
		super(0, 0, 0, width, height, text);
	}

	public GuiCustomButton setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public void drawButton(int mouseX, int mouseY, float partialTicks)
    {
		super.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
    }
	
	public boolean mousePressed(int mouseX, int mouseY) {
		return this.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	public GuiCustomButton setText(String text) {
		this.displayString = text;
		return this;
	}
}

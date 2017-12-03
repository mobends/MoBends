package net.gobbob.mobends.client.gui.packeditor;

import net.gobbob.mobends.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiAddPackButton {
	public static final int WIDTH = 100;
	public static final int HEIGHT = 12;
	protected int x, y;
	protected boolean hover;
	protected boolean enabled;
	
	public GuiAddPackButton() {
		this.x = 0;
		this.y = 0;
		this.hover = false;
		this.enabled = true;
	}
	
	public void initGui(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void update(int mouseX, int mouseY) {
		hover = mouseX >= x && mouseX <= x+WIDTH &&
				mouseY >= y && mouseY <= y+HEIGHT;
	}
	
	public void display(int mouseX, int mouseY) {
		if(!enabled) return;
		
		update(mouseX, mouseY);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPackEditor.BACKGROUND_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		int textureY = hover ? 105 : 93;
		Draw.texturedModalRect(x, y, 0, textureY, WIDTH, HEIGHT);
	}
	
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if(!enabled) return false;
		
		update(mouseX, mouseY);
		return hover;
	}
	
	public GuiAddPackButton setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
}

package net.gobbob.mobends.client.gui.packeditor;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.util.Draw;
import net.minecraft.client.Minecraft;

public class GuiPackTab {
	public static final int WIDTH = 18;
	public static final int HEIGHT = 16;
	public static final int OFFSET = 15;
	protected int x;
	protected int y;
	protected int index;
	protected String title;
	protected List<GuiPackEntry> entries;
	protected boolean hover;
	
	public GuiPackTab(int index, String title) {
		this.index = index;
		this.title = title;
		this.hover = false;
		this.entries = new ArrayList<GuiPackEntry>();
	}
	
	public void initGui(int x, int y) {
		this.x = x+index*OFFSET;
		this.y = y-20;
		int yOffset = y;
		for(GuiPackEntry entry : entries) {
			entry.initGui(x+5, yOffset-1);
			yOffset += GuiPackEntry.HEIGHT+GuiPackEntry.MARGIN;
		}
	}
	
	public void update(int mouseX, int mouseY) {
		this.hover = mouseX >= x && mouseX <= x+WIDTH &&
					 mouseY >= y && mouseY <= y+HEIGHT;
	}
	
	public void display(int mouseX, int mouseY, boolean selected) {
		update(mouseX, mouseY);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPackEditor.BACKGROUND_TEXTURE);
		int textureY = selected ? 149 : hover ? 133 : 117;
		Draw.texturedModalRect(x, y + (selected ? 0 : 1), index*WIDTH, textureY, WIDTH, HEIGHT);
	}
	
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		update(mouseX, mouseY);
		
		return hover;
	}
	
	public List<GuiPackEntry> getEntries() {
		return entries;
	}

	public void addEntry(GuiPackEntry guiPackEntry) {
		entries.add(guiPackEntry);
	}

	public String getTitle() {
		return title;
	}
}

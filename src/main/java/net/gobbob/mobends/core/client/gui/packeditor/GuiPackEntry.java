package net.gobbob.mobends.core.client.gui.packeditor;

import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiPackEntry {
	public static final int HEIGHT = 31;
	public static final int MARGIN = 2;
	protected FontRenderer fontRenderer;
	protected GuiPackList packList;
	protected String originalName;
	protected String name;
	protected String displayName;
	protected String author;
	protected String description;
	protected ResourceLocation thumbnailLocation;
	
	protected int x, y;
	protected boolean hover, selected;
	protected boolean applied;
	
	public GuiPackEntry() {
		this.x = this.y = 0;
		this.hover = this.selected = false;
		this.name = "unnamed.bends";
		this.originalName = "unnamed.bends";
		this.displayName = "Unnamed";
		this.author = Minecraft.getMinecraft().player.getName();
		this.description = "A custom pack made by " + Minecraft.getMinecraft().player.getName() + ".";
		this.thumbnailLocation = BendsPack.DEFAULT_THUMBNAIL_TEXTURE;
		this.applied = false;
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
	}
	
	public GuiPackEntry(GuiPackList packList) {
		this();
		this.packList = packList;
	}
	
	public GuiPackEntry(GuiPackList packList, BendsPack pack) {
		this(packList);
		this.name = pack.getFilename();
		this.originalName = pack.getFilename();
		this.displayName = pack.getDisplayName();
		this.author = pack.getAuthor();
		this.description = pack.getDescription();
		this.thumbnailLocation = pack.getThumbnail();
		this.applied = PackManager.getCurrentPack() != null && PackManager.getCurrentPack().getFilename().equalsIgnoreCase(pack.getFilename());
	}

	public void initGui(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void update(int mouseX, int mouseY) {
		hover = mouseX >= x && mouseX <= x+102 &&
				mouseY >= y && mouseY <= y+HEIGHT;
	}
	
	public boolean mouseClicked(int mouseX, int mouseY, int state) {
		update(mouseX, mouseY);
		return hover;
	}
	
	public int display(int mouseX, int mouseY) {
		update(mouseX, mouseY);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPackEditor.BACKGROUND_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		int textureY = this.selected ? 62 : this.hover ? 31 : 0;
        Draw.texturedModalRect(x-1, y - (this.selected ? 1 : 0), 0, textureY, 102, HEIGHT);
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(thumbnailLocation);
		GlStateManager.color(1, 1, 1, 1);
        Draw.texturedRectangle(x+2, y+2, 25, 25, 0, 0, 25.0f/32, 25.0f/32);
		
        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(this.displayName, 70), x+32, y+1, 0xffffff);
        Draw.rectangle_xgradient(x+101-40, y+1, 39, 9, 0x004e4e4e, 0xff4e4e4e);
        
        if(applied) {
        	Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPackEditor.BACKGROUND_TEXTURE);
        	GlStateManager.color(1, 1, 1, 1);
        	Draw.texturedModalRect(x+1, y+1, 45, 117, 27, 27);
        }
        
		return HEIGHT + MARGIN;
	}
	
	public GuiPackEntry setApplied(boolean value) {
		this.applied = value;
		return this;
	}
	
	public boolean isApplied() {
		return applied;
	}
}
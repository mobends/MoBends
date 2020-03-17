package goblinbob.mobends.core.client.gui.popup;

import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class GuiPopUpHelp extends GuiPopUp {
	public static final int WIDTH = 200;
	protected int exitBtnX, exitBtnY;
	protected boolean exitBtnHovered;
	protected List<Pair> definitions;
	
	public GuiPopUpHelp(int action) {
		super(I18n.format("mobends.gui.help", new Object[0]), action, new String[0]);
		this.width = WIDTH;
		this.height = 80;
		this.exitBtnHovered = false;
		this.definitions = new ArrayList<Pair>();
		this.definitions.add(new Pair(fontRenderer, "Left Click", "select nodes."));
		this.definitions.add(new Pair(fontRenderer, "Middle/Right Click", "scroll around the node editor."));
		
		int titleMaxWidth = 0;
		for(Pair pair : definitions) {
			if(pair.getTitleWidth() > titleMaxWidth)
				titleMaxWidth = pair.getTitleWidth();
		}
		
		for(Pair pair : definitions) {
			pair.updateLines(titleMaxWidth);
		}
	}
	
	public void initGui(int x, int y) {
		super.initGui(x, y);
		exitBtnX = this.x+this.width-10;
		exitBtnY = this.y-2;
	}
	
	public void display(int mouseX, int mouseY, float partialTicks) {
		super.display(mouseX, mouseY, partialTicks);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
		int textureX = exitBtnHovered ? 42 : 30;
		Draw.texturedModalRect(exitBtnX, exitBtnY, textureX, 76, 12, 12);
		
		int yOffset = y+24;
		for(Pair pair : definitions) {
			fontRenderer.drawStringWithShadow(pair.getTitle(), x+10, yOffset, 0xffffaa);
			for(String line : pair.getDefinitionLines()) {
				fontRenderer.drawStringWithShadow(line, x+15+pair.getTitleOffset(), yOffset, 0xffffff);
				yOffset+=10;
			}
			yOffset+=10;
		}
	}
	
	public void update(int mouseX, int mouseY) {
		super.update(mouseX, mouseY);
		this.exitBtnHovered = mouseX >= exitBtnX && mouseX <= exitBtnX+12 &&
							  mouseY >= exitBtnY && mouseY <= exitBtnY+12;
	}
	
	public int mouseClicked(int mouseX, int mouseY, int state) {
		if(exitBtnHovered) return 0;
		return -1;
	}
	
	private static class Pair {
		FontRenderer fontRenderer;
		String title;
		String definition;
		String[] definitionLines;
		int titleMaxWidth;
		
		public Pair(FontRenderer fontRenderer, String title, String definition) {
			this.title = title;
			this.definition = definition;
			this.fontRenderer = fontRenderer;
		}
		
		public String getTitle() {
			return title;
		}
		
		public String[] getDefinitionLines() {
			return definitionLines;
		}

		public int getTitleWidth() {
			return fontRenderer.getStringWidth(title);
		}

		public void updateLines(int titleMaxWidth) {
			this.titleMaxWidth = titleMaxWidth;
			this.definitionLines = GUtil.squashText(fontRenderer, definition, WIDTH-titleMaxWidth-40);
			for(int i = 0; i < definitionLines.length; i++) {
				definitionLines[i] = definitionLines[i].trim();
			}
		}
		
		public int getTitleOffset() {
			return titleMaxWidth;
		}
	}
}

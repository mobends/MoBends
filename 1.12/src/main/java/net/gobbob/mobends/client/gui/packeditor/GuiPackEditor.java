package net.gobbob.mobends.client.gui.packeditor;

import net.gobbob.mobends.client.gui.elements.GuiCompactTextField;
import net.gobbob.mobends.client.gui.elements.GuiCustomButton;
import net.gobbob.mobends.main.ModStatics;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.PackManager;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiPackEditor {
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ModStatics.MODID, "textures/gui/pack_editor.png");
	public static final int EDITOR_WIDTH = 248;
	public static final int EDITOR_HEIGHT = 177;
	public static final String publicInfoText = "Want your creation to be featured? Contact me at:";
	
	protected int x, y;
	protected GuiCompactTextField titleTextField;
	protected GuiCustomButton openFolderButton;
	protected GuiCustomButton useButton;
	protected GuiCustomButton editButton;
	protected GuiCustomButton doneButton;
	protected GuiPackList packList;
	protected boolean editMode;
	protected String generatedName;
	protected FontRenderer fontRenderer;
	
	float publicJumbotronTransition = 1;
	long time, lastTime;
	
	public GuiPackEditor() {
		this.titleTextField = new GuiCompactTextField(Minecraft.getMinecraft().fontRenderer, 120, 14);
		this.openFolderButton = new GuiCustomButton(130, 20);
		this.useButton = new GuiCustomButton(60, 20);
		this.editButton = new GuiCustomButton(60, 20);
		this.doneButton = new GuiCustomButton(60, 20);
		this.useButton.visible = false;
		this.doneButton.visible = false;
		this.editButton.visible = false;
		this.packList = new GuiPackList(this);
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
		this.titleTextField.setVisible(false);
		this.populate();
		
		this.time = System.nanoTime() / 1000;
		this.lastTime = System.nanoTime() / 1000;
	}
	
	public void initGui(int x, int y) {
		this.x = x-EDITOR_WIDTH/2;
		this.y = y-EDITOR_HEIGHT/2;
		this.titleTextField.setPosition(this.x+123, this.y+39);
		this.packList.initGui(this.x+5, this.y+19);
		this.updatePositions();
	}
	
	public void updatePositions() {
		String useButtonTitle = "mobends.gui.use";
		if(packList.getSelectedEntry() != null && packList.getSelectedEntry().isApplied())
			useButtonTitle = "mobends.gui.disable";
		this.useButton.setPosition(this.x+EDITOR_WIDTH-57, this.y+EDITOR_HEIGHT-17).setText(I18n.format(useButtonTitle, new Object[0]));
		this.editButton.setPosition(this.x+EDITOR_WIDTH-128, this.y+EDITOR_HEIGHT-17).setText(I18n.format("mobends.gui.edit", new Object[0]));
		this.doneButton.setPosition(this.x+EDITOR_WIDTH-57, this.y+EDITOR_HEIGHT-17).setText(I18n.format("mobends.gui.done", new Object[0]));
		this.openFolderButton.setPosition(this.x+EDITOR_WIDTH-128, this.y+EDITOR_HEIGHT-17).setText(I18n.format("mobends.gui.openpacksfolder", new Object[0]));
	}
	
	public void update(int mouseX, int mouseY) {
		if(this.titleTextField.isFocused())
			this.titleTextField.updateCursorCounter();
	}
	
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		this.titleTextField.mouseClicked(mouseX, mouseY, button);
		this.packList.mouseClicked(mouseX, mouseY, button);
		
		if(this.openFolderButton.mousePressed(mouseX, mouseY)) {
			OpenGlHelper.openFile(PackManager.localDirectory);
		}
		
		if(this.useButton.mousePressed(mouseX, mouseY)) {
			if(this.packList.getSelectedEntry().isApplied()){
				this.packList.getSelectedEntry().setApplied(false);
			}else{
				packList.usePack(this.packList.getSelectedEntry());
			}
			updatePositions();
		}
		
		if(this.editButton.mousePressed(mouseX, mouseY))
			setEditMode(true);
		if(this.doneButton.mousePressed(mouseX, mouseY)) {
			packList.getSelectedEntry().displayName = this.titleTextField.getText();
			packList.getSelectedEntry().name = this.generatedName;
			setEditMode(false);
		}
		
		return false;
	}
	
	public void handleMouseInput() {
		packList.handleMouseInput();
	}

	public void display(int mouseX, int mouseY, float partialTicks) {
		this.time = System.nanoTime() / 1000;
		float delta = (time-lastTime)/1000;
		this.lastTime = time;
		
		if(this.publicJumbotronTransition > 0) {
			this.publicJumbotronTransition = publicJumbotronTransition-delta*0.001f;
		}
		if(publicJumbotronTransition < 0) this.publicJumbotronTransition = 0;
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		Draw.borderBox(x+4, y+4, EDITOR_WIDTH, EDITOR_HEIGHT, 4, 102, 13);
		Draw.texturedModalRect(x+4+EDITOR_WIDTH/2-75, y-13, 106, 0, 150, 13);
		
		this.drawCenteredString(fontRenderer, I18n.format("mobends.gui.packs", new Object[0]), (int) this.x+4+EDITOR_WIDTH/2, y-9, 0xffffff);
		
		this.packList.display(mouseX, mouseY);
		this.titleTextField.drawTextBox();
		this.openFolderButton.drawButton(mouseX, mouseY, partialTicks);
		this.useButton.drawButton(mouseX, mouseY, partialTicks);
		this.editButton.drawButton(mouseX, mouseY, partialTicks);
		this.doneButton.drawButton(mouseX, mouseY, partialTicks);
		
		GuiPackEntry entry = packList.getSelectedEntry();
		if(packList.getSelectedTab().index == GuiPackList.TAB_PUBLIC) {
			if(entry != null) {
				String packName = entry.displayName;
				String packAuthor = "by " + entry.author;
				fontRenderer.drawStringWithShadow(packName, x+184-fontRenderer.getStringWidth(packName)/2, y+7, 0xffffff);
				fontRenderer.drawString(packAuthor, x+184-fontRenderer.getStringWidth(packAuthor)/2, y+17, 0x444444);
				int yOffset = y+29;
				for(String line : GUtil.squashText(fontRenderer, entry.description, 130)) {
					fontRenderer.drawStringWithShadow(line, x+120, yOffset, 0xffffff);
					yOffset += 12;
				}
			}else{
				GlStateManager.color(1, 1, 1, 1);
				Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
				Draw.texturedModalRect(x+EDITOR_WIDTH/2-4, y+6, 0, 195, 130, 61);
				
				float alpha = GUtil.clamp(this.publicJumbotronTransition*2, 0, 1);
				int color = 0x00ffffff;
				color |= (((int)(alpha*255) & 255) << 24);
				Draw.rectangle(x+EDITOR_WIDTH/2-4, y+6, 130, 61, color);
				
				int yOffset = y+77;
				for(String line : GUtil.squashText(fontRenderer, publicInfoText, 130)) {
					fontRenderer.drawString(line, x+121, yOffset, 0x444444);
					yOffset += 12;
				}
				fontRenderer.drawStringWithShadow("gobbobminecraft@gmail.com", x+118, y+110, 0xffffff);
			}
		}else{
			this.openFolderButton.drawButton(mouseX, mouseY, partialTicks);
			if(!editMode) {
				if(entry != null) {
					String text = entry.displayName;
					fontRenderer.drawStringWithShadow(text, x+184-fontRenderer.getStringWidth(text)/2, y+7, 0xffffff);
					int yOffset = y+27;
					for(String line : GUtil.squashText(fontRenderer, entry.description, 80)) {
						fontRenderer.drawStringWithShadow(line, x+120, yOffset, 0xffffff);
						yOffset += 12;
					}
				}else{
					String text = I18n.format("mobends.gui.selectpack", new Object[0]);
					fontRenderer.drawStringWithShadow(text, x+184-fontRenderer.getStringWidth(text)/2, y+EDITOR_HEIGHT/2, 0xffffff);
				}
			}else{
				fontRenderer.drawStringWithShadow(I18n.format("mobends.gui.entername", new Object[0]), x+125, y+25, 0xffffff);
				fontRenderer.drawStringWithShadow(I18n.format("mobends.gui.filename", new Object[0])+": ", x+125, y+58, 0xffffff);
				fontRenderer.drawString(this.generatedName, x+125, y+70, 0x777777);
			}
		}
	}

	private void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		fontRendererIn.drawStringWithShadow(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color);
	}

	public void keyTyped(char typedChar, int keyCode) {
		if(this.titleTextField.textboxKeyTyped(typedChar, keyCode)) {
			this.generatedName = BendsPack.constructName(this.titleTextField.getText()) + ".bends";
		}
	}
	
	public void populate() {
		if(!PackManager.arePublicPacksLoaded())
			PackManager.updatePublicDatabase();
		packList.populate();
	}
	
	public void setEditMode(boolean value) {
		if(editMode != value) {
			editMode = value;
			if(editMode) {
				this.titleTextField.setVisible(true);
				this.titleTextField.setText(packList.getSelectedEntry().displayName);
				this.editButton.visible = false;
				this.useButton.visible = false;
				this.doneButton.visible = true;
				this.generatedName = BendsPack.constructName(packList.getSelectedEntry().displayName) + ".bends";
			}else{
				this.titleTextField.setVisible(false);
				this.editButton.visible = true;
				this.useButton.visible = true;
				this.doneButton.visible = false;
			}
		}
	}
	
	public void onEntrySelected(GuiPackEntry entry, boolean update) {
		if(entry != null) {
			useButton.visible = true;
			switch(packList.tabId) {
				case GuiPackList.TAB_PUBLIC:
					editButton.visible = false;
					break;
				case GuiPackList.TAB_LOCAL:
					editButton.visible = true;
					break;
			}
			openFolderButton.visible = false;
		}else{
			useButton.visible = false;
			editButton.visible = false;
			openFolderButton.visible = true;
		}
		
		setEditMode(false);
		
		if(update) updatePositions();
	}
	
	public void apply() {
		packList.apply();
	}
	
	public GuiPackList getPackList() {
		return packList;
	}
	
	public void onOpened() {
		publicJumbotronTransition = 1;
	}
}
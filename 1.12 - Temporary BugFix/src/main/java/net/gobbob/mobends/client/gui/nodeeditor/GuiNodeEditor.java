package net.gobbob.mobends.client.gui.nodeeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.client.gui.GuiHelper;
import net.gobbob.mobends.client.gui.elements.GuiAddButton;
import net.gobbob.mobends.client.gui.elements.GuiDropDownList;
import net.gobbob.mobends.client.gui.elements.GuiHelpButton;
import net.gobbob.mobends.network.NetworkConfiguration;
import net.gobbob.mobends.pack.BendsCondition;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.pack.PackManager;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiNodeEditor {
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(MoBends.MODID,"textures/gui/node_editor.png");
	public static final int EDITOR_WIDTH = 210;
	public static final int EDITOR_HEIGHT = 122;
	public static final int SCROLLBAR_WIDTH = 5;
	private int x, y;
	private int height = 0;
	private FontRenderer fontRenderer;
	private List<GuiAnimationSection> sections;
	private GuiAddButton buttonAddSection;
	private GuiHelpButton helpButton;
	private GuiParameterEditor parameterEditor;
	private GuiBendsMenu mainMenu;
	private GuiDropDownList targetList;
	private boolean unappliedChanges;
	private String[] alterableParts;
	private float scrollAmountX;
	private float scrollAmountY;
	private int scrollMaxX;
	private int scrollMaxY;
	private boolean editorHovered = false;
	
	public GuiNodeEditor(GuiBendsMenu mainMenu) {
		this.sections = new ArrayList<GuiAnimationSection>();
		this.x = 0;
		this.y = 0;
		this.buttonAddSection = new GuiAddButton();
		this.helpButton = new GuiHelpButton();
		this.parameterEditor = new GuiParameterEditor(this);
		this.mainMenu = mainMenu;
		this.unappliedChanges = false;
		this.scrollAmountX = 0;
		this.scrollAmountY = 0;
		this.scrollMaxX = 0;
		this.scrollMaxY = 0;
		this.targetList = new GuiDropDownList(this).forbidNoValue();
		for(AlterEntry alterEntry : mainMenu.alterEntries) {
			this.targetList.addEntry(alterEntry.getDisplayName());
		}
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
	}
	
	public void initGui(int x, int y, int pX, int pY) {
		this.x = x;
		this.y = y;
		this.buttonAddSection.initGui(x+3, y+3 + this.height);
		this.helpButton.initGui(x+196, y-77);
		this.parameterEditor.initGui(pX, pY);
		this.targetList.setPosition(x+56, y-77);
		this.updateHeight();
	}
	
	public void display(int mouseX, int mouseY, float partialTicks) {
		editorHovered = mouseX >= x+3 && mouseX <= x+3+EDITOR_WIDTH &&
						mouseY >= y+3 && mouseY <= y+3+EDITOR_HEIGHT;
		
		this.targetList.display();
						
		if(!PackManager.isCurrentPackLocal()) return;
		
		int[] position = GuiHelper.getDeScaledCoords(x+3, y+3+EDITOR_HEIGHT/*-87*/);
		int[] size = GuiHelper.getDeScaledVector(EDITOR_WIDTH, EDITOR_HEIGHT);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(position[0], position[1], size[0], size[1]);
		GL11.glTranslatef(-scrollAmountX, -scrollAmountY, 0);
		int yOffset = y+3;
		for(int i = 0; i < this.sections.size(); i++) {
			yOffset = this.sections.get(i).display(x+3, yOffset, mouseX+(int)scrollAmountX, mouseY+(int)scrollAmountY);
		}
		this.buttonAddSection.display();
		GL11.glTranslatef(scrollAmountX, scrollAmountY, 0);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		this.parameterEditor.display(mouseX, mouseY, partialTicks);
		this.helpButton.display();
		
		if(scrollMaxY > EDITOR_HEIGHT) {
			int scrollBarHeight = EDITOR_HEIGHT*EDITOR_HEIGHT/scrollMaxY;
			Draw.rectangle(x+EDITOR_WIDTH-SCROLLBAR_WIDTH+3, y+3, SCROLLBAR_WIDTH, EDITOR_HEIGHT, 0xff333333);
			Draw.rectangle(x+EDITOR_WIDTH-SCROLLBAR_WIDTH+4, y+4+(int)(scrollAmountY*(EDITOR_HEIGHT-scrollBarHeight)/(scrollMaxY-EDITOR_HEIGHT)), SCROLLBAR_WIDTH-2, scrollBarHeight-2, 0xff666666);
		}
		
		if(!NetworkConfiguration.isModelScalingAllowed()) {
			String errorText = I18n.format("mobends.gui.noscaleallowed", new Object[0]);
			fontRenderer.drawStringWithShadow(errorText, this.x+(EDITOR_WIDTH-fontRenderer.getStringWidth(errorText))/2, this.y+EDITOR_HEIGHT+16, 0xffdd33);
		}
 	}
	
	public void populate(AlterEntry alterEntry) {
		BendsTarget target = BendsPack.getTarget(alterEntry.getName());
		this.sections = new ArrayList<GuiAnimationSection>();
		final GuiNodeEditor editor = this;
		if(target != null){
			target.conditions.forEach(new BiConsumer() {
				@Override
				public void accept(Object animationName, Object arg1) {
					GuiAnimationSection section = new GuiAnimationSection(editor, (String) animationName);
					section.populate((BendsCondition) arg1);
					editor.addSection(section);
				}
			});
		}
		this.parameterEditor.deselect();
		this.alterableParts = alterEntry.getOwner().getAlterableParts();
		this.updateHeight();
		this.unappliedChanges = false;
		this.targetList.selectValue(mainMenu.currentAlterEntry);
	}

	public void addSection(GuiAnimationSection section) {
		this.sections.add(section);
	}
	
	public void addDefaultSection() {
		GuiAnimationSection section = new GuiAnimationSection(this, "");
		this.sections.add(section);
	}
	
	public void removeSection(GuiAnimationSection guiAnimationSection) {
		this.sections.remove(guiAnimationSection);
		this.onChange();
	}

	public void update(int mouseX, int mouseY) {
		editorHovered = mouseX >= x+3 && mouseX <= x+3+EDITOR_WIDTH &&
						mouseY >= y+3 && mouseY <= y+3+EDITOR_HEIGHT;
		
		this.parameterEditor.update(mouseX, mouseY);
		this.targetList.update(mouseX, mouseY);
		this.helpButton.update(mouseX, mouseY);
		
		if(!editorHovered) return;
		
		this.buttonAddSection.update(mouseX+(int)scrollAmountX, mouseY+(int)scrollAmountY);
		int yOffset = 0;
		for(int i = 0; i < this.sections.size(); i++) {
			this.sections.get(i).update(this.x, this.y+yOffset, mouseX+(int)scrollAmountX, mouseY+(int)scrollAmountY);
			yOffset += this.sections.get(i).getHeight();
		}
	}
	
	public void updateHeight() {
		this.height = 0;
		for(int i = 0; i < this.sections.size(); i++) {
			this.height += this.sections.get(i).updateHeight(x+3, y+3+this.height);
		}
		this.buttonAddSection.setPosition(x+3, y+3 + this.height);
		updateScrollBounds();
	}
	
	public void mouseClicked(int mouseX, int mouseY, int state) {
		if(state != 0) return;
		
		boolean lastUnappliedChanged = unappliedChanges;
		
		boolean pressed = false;
		
		if(this.targetList.mouseClicked(mouseX, mouseY, state)){
			applyTargetListChanges(lastUnappliedChanged);
			pressed = true;
		}
		
		if(!PackManager.isCurrentPackLocal()) return;
		
		if(!pressed && this.parameterEditor.mouseClicked(mouseX, mouseY, state))
			pressed = true;
		else{
			if(editorHovered) {
				if(this.buttonAddSection.mouseClicked(mouseX+(int)scrollAmountX, mouseY+(int)scrollAmountY, state)) {
					this.addDefaultSection();
					this.updateHeight();
					this.onChange();
					pressed = true;
				}
				for(int i = 0; i < this.sections.size(); i++) {
					if(this.sections.get(i).mouseClicked(mouseX+(int)scrollAmountX, mouseY+(int)scrollAmountY, state))
						pressed = true;
				}
			}
		}
		
		if(this.helpButton.mouseClicked(mouseX, mouseY, state)) {
			mainMenu.popUpHelp();
		}
		
		if(!pressed) {
			this.parameterEditor.deselect();
		}
	}

	public void mouseReleased(int mouseX, int mouseY, int event) {
		this.buttonAddSection.mouseReleased(mouseX+(int)scrollAmountX, mouseY+(int)scrollAmountY, event);
		for(int i = 0; i < this.sections.size(); i++) {
			this.sections.get(i).mouseReleased(mouseX+(int)scrollAmountX, mouseY+(int)scrollAmountY, event);
		}
		parameterEditor.mouseReleased(mouseX, mouseY, event);
	}
	
	public void handleMouseInput() {
		if(targetList.handleMouseInput()){
			applyTargetListChanges(unappliedChanges);
			return;
		}
		
		if(!this.parameterEditor.handleMouseInput()) {
			if(editorHovered) {
				int i2 = Mouse.getEventDWheel();

	            if (i2 != 0)
	            {
	                if (i2 > 0)
	                {
	                    i2 = -1;
	                }
	                else if (i2 < 0)
	                {
	                    i2 = 1;
	                }

	                scrollY((float) (i2*15));
	            }
			}
		}
		
		if(Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
			scrollX(-Mouse.getDX());
			scrollY(Mouse.getDY());
		}
	}
	
	public void applyTargetListChanges(boolean unappliedChanged) {
		if(targetList.areChangedUnhandled()) {
			if(unappliedChanged) {
				mainMenu.popUpDiscardChanges(mainMenu.POPUP_CHANGETARGET);
			}else{
				mainMenu.selectAlterEntry((Integer) targetList.getSelectedValue());
			}
			targetList.setChangesHandled();
		}
	}
	
	public void keyTyped(char typedChar, int keyCode) {
		if(!PackManager.isCurrentPackLocal()) return;
		
		this.parameterEditor.keyTyped(typedChar, keyCode);
	}

	public void applyChanges(BendsTarget target) {
		target.conditions.clear();
		for(int i = 0; i < this.sections.size(); i++) {
			this.sections.get(i).applyChanges(target);
		}
		unappliedChanges = false;
	}
	
	public void onChange() {
		this.unappliedChanges = true;
		this.mainMenu.onNodeEditorChange();
		updateScrollBounds();
	}
	
	public void updateScrollBounds() {
		scrollMaxX = 0;
		scrollMaxY = 20;
		for(int i = 0; i < sections.size(); i++) {
			if(sections.get(i).getGlobalWidth() > scrollMaxX)
				scrollMaxX = sections.get(i).getGlobalWidth();
			
			scrollMaxY += sections.get(i).getHeight();
		}
	}
	
	public void scrollX(float value) {
		int width = Math.max(0, scrollMaxX-EDITOR_WIDTH);
		scrollAmountX = GUtil.clamp(scrollAmountX+value, 0, width);
	}
	
	public void scrollY(float value) {
		int height = Math.max(0, scrollMaxY-EDITOR_HEIGHT);
		scrollAmountY = GUtil.clamp(scrollAmountY+value, 0, height);
	}
	
	public GuiParameterEditor getParameterEditor() {
		return this.parameterEditor;
	}
	
	public boolean areChangesUnapplied() {
		return unappliedChanges;
	}

	public String[] getAlterableParts() {
		return alterableParts;
	}

	public GuiDropDownList getTargetList() {
		return targetList;
	}
}

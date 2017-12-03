package net.gobbob.mobends.client.gui.nodeeditor;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.client.gui.elements.GuiAddButton;
import net.gobbob.mobends.pack.BendsCondition;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.util.Draw;
import net.minecraft.client.Minecraft;

public class GuiAnimationSection {
	private List<GuiAnimationNode> nodes;
	private String animationName;
	private GuiAddButton buttonAddNode;
	private int height = 0;
	private int width;
	public boolean hover;
	private boolean selected = false;
	private GuiNodeEditor nodeEditor;
	
	public static final int HEADER_HEIGHT = 15;
	public static final int MARGIN_HEIGHT = 17;
	
	public GuiAnimationSection(GuiNodeEditor nodeEditor, String animationName) {
		this.nodes = new ArrayList<GuiAnimationNode>();
		this.setAnimationName(animationName);
		this.buttonAddNode = new GuiAddButton();
		this.nodeEditor = nodeEditor;
	}
	
	public void updatePositions(int x, int y) {
		this.buttonAddNode.setPosition(x + 16, y + this.height-MARGIN_HEIGHT);
	}
	
	public int display(int x, int y, int mouseX, int mouseY) {
		hover = mouseX >= x && mouseX < x+this.getVisualWidth() &&
				mouseY >= y && mouseY < y+GuiAnimationNode.HEIGHT;
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
		int textureX = this.selected ? 14 : this.hover ? 7 : 0;
		Draw.texturedModalRect(x, y, textureX, 0, 3, 16);
        Draw.texturedModalRect(x+3, y, width, 16, textureX+3, 0, 1, 16);
        Draw.texturedModalRect(x+3+width, y, textureX+4, 0, 3, 16);
        
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(getAnimationName(), x+5,y+3, 0xffffff);
        
		y+=HEADER_HEIGHT;
		for(int i = 0; i < this.nodes.size(); i++) {
			y = this.nodes.get(i).display(x, y);
		}
		
		this.buttonAddNode.display();
		
		y+=MARGIN_HEIGHT;
		
		return y;
	}
	
	public void addNode(GuiAnimationNode node) {
		this.nodes.add(node);
	}
	
	public void addDefaultNode() {
		GuiAnimationNode node = new GuiAnimationNode(this);
		node.setModel(nodeEditor.getAlterableParts() != null && nodeEditor.getAlterableParts().length > 0 ? nodeEditor.getAlterableParts()[0] : "");
		this.nodes.add(node);
	}
	
	public int updateHeight(int x, int y) {
		this.height = HEADER_HEIGHT+GuiAnimationNode.HEIGHT*this.nodes.size()+MARGIN_HEIGHT;
		updatePositions(x, y);
		
		return this.height;
	}
	
	public void setAnimationName(String animationName) {
		this.animationName = animationName;
		this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.animationName)+7;
	}

	public void populate(BendsCondition condition) {
		for(int i = 0; i < condition.getActionAmount(); i++) {
			this.addNode(new GuiAnimationNode(this, condition.getAction(i)));
		}
	}

	public boolean mouseClicked(int x, int y, int event) {
		boolean pressed = false;
		
		this.selected = this.hover;
		if(this.selected) {
			this.getNodeEditor().getParameterEditor().select(this);
		}
		
		if(this.buttonAddNode.mouseClicked(x, y, event)) {
			this.addDefaultNode();
			this.nodeEditor.updateHeight();
			this.nodeEditor.onChange();
			pressed = true;
		}
		
		for(int i = 0; i < this.nodes.size(); i++) {
			if(this.nodes.get(i).onMousePressed(x, y, event))
				pressed = true;
		}
		
		return pressed || this.selected;
	}

	public void mouseReleased(int x, int y, int event) {
		this.buttonAddNode.mouseReleased(x, y, event);
	}

	public void update(int x, int y, int mouseX, int mouseY) {
		for(int i = 0; i < nodes.size(); i++) {
			nodes.get(i).update(x, y+HEADER_HEIGHT+i*GuiAnimationNode.HEIGHT, mouseX, mouseY);
		}
		buttonAddNode.update(mouseX, mouseY);
		hover = mouseX >= x && mouseX < x+this.getVisualWidth() &&
				mouseY >= y && mouseY < y+GuiAnimationNode.HEIGHT;
	}
	
	public int getVisualWidth() {
		return width+7;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getAnimationName() {
		return animationName;
	}

	public GuiNodeEditor getNodeEditor() {
		return nodeEditor;
	}

	public void applyChanges(BendsTarget target) {
		BendsCondition condition = new BendsCondition(animationName);
		target.conditions.put(animationName, condition);
		for(int i = 0; i < nodes.size(); i++) {
			nodes.get(i).applyChanges(target, condition);
		}
	}

	public void removeNode(GuiAnimationNode guiAnimationNode) {
		nodes.remove(guiAnimationNode);
		nodeEditor.onChange();
	}
	
	public void remove() {
		nodeEditor.removeSection(this);
	}

	public List<GuiAnimationNode> getNodes() {
		return nodes;
	}
	
	public int getGlobalWidth() {
		int globalWidth = 0;
		for(int i = 0; i < nodes.size(); i++) {
			if(nodes.get(i).getGlobalWidth() > globalWidth) {
				globalWidth = nodes.get(i).getGlobalWidth();
			}
		}
		
		return globalWidth;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}

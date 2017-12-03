package net.gobbob.mobends.client.gui;

import net.gobbob.mobends.configuration.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiSettingsNode extends Gui{
	public int xPosition;
    public int yPosition;
    public int width;
    public int height;
	public boolean enabled;
    public boolean visible;
	public Setting settingsNode;
	
	public GuiSettingsNode(Setting settingsNode, int xPosition, int yPosition) {
		this.settingsNode = settingsNode;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.visible = true;
		this.enabled = true;
	}
	
	public abstract boolean mousePressed(Minecraft mc, int mouseX, int mouseY);
	public abstract void draw(Minecraft minecraft, int mouseX, int mouseY);
}

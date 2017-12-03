package net.gobbob.mobends.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.client.ClientProxy;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.util.Color;
import net.gobbob.mobends.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiListPacksSelectionEntry implements GuiListExtended.IGuiListEntry
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft client;
    private final GuiMBMenu moBendsMenuGui;
    private final GuiListPacksSelection containingListSel;
    private final String packDisplayName;
    private final String packDescription;
    private final String packAuthor;

    public GuiListPacksSelectionEntry(GuiListPacksSelection listPacksSelIn, String displayNameIn, String authorIn, String descriptionIn)
    {
        this.containingListSel = listPacksSelIn;
        this.moBendsMenuGui = listPacksSelIn.getMoBendsMenuGui();
        this.client = Minecraft.getMinecraft();
        
        this.packDisplayName = displayNameIn;
        this.packAuthor = authorIn;
        this.packDescription = descriptionIn;
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        String displayName = this.packDisplayName;
        String author = this.packAuthor;
        String description = this.packDescription;
        
        List<String> text = new ArrayList<String>();
		text.add("");
		
		int var1 = 0;
		int lineLength = 0;
		
		for(int s = 0; s < description.length(); s++,lineLength++){
			text.set(var1, text.get(var1) + description.charAt(s));
			if(this.client.fontRendererObj.getStringWidth(text.get(var1))*0.5f > 128){
    			if(description.charAt(s) == ' '){
    				lineLength = 0;
    				var1++;
    				text.add("");
    			}
			}
		}
		
		GL11.glPushMatrix();
    		Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
    		GL11.glColor4f(0,0,0,0.5f);
    		if(slotIndex != BendsPack.currentPack) Draw.rectangle(x,y,200, 64);
    		else								   Draw.rectangle_xgradient(x,y,200, 64, new Color(0.0f,0.0f,0.0f,0.5f), new Color(0.1f,1.0f,0.1f,0.5f));
    		GL11.glColor4f(1,1,1,1.0f);
    		this.client.fontRendererObj.drawString(displayName,(int)(this.moBendsMenuGui.width+this.moBendsMenuGui.presetWindowState*-250+10)+5,y+5, 0xffffff);
    		
    		GL11.glPushMatrix();
    			GL11.glTranslatef((int)(this.moBendsMenuGui.width+this.moBendsMenuGui.presetWindowState*-250+10)+5,y+5+10,0);
    			GL11.glScalef(0.5f,0.5f,0.5f);
    			this.client.fontRendererObj.drawString("By " + author, 0, 0, 0x777777);
    			
    			for(int s = 0;s < text.size();s++){
    				this.client.fontRendererObj.drawString(text.get(s),0,20+s*10, 0xffffff);
	    		}
    		GL11.glPopMatrix();
    	GL11.glPopMatrix();
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     *  
     * @param mouseX Scaled X coordinate of the mouse on the entire screen
     * @param mouseY Scaled Y coordinate of the mouse on the entire screen
     * @param mouseEvent The button on the mouse that was pressed
     * @param relativeX Relative X coordinate of the mouse within this entry.
     * @param relativeY Relative Y coordinate of the mouse within this entry.
     */
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        this.containingListSel.selectPack(slotIndex);
        
        return true;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }
}
package net.gobbob.mobends.client.gui;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiListPacksSelection extends GuiListExtended
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final GuiBendsMenu moBendsMenuGui;
    private final List<GuiListPacksSelectionEntry> entries = Lists.<GuiListPacksSelectionEntry>newArrayList();
    /** Index to the currently selected pack */
    private int selectedIdx = -1;

    public GuiListPacksSelection(GuiBendsMenu mbMenuIn, Minecraft clientIn, int widthIn, int heightIn, int topIn, int bottomIn)
    {
        super(clientIn, widthIn, heightIn, topIn, bottomIn, 70);
        this.moBendsMenuGui = mbMenuIn;
        
        this.fillOut();
    }
    
    public void fillOut() {
    	entries.clear();
    	/*for(int i = 0;i < BendsPack.bendsPacks.size();i++){
    		BendsPack pack = BendsPack.bendsPacks.get(i);
    		entries.add(new GuiListPacksSelectionEntry(this, pack.name, pack.author, pack.description));
    	}*/
    }
    
    public GuiListPacksSelectionEntry getListEntry(int index)
    {
        return (GuiListPacksSelectionEntry)this.entries.get(index);
    }

    protected int getSize()
    {
        return this.entries.size();
    }

    protected int getScrollBarX()
    {
        return this.getX()+250-20;
    }
    
    public int getListWidth()
    {
        return super.getListWidth() + 50;
    }
    
    protected boolean isSelected(int slotIndex)
    {
        return slotIndex == this.selectedIdx;
    }

    public GuiListPacksSelectionEntry getSelectedWorld()
    {
        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
    }

    public GuiBendsMenu getMoBendsMenuGui()
    {
        return this.moBendsMenuGui;
    }
    
    public void selectPack(int slotIndex){
    	this.selectedIdx = slotIndex;
    }
    
    public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_)
    {
    	this.height = this.moBendsMenuGui.height;
    	this.width = this.moBendsMenuGui.width;
    	this.bottom = this.height-64;
    	this.right = this.width;
    	
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            // Forge: background rendering moved into separate method.
            int k = this.getX();
            int l = this.top + 4 - (int)this.amountScrolled;

            this.drawSelectionBox(k, l, mouseXIn, mouseYIn);
            GlStateManager.disableDepth();
            int i1 = 4;
            
            int j1 = this.getMaxScroll();
            GlStateManager.disableTexture2D();
            if (j1 > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                if (l1 < this.top)
                {
                    l1 = this.top;
                }

                vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexbuffer.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                vertexbuffer.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                vertexbuffer.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                vertexbuffer.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexbuffer.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                vertexbuffer.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                vertexbuffer.pos((double)j, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                vertexbuffer.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexbuffer.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                vertexbuffer.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                vertexbuffer.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                vertexbuffer.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.renderDecorations(mouseXIn, mouseYIn);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }
    
    protected void drawSelectionBox(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn)
    {
        int i = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();

        for (int j = 0; j < i; ++j)
        {
            int k = p_148120_2_ + j * this.slotHeight + this.headerPadding;
            int l = this.slotHeight - 4;

            if (k > this.bottom || k + l < this.top)
            {
                this.updateItemPos(j, p_148120_1_, k);
            }

            this.drawSlot(j, p_148120_1_, k, l, mouseXIn, mouseYIn);
        }
    }
    
    protected void drawContainerBackground(Tessellator tessellator)
    {
    }
    
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
    {
    }
    
    public int getX() {
    	return (int) (this.moBendsMenuGui.width-250+10);
    }
    
    public int getSelectedIndex() {
    	return this.selectedIdx;
    }
    
    public int getSlotIndexFromScreenCoords(int p_148124_1_, int p_148124_2_)
    {
        int i = this.getX();
        int j = this.getX() + this.getListWidth();
        int k = p_148124_2_ - this.top - this.headerPadding + (int)this.amountScrolled - 4;
        int l = k / this.slotHeight;
        return p_148124_1_ < this.getScrollBarX() && p_148124_1_ >= i && p_148124_1_ <= j && l >= 0 && k >= 0 && l < this.getSize() ? l : -1;
    }
}
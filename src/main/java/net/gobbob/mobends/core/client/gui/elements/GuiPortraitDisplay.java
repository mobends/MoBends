package net.gobbob.mobends.core.client.gui.elements;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.Previewer;
import net.gobbob.mobends.core.client.gui.GuiHelper;
import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;

public class GuiPortraitDisplay
{
	private int x, y;
	private int width, height;
	private boolean hovered = false;
	private boolean value = false;
	private double rotation;
	private float animationValue;
	private AlterEntry alterEntryToView;
	private String animationToPreview = "";
	protected long time, lastTime;

	public GuiPortraitDisplay()
	{
		this.width = 49;
		this.height = 70;
		this.rotation = 0;
		this.animationValue = 0;
		this.time = System.nanoTime() / 1000;
		this.lastTime = System.nanoTime() / 1000;
	}

	public void initGui(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public void update(int mouseX, int mouseY)
	{
		hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public void display(float partialTicks)
	{
		this.time = System.nanoTime() / 1000;
		float delta = (time - lastTime) / 1000;
		this.lastTime = time;

		int[] position = GuiHelper.getDeScaledCoords(x, y + height + 1);
		int[] size = GuiHelper.getDeScaledVector(width, height + 2);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(position[0], position[1], size[0], size[1]);

		if (hovered)
			animationValue = Math.min(animationValue + delta * 0.01f, 1);
		else
			animationValue = Math.max(animationValue - delta * 0.01f, 0);

		rotation += delta * 0.02f;

		if (hovered)
		{
			Draw.rectangle(x, y, width, height, 0x22ffffff);
		}

		Draw.rectangle_ygradient(x, y, width, height, 0x00000000, value ? 0xff114444 : 0xff441111);

		float smoothAnimation = animationValue * animationValue * 4;
		if (smoothAnimation >= 1)
			smoothAnimation = 2 - (1 - animationValue) * (1 - animationValue) * 4;
		smoothAnimation /= 2;

		if (alterEntryToView != null)
		{
			float scale = 1.0f - smoothAnimation * 0.2f;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 25, y + height / 2, 0);
			GlStateManager.scale(scale, scale, scale);
			renderLivingEntity(0, 24, 24, alterEntryToView);
			GlStateManager.popMatrix();
		}

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		String displayText = value ? "Animated" : "Vanilla";
		fontRenderer.drawString(displayText, x + width / 2 - fontRenderer.getStringWidth(displayText) / 2,
				(int) (y + height - smoothAnimation * 10), 0xffffff);

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	public void renderLivingEntity(int argX, int argY, float scale, AlterEntry alterEntry)
	{
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		GL11.glTranslatef((float) argX, (float) argY, 50.0F);
		GL11.glScalef((float) (-scale), (float) scale, (float) scale);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		//GL11.glRotated(rotation, 0.0, 1.0, 0.0);
		GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);
		
		float lightAngle = 135.0F;
		GL11.glRotatef(lightAngle, 0.0F, 1.0F, 0.0F);
		GL11.glColor3f(1, 1, 1);
		RenderHelper.enableStandardItemLighting();
		GL11.glRotatef(-lightAngle, 0.0F, 1.0F, 0.0F);
		
		LivingEntityData data = alterEntry.getDataForPreview();
		EntityLivingBase living = data.getEntity();
		
		float f2 = living.renderYawOffset;
		float f3 = living.rotationYaw;
		float f4 = living.rotationPitch;
		float f5 = living.prevRotationYawHead;
		float f6 = living.rotationYawHead;
		living.renderYawOffset = 0;
		living.rotationYaw = 0;
		living.rotationPitch = 0;
		living.rotationYawHead = living.rotationYaw;
		living.prevRotationYawHead = living.rotationYaw;
		
		Minecraft.getMinecraft().getRenderManager().playerViewY = 180.0F;
		
		Previewer previewer = alterEntry.getPreviewer();
		
		if (previewer != null)
			previewer.prePreview(data, this.animationToPreview);
		
		Minecraft.getMinecraft().getRenderManager().renderEntity(living, 0.0D, 0.0D, 0.0D, 0.0F, 1.0f,
				false);
		
		if (previewer != null)
			previewer.postPreview(data, this.animationToPreview);
		
		living.renderYawOffset = f2;
		living.rotationYaw = f3;
		living.rotationPitch = f4;
		living.prevRotationYawHead = f5;
		living.rotationYawHead = f6;
		GL11.glPopMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public boolean mouseClicked(int mouseX, int mouseY, int state)
	{
		if (hovered)
		{
			toggle();
			return true;
		}
		return false;
	}

	public void toggle()
	{
		value = !value;
	}

	public void showAlterEntry(AlterEntry alterEntry)
	{
		this.alterEntryToView = alterEntry;
		this.value = alterEntry.isAnimated();
	}
	
	public void setAnimationToPreview(String animation)
	{
		this.animationToPreview = animation;
	}

	public boolean getValue()
	{
		return value;
	}
}

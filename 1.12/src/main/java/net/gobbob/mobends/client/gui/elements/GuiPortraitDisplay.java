package net.gobbob.mobends.client.gui.elements;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.previewer.Previewer;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.gui.GuiHelper;
import net.gobbob.mobends.util.Color;
import net.gobbob.mobends.util.Draw;
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
	private EntityLivingBase viewEntity;
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

		if (viewEntity != null)
		{
			float scale = 1.0f - smoothAnimation * 0.2f;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 25, y + height / 2, 0);
			GlStateManager.scale(scale, scale, scale);
			renderLivingEntity(0, 24, 24, viewEntity);
			GlStateManager.popMatrix();
		}

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		String displayText = value ? "Animated" : "Vanilla";
		fontRenderer.drawString(displayText, x + width / 2 - fontRenderer.getStringWidth(displayText) / 2,
				(int) (y + height - smoothAnimation * 10), 0xffffff);

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	public void renderLivingEntity(int argX, int argY, float scale, EntityLivingBase entityLivingBase)
	{
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		GL11.glTranslatef((float) argX, (float) argY, 50.0F);
		GL11.glScalef((float) (-scale), (float) scale, (float) scale);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		GL11.glRotated(rotation, 0.0, 1.0, 0.0);
		
		float lightAngle = 135.0F;
		GL11.glRotatef(lightAngle, 0.0F, 1.0F, 0.0F);
		GL11.glColor3f(1, 1, 1);
		RenderHelper.enableStandardItemLighting();
		GL11.glRotatef(-lightAngle, 0.0F, 1.0F, 0.0F);
		
		float f2 = entityLivingBase.renderYawOffset;
		float f3 = entityLivingBase.rotationYaw;
		float f4 = entityLivingBase.rotationPitch;
		float f5 = entityLivingBase.prevRotationYawHead;
		float f6 = entityLivingBase.rotationYawHead;
		entityLivingBase.renderYawOffset = 0;
		entityLivingBase.rotationYaw = 0;
		entityLivingBase.rotationPitch = 0;
		entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
		entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
		
		Minecraft.getMinecraft().getRenderManager().playerViewY = 180.0F;
		
		Previewer previewer = null;
		AnimatedEntity animatedEntity = AnimatedEntity.getByEntity(entityLivingBase);
		if (animatedEntity != null)
		{
			previewer = animatedEntity.getPreviewer();
		}
		
		if (previewer != null)
			previewer.prePreview(entityLivingBase, this.animationToPreview);
		
		Minecraft.getMinecraft().getRenderManager().renderEntity(entityLivingBase, 0.0D, 0.0D, 0.0D, 0.0F, 1.0f,
				false);
		
		if (previewer != null)
			previewer.postPreview(entityLivingBase, this.animationToPreview);
		
		entityLivingBase.renderYawOffset = f2;
		entityLivingBase.rotationYaw = f3;
		entityLivingBase.rotationPitch = f4;
		entityLivingBase.prevRotationYawHead = f5;
		entityLivingBase.rotationYawHead = f6;
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

	public GuiPortraitDisplay setValue(boolean value)
	{
		this.value = value;
		return this;
	}

	public void toggle()
	{
		value = !value;
	}

	public GuiPortraitDisplay setViewEntity(EntityLivingBase viewEntity)
	{
		this.viewEntity = viewEntity;
		return this;
	}
	
	public void setAnimationToPreview(String animation)
	{
		this.animationToPreview = animation;
	}

	public EntityLivingBase getViewEntity()
	{
		return viewEntity;
	}

	public boolean getValue()
	{
		return value;
	}
}

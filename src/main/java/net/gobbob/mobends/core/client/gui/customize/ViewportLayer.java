package net.gobbob.mobends.core.client.gui.customize;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.gui.GuiHelper;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.util.Color;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.MeshBuilder;
import net.gobbob.mobends.core.util.Vector3;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ViewportLayer extends Gui implements IGuiLayer
{
	private final ResourceLocation STAND_BLOCK_TEXTURE = new ResourceLocation(ModStatics.MODID, "textures/stand_block.png");
	private final Minecraft mc;
	private final ViewportCamera camera;
	private int x, y;
	private int width, height;
	private AlterEntry<?> alterEntryToView;
	
	private VertexBuffer buffer;
	
	public ViewportLayer()
	{
		this.mc = Minecraft.getMinecraft();
		this.camera = new ViewportCamera(0, 0, 0, -45F, 45F);
		this.camera.anchorTo(0, 0, 0, 1);
		
		IBlockState state = Blocks.GRASS.getDefaultState();
		IBakedModel model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		
		BufferBuilder bufferBuilder = new BufferBuilder(16);
		bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
		System.out.println(mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModelFlat(mc.world, model, state, BlockPos.ORIGIN, bufferBuilder, false, 0));
		bufferBuilder.finishDrawing();
		
		buffer = new VertexBuffer(bufferBuilder.getVertexFormat());
		buffer.bufferData(bufferBuilder.getByteBuffer());
	}
	
	public void showAlterEntry(AlterEntry<?> alterEntry)
	{
		this.alterEntryToView = alterEntry;
		Vector3 anchorPoint = Vector3.ZERO;
		IPreviewer previewer = alterEntry.getPreviewer();
		if (previewer != null)
			anchorPoint = previewer.getAnchorPoint();
		this.camera.anchorTo(anchorPoint.x, anchorPoint.y, anchorPoint.z, 5);
	}
	
	public void initGui(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public void update(int mouseX, int mouseY)
	{
	}
	
	@Override
	public boolean handleMouseInput()
	{
		boolean eventHandled = false;
		
		float dx = Mouse.getEventDX();
		float dy = Mouse.getEventDY();
		
		if (Mouse.isButtonDown(2))
		{
			this.camera.rotateYaw(dx * 1F);
			this.camera.rotatePitch(dy * -1F);
			eventHandled |= true;
		}
		
		int mouseWheelRoll = Mouse.getEventDWheel();

		if (mouseWheelRoll != 0)
		{
			mouseWheelRoll = mouseWheelRoll > 0 ? 1 : -1;
			
			this.camera.zoomInOrOut(-mouseWheelRoll);
			eventHandled |= true;
		}
		
		return eventHandled;
	}
	
	@Override
	public void draw()
	{
		int[] position = GuiHelper.getDeScaledCoords(x, y + height + 1);
		int[] size = GuiHelper.getDeScaledVector(width, height + 2);
		//GL11.glEnable(GL11.GL_SCISSOR_TEST);
		//GL11.glScissor(position[0], position[1], size[0], size[1]);

		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		float ratio = (float)mc.displayWidth / (float)mc.displayHeight;
		Project.gluPerspective(60.0F, ratio, 0.05F, 1000);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.clearColor(0.1F, 0.17F, 0.2F, 1F);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		if (alterEntryToView != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			this.camera.applyTransform();
			
			RenderHelper.enableStandardItemLighting();
			GlStateManager.color(1, 1, 1);
			mc.getTextureManager().bindTexture(STAND_BLOCK_TEXTURE);
			Tessellator tess = Tessellator.getInstance();
			tess.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
			MeshBuilder.texturedSimpleCube(tess.getBuffer(), -0.5, -1, -0.5, 0.5, 0, 0.5, Color.WHITE, new int[] {16, 0, 16, 0, 16, 0, 16, 0, 0, 0, 32, 0}, 64, 16, 16);
			tess.draw();
			
			//Draw.cube(0, 0, 0, 1, 1, 1, Color.BLUE);
			
			renderLivingEntity(alterEntryToView);
			
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		//GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	private static void renderLivingEntity(AlterEntry<?> alterEntry)
	{
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		
		float lightAngle = 45.0F;
		GL11.glRotatef(lightAngle, 0.0F, 1.0F, 0.0F);
		GL11.glColor3f(1, 1, 1);
		
		GL11.glRotatef(-lightAngle, 0.0F, 1.0F, 0.0F);
		
		LivingEntityData<?> data = alterEntry.getDataForPreview();
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
		
		@SuppressWarnings("unchecked")
		IPreviewer<LivingEntityData<?>> previewer = (IPreviewer<LivingEntityData<?>>) alterEntry.getPreviewer();
		
//		if (previewer != null)
//			previewer.prePreview(data, this.animationToPreview);
		
		Minecraft.getMinecraft().getRenderManager().renderEntity(living, 0.0D, 0.0D, 0.0D, 0.0F, 1.0f,
				false);
		
//		if (previewer != null)
//			previewer.postPreview(data, this.animationToPreview);
		
		living.renderYawOffset = f2;
		living.rotationYaw = f3;
		living.rotationPitch = f4;
		living.prevRotationYawHead = f5;
		living.rotationYawHead = f6;
		GL11.glPopMatrix();
		
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
}

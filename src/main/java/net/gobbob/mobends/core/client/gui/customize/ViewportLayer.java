package net.gobbob.mobends.core.client.gui.customize;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.Mesh;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.gui.GuiHelper;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.math.Ray;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.util.Color;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.MeshBuilder;
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
	private Mesh standBlockMesh;
	private Ray ray;
	
	public ViewportLayer()
	{
		this.mc = Minecraft.getMinecraft();
		this.camera = new ViewportCamera(0, 0, 0, -45F / 180.0F * GUtil.PI, 45F / 180.0F * GUtil.PI);
		this.camera.anchorTo(0, 0, 0, 1);
		
		IBlockState state = Blocks.GRASS.getDefaultState();
		IBakedModel model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		
		this.standBlockMesh = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 24);
		this.standBlockMesh.beginDrawing(GL11.GL_QUADS);
		MeshBuilder.texturedSimpleCube(this.standBlockMesh, -0.5, -1, -0.5, 0.5, 0, 0.5, Color.WHITE, new int[] {16, 0, 16, 0, 16, 0, 16, 0, 0, 0, 32, 0}, 64, 16, 16);
		this.standBlockMesh.finishDrawing();
	}
	
	public void showAlterEntry(AlterEntry<?> alterEntry)
	{
		this.alterEntryToView = alterEntry;
		IVec3fRead anchorPoint = Vec3f.ZERO;
		IPreviewer previewer = alterEntry.getPreviewer();
		if (previewer != null)
			anchorPoint = previewer.getAnchorPoint();
		this.camera.anchorTo(anchorPoint.getX(), anchorPoint.getY(), anchorPoint.getZ(), 5);
	}
	
	public void initGui(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public void update(int mouseX, int mouseY)
	{
		final float moveSpeed = 0.5F;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
			this.camera.moveForward(moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			this.camera.moveForward(-moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			this.camera.moveSideways(moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			this.camera.moveSideways(-moveSpeed);
	}
	
	@Override
	public boolean handleKeyTyped(char typedChar, int keyCode)
	{
		// Assuming that it will be handled.
		boolean eventHandled = true;
		
		final float moveSpeed = 1;
		
		switch(keyCode)
		{
			default:
				// No case met, event was actually not handled.
				eventHandled = false;
		}
		
		System.out.println(keyCode);
		
		return eventHandled;
	}
	
	@Override
	public boolean handleMouseInput()
	{
		boolean eventHandled = false;
		
		// Camera rotation
		
		float dx = Mouse.getEventDX();
		float dy = Mouse.getEventDY();
		
		if (Mouse.isButtonDown(2))
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				final float speed = 0.02F;
				this.camera.moveSideways(-dx * speed);
				this.camera.moveUp(-dy * speed);
			}
			else
			{
				final float speed = 1 / 180.0F * GUtil.PI;
				this.camera.rotateYaw(dx * speed);
				this.camera.rotatePitch(-dy * speed);
			}
			
			eventHandled |= true;
		}

		// Mouse wheel
		
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
	public boolean handleMouseClicked(int mouseX, int mouseY, int button)
	{
		boolean eventHandled = false;
		
		if (button == 0)
		{
			this.ray = new Ray(this.camera.getPosition(), this.camera.getForward());
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
			//this.standBlockMesh.display();
			
			GlStateManager.disableTexture2D();
			
			if (this.ray != null)
			{
				final float scale = 5;
				IVec3fRead pos = this.ray.getPosition();
				IVec3fRead dir = this.ray.getDirection();
				
				Draw.line(pos.getX(), pos.getY(), pos.getZ(),
						  pos.getX() + dir.getX() * scale, pos.getY() + dir.getY() * scale, pos.getZ() + dir.getZ() * scale,
						  Color.RED);
			}
			
			GlStateManager.enableTexture2D();
			
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

package net.gobbob.mobends.core.client.gui.customize.viewport;

import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.gui.elements.GuiToggleButton;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.Mesh;
import net.gobbob.mobends.core.client.gui.GuiHelper;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.math.TransformUtils;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.physics.OBBox;
import net.gobbob.mobends.core.math.physics.Physics;
import net.gobbob.mobends.core.math.physics.Plane;
import net.gobbob.mobends.core.math.physics.Ray;
import net.gobbob.mobends.core.math.physics.RayHitInfo;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.math.vector.VectorUtils;
import net.gobbob.mobends.core.util.Color;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.GlHelper;
import net.gobbob.mobends.core.util.MeshBuilder;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class ViewportLayer extends Gui implements IGuiLayer
{
	
	private final ResourceLocation STAND_BLOCK_TEXTURE = new ResourceLocation(ModStatics.MODID, "textures/stand_block.png");
	private final Minecraft mc;
	private final ViewportCamera camera;
	private int x, y;
	private int width, height;
	private AlterEntry<?> alterEntryToView;
	private AlterEntryRig rig;
	private GuiToggleButton toggleButton;

	private Mesh standBlockMesh;
	private Plane groundPlane;
	private OBBox obBox;
	private Vec3f contactPoint;
	
	public ViewportLayer()
	{
		this.mc = Minecraft.getMinecraft();
		this.camera = new ViewportCamera(0, 0, 0, -45F / 180.0F * GUtil.PI, 45F / 180.0F * GUtil.PI);
		this.camera.anchorTo(0, 0, 0, 1);

		this.toggleButton = new GuiToggleButton();

		IBlockState state = Blocks.GRASS.getDefaultState();
		IBakedModel model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		
		this.standBlockMesh = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 24);
		this.standBlockMesh.beginDrawing(GL11.GL_QUADS);
		MeshBuilder.texturedSimpleCube(this.standBlockMesh, -0.5, -1, -0.5, 0.5, 0, 0.5, Color.WHITE, new int[] {16, 0, 16, 0, 16, 0, 16, 0, 0, 0, 32, 0}, 64, 16, 16);
		this.standBlockMesh.finishDrawing();
		
		this.groundPlane = new Plane(0, 0, 0, 0, 1, 0);
		Mat4x4d mat = new Mat4x4d(Mat4x4d.IDENTITY);
		TransformUtils.translate(mat, 0, 0, -4, mat);
		TransformUtils.rotate(mat, Math.PI/4, 0, 1, 0, mat);
		TransformUtils.scale(mat, 2F, 2F, 2F);
		this.obBox = new OBBox(-0.2F, -0.2F, -0.2F, 0.2F, 0.2F, 0.2F, mat);
		this.contactPoint = new Vec3f();
	}
	
	public void showAlterEntry(AlterEntry<?> alterEntry)
	{
		this.alterEntryToView = alterEntry;
		IVec3fRead anchorPoint = Vec3f.ZERO;
		IPreviewer previewer = alterEntry.getPreviewer();
		if (previewer != null)
		{
			anchorPoint = previewer.getAnchorPoint();
			this.rig = new AlterEntryRig(alterEntry);
		}
		else
		{
			this.rig = null;
		}
		this.camera.anchorTo(anchorPoint.getX(), anchorPoint.getY(), anchorPoint.getZ(), 5);
	}
	
	@Override
	public void handleResize(int width, int height)
	{
		this.width = width;
		this.height = height;
		float ratio = (float) mc.displayWidth / (float) mc.displayHeight;
		this.camera.setupProjection(60.0F, ratio, 0.05F, 1000);
	}

	@Override
	public void update(int mouseX, int mouseY)
	{
		final float moveSpeed = 0.5F;

		this.camera.update();

		if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
			this.camera.moveForward(moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			this.camera.moveForward(-moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			this.camera.moveSideways(moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			this.camera.moveSideways(-moveSpeed);
		
		AlterEntryRig.Bone boneAtMouse = this.getBoneAtScreenCoords(mouseX, mouseY);
		if (this.rig != null)
		{
			this.rig.hoverOver(boneAtMouse);
		}
	}
	
	public AlterEntryRig.Bone getBoneAtScreenCoords(int screenX, int screenY)
	{
		AlterEntryRig.Bone closestBone = null;
		Ray ray = this.camera.getRayFromScreen(screenX, screenY, this.width, this.height);
		if (this.rig != null)
		{
			float smallestDistanceSq = 0;
			for (AlterEntryRig.Bone bone : this.rig.nameToBoneMap.values())
			{
				RayHitInfo hit = Physics.intersect(ray, bone.collider);
				if (hit != null)
				{
					float distanceSq = VectorUtils.distanceSq(hit.hitPoint, camera.getPosition());
					if (closestBone == null || distanceSq < smallestDistanceSq)
					{
						closestBone = bone;
						smallestDistanceSq = distanceSq;
					}
				}
			}
		}
		
		return closestBone;
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
			AlterEntryRig.Bone boneAtMouse = this.getBoneAtScreenCoords(mouseX, mouseY);
			
			if (boneAtMouse != null)
			{
				rig.select(boneAtMouse);
			}
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
		this.camera.applyProjection();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.clearColor(0.1F, 0.17F, 0.2F, 1F);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		if (alterEntryToView != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			this.camera.applyViewTransform(DataUpdateHandler.partialTicks);
			
			RenderHelper.enableStandardItemLighting();
			GlStateManager.color(1, 1, 1);
			mc.getTextureManager().bindTexture(STAND_BLOCK_TEXTURE);
			this.standBlockMesh.display();
			
			GlStateManager.disableTexture2D();
			
			/*if (this.ray != null)
			{
				final float scale = 5;
				IVec3fRead pos = this.ray.getPosition();
				IVec3fRead dir = this.ray.getDirection();
				
				Draw.line(pos.getX(), pos.getY(), pos.getZ(),
						  pos.getX() + dir.getX() * scale, pos.getY() + dir.getY() * scale, pos.getZ() + dir.getZ() * scale,
						  Color.RED);
			}*/
			
			/*if (this.contactPoint != null)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.contactPoint.x, this.contactPoint.y, this.contactPoint.z);
				float s = 0.03F;
				Draw.cube(-s, -s, -s, s, s, s, Color.RED);
				GlStateManager.popMatrix();
			}*/
			
			GlStateManager.pushMatrix();
			GlHelper.transform(this.obBox.transform);
			double[] fields = this.obBox.transform.getFields();
			Draw.cube(this.obBox.min.getX(), this.obBox.min.getY(), this.obBox.min.getZ(),
					  this.obBox.max.getX(), this.obBox.max.getY(), this.obBox.max.getZ(), new Color(0, 0, 0, 0.5F));
			GlStateManager.popMatrix();
			
			GlStateManager.enableTexture2D();
			
			renderLivingEntity(this.alterEntryToView);
			
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderHelper.disableStandardItemLighting();
			
			if (this.rig != null)
			{
				this.rig.updateTransform();
				this.rig.nameToBoneMap.forEach((key, bone) -> {
					
					if (this.rig.isBoneHoveredOver(bone) || this.rig.isBoneSelected(bone))
					{
						Color color = this.rig.isBoneHoveredOver(bone) ? new Color(1, 1, 1, 0.6F) : new Color(1, 1, 0.9F, 0.7F);
						
						GlStateManager.pushMatrix();
						GlHelper.transform(bone.collider.transform);
						final double p = 0.03;
						Draw.cube(bone.collider.min.getX() - p, bone.collider.min.getY() - p, bone.collider.min.getZ() - p,
								  bone.collider.max.getX() + p, bone.collider.max.getY() + p, bone.collider.max.getZ() + p, color);
						GlStateManager.popMatrix();
					}
					
				});
			}
			
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
		
		if (previewer != null)
			previewer.prePreview(data, "walk");
		
		Minecraft.getMinecraft().getRenderManager().renderEntity(living, 0.0D, 0.0D, 0.0D, 0.0F, 1.0f,
				false);
		
		if (previewer != null)
			previewer.postPreview(data, "walk");
		
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

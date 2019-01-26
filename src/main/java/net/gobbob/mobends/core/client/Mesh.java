package net.gobbob.mobends.core.client;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.core.util.Color;
import net.gobbob.mobends.core.util.IColorRead;
import net.gobbob.mobends.core.util.MeshBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class Mesh
{
	
	private VertexFormat vertexFormat;
	private BufferBuilder bufferBuilder;
	private VertexBuffer buffer;
	
	public Mesh(VertexFormat vertexFormat, int maxVertices)
	{
		this.vertexFormat = vertexFormat;
		this.bufferBuilder = new BufferBuilder(vertexFormat.getSize() * maxVertices);
	}
	
	public void beginDrawing(int drawMode)
	{
		this.bufferBuilder.begin(drawMode, this.vertexFormat);
	}
	
	public void finishDrawing()
	{
		this.bufferBuilder.finishDrawing();
		this.buffer = new VertexBuffer(this.vertexFormat);
		this.buffer.bufferData(this.bufferBuilder.getByteBuffer());
	}
	
	public Mesh pos(double x, double y, double z)
	{
		this.bufferBuilder.pos(x, y, z);
		return this;
	}
	
	public Mesh normal(float x, float y, float z)
	{
		this.bufferBuilder.normal(x, y, z);
		return this;
	}
	
	public Mesh tex(double u, double v)
	{
		this.bufferBuilder.tex(u, v);
		return this;
	}
	
	public Mesh color(IColorRead color)
	{
		this.bufferBuilder.color(color.getR(), color.getG(), color.getB(), color.getA());
		return this;
	}
	
	public void endVertex()
	{
		this.bufferBuilder.endVertex();
	}
	
	public void display()
	{
		if (this.bufferBuilder.getVertexCount() > 0)
        {
            int i = this.vertexFormat.getSize();
            ByteBuffer bytebuffer = this.bufferBuilder.getByteBuffer();
            List<VertexFormatElement> list = this.vertexFormat.getElements();

            for (int j = 0; j < list.size(); ++j)
            {
                VertexFormatElement vertexformatelement = list.get(j);
                VertexFormatElement.EnumUsage usage = vertexformatelement.getUsage();
                int k = vertexformatelement.getType().getGlConstant();
                int l = vertexformatelement.getIndex();
                bytebuffer.position(this.vertexFormat.getOffset(j));

                // moved to VertexFormatElement.preDraw
                usage.preDraw(this.vertexFormat, j, i, bytebuffer);
            }

            GlStateManager.glDrawArrays(this.bufferBuilder.getDrawMode(), 0, this.bufferBuilder.getVertexCount());
            int i1 = 0;

            for (int j1 = list.size(); i1 < j1; ++i1)
            {
                VertexFormatElement vertexformatelement1 = list.get(i1);
                VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
                int k1 = vertexformatelement1.getIndex();

                // moved to VertexFormatElement.postDraw
                vertexformatelement1.getUsage().postDraw(this.vertexFormat, i1, i, bytebuffer);
            }
        }
	}

	public BufferBuilder getBufferBuilder()
	{
		return this.bufferBuilder;
	}
	
}

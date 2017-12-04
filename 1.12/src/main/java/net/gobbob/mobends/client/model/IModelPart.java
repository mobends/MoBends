package net.gobbob.mobends.client.model;

public interface IModelPart
{
	public void applyTransform(float scale);
	public void renderPart(float scale);
	public void update(float ticksPerFrame);
}

package net.minecraft.client.renderer.entity;

import net.minecraft.entity.Entity;

public class RenderManager
{
    public RenderPlayer playerRenderer = new RenderPlayer(false);

    @SuppressWarnings("unchecked")
    public <T extends Entity> Render<T> getEntityRenderObject(T entity)
    {
        return (Render<T>) playerRenderer;
    }
}

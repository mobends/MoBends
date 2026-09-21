package net.minecraft.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.world.World;

public class Minecraft
{
    private static final Minecraft INSTANCE = new Minecraft();

    public World world;
    public EntityPlayerSP player;
    public boolean gamePaused;
    private final RenderManager renderManager = new RenderManager();

    public static Minecraft getMinecraft() { return INSTANCE; }
    public RenderManager getRenderManager() { return renderManager; }
    public boolean isGamePaused() { return gamePaused; }
}

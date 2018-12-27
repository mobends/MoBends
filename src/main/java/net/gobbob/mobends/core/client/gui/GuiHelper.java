package net.gobbob.mobends.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class GuiHelper {
	public static int[] getDeScaledCoords(int x, int y) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int i1 = scaledresolution.getScaledWidth();
        int j1 = scaledresolution.getScaledHeight();
        int k1 = x * Minecraft.getMinecraft().displayWidth / i1;
        int l1 = (j1 - y) * Minecraft.getMinecraft().displayHeight / j1 + 1;
        return new int[] {k1, l1};
	}
	
	public static int[] getDeScaledVector(int x, int y) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int i1 = scaledresolution.getScaledWidth();
        int j1 = scaledresolution.getScaledHeight();
        int k1 = x * Minecraft.getMinecraft().displayWidth / i1;
        int l1 = y * Minecraft.getMinecraft().displayHeight / j1;
        return new int[] {k1, l1};
	}
}

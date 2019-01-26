package net.gobbob.mobends.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.gui.FontRenderer;

public class GUtil
{
	public static final float PI = (float) Math.PI;
	
	public static float clamp(float value, float min, float max)
	{
		return Math.min(Math.max(value, min), max);
	}
	
	public static int clamp(int value, int min, int max)
	{
		return Math.min(Math.max(value, min), max);
	}
	
	public static double angleFromCoordinates(double x, double z)
	{
		return Math.atan2(x, z) / Math.PI * 180.0;
	}
	
	public static double getRadianDifference(double a, double b)
	{
		a = wrapRadians(a);
		b = wrapRadians(b);
		double d = Math.abs(a - b);
		d = d > Math.PI ? Math.PI*2-d : d;
		return d;
	}
	
	public static double wrapRadians(double a)
	{
		a = a % Math.PI;

        if (a >= Math.PI)
        {
            a -= Math.PI*2;
        }
        else if (a < -Math.PI)
        {
            a += Math.PI*2;
        }
        
        return a;
	}
	
	public static IVec3f translate(IVec3f vector, float x, float y, float z)
	{
		vector.add(x, y, z);
		return vector;
	}
	
	public static IVec3f scale(IVec3f vector, float x, float y, float z)
	{
		vector.scale(x, y, z);
		return vector;
	}
	
	public static Vector3f rotateX(Vector3f num, float rotation)
	{
		Vector3f y = new Vector3f();
		Vector3f z = new Vector3f();
		
		y.y = (float) Math.cos((180F+rotation)/180F*PI);
		y.z = (float) Math.sin((180F+rotation)/180F*PI);
		y.normalise();
		y.y*=-num.y;
		y.z*=num.y;
		
		z.y = (float) Math.sin((180.0f+rotation)/180.0f*PI);
		z.z = (float) Math.cos((180.0f+rotation)/180.0f*PI);
		z.normalise();
		z.y*=-num.z;
		z.z*=-num.z;
		
		num = new Vector3f(num.x,y.y+z.y,y.z+z.z);
		return num;
	}
	
	public static Vector3f rotateY(Vector3f num, float rotation){
		Vector3f x = new Vector3f();
		Vector3f z = new Vector3f();
		
		x.x = (float) Math.cos((-rotation)/180.0f*Math.PI);
		x.z = (float) Math.sin((-rotation)/180.0f*Math.PI);
		x.normalise();
		x.x*=-num.x;
		x.z*=num.x;
		
		z.x = (float) Math.sin((-rotation)/180.0f*Math.PI);
		z.z = (float) Math.cos((-rotation)/180.0f*Math.PI);
		z.normalise();
		z.x*=num.z;
		z.z*=num.z;
		
		num = new Vector3f(x.x+z.x,num.y,x.z+z.z);
		return num;
	}
	
	public static Vector3f rotateZ(Vector3f num, float rotation){
		Vector3f x = new Vector3f();
		Vector3f y = new Vector3f();
		
		x.x = (float) Math.sin((rotation-90.0f)/180.0f*Math.PI);
		x.y = (float) Math.cos((rotation-90.0f)/180.0f*Math.PI);
		x.normalise();
		x.x*=-num.x;
		x.y*=num.x;
		
		y.x = (float) Math.cos((rotation-90.0f)/180.0f*Math.PI);
		y.y = (float) Math.sin((rotation-90.0f)/180.0f*Math.PI);
		y.normalise();
		y.x*=-num.y;
		y.y*=-num.y;
		
		num = new Vector3f(y.x+x.x,y.y+x.y,num.z);
		return num;
	}
	
	public static void rotate(IVec3f[] points, Quaternion rotation)
	{
		for (IVec3f point : points)
		{
			QuaternionUtils.multiply(point, rotation, point);
		}
	}
	
	public static float interpolateRotation(float a, float b, float partialTicks)
    {
        float f;
        for (f = b - a; f < -180.0F; f += 360.0F);

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return a + partialTicks * f;
    }
	
	public static IVec3f[] translate(IVec3f[] vectors, float x, float y, float z)
	{
		for(IVec3f vector : vectors)
		{
			translate(vector, x, y, z);
		}
		return vectors;
	}
	
	public static IVec3f[] scale(IVec3f[] vectors, float x, float y, float z)
	{
		for(IVec3f vector : vectors)
		{
			scale(vector, x, y, z);
		}
		return vectors;
	}
	
	public static Vector3f[] rotateX(Vector3f[] nums, float move){
		for(int i = 0;i < nums.length;i++){
			nums[i] = rotateX(nums[i],move);
		}
		return nums;
	}
	
	public static Vector3f[] rotateY(Vector3f[] nums, float move){
		for(int i = 0;i < nums.length;i++){
			nums[i] = rotateY(nums[i],move);
		}
		return nums;
	}
	
	public static Vector3f[] rotateZ(Vector3f[] nums, float move){
		for(int i = 0;i < nums.length;i++){
			nums[i] = rotateZ(nums[i],move);
		}
		return nums;
	}
	
	public static String[] squashText(FontRenderer fontRenderer, String text, int maxWidth) {
		if(maxWidth <= 0) return new String[]{};
		if(text.indexOf(" ") == -1)
			return new String[]{text};
		
		List<String> lines = new ArrayList<String>();
		String leftover = text + "";
		String line = "";
		do {
			int currentWidth = fontRenderer.getStringWidth(line + " " + leftover.substring(0, leftover.indexOf(" ")));
			if(currentWidth > maxWidth) {
				lines.add(line.trim());
				line = leftover.substring(0, leftover.indexOf(" "));
			}else{
				line += " " + leftover.substring(0, leftover.indexOf(" "));
			}
			
			leftover = leftover.substring(leftover.indexOf(" ")+1);
		}while(fontRenderer.getStringWidth(leftover) > maxWidth && leftover.indexOf(" ") != -1);
		lines.add(line.trim());
		line = leftover;
		lines.add(line.trim());
		return lines.toArray(new String[]{});
	}
	
	public static String[] readLines(BufferedReader reader) {
		try {
			List<String> lines = new ArrayList<String>();
			String line = reader.readLine();
			while(line != null) {
				lines.add(line);
				line = reader.readLine();
			}
			reader.close();
			return lines.toArray(new String[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[] readLines(File file) {
		try {
			return readLines(new BufferedReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readFile(BufferedReader reader) {
		try {
			String content = "";
			String line;
			while((line = reader.readLine()) != null) {
				content += line + "\n";
			}
			reader.close();
			return content;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readFile(File file) {
		try {
			return readFile(new BufferedReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeLines(File file, String[] lines) {
		BufferedWriter os;
		try {
			os = new BufferedWriter(new FileWriter(file));
			for(String line : lines) {
				os.write(line);
				os.newLine();
			}
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double easeInOut(double a, double power)
	{
		if(a < 0.5)
		{
			a *= 2;
			a = Math.pow(a, power);
			a /= 2;
		}
		else
		{
			a = 1 - a;
			a *= 2;
			a = Math.pow(a, power);
			a /= 2;
			a = 1 - a;
		}
		return a;
	}
}

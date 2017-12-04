package net.gobbob.mobends.util;

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

import net.gobbob.mobends.pack.PackManager;
import net.gobbob.mobends.pack.BPDFile.Entry;
import net.minecraft.client.gui.FontRenderer;

public class GUtil {
	public static Vector3f min(Vector3f vec, float value){
		Vector3f result = vec;
		result.x = vec.x > value ? value : vec.x;
		result.y = vec.y > value ? value : vec.y;
		result.z = vec.z > value ? value : vec.z;
		return result;
	}
	
	public static float clamp(float value, float min, float max) {
		return Math.min(Math.max(value, min), max);
	}
	
	public static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}
	
	public static Vector3f translate(Vector3f num, Vector3f move){
		num.x+=move.x;
		num.y+=move.y;
		num.z+=move.z;
		return num;
	}
	
	public static Vector3f scale(Vector3f num, Vector3f move){
		num.x*=move.x;
		num.y*=move.y;
		num.z*=move.z;
		return num;
	}
	
	public static Vector3f rotateX(Vector3f num, float rotation){
		Vector3f y = new Vector3f();
		Vector3f z = new Vector3f();
		
		y.y = (float) Math.cos((180.0f+rotation)/180.0f*Math.PI);
		y.z = (float) Math.sin((180.0f+rotation)/180.0f*Math.PI);
		y.normalise();
		y.y*=-num.y;
		y.z*=num.y;
		
		z.y = (float) Math.sin((180.0f+rotation)/180.0f*Math.PI);
		z.z = (float) Math.cos((180.0f+rotation)/180.0f*Math.PI);
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
	
	public static Vector3f[] translate(Vector3f[] nums, Vector3f move){
		for(int i = 0;i < nums.length;i++){
			nums[i] = translate(nums[i],move);
		}
		return nums;
	}
	
	public static Vector3f[] scale(Vector3f[] nums, Vector3f move){
		for(int i = 0;i < nums.length;i++){
			nums[i] = scale(nums[i],move);
		}
		return nums;
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
}

package net.gobbob.mobends.core.animation.keyframe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class AnimationLoader
{
	public static KeyframeAnimation loadFromFile(File file) throws FileNotFoundException
	{
		JsonReader fileReader = new JsonReader(new FileReader(file));
		Gson gson = new Gson();
		KeyframeAnimation animation = gson.fromJson(fileReader, KeyframeAnimation.class);
		
		return animation;
	}

	public static KeyframeAnimation loadFromString(String animationJson)
	{
		return (new Gson()).fromJson(animationJson, KeyframeAnimation.class);
	}
}

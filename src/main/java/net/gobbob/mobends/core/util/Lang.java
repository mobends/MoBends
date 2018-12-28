package net.gobbob.mobends.core.util;

import net.minecraft.client.resources.I18n;

public class Lang
{
	public static String format(String langKey, Object... params)
	{
		return I18n.format(langKey, params);
	}
	
	public static String localize(String key)
	{
		return I18n.format(key);
	}
}

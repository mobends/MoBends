package net.gobbob.mobends.core.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.gobbob.mobends.core.main.ModStatics;

public class BendsLogger {
	private static Logger logger = Logger.getLogger(ModStatics.MODID);
	
	public static void log(Level level, String msg){
    	logger.log(level, msg);
    }
	
	public static void info(String msg) {
		logger.info(msg);
	}
}
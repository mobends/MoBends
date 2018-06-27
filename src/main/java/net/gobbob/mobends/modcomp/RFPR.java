package net.gobbob.mobends.modcomp;

public class RFPR {
	public static boolean enabled = false;
	
	public static void init() {
		RFPR.enabled = true;
		try {
			Class.forName( "realrender.REN" );
		} catch( ClassNotFoundException e ) {
			RFPR.enabled = false;
		}
	}
	
	public static boolean hideFirstPersonHead() {
		return RFPR.enabled;
	}
}
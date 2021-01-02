package io.opensw.scheduler.core.utils;

import java.net.InetAddress;

public class ServerUtils {

	
	private ServerUtils() {
		// do nothing
	}
	
	/**
	 * Load server name
	 * 
	 * @return server name
	 */
	public static String loadServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		}
		catch ( Exception e ) {
			return "unknown";
		}
	}
}

package io.opensw.scheduler.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class SnapExceptionUtils {

	
	private SnapExceptionUtils() {
		// do nothing
	}
	
	/**
	 * Convert exception to map.
	 *
	 * @param e - exception to process
	 * @return map with exception stack trace
	 */
	public static Map< String, String > toMap( Exception e ) {
		Map< String, String > map = new HashMap<>();
		map.put( "message", e.getMessage() );
		map.put( "stacktrace", ExceptionUtils.getStackTrace( e ) );

		return map;
	}
	
}

package io.opensw.scheduler.core.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith( JUnitPlatform.class )
class SnapExceptionUtilsTest {

	@Test
	void toMap() {
		Map< String, String > map = SnapExceptionUtils.toMap( new Exception( "Message test" ) );
		assertEquals( "Message test", map.get( "message" ) );
		assertNotNull( map.get( "stacktrace" ) );
	}
	
}

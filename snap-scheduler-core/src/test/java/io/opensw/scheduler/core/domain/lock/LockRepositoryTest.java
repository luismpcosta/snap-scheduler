package io.opensw.scheduler.core.domain.lock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.opensw.scheduler.core.exceptions.DatabaseException;

@RunWith( JUnitPlatform.class )
class LockRepositoryTest {

	private LockRepository lockRepository;

	protected DataSource dataSource() {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL(
				"jdbc:h2:mem:snap;DB_CLOSE_DELAY=-1;init=runscript from 'classpath:schema.sql'"
		);
		dataSource.setUser( "sa" );
		dataSource.setPassword( "sa" );
		return dataSource;
	}

	@BeforeEach
	public void init() {
		lockRepository = new LockRepositoryImpl( dataSource() );
	}

	@Test
	void insertLockSuccess() throws SQLException, DatabaseException {
		boolean result = lockRepository
				.lockTask( UUID.randomUUID().toString(), "io.opensw.scheduler", 60000, "server-test" );

		assertTrue( result );
	}

	@Test
	void updateLockSuccess() throws SQLException, DatabaseException, InterruptedException {
		final String key = UUID.randomUUID().toString();
		boolean result = lockRepository.lockTask( key, "io.opensw.scheduler", 1, "server-test" );

		assertTrue( result );

		// only sleep to guarantee that lock pass
		TimeUnit.MILLISECONDS.sleep(1100);

		result = lockRepository.lockTask( key, "io.opensw.scheduler", 1, "server-test" );

		assertTrue( result );
	}

	@Test
	void updateLockTimeAfter() throws SQLException, DatabaseException, InterruptedException {
		final String key = UUID.randomUUID().toString();
		boolean result = lockRepository.lockTask( key, "io.opensw.scheduler", 10, "server-test" );

		assertTrue( result );

		result = lockRepository.lockTask( key, "io.opensw.scheduler", 10, "server-test" );

		assertFalse( result );
	}

	@Test
	void insertLock_DatabaseException() throws SQLException, DatabaseException {
		Assertions.assertThrows( DatabaseException.class, () -> {
			new LockRepositoryImpl( null ).lockTask(
					UUID.randomUUID().toString(), "io.opensw.scheduler", 60000, "server-test"
			);
		} );
	}

	@Test
	void insertLock_PK_NULL() throws SQLException, DatabaseException {
		boolean result = lockRepository.lockTask( null, "io.opensw.scheduler", 60000, "server-test" );

		assertFalse( result );
	}

}

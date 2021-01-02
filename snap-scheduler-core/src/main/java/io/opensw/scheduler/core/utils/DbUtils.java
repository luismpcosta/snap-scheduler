package io.opensw.scheduler.core.utils;

import java.sql.Connection;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbUtils {

	public static final String ERROR_CLOSE_STMT_MSG = "Error on trie to close statement, message: {}";

	public static final String DB_H2 = "H2";

	public static final String DB_POSTGRESQL = "PostgreSQL";

	public static final String DB_MYSQL = "MySQL";

	public static final String DB_MARIADB = "MariaDB";

	public static final String DB_MSSQL_SERVER = "MsSQLServer";

	
	private DbUtils() {
		// do nothing
	}
	
	/**
	 * Determine database type
	 * 
	 * @param dataSource instance to determine type
	 * @return type of database
	 */
	public static String databaseType( final DataSource dataSource ) {
		try ( Connection connection = dataSource.getConnection() ) {
			String productName = connection.getMetaData().getDatabaseProductName().replace( " ", "" ).toLowerCase()
					.trim();
			if ( productName == null || productName.isEmpty() ) {
				return null;
			}
			else if ( productName.contains( DB_POSTGRESQL.toLowerCase() ) ) {
				return DB_POSTGRESQL;
			}
			else if ( productName.contains( DB_MYSQL.toLowerCase() ) ) {
				return DB_MYSQL;
			}
			else if ( productName.contains( DB_MARIADB.toLowerCase() ) ) {
				return DB_MARIADB;
			}
			else if ( productName.contains( "microsoft" ) || productName.contains( "sqlserver" ) ) {
				return DB_MSSQL_SERVER;
			}
			else if ( productName.equalsIgnoreCase( DB_H2 ) ) {
				return DB_H2;
			}
		}
		catch ( Exception e ) {
			log.error( "Error in identify DB type, erro: {}", e.getMessage() );
		}
		return null;
	}
}

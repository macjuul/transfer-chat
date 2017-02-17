package net.exodiusmc.shared;

import net.exodiusmc.shared.exception.SQLConnectionException;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class used to setup a MySQL database connection. The class is supplied with methods to
 * handle data transfer and connection management. It's recommended to {@link #disconnect(DatabasePromise)}
 * from the MySQL server once you are done with it.
 *
 * All datbase interaction is done async, using a chached thread pool. This assures stable thread
 * performance.
 * 
 * @author Macjuul
 * @version 3.0.0
 * @since 26-08-2016
 */
public class Database implements AutoCloseable {

	private static ExecutorService pool = Executors.newCachedThreadPool();

	private Connection conn;

	private String hostname;
	private int port;
	private String username;
	private String password;
	private String database;

	/**
	 * Basic localhost constructor
	 *
	 * @param username Username
	 * @param password Password
	 * @param database Database
	 */
	public Database(String username, String password, String database) {
		this.hostname = "localhost";
		this.port = 3306;
		this.username = username;
		this.password = password;
		this.database = database;
	}

	/**
	 * Advanced custom conn constructor
	 *
	 * @param hostname MySQL Server hostname
	 * @param username Username
	 * @param password Password
	 * @param database Database
	 */
	public Database(String hostname, String username, String password, String database) {
		this.hostname = hostname;
		this.port = 3306;
		this.username = username;
		this.password = password;
		this.database = database;
	}

	/**
	 * Custom conn constructor
	 *
	 * @param hostname MySQL Server constructor
	 * @param port     Port
	 * @param username Username
	 * @param password Password
	 * @param database Datbase
	 */
	public Database(String hostname, int port, String username, String password, String database) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
	}

	/**
	 * Connect to the remote MySQL database.
	 */
	public void connect() {
		connect(null);
	}

	/**
	 * Connect to the remote MySQL database.
	 *
	 * @param callback Async callback
	 * @throws SQLConnectionException When a conn cannot be established
	 */
	public void connect(DatabasePromise callback) {
		ExoValidate.notNull(this.conn, "Could not connect to database: Connection already open");

		// Concat a jdbc adress
		String url = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database;

		// Executor - Attempt a connection
		pool.execute(() -> {
			try {
				this.conn = DriverManager.getConnection(url, username, password);

				if(callback != null) callback.complete(true, null);
			} catch (SQLException ex) {
				Throwable error = new SQLConnectionException("Failed to connect to the database (" + this + ")", ex);

				if(callback != null) callback.complete(false, error);
			}
		});
	}

	/**
	 * Disconnect from the remote MySQL database.
	 */
	public void disconnect() {
		disconnect(null);
	}

	/**
	 * Disconnect from the remote MySQL database.
	 *
	 * @param callback Async callback
	 */
	public void disconnect(DatabasePromise callback) {
		ExoValidate.isNull(this.conn, "Could not disconnect from MySQL Server: Already connected!");

		// Executor - Attempt a connection
		pool.execute(() -> {
			try {
				conn.close();
				conn = null;

				if(callback != null) callback.complete(true, null);
			} catch (SQLException ex) {
				Throwable error = new SQLConnectionException("Failed to disconnect from the database (" + this + ")", ex);

				if(callback != null) callback.complete(false, error);
			}
		});
	}

	@Override
	public void close() {
		disconnect(null);
	}

	/**
	 * Execute a Query on the MySQL remote
	 *
	 * @param query Query
	 * @param values Values
	 * @param callback Callback
	 */
	public void sql(String query, Object[] values, MysqlConnection.MysqlQueryPromise callback) {
		this.executeQuery(query, Arrays.asList(values), callback);
	}

	/**
	 * Execute a Query on the MySQL remote
	 *
	 * @param query Query
	 * @param values Values
	 * @param callback Callback
	 */
	public void executeQuery(String query, List<Object> values, MysqlQueryPromise callback) {
		if(!this.isConnected()) {
			if(logger != null) logger.warning("Could not execute query: No open conn exists!");
			return;
		}

		working = true;

		new Thread() {
			public void run() {
				try {
					PreparedStatement ps = conn.prepareStatement(query);

					for (int i = 0; i < values.size(); i++) {
						ps.setObject(i + 1, values.get(i));
					}

					ps.execute();

					callback.complete(true, ps.getResultSet(), null);
				} catch(SQLException ex) {
					Throwable error = new SQLConnectionException("Failed to execute query (" + this + ")", ex);

					callback.complete(false, null, error);
				}

				working = false;

			}
		}.start();
	}

	/**
	 * Execute a simple query on the MySQL database
	 *
	 * @param query Query
	 * @param callback Callback
	 */
	public void executeQuery(String query, MysqlQueryPromise callback) {
		if(!this.isConnected()) {
			if(logger != null) logger.warning("Could not execute query: No open conn exists!");
			return;
		}

		working = true;

		new Thread() {
			public void run() {
				try {
					Statement ps = conn.createStatement();

					ps.execute(query);

					callback.complete(true, ps.getResultSet(), null);
				} catch(SQLException ex) {
					Throwable error = new SQLConnectionException("Failed to execute query (" + this + ")", ex);

					callback.complete(false, null, error);
				}

				working = false;

			}
		}.start();
	}

	/**
	 * Executes an edit query (UPDATE, DELETE, INSERT) on the MySQL database. Instead of a
	 * ResultSet, this query returns a response int. -1 is returned when no changes have been made.
	 * The row count is returned when a table changing update was successfull.
	 *
	 * @param query Query
	 * @param callback Callback
	 */
	public void executeEditQuery(String query, QueryPromise callback) {
		if(!this.isConnected()) {
			if(logger != null) logger.warning("Could not execute query: No open conn exists!");
			return;
		}

		working = true;

		new Thread() {
			public void run() {
				try {
					Statement ps = conn.createStatement();

					int code = ps.executeUpdate(query);

					callback.complete(true, code, null);
				} catch(SQLException ex) {
					Throwable error = new SQLConnectionException("Failed to execute query (" + this + ")", ex);

					callback.complete(false, -1, error);
				}

				working = false;

			}
		}.start();

	}

	public String getHostname() {
		return this.hostname;
	}

	public int getPort() {
		return this.port;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public String getDatabase() {
		return this.database;
	}

	public boolean isWorking() {
		return this.working;
	}

	/**
	 * Returns true when the conn is currently open
	 *
	 * @return boolean
	 */
	public synchronized boolean isConnected() {
		try {
			return this.conn != null && !this.conn.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	private synchronized void setConnection(Connection con) {
		this.conn = con;
	}

	private synchronized Connection getConnection() {
		return this.conn;
	}
	/*
	 * Promise interfaces
	 */

	/**
	 * Callback handler for conn related methods
	 */
	public interface DatabasePromise {

		void complete(boolean success, Throwable error);

	}

	/**
	 * Callback handler for queries
	 */
	public interface QueryPromise {

		void complete(boolean success, int result, Throwable error);

	}

	@Override
	public String toString() {
		return "MysqlConnection{" + "hostname='" + hostname + '\'' +
				", port='" + port + '\'' +
				", username='" + username + '\'' +
				", database='" + database + '\'' +
				'}';
	}

}

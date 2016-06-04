/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.webserver;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.domain.RegisteredPlayer;

/**
 *
 * @author picardi
 */
public class DBController {

	private final String userName = "root";
	private final String password = "root";
	private final String dbms = "derby";
	private final String server = "localhost";
	private final String port = "1527";
	private final String dbName = "unoxtutti";

	private Connection currentConnection;
	
	private final DBHelper helper;

	public DBController() {
		helper = new DBHelper();
	}
	
	public boolean connect() throws SQLException {
		try {
			if (currentConnection != null) {
				return true;
			}
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			Properties connectionProps = new Properties();
			connectionProps.put("user", this.userName);
			connectionProps.put("password", this.password);
			
			currentConnection = DriverManager.getConnection(
				"jdbc:"
					+ this.dbms + "://"
					+ this.server + ":"
					+ this.port + "/"
					+ this.dbName + ";create=true",
				connectionProps);
			System.out.println("Connected to database");
			return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(DBController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	void disconnect() throws SQLException {
		if (currentConnection != null) {
			currentConnection.close();
		}
		currentConnection = null;
	}	
	
	/* These below are utility methods encapsulating common sql statements
	*/
	public ResultSet query(String query) throws SQLException {
		Statement stmt = currentConnection.createStatement();
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.closeOnCompletion();
			}
		}
		return rs;
	}

	public void update(String command) throws SQLException {
		Statement stmt = currentConnection.createStatement();
		try {
			stmt.executeUpdate(command);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	DatabaseMetaData getMetaData() throws SQLException {
		return currentConnection.getMetaData();
	}
	
	void resetDB() throws SQLException {
		// Set up registered users table
		if (helper.tableExists("App", "RegisteredPlayers", this)) {
			helper.truncate("App.RegisteredPlayers", this);
		} else
		{
			String[] colNames = new String[]{"id_player", "username", "email", "password"};
			String[] colTypes = new String[]{"INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)", 
				"VARCHAR(20) NOT NULL", "VARCHAR(100) NOT NULL", "VARCHAR(20) NOT NULL"};
			helper.createTable("App.RegisteredPlayers", colNames, colTypes, this);
		}
	}

	boolean checkRegisteredPlayer(String email) throws SQLException {
		boolean result = false;
		String selectCol = "email";
		try (ResultSet rs = helper.selectWhereLike("App.RegisteredPlayers", "*", selectCol, "'" + email + "'", this)) {
			result = rs.next();
		}
		System.out.println("Giocatore esistente con email " + email);
		return result;
	}

	void saveRegisteredPlayer(RegisteredPlayer reg) throws SQLException {
		String[] cols = new String[]{"username", "email", "password"};
		String[] vals = new String[]{"'" + reg.getUserName() + "'", 
			"'" + reg.getEmail() + "'", 
			"'" + reg.getPassword()+ "'"};
		helper.insertValues("App.RegisteredPlayers", cols, vals, this);
	}

	RegisteredPlayer loadRegisteredPlayer(String email, String password) throws SQLException {
		RegisteredPlayer rpl = null;
		String[] selCols = new String[]{"email", "password"};
		String[] selVals = new String[]{"'" + email + "'", "'" + password + "'"};
		try (ResultSet rs = helper.selectWhereLike("App.RegisteredPlayers", new String[]{"*"}, selCols, selVals, this)) {
			if (rs.next()) {
				rpl = new RegisteredPlayer(rs.getInt("id_player"), rs.getString("username"), email, password);
			}
		}
		return rpl;
	}




}

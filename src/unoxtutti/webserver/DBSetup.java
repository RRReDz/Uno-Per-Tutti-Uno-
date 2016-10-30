/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.webserver;

import java.sql.SQLException;

/**
 *
 * @author picardi
 */
public class DBSetup {
	public static void main(String[] args) {
		DBController dbc = new DBController();
		try{
		dbc.connect();
		try {
			dbc.resetDB();
		} finally {
			dbc.disconnect();
		}
		} catch(SQLException exc) {
			exc.printStackTrace();
		}
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.webserver;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author picardi
 */
class DBHelper {

	private DBController dbController;
		
	void truncate(String tablename, DBController dbController) throws SQLException {
		dbController.update("TRUNCATE TABLE " + tablename);
	}

	void deleteWhereEquals(String tableName, String[] whereCols, String[] whereVals, DBController dbController) throws SQLException {
		deleteWhereOp(tableName, whereCols, whereVals, "=", dbController);
	}

	void deleteWhereEquals(String tableName, String whereCol, String whereVal, DBController dbController) throws SQLException {
		deleteWhereOp(tableName, whereCol, whereVal, "=", dbController);
	}

	void createTableAs(String tablename, String query, DBController dbController) throws SQLException {
		dbController.update("CREATE TABLE " + tablename + " AS (" + query + ")");
	}

	ResultSet countEquals(String table, String column, String value, String countName, DBController dbController) throws SQLException {
		return countOp(table, column, value, countName, "=", dbController);
	}

	ResultSet countOp(String table, String column, String value, String countName, String operator, DBController dbController) throws SQLException {
		return dbController.query("SELECT COUNT(" + column + ") AS " + countName + " FROM " + table + " WHERE " + column + operator + value);
	}

	void deleteTable(String table, DBController dbController) throws SQLException {
		String[] nameParts = table.split(".");
		String tablename = nameParts[nameParts.length-1];
		String tableschema = null;
		if (nameParts.length > 1) tableschema = nameParts[nameParts.length-2];
		if (this.tableExists(tableschema, tablename, dbController)) {
			dbController.update("DROP TABLE " + table);
		}
	}

	void deleteWhereOp(String tableName, String[] whereCols, String[] whereVals, String op, DBController dbController) throws SQLException {
		if (whereCols.length != whereVals.length) {
			throw new IllegalArgumentException();
		}
		if (whereCols.length == 0) {
			return;
		}
		StringBuilder wherebuf = new StringBuilder(whereCols[0] + op + whereVals[0]);
		for (int i = 1; i < whereCols.length; i++) {
			wherebuf.append(" AND ").append(whereCols[i]).append(op).append(whereVals[i]);
		}
		dbController.update("DELETE FROM " + tableName + " WHERE " + wherebuf.toString());
	}

	void deleteWhereOp(String tableName, String whereCol, String whereVal, String op, DBController dbController) throws SQLException {
		dbController.update("DELETE FROM " + tableName + " WHERE " + whereCol + "=" + whereVal);
	}

	void createTable(String tableName, String[] schemaColNames, String[] schemaTypes, DBController dbController) throws SQLException {
		if (schemaColNames.length <= 0) {
			throw new IllegalArgumentException();
		}
		if (schemaColNames.length != schemaTypes.length) {
			throw new IllegalArgumentException();
		}
		StringBuilder schemabuf = new StringBuilder(schemaColNames[0] + " " + schemaTypes[0]);
		for (int i = 1; i < schemaColNames.length; i++) {
			schemabuf.append(',').append(schemaColNames[i]).append(' ').append(schemaTypes[i]);
		}
		dbController.update("CREATE TABLE " + tableName + " (" + schemabuf.toString() + ")");
	}

	void insertValues(String tableName, String[] colNames, String[] values, DBController dbController) throws SQLException {
		if (colNames.length != values.length) {
			throw new IllegalArgumentException();
		}
		String cols = "";
		String vals = "";
		if (colNames.length > 0) {
			StringBuilder colbuf = new StringBuilder(colNames[0]);
			for (int i = 1; i < colNames.length; i++) {
				colbuf.append(",").append(colNames[i]);
			}
			cols = colbuf.toString();
		}
		if (values.length > 0) {
			StringBuilder valbuf = new StringBuilder(values[0]);
			for (int i = 1; i < values.length; i++) {
				valbuf.append(',').append(values[i]);
			}
			vals = valbuf.toString();
		}
		dbController.update("INSERT INTO " + tableName + " (" + cols + ") VALUES (" + vals + ")");
	}

	void insertValues(String tableName, String colName, String value, DBController dbController) throws SQLException {
		String updString = "INSERT INTO " + tableName + " (" + colName + ") VALUES (" + value + ")";
		dbController.update(updString);
	}

	ResultSet selectJoinOnEqualsWhereEquals(String firstTable, String secondTable, String[] colName, 
		String[] onColsLeft, String[] onColsRight, String[] whereColsLeft, 
		String[] whereColsRight, DBController dbController) throws SQLException {
		return this.selectJoinOnOpWhereOp(firstTable, secondTable, colName, onColsLeft, 
			onColsRight, "=", whereColsLeft, whereColsRight, "=", dbController);
	}

	ResultSet selectJoinOnEqualsWhereEquals(String firstTable, String secondTable, 
		String colName, String[] onColsLeft, String[] onColsRight, String[] whereColsLeft, 
		String[] whereColsRight, DBController dbController) throws SQLException {
		return this.selectJoinOnOpWhereOp(firstTable, secondTable, new String[]{colName}, 
			onColsLeft, onColsRight, "=", whereColsLeft, whereColsRight, "=", dbController);
	}

	void setValueWhereOp(String tablename, String colToSet, String value, String whereCol, String whereVal, String operator, DBController dbController) throws SQLException {
		dbController.update("UPDATE " + tablename + " SET " + colToSet + "=" + value + " WHERE " + whereCol + operator + whereVal);
	}

	ResultSet selectJoinOnEquals(String firstTable, String secondTable, 
		String[] colName, String[] onColsLeft, 
		String[] onColsRight, DBController dbController) throws SQLException {
		return selectJoinOnOp(firstTable, secondTable, colName, onColsLeft, onColsRight, "=", dbController);
	}

	ResultSet selectColumn(String tableName, String colName, DBController dbController) throws SQLException {
		return dbController.query("SELECT " + colName + " FROM " + tableName);
	}

	void setValueWhereLike(String tablename, String colToSet, String value, 
		String whereCol, String whereVal, DBController dbController) throws SQLException {
		setValueWhereOp(tablename, colToSet, value, whereCol, whereVal, " LIKE ", dbController);
	}

	ResultSet selectColumnOrderBy(String tableName, String colName, String orderBy, DBController dbController) throws SQLException {
		return dbController.query("SELECT " + colName + " FROM " + tableName + " ORDER BY " + orderBy);
	}

	ResultSet selectWhereLike(String tableName, String colName, 
		String whereCols, String whereValues, DBController dbController) throws SQLException {
		return selectWhereOp(tableName, colName, whereCols, whereValues, " LIKE ", dbController);
	}

	ResultSet selectWhereLike(String tableName, String[] colName, 
		String[] whereCols, String[] whereValues, DBController dbController) throws SQLException {
		return selectWhereOp(tableName, colName, whereCols, whereValues, " LIKE ", dbController);
	}

	void setValueWhereEquals(String tablename, String colToSet, String value, 
		String whereCol, String whereVal, DBController dbController) throws SQLException {
		setValueWhereOp(tablename, colToSet, value, whereCol, whereVal, "=", dbController);
	}

	ResultSet selectWhereOp(String tableName, String[] colName, String[] whereCols, 
		String[] whereValues, String operator, DBController dbController) throws SQLException {
		if (colName.length <= 0) {
			throw new IllegalArgumentException();
		}
		if (whereCols.length != whereValues.length) {
			throw new IllegalArgumentException();
		}
		if (whereCols.length == 0) {
			return this.selectColumns(tableName, colName, dbController);
		}
		StringBuilder colbuf = new StringBuilder(colName[0]);
		for (int i = 1; i < colName.length; i++) {
			colbuf.append(",").append(colName[i]);
		}
		StringBuilder wherebuf = new StringBuilder(whereCols[0] + operator + whereValues[0]);
		for (int i = 1; i < whereCols.length; i++) {
			wherebuf.append(" AND ").append(whereCols[i]).append(operator).append(whereValues[i]);
		}
		return dbController.query("SELECT " + colbuf.toString() + " FROM " + tableName + " WHERE " + wherebuf.toString());
	}

	ResultSet selectWhereEquals(String tableName, String[] colName, 
		String[] whereCols, String[] whereValues, DBController dbController) throws SQLException {
		return selectWhereOp(tableName, colName, whereCols, whereValues, "=", dbController);
	}

	ResultSet selectWhereOp(String tableName, String colName, String whereCols, String whereValues, String operator, DBController dbController) throws SQLException {
		return dbController.query("SELECT " + colName + " FROM " + tableName + " WHERE " + whereCols + operator + whereValues);
	}

	ResultSet selectWhereEquals(String tableName, String colName, 
		String whereCols, String whereValues, DBController dbController) throws SQLException {
		return selectWhereOp(tableName, colName, whereCols, whereValues, "=", dbController);
	}

	ResultSet selectJoinOnOp(String firstTable, String secondTable, String[] colName, String[] onColsLeft, String[] onColsRight, String operator, DBController dbController) throws SQLException {
		if (colName.length <= 0) {
			throw new IllegalArgumentException();
		}
		if (onColsLeft.length != onColsRight.length) {
			throw new IllegalArgumentException();
		}
		StringBuilder colbuf = new StringBuilder(colName[0]);
		for (int i = 1; i < colName.length; i++) {
			colbuf.append(",").append(colName[i]);
		}
		StringBuilder wherebuf = new StringBuilder("");
		if (onColsLeft.length != 0) {
			wherebuf.append(onColsLeft[0]).append(operator).append(onColsRight[0]);
		}
		for (int i = 1; i < onColsLeft.length; i++) {
			wherebuf.append(" AND ").append(onColsLeft[i]).append(operator).append(onColsRight[i]);
		}
		String q = "SELECT " + colbuf.toString() + " FROM " + firstTable + " JOIN " + secondTable + " ON " + wherebuf.toString();
		return dbController.query(q);
	}

	ResultSet selectWhereEqualsOrderBy(String tableName, String colName, String whereCols, String whereValues, 
		String ordercol, DBController dbController) throws SQLException {
		return dbController.query("SELECT " + colName + " FROM " + tableName + " WHERE " + 
			whereCols + "=" + whereValues + " ORDER BY " + ordercol);
	}

	ResultSet selectWhereEqualsOrderBy(String tableName, String[] colName, String[] whereCols, 
		String[] whereValues, String ordercol, DBController dbController) throws SQLException {
		if (colName.length <= 0) {
			throw new IllegalArgumentException();
		}
		if (whereCols.length != whereValues.length) {
			throw new IllegalArgumentException();
		}
		if (whereCols.length == 0) {
			return this.selectColumns(tableName, colName, dbController);
		}
		StringBuilder colbuf = new StringBuilder(colName[0]);
		for (int i = 1; i < colName.length; i++) {
			colbuf.append(",").append(colName[i]);
		}
		StringBuilder wherebuf = new StringBuilder(whereCols[0] + "=" + whereValues[0]);
		for (int i = 1; i < whereCols.length; i++) {
			wherebuf.append(',').append(whereCols[i]).append('=').append(whereValues[i]);
		}
		return dbController.query("SELECT " + colbuf.toString() + " FROM " + tableName + " WHERE " + wherebuf.toString() + " ORDER BY " + ordercol);
	}

	ResultSet selectColumns(String tableName, String[] colName, DBController dbController) throws SQLException {
		if (colName.length <= 0) {
			throw new IllegalArgumentException();
		}
		StringBuilder buf = new StringBuilder(colName[0]);
		for (int i = 1; i < colName.length; i++) {
			buf.append(',').append(colName[i]);
		}
		return dbController.query("SELECT " + buf.toString() + " FROM " + tableName);
	}

	ResultSet selectJoinOnOpWhereOp(String firstTable, String secondTable, String[] colName, String[] onColsLeft, String[] onColsRight, String onOp, String[] whereColsLeft, String[] whereColsRight, String whereOp, DBController dbController) throws SQLException {
		if (colName.length <= 0) {
			throw new IllegalArgumentException();
		}
		if (onColsLeft.length != onColsRight.length) {
			throw new IllegalArgumentException();
		}
		if (whereColsLeft.length != whereColsRight.length) {
			throw new IllegalArgumentException();
		}
		StringBuilder colbuf = new StringBuilder(colName[0]);
		for (int i = 1; i < colName.length; i++) {
			colbuf.append(",").append(colName[i]);
		}
		StringBuilder onbuf = new StringBuilder("");
		if (onColsLeft.length != 0) {
			onbuf.append(onColsLeft[0]).append(onOp).append(onColsRight[0]);
		}
		for (int i = 1; i < onColsLeft.length; i++) {
			onbuf.append(" AND ").append(onColsLeft[i]).append(onOp).append(onColsRight[i]);
		}
		StringBuilder wherebuf = new StringBuilder("");
		if (whereColsLeft.length != 0) {
			wherebuf.append(whereColsLeft[0]).append(whereOp).append(whereColsRight[0]);
		}
		for (int i = 1; i < whereColsLeft.length; i++) {
			wherebuf.append(" AND ").append(whereColsLeft[i]).append(whereOp).append(whereColsRight[i]);
		}
		String q = "SELECT " + colbuf.toString() + " FROM " + firstTable + " JOIN " + secondTable + " ON " + onbuf.toString() + " WHERE " + wherebuf.toString();
		System.out.println(q);
		return dbController.query(q);
	}
	
	public boolean tableExists(String schemaName, String tableName, DBController dbController) throws SQLException	{
		boolean exists;
		try (ResultSet rs = dbController.getMetaData().getTables(null, schemaName.toUpperCase(), tableName.toUpperCase(), null)) {
			exists = rs.next();
		}
		return exists;
	}
		
}

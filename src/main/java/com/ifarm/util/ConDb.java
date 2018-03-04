package com.ifarm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConDb {
	private String url = "jdbc:mysql://localhost:3308/ifarm";
	private String userName = "root";
	private String password = "yls1911";

	public Connection openCon() {
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, userName, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}
}

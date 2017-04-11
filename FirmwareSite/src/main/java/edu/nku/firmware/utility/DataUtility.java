package edu.nku.firmware.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DataUtility {
	/*
	 * A singleton class to centralize database handle access.
	 * */
	
	private static DataUtility instance = null;
	private static ServiceLogger logger;
	
	private Connection conn = null;
	private String dbName = "FirmwareSite.db";
	
	private byte[] cachedPrivateKeyBytes;
	private byte[] cachedPublicKeyBytes;
	
	private DataUtility() {
		logger = ServiceLogger.getInstance();
		this.getConnection();
	}
	
	public static DataUtility getInstance() {
		if (instance == null) {
			instance = new DataUtility();
		}
		return instance;
	}
	
	private void getConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			logger.writeLog("DataUtility.getConnection() - org.sqlite.JDBC not found");
			e.printStackTrace();
		}
		
		try {
			this.conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbName);
		} catch (SQLException e) {
			logger.writeLog("DataUtility.getConnection() - Could not get connection.");
			e.printStackTrace();
		}
	}
	
	private void closeConnection() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			logger.writeLog("DataUtility.closeConnection() - Could not close connection.");
			e.printStackTrace();
		}
	}
	
	public void writeDatabaseEntry(String data) {
		String log = "writeDatabaseEntry:" + data;
		logger.writeLog(log);
		// TODO: Fill in the rest of this class.
	}
	
	public void storeKeyPair(byte[] publicString, byte[] privateString) {		
		String query = "INSERT INTO tblKeys (publicKey, privateKey) VALUES (?, ?)";
		PreparedStatement state;		
		try {
			state = this.conn.prepareStatement(query);
			state.setBytes(1, publicString);
			state.setBytes(2, privateString);
			state.executeUpdate();
		} catch (SQLException e) {
			logger.writeLog("DataUtility.storeKeyPair() - Could not create prepared statement.");
			e.printStackTrace();
		}
	}
	
	public byte[] retrievePublicKey() {
		logger.writeLog("DataUtility.retrievePublicKey() - Beginning retrieval");
		if(cachedPrivateKeyBytes == null || cachedPublicKeyBytes == null) {		
			String query = "SELECT publicKey FROM tblKeys";
			PreparedStatement state;
			ResultSet result;		
			try {
				state = this.conn.prepareStatement(query);
				result = state.executeQuery();
				byte[] publicBytes = result.getBytes("publicKey");
				cachedPublicKeyBytes = publicBytes;
				logger.writeLog("Data - pub:" + publicBytes);
				return publicBytes;
			} catch (SQLException e) {
				logger.writeLog("DataUtility.retrievePublicKey() - Could not create prepared statement.");
				e.printStackTrace();
			}		
		} else {
			logger.writeLog("Data - already had cached keys");
			return cachedPublicKeyBytes;
		}
		return null;
	}
	
	public byte[] retrievePrivateKey() {
		logger.writeLog("DataUtility.retrievePrivateKey() - Beginning retrieval");
		if(cachedPrivateKeyBytes == null || cachedPublicKeyBytes == null) {		
			String query = "SELECT privateKey FROM tblKeys";
			PreparedStatement state;
			ResultSet result;		
			try {
				state = this.conn.prepareStatement(query);
				result = state.executeQuery();
				byte[] privateBytes = result.getBytes("privateKey");
				cachedPrivateKeyBytes = privateBytes;
				logger.writeLog("Data - pub:" + privateBytes);
				return privateBytes;
			} catch (SQLException e) {
				logger.writeLog("DataUtility.retrievePrivateKey() - Could not create prepared statement.");
				e.printStackTrace();
			}		
		} else {
			logger.writeLog("Data - already had cached keys");
			return cachedPrivateKeyBytes;
		}
		return null;
	}
}

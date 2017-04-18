package edu.nku.firmware.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DataUtility {
	/*
	 * A singleton class to centralize database handle access.
	 */

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
			this.conn = DriverManager.getConnection("jdbc:sqlite::resource:" + getClass().getResource("/" + dbName));
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

	public int getFirmwareVersion(String vendorId, String modelId) {
		getConnection();
		String query = "SELECT firmwareVersion FROM tblFirmware WHERE vendorId = ? AND modelId = ?";
		PreparedStatement state;
		ResultSet result;
		try {
			state = this.conn.prepareStatement(query);
			state.setInt(1, Integer.parseInt(vendorId));
			state.setInt(2, Integer.parseInt(modelId));
			result = state.executeQuery();
			int firmwareVersion = result.getInt("firmwareVersion");
			logger.writeLog("Data - firmwareVersion:" + firmwareVersion + ", Model:" + modelId);
			closeConnection();
			return firmwareVersion;
		} catch (Exception e) {
			logger.writeLog("DataUtility.getFirmwareVersion() - Could not create prepared statement.");
			e.printStackTrace();
		}
		closeConnection();
		return 0;
	}

	public void updateFirmwareVersions(int vendorId) {
		getConnection();
		Random rand = new Random();
		for (int modelId = 0; modelId < 10; modelId++) {
			int firmwareVersion = rand.nextInt(100);

			String query = "UPDATE tblFirmware SET firmwareVersion = ? WHERE vendorId = ? AND modelId = ?;";
			PreparedStatement state;
			try {
				state = this.conn.prepareStatement(query);
				state.setInt(1, firmwareVersion);
				state.setInt(2, vendorId);
				state.setInt(3, modelId);

				int rows = state.executeUpdate();

				if (rows < 1) {
					query = "INSERT INTO tblFirmware (vendorId, modelId, firmwareVersion) SELECT ?, ?, ? WHERE (Select Changes() = 0);";
					state = this.conn.prepareStatement(query);
					state.setInt(1, vendorId);
					state.setInt(2, modelId);
					state.setInt(3, firmwareVersion);
					state.executeUpdate();
				}
			} catch (SQLException e) {
				logger.writeLog("DataUtility.updateFirmwareVersions() - Could not create prepared statement.");
				e.printStackTrace();
			}
		}
		closeConnection();
	}

	public void tickUpFirmwareVersion(String pVendorId, String pModelId, int firmwareVersion) {
		getConnection();
		String query = "UPDATE tblFirmware SET firmwareVersion = ? WHERE vendorId = ? AND modelId = ?";
		PreparedStatement state;
		try {
			state = this.conn.prepareStatement(query);
			state.setInt(1, firmwareVersion);
			state.setInt(2, Integer.parseInt(pVendorId));
			state.setInt(3, Integer.parseInt(pModelId));
			state.executeUpdate();
		} catch (SQLException e) {
			logger.writeLog("DataUtility.tickUpFirmwareVersion() - Could not create prepared statement.");
			e.printStackTrace();
		}
		closeConnection();
	}

	public void writeDatabaseEntry(String data) {
		String log = "writeDatabaseEntry:" + data;
		logger.writeLog(log);
		// TODO: Fill in the rest of this class.
	}

	public void storeKeyPair(byte[] publicString, byte[] privateString) {
		getConnection();
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
		closeConnection();
	}

	public byte[] retrievePublicKey() {
		logger.writeLog("DataUtility.retrievePublicKey() - Beginning retrieval");
		getConnection();
		if (cachedPrivateKeyBytes == null || cachedPublicKeyBytes == null) {
			String query = "SELECT publicKey FROM tblKeys";
			PreparedStatement state;
			ResultSet result;
			try {
				state = this.conn.prepareStatement(query);
				result = state.executeQuery();
				byte[] publicBytes = result.getBytes("publicKey");
				cachedPublicKeyBytes = publicBytes;
				logger.writeLog("Data - pub:" + publicBytes);
				closeConnection();
				return publicBytes;
			} catch (SQLException e) {
				logger.writeLog("DataUtility.retrievePublicKey() - Could not create prepared statement.");
				e.printStackTrace();
			}
		} else {
			logger.writeLog("Data - already had cached keys");
			closeConnection();
			return cachedPublicKeyBytes;
		}
		closeConnection();
		return null;
	}

	public byte[] retrievePrivateKey() {
		logger.writeLog("DataUtility.retrievePrivateKey() - Beginning retrieval");
		getConnection();
		if (cachedPrivateKeyBytes == null || cachedPublicKeyBytes == null) {
			String query = "SELECT privateKey FROM tblKeys";
			PreparedStatement state;
			ResultSet result;
			try {
				state = this.conn.prepareStatement(query);
				result = state.executeQuery();
				byte[] privateBytes = result.getBytes("privateKey");
				cachedPrivateKeyBytes = privateBytes;
				logger.writeLog("Data - pub:" + privateBytes);
				closeConnection();
				return privateBytes;
			} catch (SQLException e) {
				logger.writeLog("DataUtility.retrievePrivateKey() - Could not create prepared statement.");
				e.printStackTrace();
			}
		} else {
			logger.writeLog("Data - already had cached keys");
			closeConnection();
			return cachedPrivateKeyBytes;
		}
		closeConnection();
		return null;
	}
}

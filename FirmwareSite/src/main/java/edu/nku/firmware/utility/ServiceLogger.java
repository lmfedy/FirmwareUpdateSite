package edu.nku.firmware.utility;

import java.sql.Timestamp;

public class ServiceLogger {
	/*
	 * A singleton class to centralize disk access to log files.
	 * */

	private static ServiceLogger instance = null;
	
	private ServiceLogger() {}
	
	public static ServiceLogger getInstance() {
		if (instance == null) {
			instance = new ServiceLogger();
		}
		return instance;
	}
	
	public void writeLog(String x) {
		Timestamp time = new Timestamp(System.currentTimeMillis());
		// TODO: Write log files to file system rather than to the console.
		System.out.println("[" + time + "]" + ":" +x);
	}
}
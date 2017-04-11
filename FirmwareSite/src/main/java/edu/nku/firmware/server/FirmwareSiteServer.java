package edu.nku.firmware.server;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import edu.nku.firmware.resource.FirmwareUpdate;
import edu.nku.firmware.utility.CryptoUtility;
import edu.nku.firmware.utility.DataUtility;
import edu.nku.firmware.utility.ServiceLogger;

public class FirmwareSiteServer {
	private static final int DEFAULT_PORT = 8080;
	private int serverPort;

	public FirmwareSiteServer(int serverPort) throws Exception {
		this.serverPort = serverPort;

		Server server = configureServer();
		server.start();
		server.join();
	}

	private Server configureServer() throws NoSuchAlgorithmException, NoSuchProviderException {
		
		ServiceLogger logger = ServiceLogger.getInstance();
		DataUtility data = DataUtility.getInstance();
		CryptoUtility crypto = new CryptoUtility(data, logger);		
		
		Map<String, Object> oPropertyMap = new HashMap<>();
		oPropertyMap.put("CryptoUtility", crypto);
		oPropertyMap.put("firmwareID", serverPort);
		
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(FirmwareUpdate.class.getPackage().getName());
		resourceConfig.register(JacksonFeature.class);
		resourceConfig.setProperties(oPropertyMap);		
		
		// TODO: Randomize models/version to put into SQL Table
		
		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder sh = new ServletHolder(servletContainer);
		Server server = new Server(serverPort);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(sh, "/*");
		server.setHandler(context);
		return server;
	}
	

	public static void main(String[] args) throws Exception {
		int serverPort = DEFAULT_PORT;
		if (args.length >= 1) {
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		new FirmwareSiteServer(serverPort);
	}

}

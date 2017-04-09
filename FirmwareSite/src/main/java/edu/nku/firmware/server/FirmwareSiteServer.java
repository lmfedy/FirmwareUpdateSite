package edu.nku.firmware.server;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
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
import edu.nku.firmware.utility.ServiceLogger;

public class FirmwareSiteServer {
	private static final int DEFAULT_PORT = 8080;
	private ServiceLogger logger;
	private int serverPort;

	public FirmwareSiteServer(int serverPort, ServiceLogger logger) throws Exception {
		this.serverPort = serverPort;

		Server server = configureServer(logger);
		server.start();
		server.join();
	}

	private Server configureServer(ServiceLogger logger) throws NoSuchAlgorithmException, NoSuchProviderException {
		
		Map<String, Object> oPropertyMap = generateKeys(logger);
		oPropertyMap.put("firmwareID", serverPort);
		
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(FirmwareUpdate.class.getPackage().getName());
		resourceConfig.register(JacksonFeature.class);
		resourceConfig.setProperties(oPropertyMap);		
		
		//TODO: INSERT Vendor, Port, URL & Public Key into SQL Table
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
	
	// Generate key pair
	private Map<String, Object> generateKeys(ServiceLogger logger) throws NoSuchAlgorithmException, NoSuchProviderException{
//		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
//		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
//		keyGen.initialize(1024, random);
//		KeyPair pair = keyGen.generateKeyPair();
//		PrivateKey sPrivateKey = pair.getPrivate();
//		PublicKey sPublicKey = pair.getPublic();
		
		CryptoUtility crypto = new CryptoUtility(logger);		

		Map<String, Object> oPropertyMap = new HashMap<>();
		oPropertyMap.put("CryptoUtility", crypto);
		
		return oPropertyMap;
	}

	public static void main(String[] args) throws Exception {
		int serverPort = DEFAULT_PORT;
		ServiceLogger logger =  ServiceLogger.getInstance();
		if (args.length >= 1) {
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		new FirmwareSiteServer(serverPort, logger);
	}

}

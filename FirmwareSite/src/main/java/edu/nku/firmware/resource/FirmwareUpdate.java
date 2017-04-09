package edu.nku.firmware.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import edu.nku.firmware.utility.CryptoUtility;

@Path("/firmware")
public class FirmwareUpdate {

	private String UPDATE_FILE_PATH = "C:\\Users\\Admin\\git\\FirmwareUpdateSite\\FirmwareSite\\updateFiles";
	
	@Context
	private Application appContext;

	@GET
	@Path("/update/{model}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getLatestVersion(@PathParam("model") String pModel) {
		Result oResult = new Result("version");
		oResult.setFirmware(appContext.getProperties().get("firmwareID").toString());
		oResult.setModel(pModel);
		// TODO: Get version from database
		oResult.setVersion("52");
		return oResult;
	}

	@GET
	@Path("/update/package/{model}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getUpdate(@PathParam("model") String pModel) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		Result oResult = new Result("update");
		oResult.setModel(pModel);
		oResult.setVersion("52");
		oResult.setFirmware(appContext.getProperties().get("firmwareID").toString());
		List<String> lines = Arrays.asList("Firwmare Update", "Firmware ID: " + oResult.getFirmware(),"Model ID: " + pModel, "Version: " + oResult.getVersion());
		Files.write(Paths.get(UPDATE_FILE_PATH + "\\Update_" + oResult.getVersion() + ".txt"), lines);
		
		CryptoUtility crypto = (CryptoUtility) appContext.getProperties().get("CryptoUtility"); 
		oResult.setFile(crypto.signFile(UPDATE_FILE_PATH + "\\Update_" + oResult.getVersion() + ".txt"));
		
		// TODO: Randomly decide whether or not a new update will be available next time
		//  If yes, update database with the new version.
		
		return oResult;
	}

	@GET
	@Path("/publickey")
	@Produces(MediaType.APPLICATION_JSON)
	public KeyResult getVendorPublicKey() {
		KeyResult oResult = new KeyResult("publickey");
		oResult.setPublickey((PublicKey) appContext.getProperties().get("publickey"));
		return oResult;
	}
	
	// Convert file to Base64 string and sign with private key
//    private String signFile(String pFile) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException{
//    	CryptoUtility crypto = (CryptoUtility) appContext.getProperties().get("CryptoUtility"); 
//    	String encryptedFile = crypto.encryptMessage(pFile);
//    	
//    	PrivateKey sPrivateKey = (PrivateKey) appContext.getProperties().get("privatekey");
//    	 
//    	 Signature sSignature = Signature.getInstance("SHA1withDSA", "SUN");
//    	 sSignature.initSign(sPrivateKey);
//    	 
//    	 InputStream oInputStream = Files.newInputStream(Paths.get(pFile));
//    	 BufferedInputStream oBuffInput = new BufferedInputStream(oInputStream);
//    	 byte[] buffer = new byte[1024];
//    	 int len;
//    	 while ((len = oBuffInput.read(buffer)) >= 0) {
//    		 sSignature.update(buffer, 0, len);
//    	 };
//    	 oBuffInput.close();    	 
//    	 
//         return new String(sSignature.sign());
//    }
}

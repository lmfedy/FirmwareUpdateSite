package edu.nku.firmware.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/firmware")
public class FirmwareUpdate {

	@Context
	private Application appContext;

	@GET
	@Path("/version/{model}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getLatestVersion(@PathParam("model") String pModel) {
		Result oResult = new Result("version");
		oResult.setModel(pModel);
		oResult.setVersion("5.1.2");
		return oResult;
	}

	@GET
	@Path("/update/{model}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getUpdate(@PathParam("model") String pModel) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		Result oResult = new Result("update");
		oResult.setModel(pModel);
		oResult.setVersion("5.1.2");
		oResult.setFile(convertFile(new File("C:\\Users\\l.lykowski\\workspace\\firmwareUpdate.txt")));
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
    private String convertFile(File file) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException{
    	 PrivateKey sPrivateKey = (PrivateKey) appContext.getProperties().get("privatekey");
    	 
    	 Signature sSignature = Signature.getInstance("SHA1withDSA", "SUN");
    	 sSignature.initSign(sPrivateKey);
    	 
    	 FileInputStream oInputStream = new FileInputStream(file);
    	 BufferedInputStream oBuffInput = new BufferedInputStream(oInputStream);
    	 byte[] buffer = new byte[1024];
    	 int len;
    	 while ((len = oBuffInput.read(buffer)) >= 0) {
    		 sSignature.update(buffer, 0, len);
    	 };
    	 oBuffInput.close();    	 
    	 
         return new String(sSignature.sign());
    }
}

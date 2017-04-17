package edu.nku.firmware.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import com.google.gson.Gson;

import edu.nku.firmware.utility.CryptoUtility;

@Path("/firmware")
public class FirmwareUpdate {

	private String UPDATE_FILE_PATH = "C:\\Users\\llyko_000\\git\\FirmwareUpdateSite\\FirmwareSite\\updateFiles";

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
	public String getUpdate(@PathParam("model") String pModel) throws IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		Result oResult = new Result("update");
		oResult.setModel(pModel);
		oResult.setVersion("52");
		oResult.setFirmware(appContext.getProperties().get("firmwareID").toString());
		List<String> lines = Arrays.asList("Firwmare Update", "Firmware ID: " + oResult.getFirmware(),
				"Model ID: " + pModel, "Version: " + oResult.getVersion());
		Files.write(Paths.get("Update_" + oResult.getVersion() + ".txt"), lines);
		
		ByteArrayOutputStream ba = loadFileAsStream("Update_" + oResult.getVersion() + ".txt");
		oResult.setFile(StringUtils.newStringUtf8(Base64.encodeBase64(ba.toByteArray())));

		// TODO: Randomly decide whether or not a new update will be available
		// next time
		// If yes, update database with the new version.

		Gson gson = new Gson();
		String sResponse = gson.toJson(oResult, Result.class);
		
		CryptoUtility crypto = new CryptoUtility();
		sResponse = crypto.encryptMessage(sResponse);

		return sResponse;
	}

	private ByteArrayOutputStream loadFileAsStream(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buf = new byte[1024];
		try {
			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				bos.write(buf, 0, readNum); // no doubt here is 0
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bos;
	}

	@GET
	@Path("/publickey")
	@Produces(MediaType.APPLICATION_JSON)
	public KeyResult getVendorPublicKey() {
		KeyResult oResult = new KeyResult("publickey");
		CryptoUtility crypto = new CryptoUtility();
		oResult.setPublickey(crypto.getPublicKey());
		return oResult;
	}

}

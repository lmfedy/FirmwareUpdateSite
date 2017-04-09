package edu.nku.firmware.utility;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoUtility {
	/*
	 * Java Utility Class to handle encryption/decryption of messages.
	 */
	// private DataUtility data;
	private ServiceLogger logger;
	private PublicKey sPublicKey;
	private PrivateKey sPrivateKey;
	private Cipher cipher;
	private String defaultAlgorithm = "DSA";

	public CryptoUtility() {
		// this.data = DataUtility.getInstance();
		this.logger = ServiceLogger.getInstance();
		try {
			this.cipher = Cipher.getInstance(defaultAlgorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			logger.writeLog("CryptoUtility.init() - Could not generate cipher.");
			e.printStackTrace();
		}
		setUpKeys();
	}

	public CryptoUtility(ServiceLogger logger) {
		// this.data = data;
		this.logger = logger;
		try {
			this.cipher = Cipher.getInstance(defaultAlgorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			logger.writeLog("CryptoUtility.init(ServiceLogger) - Could not generate cipher.");
			e.printStackTrace();
		}
		setUpKeys();
	}

	private void setUpKeys() {
		logger.writeLog("Crypto - Beginning setup key");
		if (sPublicKey != null && sPrivateKey != null) {
			return;
		} else {
			logger.writeLog("Crypto - Retrieved keystrings were null");
			generateKeys();
		}
	}

	private void generateKeys() {
		logger.writeLog("Crypto - Beginning key generation.");
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(defaultAlgorithm);
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);
			KeyPair pair = keyGen.generateKeyPair();
			this.sPrivateKey = pair.getPrivate();
			this.sPublicKey = pair.getPublic();
			sPublicKey.getEncoded();
			sPrivateKey.getEncoded();

		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			logger.writeLog("CryptoUtility.generateKeys() - Key Generation Failed.");
			e.printStackTrace();
		}
	}

	public PublicKey getPublicKey() {
		return this.sPublicKey;
	}

	public PrivateKey getPrivateKey() {
		return this.sPrivateKey;
	}

	public String encryptMessage(String message) {
		try {
			System.out.println(this.cipher);
			this.cipher.init(Cipher.ENCRYPT_MODE, this.sPrivateKey);
			try {
				logger.writeLog("Crypto - beginning encryption.");
				return Base64.encodeBase64String(cipher.doFinal(message.getBytes("UTF-8")));
			} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
				logger.writeLog("CryptoUtility.encryptMessage() - Failed to encode string.");
				e.printStackTrace();
			}
		} catch (InvalidKeyException e) {
			logger.writeLog("CryptoUtility.encryptMessage() - Failed to init cipher.");
			e.printStackTrace();
		}
		return null;
	}

	public String decryptMessage(String message, PublicKey otherPublicKey) {
		try {
			this.cipher.init(Cipher.DECRYPT_MODE, otherPublicKey);
			try {
				return new String(cipher.doFinal(Base64.decodeBase64(message)), "UTF-8");
			} catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
				logger.writeLog("CryptoUtility.decryptMessage() - Failed to decode string.");
				e.printStackTrace();
			}
		} catch (InvalidKeyException e) {
			logger.writeLog("CryptoUtility.decryptMessage() - Failed to init cipher");
			e.printStackTrace();
		}
		return null;
	}

	public String signFile(String pFile) throws IOException, NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeyException, SignatureException {
		Signature sSignature = Signature.getInstance("SHA1withDSA", "SUN");
		sSignature.initSign(sPrivateKey);

		InputStream oInputStream = Files.newInputStream(Paths.get(pFile));
		BufferedInputStream oBuffInput = new BufferedInputStream(oInputStream);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = oBuffInput.read(buffer)) >= 0) {
			sSignature.update(buffer, 0, len);
		}
		;
		oBuffInput.close();
		return new String(sSignature.sign());
	}

}

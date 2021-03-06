package edu.nku.firmware.utility;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoUtility {
	/*
	 * Java Utility Class to handle encryption/decryption of messages.
	 */
	// private DataUtility data;
	private DataUtility data;
	private ServiceLogger logger;
	private PublicKey sPublicKey;
	private PrivateKey sPrivateKey;
	private Cipher cipher;
	private String defaultAlgorithm = "RSA";

	public CryptoUtility() {
		this.data = DataUtility.getInstance();
		this.logger = ServiceLogger.getInstance();
		try {
			this.cipher = Cipher.getInstance(defaultAlgorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.writeLog("CryptoUtility.init() - Could not generate cipher.");
			e.printStackTrace();
		}
		setUpKeys();
	}

	public CryptoUtility(DataUtility data, ServiceLogger logger) {
		this.data = data;
		this.logger = logger;
		try {
			this.cipher = Cipher.getInstance(defaultAlgorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.writeLog("CryptoUtility.init(DataUtility, ServiceLogger) - Could not generate cipher.");
			e.printStackTrace();
		}
		setUpKeys();
	}

	private void setUpKeys() {
		logger.writeLog("Crypto - Beginning setup key");
		if (sPublicKey != null && sPrivateKey != null) {
			return;
		} else {
			byte[] publicBytes = data.retrievePublicKey();
			byte[] privateBytes = data.retrievePrivateKey();
			if (publicBytes != null && privateBytes != null) {
				PublicKey pubKey = inflatePublicKey(publicBytes);
				PrivateKey privKey = inflatePrivateKey(privateBytes);
				if (pubKey != null && privKey != null) {
					sPublicKey = pubKey;
					sPrivateKey = privKey;
				} else {
					logger.writeLog("Crypto - Key inflation failed.");
					generateKeys();
				}
			} else {
				logger.writeLog("Crypto - Retrieved keystrings were null");
				generateKeys();
			}
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
			byte[] pub = sPublicKey.getEncoded();
			byte[] pri = sPrivateKey.getEncoded();
			data.storeKeyPair(pub, pri);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			logger.writeLog("CryptoUtility.generateKeys() - Key Generation Failed.");
			e.printStackTrace();
		}
	}

	private PrivateKey inflatePrivateKey(byte[] keyBytes) {
		return inflatePrivateKey(keyBytes, defaultAlgorithm);
	}

	private PrivateKey inflatePrivateKey(byte[] keyBytes, String algorithm) {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		try {
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			try {
				return kf.generatePrivate(spec);
			} catch (InvalidKeySpecException e) {
				logger.writeLog(
						"CryptoUtility.inflatePrivateKey() - KeyFactory could not generate private key from PKCS8EncodedKeySpec.");
				e.printStackTrace();
			}
		} catch (NoSuchAlgorithmException e) {
			logger.writeLog("CryptoUtility.inflatePrivateKey() - Could not get KeyFactory instance.");
			e.printStackTrace();
		}
		return null;
	}

	private PublicKey inflatePublicKey(byte[] keyBytes) {
		return inflatePublicKey(keyBytes, defaultAlgorithm);
	}

	private PublicKey inflatePublicKey(byte[] keyBytes, String algorithm) {
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance(algorithm);
			try {
				return kf.generatePublic(spec);
			} catch (InvalidKeySpecException e) {
				logger.writeLog(
						"CryptoUtility.inflatePublicKey() - KeyFactory could not generate public key from X509EncodedKeySpec.");
				e.printStackTrace();
			}
		} catch (NoSuchAlgorithmException e) {
			logger.writeLog("CryptoUtility.inflatePublicKey() - Could not get KeyFactory instance.");
			e.printStackTrace();
		}
		return null;
	}

	public String getPublicKeyString() {
		try {
			KeyFactory fact = KeyFactory.getInstance(defaultAlgorithm);
			X509EncodedKeySpec spec = fact.getKeySpec(this.sPublicKey, X509EncodedKeySpec.class);
			return Base64.encodeBase64String(spec.getEncoded());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	public PublicKey inflatePublicKeyFromString(String publicKeyString) {
		byte[] keyBytes = Base64.decodeBase64(publicKeyString);
		return inflatePublicKey(keyBytes);
	}

	public PublicKey getPublicKey() {
		return this.sPublicKey;
	}

	public String encryptMessage(String message, PublicKey publicKey) {
		try {
			this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] bytes = message.getBytes("UTF-8");
			byte[] encrypted = blockCipher(bytes,Cipher.ENCRYPT_MODE);
			char[] encryptedTranspherable = Hex.encodeHex(encrypted);
			return new String(encryptedTranspherable);
		} catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
			logger.writeLog("Failed to encrypt message");
			e.printStackTrace();
		}
		return null;
	}
	
	private byte[] blockCipher(byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException{
		// string initialize 2 buffers.
		// scrambled will hold intermediate results
		byte[] scrambled = new byte[0];

		// toReturn will hold the total result
		byte[] toReturn = new byte[0];
		// if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
		int length = (mode == Cipher.ENCRYPT_MODE)? 100 : 128;

		// another buffer. this one will hold the bytes that have to be modified in this step
		byte[] buffer = new byte[length];

		for (int i=0; i< bytes.length; i++){

			// if we filled our buffer array we have our block ready for de- or encryption
			if ((i > 0) && (i % length == 0)){
				//execute the operation
				scrambled = cipher.doFinal(buffer);
				// add the result to our total result.
				toReturn = append(toReturn,scrambled);
				// here we calculate the length of the next buffer required
				int newlength = length;

				// if newlength would be longer than remaining bytes in the bytes array we shorten it.
				if (i + length > bytes.length) {
					 newlength = bytes.length - i;
				}
				// clean the buffer array
				buffer = new byte[newlength];
			}
			// copy byte into our buffer.
			buffer[i%length] = bytes[i];
		}

		// this step is needed if we had a trailing buffer. should only happen when encrypting.
		// example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
		scrambled = cipher.doFinal(buffer);

		// final step before we can return the modified data.
		toReturn = append(toReturn,scrambled);

		return toReturn;
	}
	
	private byte[] append(byte[] prefix, byte[] suffix){
		byte[] toReturn = new byte[prefix.length + suffix.length];
		for (int i=0; i< prefix.length; i++){
			toReturn[i] = prefix[i];
		}
		for (int i=0; i< suffix.length; i++){
			toReturn[i+prefix.length] = suffix[i];
		}
		return toReturn;
	}

	public String encryptMessage(String message) {
		try {
			this.cipher.init(Cipher.ENCRYPT_MODE, this.sPrivateKey);
			byte[] bytes = message.getBytes("UTF-8");
			byte[] encrypted = blockCipher(bytes,Cipher.ENCRYPT_MODE);
			char[] encryptedTranspherable = Hex.encodeHex(encrypted);
			return new String(encryptedTranspherable);
		} catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
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
		Signature sSignature = Signature.getInstance("SHA1withRSA");
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
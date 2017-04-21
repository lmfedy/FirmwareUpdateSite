package edu.nku.firmware.resource;

import java.security.PublicKey;

public class KeyResult {
	transient String  action;
	byte[] publickey;
	
	public KeyResult() {

	}
	
	public KeyResult(String action) {
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public byte[] getPublickey() {
		return publickey;
	}
	public void setPublickey(PublicKey publickey) {
		this.publickey = publickey.getEncoded();
	}
}

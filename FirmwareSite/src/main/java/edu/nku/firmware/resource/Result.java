package edu.nku.firmware.resource;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Result {
	// I have no problem with changing these field names to match more closely
	// with the central service conventions
	
	public String action;
	public String model; // Device Model
	public int version; // Firmware Version
	public String file; // Actual File as encoded string
	public String firmware; // Vendor Id

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Result() {

	}

	public String getFirmware() {
		return firmware;
	}

	public void setFirmware(String firmware) {
		this.firmware = firmware;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int i) {
		this.version = i;
	}

	public Result(String pAction) {
		this.action = pAction;
	}
}

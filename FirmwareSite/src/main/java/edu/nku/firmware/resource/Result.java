package edu.nku.firmware.resource;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Result {
	public String action;
	public String model;
	public String version;
	public String file;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Result() {

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Result(String pAction) {
		this.action = pAction;
	}
}

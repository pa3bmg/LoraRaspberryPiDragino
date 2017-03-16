package nl.pa3bmg.pi4j.lora.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class KeysHolder {
	private String DeviceAddress;
	private String NetworkSessionKey;
	private String AppSessionKey;
	
	public String getDeviceAddress() {
		return DeviceAddress;
	}
	public void setDeviceAddress(String deviceAddress) {
		DeviceAddress = deviceAddress;
	}
	public String getNetworkSessionKey() {
		return NetworkSessionKey;
	}
	public void setNetworkSessionKey(String networkSessionKey) {
		NetworkSessionKey = networkSessionKey;
	}
	public String getAppSessionKey() {
		return AppSessionKey;
	}
	public void setAppSessionKey(String appSessionKey) {
		AppSessionKey = appSessionKey;
	}
	
	public boolean LoadKeysFromFile(String keyFile){
		Properties prop = new Properties();
		InputStream input = null;
		try {
			if (keyFile.endsWith(".keyset")){
				input = new FileInputStream(keyFile);
			} else {
				input = new FileInputStream(keyFile+".keyset");
			}
			prop.load(input);
			DeviceAddress = prop.getProperty("DeviceAddress");
			AppSessionKey = prop.getProperty("AppSessionKey");
			NetworkSessionKey = prop.getProperty("NetworkSessionKey");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (input != null) {
				try {
					input.close();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public boolean SaveKeysToFile(){
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream(DeviceAddress+".keyset");

			prop.setProperty("DeviceAddress", DeviceAddress);
			prop.setProperty("AppSessionKey", AppSessionKey);
			prop.setProperty("NetworkSessionKey", NetworkSessionKey);

			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		KeysHolder kh = new KeysHolder();
		if (args.length == 3){
			kh.setDeviceAddress(args[0]);
			kh.setAppSessionKey(args[1]);
			kh.setNetworkSessionKey(args[2]);
			kh.SaveKeysToFile();
		}
	}
}

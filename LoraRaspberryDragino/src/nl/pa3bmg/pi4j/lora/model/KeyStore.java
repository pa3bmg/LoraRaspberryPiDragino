package nl.pa3bmg.pi4j.lora.model;

import java.io.File;
import java.util.HashMap;

public class KeyStore {
	private HashMap<String, KeysHolder> Store = new HashMap<String, KeysHolder>();
	
	public HashMap<String, KeysHolder> getStore() {
		return Store;
	}

	public void setStore(HashMap<String, KeysHolder> store) {
		Store = store;
	}
	
	public boolean LoadAllKeysFromFile(){
		File f = new File(System.getProperty("user.dir"));
		if (f.isDirectory()){
			for (String S : f.list()){
				if (S.endsWith(".keyset")){
					KeysHolder kh = new KeysHolder();
					if (kh.LoadKeysFromFile(S)){
						Store.put(kh.getDeviceAddress(), kh);
					}
					
				}
			}
		}
		System.out.println(Store.keySet());
		return false;
	}
	
	public static void main(String[] args) {
		KeyStore ks = new KeyStore();
		ks.LoadAllKeysFromFile();
	}
}

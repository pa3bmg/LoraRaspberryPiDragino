package nl.pa3bmg.pi4j.lora.apps.multicast;

import org.bouncycastle.util.encoders.Hex;
import nl.pa3bmg.pi4j.lora.crypto.LoraCrypto;
import nl.pa3bmg.pi4j.lora.model.KeyStore;
import nl.pa3bmg.pi4j.lora.model.KeysHolder;
import nl.pa3bmg.pi4j.lora.model.MHDR_MType;
import nl.pa3bmg.pi4j.lora.model.SnifModel;
import nl.pa3bmg.pi4j.lora.multicast.BusMCCallback;
import nl.pa3bmg.pi4j.lora.multicast.BusMCTask;

public class ViewBusTraffic extends Thread implements  BusMCCallback{
	private BusMCTask MCTask;
	private KeyStore keyStore;
	private LoraCrypto crypto;
	
	public ViewBusTraffic(){
		keyStore = new KeyStore();
		keyStore.LoadAllKeysFromFile();
		System.out.println("Loaded "+keyStore.getStore().keySet().size() + " Keys");
		MCTask = new BusMCTask(this);
		this.start();
	}

	public void run(){
		System.out.println("Run");
		while(true){
			try {
				sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ViewBusTraffic();
	}
	
	private MHDR_MType DecodeMHDR(byte b){
		MHDR_MType[] allv = MHDR_MType.values();
		return allv[(b & 0xf0)>>5];
	}
	
	private String DecodeMHDRFRU(byte b){
		int v = b & 0x03;
		switch(v){
		case 0: return "LoRaWAN R1";
		case 1: return "RFU 1";
		case 2: return "RFU 2";
		case 3: return "RFU 3";
		}
		return "UNKNOWN";
	}
	
	private int DecodeSequence(byte[] bb){
		int A = bb[5];
		int B = bb[6];
		if (bb[5]<0) A = 256 + bb[5];
		if (bb[6]<0) B = 256 + bb[6];
		return 256*A+B;
	}
	
	private boolean[] FCtrlStat(byte b){
		boolean[] retb = {false,false,false,false};
		if ((b & 0x80) == 0x80) retb[0] = true;
		if ((b & 0x40) == 0x40) retb[1] = true;
		if ((b & 0x20) == 0x20) retb[2] = true;
		if ((b & 0x10) == 0x10) retb[3] = true;
		return retb;
	}
	
	private int FCtrlStat_FOptsLen(byte b){
		return b & 0x0F;
	}

	@Override
	public void Receive(SnifModel data) {
		byte[] header = Hex.decode(data.header);
		System.out.println("============= Receive SnifModel =================");
		System.out.println("= address  = 0x"+Integer.toHexString(data.address)+" =");
		System.out.println("= sequence = 0x"+Integer.toHexString(data.sequence)+" =");
		System.out.println("= header   = "+ data.header+" =");
		System.out.println("= Bdata    = "+ data.bdata+" =");
		System.out.println("= data     = "+ data.rXpk.data+" =");
		System.out.println("= RSSI     = "+ data.rssi+" =");
		System.out.println("= MType    = "+ DecodeMHDR(header[0]).toString()+" =");
		System.out.println("= Major    = "+ DecodeMHDRFRU(header[0]).toString()+" =");
		System.out.println("= sequence = "+ DecodeSequence(header)+" =");
		;
		
		String adess = Integer.toHexString(data.address).toUpperCase();
		KeysHolder keysHolder = keyStore.getStore().get(adess);
		if (keysHolder!=null){
			crypto = new LoraCrypto(keysHolder.getAppSessionKey(), keysHolder.getNetworkSessionKey(),data.address);
			String dec = crypto.LoraDecrypter(data.rXpk.data);
			System.out.println("= dataDev  = "+ dec+" =");
		}
		
		
		System.out.println("=================================================");
		System.out.println();
	}
}

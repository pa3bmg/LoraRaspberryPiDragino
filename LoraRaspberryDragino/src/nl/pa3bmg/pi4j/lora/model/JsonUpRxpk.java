package nl.pa3bmg.pi4j.lora.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JsonUpRxpk implements Serializable {
	private static final long serialVersionUID = 1L;
	@SerializedName("rxpk") @Expose public ArrayList<rxpk> rxpks = new ArrayList<rxpk>();
	
	public static class rxpk implements Serializable {
		private static final long serialVersionUID = 1L;
		@SerializedName("time") @Expose public String time;
		@SerializedName("tmst") @Expose public int tmst;
		@SerializedName("freq") @Expose public double freq;
		@SerializedName("chan") @Expose public int chan;
		@SerializedName("rfch") @Expose public int rfch;
		@SerializedName("stat") @Expose public int stat;
		@SerializedName("modu") @Expose public String modu;
		@SerializedName("datr") @Expose public String datr;
		@SerializedName("codr") @Expose public String codr;
		@SerializedName("rssi") @Expose public int rssi;
		@SerializedName("lsnr") @Expose public int lsnr;
		@SerializedName("size") @Expose public int size;
		@SerializedName("data") @Expose public String data;
	}
}

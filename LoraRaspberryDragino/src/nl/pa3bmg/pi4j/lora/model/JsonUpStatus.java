package nl.pa3bmg.pi4j.lora.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JsonUpStatus {
	
	@SerializedName("stat") @Expose public stat stats = new stat();
	
	public static class stat {
		@SerializedName("time") @Expose	public String time;
		@SerializedName("lati") @Expose	public double lati;
		@SerializedName("long") @Expose	public double lng;
		@SerializedName("alti") @Expose	public int alti;
		@SerializedName("rxnb") @Expose	public int rxnb;
		@SerializedName("rxok") @Expose	public int rxok;
		@SerializedName("rxfw") @Expose	public int rxfw;
		@SerializedName("ackr") @Expose	public int ackr;
		@SerializedName("dwnb") @Expose	public int dwnb;
		@SerializedName("txnb") @Expose	public int txnb;
		@SerializedName("pfrm") @Expose	public String pfrm;
		@SerializedName("mail") @Expose	public String mail;
		@SerializedName("desc") @Expose	public String desc;
	}
}

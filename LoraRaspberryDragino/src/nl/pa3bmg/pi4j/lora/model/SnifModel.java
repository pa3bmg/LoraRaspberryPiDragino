package nl.pa3bmg.pi4j.lora.model;

import java.io.Serializable;

import nl.pa3bmg.pi4j.lora.model.JsonUpRxpk.rxpk;

public class SnifModel implements Serializable {
	private static final long serialVersionUID = 1L;
	public int ID = 0;
	public int address;
	public int sequence;
	public int rssi;
	public String header;
	public String bdata;
	public rxpk rXpk;
	
}

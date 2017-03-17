package nl.pa3bmg.pi4j.lora.multicast;

import nl.pa3bmg.pi4j.lora.model.SnifModel;

public interface BusMCCallback {
	public void Receive(SnifModel data);
	public void Command(String sdata); 
}

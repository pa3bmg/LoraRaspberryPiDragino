package nl.pa3bmg.pi4j.lora.common;

public interface UDPCommCallback {
	public void ReceivedUDPString(String rec);
	public void ErrorinUdptx();
	public void ErrorinUdprx();
}

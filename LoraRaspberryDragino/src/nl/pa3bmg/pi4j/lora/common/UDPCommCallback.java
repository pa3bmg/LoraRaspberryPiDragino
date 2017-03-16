package nl.pa3bmg.pi4j.lora.common;

public interface UDPCommCallback {
	public void ReceivedString(String rec);
	public void ErrorinUdptx();
	public void ErrorinUdprx();
}

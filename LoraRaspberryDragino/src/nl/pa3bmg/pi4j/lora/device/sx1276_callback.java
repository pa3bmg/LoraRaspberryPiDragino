package nl.pa3bmg.pi4j.lora.device;

import nl.pa3bmg.pi4j.lora.model.SnifModel;

public interface sx1276_callback {
	public void MessageReceiveed(SnifModel snifmodel);
	public void Error(int foutCode);
}

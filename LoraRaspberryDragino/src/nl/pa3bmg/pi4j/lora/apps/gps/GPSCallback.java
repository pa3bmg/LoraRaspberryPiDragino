package nl.pa3bmg.pi4j.lora.apps.gps;

import nl.pa3bmg.pi4j.lora.model.GPSPosition;

public interface GPSCallback {
	public void UpdatePosition(GPSPosition position);
	
	public void GPSConnectionProblem();
}

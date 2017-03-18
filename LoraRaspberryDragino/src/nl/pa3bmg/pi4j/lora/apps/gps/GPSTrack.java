package nl.pa3bmg.pi4j.lora.apps.gps;

import java.io.IOException;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;

import nl.pa3bmg.pi4j.lora.model.GPSPosition;

public class GPSTrack extends Thread {
	private GPSCallback Callback;
	final Serial serial = SerialFactory.createInstance();
	private GPSPosition position = new GPSPosition();
	private int nochange = 0;
	private boolean debug = false;

	public GPSTrack(GPSCallback _Callback) {
		Callback = _Callback;
		if (SetupSerial()) {
			this.start();
		} else {
			if (Callback != null)
				Callback.GPSConnectionProblem();
		}
	}
	
	public GPSTrack(GPSCallback _Callback, boolean _debug) {
		debug = _debug;
		Callback = _Callback;
		if (SetupSerial()) {
			this.start();
		} else {
			if (Callback != null)
				Callback.GPSConnectionProblem();
		}
	}

	public void run() {
		serial.addListener(new SerialDataEventListener() {

			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					String line = event.getAsciiString();
					if (debug) System.out.println(line);
					if (line.startsWith("$")) {
						if (debug) System.out.println(line);
						String nmea = line.substring(1);
						String[] tokens = nmea.split("\\,");
						String type = tokens[0];
						if (type.equals("GPGGA")) {
							position.time = Float.parseFloat(tokens[1]);
							position.lat = Latitude2Decimal(tokens[2], tokens[3]);
							position.lon = Longitude2Decimal(tokens[4], tokens[5]);
							position.quality = Integer.parseInt(tokens[6]);
							if (tokens.length > 9) {
								position.altitude = Float.parseFloat(tokens[9]);
							}
							if (nochange++ > 30) {
								if (debug) System.out.println("$$ " + position.toString());
								if (Callback != null) Callback.UpdatePosition(position);
								nochange = 0;
							}
						} else if (type.equals("GPRMC")) {
							position.time = Float.parseFloat(tokens[1]);
							position.lat = Latitude2Decimal(tokens[3], tokens[4]);
							position.lon = Longitude2Decimal(tokens[5], tokens[6]);
							position.velocity = Float.parseFloat(tokens[7]);
							position.dir = Float.parseFloat(tokens[8]);
							if (debug) System.out.println("@@ " + position.toString());
							if (Callback != null) Callback.UpdatePosition(position);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (Callback != null) Callback.GPSConnectionProblem();
				}

			}
		});
		while (true) {
			try {
				sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	static float Latitude2Decimal(String lat, String NS) {
		float med;
		try {
			med = Float.parseFloat(lat.substring(2)) / 60.0f;
			med += Float.parseFloat(lat.substring(0, 2));
			if (NS.startsWith("S")) {
				med = -med;
			}
			return med;
		} catch (Exception e) {
			return 0.00f;
		}
	}

	static float Longitude2Decimal(String lon, String WE) {
		try {
			float med = Float.parseFloat(lon.substring(3)) / 60.0f;
			med += Float.parseFloat(lon.substring(0, 3));
			if (WE.startsWith("W")) {
				med = -med;
			}
			return med;
		} catch (Exception e) {
			return 0.00f;
		}
	}

	private boolean SetupSerial() {
		try {
			SerialConfig config = new SerialConfig();
			config.device(SerialPort.getDefaultPort()).baud(Baud._9600).dataBits(DataBits._8).parity(Parity.NONE)
					.stopBits(StopBits._1).flowControl(FlowControl.NONE);
			serial.open(config);
			return true;
		} catch (UnsupportedBoardType e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) {
		if (args.length==1){
			new GPSTrack(null,true);
		} else {
			new GPSTrack(null);
		}
	}
}

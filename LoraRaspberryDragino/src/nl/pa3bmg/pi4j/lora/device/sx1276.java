package nl.pa3bmg.pi4j.lora.device;

import java.io.IOException;
import java.util.Arrays;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import nl.pa3bmg.pi4j.lora.crypto.LoraCrypto;
import nl.pa3bmg.pi4j.lora.model.JsonUpRxpk;
import nl.pa3bmg.pi4j.lora.model.KeysHolder;
import nl.pa3bmg.pi4j.lora.model.SnifModel;
import nl.pa3bmg.pi4j.lora.model.JsonUpRxpk.rxpk;

public class sx1276 extends Thread {
	public static int REG_FIFO                    	= 0x00;
	public static int REG_OPMODE                  	= 0x01;
	public static int REG_FIFO_ADDR_PTR           	= 0x0D;
	public static int REG_FIFO_TX_BASE_AD         	= 0x0E;
	public static int REG_FIFO_RX_BASE_AD         	= 0x0F;
	public static int REG_FIFO_RX_CURRENT_ADDR    	= 0x10;
	public static int REG_IRQ_FLAGS_MASK          	= 0x11;
	public static int REG_IRQ_FLAGS               	= 0x12;
	public static int REG_RX_NB_BYTES             	= 0x13;
	public static int REG_PKT_SNR_VALUE				= 0x19;
	public static int REG_PKT_RSSI_VALUE			= 0x1A;
	public static int REG_RSSI_VALUE				= 0x1B;
	public static int REG_MODEM_CONFIG            	= 0x1D;
	public static int REG_MODEM_CONFIG2           	= 0x1E;
	public static int REG_SYMB_TIMEOUT_LSB  		= 0x1F;
	public static int REG_PAYLOAD_LENGTH          	= 0x22;
	public static int REG_MAX_PAYLOAD_LENGTH 		= 0x23;
	public static int REG_HOP_PERIOD              	= 0x24;
	public static int REG_MODEM_CONFIG3           	= 0x26;
	public static int REG_SYNC_WORD					= 0x39;
	public static int REG_DIO_MAPPING_1           	= 0x40;
	public static int REG_DIO_MAPPING_2           	= 0x41;
	public static int REG_VERSION	  				= 0x42; 
	public static int REG_4D_PA_DAC					= 0x4d;
	
	public static int PAYLOAD_LENGTH              	= 0x40;
	
	public static int SX7X_MODE_SLEEP             	= 0x80;
	public static int SX7X_MODE_STANDBY           	= 0x81;
	public static int SX7X_MODE_TX                	= 0x83;
	public static int SX7X_MODE_RX_CONTINUOS      	= 0x85;
	
	// FRF
	public static int REG_FRF_MSB          			= 0x06;
	public static int REG_FRF_MID          			= 0x07;
	public static int REG_FRF_LSB          			= 0x08;

	public static int FRF_MSB              			= 0xD9; // 868.1 Mhz
	public static int FRF_MID              			= 0x06;
	public static int FRF_LSB              			= 0x66;
	
	// LOW NOISE AMPLIFIER
	public static int REG_LNA                     	= 0x0C;
	public static int LNA_MAX_GAIN                	= 0x23;
	public static int LNA_OFF_GAIN                	= 0x00;
	public static int LNA_LOW_GAIN		    		= 0x20;

	public static int PA_DAC_DISABLE              	= 0x04;
	public static int PA_DAC_ENABLE              	= 0x07;
	
	public static SpiDevice spi = null;
	private GpioPinDigitalOutput RstPin;
	private GpioPinDigitalOutput SSPin;
	private GpioPinDigitalInput DIOPin;
	private Pin ssPin 	= RaspiPin.GPIO_10;  		//pin 10
	private Pin dio0 	= RaspiPin.GPIO_07;			//pin 7
	private Pin RST 	= RaspiPin.GPIO_00;			//pin 0
	private GpioController gpio;
	private boolean LastSend = false;
	private sx1276_callback callback;
	private KeysHolder keyS = new KeysHolder();
	private LoraCrypto crypto;
	private String DeviceAddress;
	private int messageCounter = 0;
	
	public sx1276(sx1276_callback _callback, GpioController _gpio){
		new sx1276(_callback,_gpio,null);
	}
	
	public sx1276(sx1276_callback _callback, GpioController _gpio, String _DeviceAddress){
		gpio = _gpio;
		callback = _callback;
		DeviceAddress = _DeviceAddress;
		InitStuff();
		if (_DeviceAddress!=null) SetupKeys();
		try {
			SetupLoRa();
			this.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		while(true){
			try {
				sleep(1);
			} catch (InterruptedException e) {
				//swall
			}
		}	
	}
	
	private byte[] GetMessage(){
		writeSPI(REG_IRQ_FLAGS,0x40);  //clear rx
		int irqflags = readSPI(REG_IRQ_FLAGS);
		System.out.println("irqflags = "+irqflags);
		if ((irqflags & 0x20) == 0x20){
			System.out.println("CRC Error");
			writeSPI(REG_IRQ_FLAGS, 0x20);
			return null;
		}
		int currentAddr = readSPI(REG_FIFO_RX_CURRENT_ADDR);
		int receivedCount = readSPI(REG_RX_NB_BYTES);
		writeSPI(REG_FIFO_ADDR_PTR,currentAddr);
		byte[] res = new byte[receivedCount];
		for (int i = 0; i < receivedCount; i++){
			int c = readSPI(REG_FIFO) & 0xff;
			res[i] = (byte) c;
		}
		return res;
	}
	
	private void SendMessagePacket(byte[] message){
		writeSPI(REG_OPMODE, SX7X_MODE_STANDBY);
		LastSend = true;
		System.out.println("SendMessagePacket");
		writeSPI(sx1276.REG_FIFO_ADDR_PTR,0);
		for (Byte b : message){
			writeSPI(REG_FIFO, b.intValue());
		}
		writeSPI(REG_PAYLOAD_LENGTH,message.length);
		writeSPI(REG_OPMODE, SX7X_MODE_TX);
		writeSPI(REG_DIO_MAPPING_1,0x40);
	}
	
	private void selectSPI(){
		SSPin.low();
	}
	
	private void unselectSPI(){
		SSPin.high();
	}
	
	private int readSPI(int addr){
		byte spibuf[] = new byte[2];
		int res = 0x00;
		spibuf[0] = (byte)(addr);
		spibuf[1] = 0x00;
		selectSPI();
		try {
			byte[] result = spi.write(spibuf);
			res = result[1];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unselectSPI();
		return res;
	}
	
	private void writeSPI(int addr, int value){
		byte spibuf[] = new byte[2];
		spibuf[0] = (byte)(addr | 0x80);
		spibuf[1] = (byte)value;
		selectSPI();
		try {
			spi.write(spibuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unselectSPI();
	}
	
	public void SendEncPayloadMessage(String Message){
		byte[] mdata = crypto.LoraEncrypter(Message, messageCounter++);
		SendMessagePacket(mdata);
	}
	
	public String DecriptMessage(byte[] messin){
		return crypto.LoraDecrypter(messin);
	}
	
	private void ReceiveMessagePacket(){
		if (LastSend){
			LastSend = false;
			writeSPI(REG_OPMODE, SX7X_MODE_RX_CONTINUOS);
			writeSPI(0x40,0x00);
		} else {
			if (DIOPin.isHigh()){
				byte[] message = GetMessage();
				if (message!=null){
					System.out.println("Message = "+ ByteUtils.toHexString(message) + " length = "+message.length);
					SnifModel sm = ProcessMessage(message);
					if (callback !=null) callback.MessageReceiveed(sm);
				}
			}
		}
	}
	
	private SnifModel ProcessMessage(byte[] message){
		SnifModel sm = new SnifModel();
		sm.bdata = ByteUtils.toHexString(message);
		int value = readSPI(sx1276.REG_PKT_SNR_VALUE);
		int SNR = (value & 0xFF) >> 2;
		int rssi = readSPI(sx1276.REG_RSSI_VALUE)-157;
		int A = message[6];
		int B = message[7];
		if (message[6]<0) A = 256 + message[6];
		if (message[7]<0) B = 256 + message[7];
		int sequence = 256*B+A;
		// Get the adress
		byte[] addrbytes = Arrays.copyOfRange(message, 1, 5);
		int address = java.nio.ByteBuffer.wrap(addrbytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
		String addr = Integer.toHexString(address).toUpperCase();
		byte[] header = new byte[9];
		for (int i = 0; i < 9 ; i++){
			header[i] = message[i];
		}
		String BaseData = new String(Base64.encode(message));
		DateTime dt = new DateTime();
		JsonUpRxpk us = new JsonUpRxpk();
		rxpk rx= new rxpk();
		rx.tmst = dt.getSecondOfDay()*10000;
		rx.freq = 868.1;
		rx.chan = 0;
		rx.rfch = 0;
		rx.stat = 1;
		rx.modu = "LORA";
		rx.datr = "SF7BW125";
		rx.codr = "4/5";
		rx.rssi = rssi;
		rx.lsnr = SNR;
		rx.size = message.length;
		rx.data = BaseData;
		sm.address = address;
		sm.sequence = sequence;
		sm.rssi = rssi;
		sm.rXpk = rx;
		sm.header = ByteUtils.toHexString(header);
		return sm;
	}
	
	private boolean SetupLoRa() throws Exception{
		System.out.println("SetupLoRa");
		int version = readSPI(REG_VERSION);
		if (version == 0x12){
			System.out.println("SX1276 detected, starting");
		} else {
			System.out.println("Unrecornized transceiver. version 0x"+Integer.toHexString(version));
			return false;
		}
		writeSPI(REG_OPMODE, SX7X_MODE_SLEEP);
		//Set freq
		writeSPI(REG_FRF_MSB, FRF_MSB);
		writeSPI(REG_FRF_MID, FRF_MID);
		writeSPI(REG_FRF_LSB, FRF_LSB);
		//LoRaWAN public sync word
		writeSPI(REG_SYNC_WORD, 0x34);	
		writeSPI(REG_MODEM_CONFIG3, 0x04);
		writeSPI(REG_MODEM_CONFIG, 0x72);
		writeSPI(REG_MODEM_CONFIG2, 0x74);	
		writeSPI(REG_SYMB_TIMEOUT_LSB, 0x08);
		writeSPI(REG_MAX_PAYLOAD_LENGTH, 0x80);
		writeSPI(REG_PAYLOAD_LENGTH, 0x40);
		writeSPI(REG_HOP_PERIOD, 0xFF);
		writeSPI(REG_FIFO_ADDR_PTR, readSPI(REG_FIFO_RX_BASE_AD));
		writeSPI(REG_FIFO_TX_BASE_AD,0x00);
		//Set Continous Receive Mode
		writeSPI(REG_LNA, sx1276.LNA_MAX_GAIN);
		writeSPI(REG_4D_PA_DAC, PA_DAC_ENABLE );
		writeSPI(REG_OPMODE, SX7X_MODE_STANDBY);
		return true;
	}
	
	public void ReceiverOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_RX_CONTINUOS);
	}
	
	public void transmitterOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_TX);
		writeSPI(REG_DIO_MAPPING_1,0x40);
		LastSend = true;
	}
	
	public void StandbyOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_STANDBY);
	}
	
	public void SleepOn(){
		writeSPI(sx1276.REG_OPMODE, SX7X_MODE_SLEEP);
	}
	
	private boolean SetupKeys(){
		if (keyS.LoadKeysFromFile(DeviceAddress)){
			int da = Integer.parseInt(keyS.getDeviceAddress(), 16);
			crypto = new LoraCrypto(keyS.getAppSessionKey(),keyS.getNetworkSessionKey(),da);
			return true;
		}
		return false;
	}
	
	private void InitStuff(){
		System.out.println("InitStuff");
		try {
			//SpiDevice.DEFAULT_SPI_SPEED
			spi = SpiFactory.getInstance(SpiChannel.CS0,500000,SpiMode.MODE_0);
			RstPin = gpio.provisionDigitalOutputPin(RST, "RST", PinState.HIGH);
			RstPin.setShutdownOptions(true, PinState.LOW);
			RstPin.low();
			DIOPin = gpio.provisionDigitalInputPin(dio0,PinPullResistance.PULL_DOWN);
			DIOPin.setShutdownOptions(true);
			DIOPin.addListener(new GpioPinListenerDigital(){
				@Override
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
					if (arg0.getEdge() == PinEdge.RISING){
						System.out.println("ReceiveMessage");
						Logger.info("ReceiveMessage");
						ReceiveMessagePacket();
					}
				}
			});
			SSPin = gpio.provisionDigitalOutputPin(ssPin, "SS", PinState.HIGH);
			SSPin.setShutdownOptions(true, PinState.LOW);
			SSPin.high();
			RstPin.high();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

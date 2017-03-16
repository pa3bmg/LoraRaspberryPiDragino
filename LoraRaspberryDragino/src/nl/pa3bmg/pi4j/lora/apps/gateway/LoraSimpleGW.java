package nl.pa3bmg.pi4j.lora.apps.gateway;

import com.google.gson.Gson;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import nl.pa3bmg.pi4j.lora.common.UDPCommCallback;
import nl.pa3bmg.pi4j.lora.common.UDPcomm;
import nl.pa3bmg.pi4j.lora.device.sx1276;
import nl.pa3bmg.pi4j.lora.device.sx1276_callback;
import nl.pa3bmg.pi4j.lora.model.JsonUpRxpk;
import nl.pa3bmg.pi4j.lora.model.JsonUpStatus;
import nl.pa3bmg.pi4j.lora.model.JsonUpStatus.stat;
import nl.pa3bmg.pi4j.lora.model.SnifModel;
import nl.pa3bmg.pi4j.lora.multicast.BusMCCallback;
import nl.pa3bmg.pi4j.lora.multicast.BusMCTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bouncycastle.util.encoders.Hex;
import org.pmw.tinylog.Logger;

public class LoraSimpleGW extends Thread implements UDPCommCallback  , BusMCCallback,  sx1276_callback {
	final GpioController gpio = GpioFactory.getInstance();
	private UDPcomm UDP;  //communication to TheThingsNetwork
	private String GateWayMac = "abcdefffff0123456";
	private long LastStatsTime = 0;
	private BusMCTask MCTask;
	private String GateWayAddress = "router.eu.thethings.network";
	private sx1276 sxdevice;
	private Gson gs = new Gson();
	
	
	public LoraSimpleGW(String _GateWayMac, String _GateWayAddress){
		Logger.info("Start LoraSimpleGW");
		GateWayMac = _GateWayMac;
		GateWayAddress = _GateWayAddress;
		sxdevice = new sx1276(this,gpio);
		sxdevice.ReceiverOn();
		UDP = new UDPcomm(this, 1700,1700, GateWayAddress);
		this.run();
	}

	public static void main(String[] args) {
		if (args.length==3){
			String GateWayMac = args[0];
			String GateWayAddress = args[1];
			new LoraSimpleGW(GateWayMac, GateWayAddress);
		} else {
			new LoraSimpleGW("abcdefffff0123456","router.eu.thethings.network");
		}
	}
	
	public void SendStatusMessage(){
		JsonUpStatus jus = new JsonUpStatus();
		jus.stats = new stat();
		jus.stats.time = GetSimpleNowTime();
		jus.stats.lati = 0.0000;
		jus.stats.lng = 0.0000;
		jus.stats.alti = 20;
		jus.stats.rxnb = 0;
		jus.stats.rxok = 1;
		jus.stats.rxfw = 0;
		jus.stats.ackr = 0;
		jus.stats.dwnb = 0;
		jus.stats.txnb = 0;
		jus.stats.pfrm = "Single Channel Gateway";
		jus.stats.mail = "";
		jus.stats.desc = "";
		String json = gs.toJsonTree(jus).toString();
		Logger.info(json);
		UDP.SendLoraPacket(Hex.decode(GateWayMac), json);
	}
	
	public String GetSimpleNowTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");
		return sdf.format(new Date());
	}
	
	public void run(){
		while(true){
			try {
				sleep(1);
				Date now = new Date();
				if (now.getTime() > (LastStatsTime + 30000)){
					LastStatsTime = now.getTime();
					SendStatusMessage();
				}
			} catch (InterruptedException e) {
				// Swall
			}
		}	
	}

	@Override
	public void Receive(SnifModel data) {
		System.out.println("Receive SnifModel 0x"+ Integer.toHexString(data.address));
		
	}

	@Override
	public void ReceivedUDPString(String rec) {
		System.out.println("ReceivedUDPString " + rec);
	}

	@Override
	public void ErrorinUdptx() {
		System.out.println("ErrorinUdptx");
	}

	@Override
	public void ErrorinUdprx() {
		System.out.println("ErrorinUdprx");
	}

	@Override
	public void MessageReceiveed(SnifModel snifmodel) {
		System.out.println("MessageReceiveed");
		MCTask.SendMCID(snifmodel);
		JsonUpRxpk us = new JsonUpRxpk();
		us.rxpks.add(snifmodel.rXpk);
		String json = gs.toJsonTree(us).toString();
		UDP.SendLoraPacket(Hex.decode(GateWayMac), json);
	}

	@Override
	public void Error(int foutCode) {
		System.out.println("Error " + foutCode);
	}
}

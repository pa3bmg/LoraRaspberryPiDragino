package nl.pa3bmg.pi4j.lora.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Random;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.pmw.tinylog.Logger;


public class UDPcomm {
	private int RecSock = 1700;
	private int TXSock = 1700;
	private UDPCommCallback callback;
	private String IP = "router.eu.thethings.network";
	private Random rn = new Random();
	
	public UDPcomm(int TXSock){
		//TX only to remote sock TXSock
		this.TXSock = TXSock;
	}
	
	public UDPcomm(UDPCommCallback callback, int TXSock, int RecSock){
		this.TXSock = TXSock;
		this.RecSock = RecSock;
		this.callback = callback;
		new Receive().start();
	}
	
	public void SendLoraPacket(byte[] GateWayMac ,String Message){
		byte[] LoraHeader = new byte[12];
		LoraHeader[0]  = (byte) 0x01;  //protocol version
		LoraHeader[1]  = (byte) rn.nextInt(200+1);  //random token
		LoraHeader[2]  = (byte) rn.nextInt(200+1);;  //random token
		LoraHeader[3]  = (byte) 0x00;  //PUSH_DATA identifier
		LoraHeader[4]  = GateWayMac[0];  //Gateway unique identifier
		LoraHeader[5]  = GateWayMac[1];  //Gateway unique identifier
		LoraHeader[6]  = GateWayMac[2];  //Gateway unique identifier
		LoraHeader[7]  = GateWayMac[3];  //Gateway unique identifier
		LoraHeader[8]  = GateWayMac[4];  //Gateway unique identifier
		LoraHeader[9]  = GateWayMac[5];  //Gateway unique identifier
		LoraHeader[10] = GateWayMac[6]; //Gateway unique identifier
		LoraHeader[11] = GateWayMac[7]; //Gateway unique identifier
		byte[] json = Message.getBytes();
		byte[] sendData = new byte[12+json.length];
		for (int i=0; i < LoraHeader.length; i++){
			sendData[i] = LoraHeader[i];
		}
		int i = 12;
		for (byte b : json){
			sendData[i++] = b;
		}
		SendPacket(sendData);
	}
	
	public void SendPacket(byte[] sendMessage){
		try {
			DatagramPacket packet = new DatagramPacket(sendMessage, sendMessage.length, new InetSocketAddress(IP, TXSock));
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
		
	public class Receive extends Thread {
		
		public void run() {
			try {
				byte[] buffer = new byte[3000];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				DatagramSocket socket = new DatagramSocket(RecSock);
				while(true){
					try {
						socket.receive(packet);
						buffer = packet.getData();
						String Redata = new String(buffer);
						Logger.info("RecData="+Redata);
						if (callback!=null) callback.ReceivedUDPString(Redata);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}	
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new UDPcomm(null,1700,1700);
	}
}


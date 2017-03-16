package nl.pa3bmg.pi4j.lora.multicast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;

import nl.pa3bmg.pi4j.lora.model.SnifModel;

public class BusMCTask {
	BusMCCallback callback;
	long StartTime = new Date().getTime();
	Timer timer;
	String InfoGroup = "239.192.8.61";
	public int InfoPort = 5000;
	int InfoTtl = 32;
	MulticastSocket InfoSock = null;

	public BusMCTask(BusMCCallback callback) {
		this.callback = callback;
		try {
			InfoSock = new MulticastSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new ReceiverIDTask(this).start();
	}

	// Send
	public void SendMCID(Object senddata) {
		try {
			ByteArrayOutputStream b_out = new ByteArrayOutputStream();
			ObjectOutputStream o_out = new ObjectOutputStream(b_out);
			o_out.writeObject(senddata);
			byte[] buf = b_out.toByteArray();
			DatagramPacket pack = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(InfoGroup), InfoPort);
			InfoSock.setTimeToLive(InfoTtl);
			InfoSock.send(pack);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// MC-Receiver
	class ReceiverIDTask extends Thread {
		BusMCTask f2 = null;
		MulticastSocket rx = null;

		public ReceiverIDTask(BusMCTask frameref) {
			f2 = frameref;
			try {
				rx = new MulticastSocket(f2.InfoPort);
				rx.joinGroup(InetAddress.getByName(f2.InfoGroup));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			byte buf[] = new byte[65535];
			ByteArrayInputStream b_in = new ByteArrayInputStream(buf);
			DatagramPacket pack = new DatagramPacket(buf, buf.length);
			while (true) {
				try {
					rx.receive(pack);
					System.out.println("Rec data from " + pack.getAddress());
					ObjectInputStream o_in = new ObjectInputStream(b_in);
					Object O_in = o_in.readObject();
					System.out
							.println("Rec Class" + O_in.getClass().toString());
					pack.setLength(buf.length);
					b_in.reset();
					if (O_in instanceof SnifModel) {
						SnifModel SM = (SnifModel) O_in;
						System.out.println("======== SnifModel Info =======");
						System.out.println("= Lora Device Address  0x" + Integer.toHexString(SM.address));
						System.out.println("= Lora Device Sequence " + SM.sequence);
						callback.Receive(SM);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

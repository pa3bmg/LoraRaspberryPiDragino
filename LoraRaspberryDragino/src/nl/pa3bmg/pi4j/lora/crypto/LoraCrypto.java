package nl.pa3bmg.pi4j.lora.crypto;

/*
 * TS-K March 2017
 */

import java.util.Arrays;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class LoraCrypto {
	/**
	 * AES engine
	 */
	final AESFastEngine aesEngine = new AESFastEngine();
	final BlockCipher aes = new AESEngine();
	/**
	 * Key of the application
	 */
	private KeyParameter Appkey = null;
	private KeyParameter Netkey = null;
	/**
	 * Lora Device Address
	 */
	private int DevAddr;
	/**
	 * Lora Message sequenceCounter
	 */
	private int sequenceCounter = 0;
	
	public LoraCrypto(String _AppkeyString, String _NetkeyString, int _DevAddr){
		DevAddr = _DevAddr;
		Appkey = new KeyParameter(Hex.decode(_AppkeyString));
		Netkey = new KeyParameter(Hex.decode(_NetkeyString));
	}
	
	public byte[] LoraEncrypter(String PlainText){
		return LoraEncrypter(PlainText, sequenceCounter++);
	}
	
	public byte[] LoraEncrypter(String PlainText, int sequenceCounter){
		aesEngine.init(true, Appkey);
		byte[] plaintext = PlainText.getBytes();
		byte[] transport = new byte[9+plaintext.length];
		byte[] mictransport = new byte[9+plaintext.length+4];
		transport[0] = (byte) 0x40;
		transport[1] = (byte) ((DevAddr) & 0xFF);
		transport[2] = (byte) ((DevAddr >> 8) & 0xFF);
		transport[3] = (byte) ((DevAddr >> 16) & 0xFF);
		transport[4] = (byte) ((DevAddr >> 24) & 0xFF);
		transport[6] = (byte) ((sequenceCounter) & 0xFF);
		transport[7] = (byte) ((sequenceCounter >> 8) & 0xFF);
		transport[8] = (byte) 0x01;
		int blocks = plaintext.length/16+1;
		int pos = 9;
		for (int bc = 1; bc < blocks+1 ; bc++){
			byte[] aBlock = GetaBlock(DevAddr,sequenceCounter,bc);
			byte[] sBlock = { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			aesEngine.processBlock(aBlock, 0, sBlock, 0);
			for (int i=0 ; i < 16 ; i++){
				if (pos-8 > plaintext.length) break;
				transport[pos] = (byte) (plaintext[pos-9] ^ sBlock[i]);
				pos++;
			}
		}
		byte[] mic = GenMIC(transport, sequenceCounter);
		System.arraycopy(transport, 0, mictransport, 0, transport.length);
		System.arraycopy(mic, 0, mictransport, transport.length,4);
		return mictransport;
	}
	
	public String LoraDecrypter(String base64encodedData){
		return LoraDecrypter(Base64.decode(base64encodedData));
	}
	
	public String LoraDecrypter(byte[] decode){
		int A = decode[6];
		int B = decode[7];
		if (decode[6]<0) A = 256 + decode[6];
		if (decode[7]<0) B = 256 + decode[7];
		int sequenceCounter = 256*B+A;
		byte[] addrbytes = Arrays.copyOfRange(decode, 1, 5);
		int address = java.nio.ByteBuffer.wrap(addrbytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
		byte[] decodeR = new byte[decode.length-4];
		byte[] mic = new byte[4];
		System.arraycopy(decode, 0, decodeR, 0, decode.length - 4);
		System.arraycopy(decode, decode.length-4 , mic, 0, 4);
		byte[] payload = new byte[decodeR.length - 9];
		System.arraycopy(decodeR, 9, payload, 0, decodeR.length - 9);
		byte[] CalMic = GenMIC(decodeR,sequenceCounter);
		int blocks = payload.length/16+1;
		byte[] Message = new byte[payload.length];
		int pos=0;
		aesEngine.init(true, Appkey);
		for (int bc = 1; bc < blocks+1 ; bc++){
			byte[] aBlock = GetaBlock(address,sequenceCounter,bc);
			byte[] sBlock = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			aesEngine.processBlock(aBlock, 0, sBlock, 0);
			for (int i=0 ; i < 16 ; i++){
				Message[pos] = (byte) (payload[pos] ^ sBlock[i]);
				if (pos+2 > payload.length) {
					break;
				} else {
					pos++;
				}
			}
		}
		return new String(Message);
	}
	
	private byte[] GenMIC(byte[] totalMsg, int sequenceCounter){
		int lenmsg = totalMsg.length;
		CipherParameters params = Netkey;
		CMac mac = new CMac(aes);
		mac.init(params);
		byte[] sBlock = new byte[16];		
		byte[] mBlock = new byte[totalMsg.length + 16];
		byte[] a0Block = Getb0Block(sequenceCounter, lenmsg);
		System.arraycopy(a0Block, 0, mBlock, 0, 16);
		System.arraycopy(totalMsg, 0, mBlock, 16, totalMsg.length);
		mac.update(mBlock, 0, mBlock.length);
		mac.doFinal(sBlock, 0);
		byte[] result4b = new byte[4];
		System.arraycopy(sBlock, 0, result4b, 0, 4);
		return result4b;
	}
	
	private byte[] GetaBlock(int DevAddr, int sequenceCounter, int blockCounter){
		byte[] aBlock = { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		aBlock[6] = (byte) ((DevAddr) & 0xFF);
		aBlock[7] = (byte) ((DevAddr >> 8) & 0xFF);
		aBlock[8] = (byte) ((DevAddr >> 16) & 0xFF);
		aBlock[9] = (byte) ((DevAddr >> 24) & 0xFF);
		aBlock[10] = (byte) ((sequenceCounter) & 0xFF);
		aBlock[11] = (byte) ((sequenceCounter >> 8) & 0xFF);
		aBlock[12] = (byte) ((sequenceCounter >> 16) & 0xFF);
		aBlock[13] = (byte) ((sequenceCounter >> 24) & 0xFF);
		aBlock[15] = (byte) ((blockCounter) & 0xFF);
		return aBlock;
	}
	
	private byte[] Getb0Block(int sequenceCounter, int lenmsg){
		byte[] b0Block = { 0x49, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		b0Block[6] = (byte) ((DevAddr) & 0xFF);
		b0Block[7] = (byte) ((DevAddr >> 8) & 0xFF);
		b0Block[8] = (byte) ((DevAddr >> 16) & 0xFF);
		b0Block[9] = (byte) ((DevAddr >> 24) & 0xFF);
		b0Block[10] = (byte) ((sequenceCounter) & 0xFF);
		b0Block[11] = (byte) ((sequenceCounter >> 8) & 0xFF);
		b0Block[12] = (byte) ((sequenceCounter >> 16) & 0xFF);
		b0Block[13] = (byte) ((sequenceCounter >> 24) & 0xFF);
		b0Block[15] = (byte) ((lenmsg) & 0xFF);
		return b0Block;
	}
}

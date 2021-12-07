package Peer;

import java.net.Socket;
import java.util.HashMap;

public class Peer {
	private int persPeerID;
	private Socket sock;
	private int PeerID;
	private boolean interested;
	private byte[] bitfield;
	public HashMap<Integer, Long> dataStats = new HashMap<>();
	public HashMap<Integer, Long> getDataStats() {
		return dataStats;
	}
	public void setDataStats(HashMap<Integer, Long> ds) {
		dataStats = ds;
	}
	public int getPersPeerID() {
		return persPeerID;
	}
	public void setPersPeerID(int persPeerID) {
		this.persPeerID = persPeerID;
	}
	public Socket getSock() {
		return sock;
	}
	public void setSock(Socket sock) {
		this.sock = sock;
	}
	public int getPeerID() {
		return PeerID;
	}
	public void setPeerID(int peerID) {
		PeerID = peerID;
	}
	public boolean isInterested() {
		return interested;
	}
	public void setInterested(boolean interested) {
		this.interested = interested;
	}
	public byte[] getBitfield() {
		return bitfield;
	}
	public void setBitfield(byte[] bitfield) {
		this.bitfield = bitfield;
	}
	
	
}

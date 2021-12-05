package Peer;

public class HandShake {

	private int persPeerID;
	public byte[] content = new byte[32];
	private String head = "PEER2PEERCNGROUP28";
	private String zeroPadding = "0000000000";
	
	public HandShake(int persPeerID) {
		// TODO Auto-generated constructor stub
		this.persPeerID = persPeerID;
		String chars = head + zeroPadding + Integer.toString(this.persPeerID);
		content = chars.getBytes();
	}

}

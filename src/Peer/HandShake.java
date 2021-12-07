package Peer;

public class HandShake {

	private int persPeerID;
	public byte[] content = new byte[32];
	private String head = "PEER2PEERCNGROUP28";
	private String zeroPadding = "0000000000";
	
	/**
	 * Generates handshake of desired configuration 
	 * @param persPeerID uses peerID to generate handshake
	 */
	public HandShake(int persPeerID) {
		this.persPeerID = persPeerID;
		String chars = head + zeroPadding + Integer.toString(this.persPeerID);
		content = chars.getBytes();
	}

}

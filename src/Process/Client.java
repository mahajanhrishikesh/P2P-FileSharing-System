package Process;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import FileManagement.Logger;
import FileManagement.PeerInfoFileParser;
import Peer.CompleteFile;
import Peer.HandShake;
import Peer.Peer;
import Peer.PeerProcess;
import Peer.message.BitField;
import Peer.message.Request;

public class Client extends Thread{
	
	private int PORT;
	private int persPeerID;
	private String peerIP;
	private ArrayList<String[]> c = new ArrayList<String[]>();
	private int nPieces;
	private long fSize;
	private long pSize;
	private boolean completeFile;
	private String EXPECTED_HEADER_VALUE = "PEER2PEERCNGROUP280000000000";

	
	public Client(int peerID, boolean completeFile, int nPieces, long fSize, long pSize) {
		
		persPeerID = peerID;
		this.fSize = fSize;
		this.pSize = pSize;
		this.nPieces = nPieces;
		this.completeFile = completeFile;
		
	}

	@Override
	public void run() {
	
		PeerInfoFileParser peerInfoParser = new PeerInfoFileParser(persPeerID);
		c = peerInfoParser.getAllPeerInfo();
		Iterator<String[]> itr = c.iterator();
		
		while(itr.hasNext())
		{
			String[] infoArr = itr.next();
			peerIP = infoArr[1];
			PORT = Integer.parseInt(infoArr[2]);
			
			try {
				System.out.println("Inside client "+peerIP);
				Socket sock = new Socket(peerIP, PORT);
				HandShake sendHS = new HandShake(persPeerID);
				handShakeSender(sock, sendHS.content);
				System.out.println("Sent Handshake.");
				
				// Receiving handshake from recently connected peer
				byte[] recvHSContent = handShakeReceiver(sock);
				// Extracting head from content
				String head = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
				System.out.println(head);
				//Extracting peer id from content 
				String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32));
			    String trimmedPeerID = peerIDStr.trim();
			    int rcvdID = Integer.parseInt(trimmedPeerID);
			    
			    // Check header value
			    if(head.equals(EXPECTED_HEADER_VALUE))
			    {
			    	System.out.println("Head confirmed.");
			    	boolean flag = false;
			    	Iterator<Integer> itr2 = PeerProcess.peerIDList.iterator();
			    	
			    	// Checking for ID in handshake content
			    	while(itr2.hasNext())
			    	{
			    		int tid = itr2.next();
			    		
			    		if(tid != persPeerID) 
			    		{
			    			if(tid == rcvdID)
			    			{
			    				flag = true;
			    				break;
			    			}
			    		}
			    	}
			    	
			    	if(flag)
			    	{
			    		//Populate Peer Object with appropriate values.
			    		Peer peer = new Peer();
			    		peer.setPersPeerID(persPeerID);
			    		peer.setSock(sock);
			    		peer.setPeerID(Integer.parseInt(infoArr[0]));
			    		
			    		// Receive bitfield from the peer
			    		byte[] rcvdfield = recieveBitField(sock);
			    		peer.setBitfield(rcvdfield);
			    		
			    		// Send bitfield of available pieces to connected peer
			    		sendBitField(sock);
			    		peer.setInterested(false);
			    		
			    		synchronized (PeerProcess.peersList)
			    		{
			    			PeerProcess.peersList.add(peer);
			    			try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			    		}
			    		
			    		CompleteFile completefile = new CompleteFile();
			    		completefile.setSock(sock);
			    		completefile.setHasFullFile(false);
			    		
			    		PeerProcess.hasFullFile.add(completefile);
			    		
			    		System.out.println("Requesting Connection to PeerID: "+Integer.parseInt(infoArr[0]));
			    		System.out.println();
			    		Logger.makeTCPConnection(Integer.parseInt(infoArr[0]));
			    		
			    		// Collect any messages present in the message pool and send them 
			    		// in a synchronous fashion
			    		MessageSender ms = new MessageSender();
			    		ms.start();
			    		//System.out.println("Sending Message");
			    		
			    		// Keep requesting new pieces, PieceRequest program will terminate
			    		// program thread if all peers finish downloading.
			    		PieceRequest pr = new PieceRequest(Integer.parseInt(infoArr[0]), nPieces, completeFile, fSize, pSize);
			    		pr.start();
			    		//System.out.println("Requested Piece");
			    		
			    		// Receive & Process messages by type
			    		MessageReciever mr = new MessageReciever(persPeerID, rcvdID, sock, pSize);
			    		mr.start();
			    	}
			    	else
			    	{
			    		System.out.println("Unknown Peer Found.");
			    	}
			    }
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	/****************OUTGOING DATA FUNCTIONS*****************/
	
	/**
	 * Sends the bitfield attribute of message type BitField.
	 * @param sock Socket generated and set beforehand for ongoing connection
	 */
	private void sendBitField(Socket sock) {
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(BitField.bitfield);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends the already generated handshake via given socket
	 * @param sock Socket generated and set beforehand for ongoing connection
	 * @param content Content for handshake along with header stored here
	 */
	private void handShakeSender(Socket sock, byte[] content) {
		//System.out.println("Inside HSS");
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	/******************************************************/

	/****************INCOMING DATA FUNCTIONS*****************/
		
	/**
	 * Receives the bitfield from peer
	 * @param sock Socket generated and set beforehand for ongoing connection
	 * @return
	 */
	private byte[] recieveBitField(Socket sock) {
		byte[] bf = null;
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(sock.getInputStream());
			bf = (byte[]) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bf;
	}

	/**
	 * Receives the handshake from connected peer
	 * @param sock Socket generated and set beforehand for ongoing connection
	 * @return Receives new handshake object 
	 */
	private byte[] handShakeReceiver(Socket sock) {
		//System.out.println("Inside HSR");
		byte[] content = null;
		try {
			//System.out.println("Waiting for socket Input.");
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			content = (byte[]) in.readObject();
			//System.out.println(String.valueOf(content));
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
	/******************************************************/
	

}

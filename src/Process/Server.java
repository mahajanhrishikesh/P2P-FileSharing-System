package Process;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

import FileManagement.Logger;
import Peer.CompleteFile;
import Peer.HandShake;
import Peer.Peer;
import Peer.PeerProcess;
import Peer.message.BitField;

public class Server extends Thread {

	private int PORT;
	private int nPieces;
	private int persPeerID;
	private boolean completeFile;
	private long fSize;
	private long pSize;
	private String EXPECTED_HEADER_VALUE = "PEER2PEERCNGROUP280000000000";
	
	public Server(int PORT, int peerID, boolean completeFile, int nPieces, long fSize, long pSize) {
		this.PORT = PORT;
		persPeerID = peerID;
		this.completeFile = completeFile;
		this.nPieces = nPieces;
		this.fSize = fSize;
		this.pSize = pSize;
	}

	@Override
	public void run() {
		try
		{
			ServerSocket s = new ServerSocket(PORT);
			System.out.println("Server is running on "+PORT);
			while(true)
			{
				Socket sock = s.accept();
				System.out.println("Accepted Connection");
				
				// Receiving handshake from recently connected peer
				byte[] recvHSContent = handShakeReceiver(sock);
				// Extracting head from content
				String head = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
				System.out.println(head);
				//Extracting peer id from content 
				String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32));
			    String trimmedPeerID = peerIDStr.trim();
			    int rcvdID = Integer.parseInt(trimmedPeerID);
			    
			    // Sending Servers handshake to connected peer
			    HandShake sendHS = new HandShake(persPeerID);
			    handShakeSender(sock, sendHS.content);
			    
			    // Check header value 
			    if(head.equals(EXPECTED_HEADER_VALUE))
			    {
			    	System.out.println("Head Confirmed.");
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
			    		peer.setPeerID(rcvdID);
			    		
			    		// Send bitfield of available pieces to connected peer
			    		sendBitField(sock);
			    		
			    		// Receive bitfield from the peer
			    		byte[] rcvdfield = recieveBitField(sock);
			    		peer.setBitfield(rcvdfield);
			    		
			    		peer.setInterested(false);
			    		
			    		PeerProcess.peersList.add(peer);
			    		
			    		CompleteFile completefile = new CompleteFile();
			    		completefile.setSock(sock);
			    		completefile.setHasFullFile(false);
			    		
			    		System.out.println("Incoming Connection Request From PeerID: "+rcvdID);
			    		
			    		Logger.madeTCPConnected(rcvdID);
			    		
			    		// Collect any messages present in the message pool and send them 
			    		// in a synchronous fashion
			    		MessageSender ms = new MessageSender();
			    		ms.start();
			    		
			    		// Keep requesting new pieces, PieceRequest program will terminate
			    		// program thread if all peers finish downloading.
			    		PieceRequest pr = new PieceRequest(rcvdID, nPieces, completeFile, fSize, pSize);
			    		pr.start();
			    		
			    		// Receive & Process messages by type
			    		MessageReciever mr = new MessageReciever(sock, pSize);
			    		mr.start();
			    	}
			    	else
			    	{
			    		System.out.println("Unknown Peer Found.");
			    	}
			    }
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
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
			byte[] content = null;
			try {
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				content = (byte[]) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return content;
		}
		/******************************************************/
		
}

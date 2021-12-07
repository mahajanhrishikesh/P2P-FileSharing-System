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
				byte[] recvHSContent = handShakeReceiver(sock);
				String head = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
				System.out.println(head);
				
				String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32));
			    String trimmedPeerID = peerIDStr.trim();
			    int rcvdID = Integer.parseInt(trimmedPeerID);
			    
			    HandShake sendHS = new HandShake(persPeerID);
			    handShakeSender(sock, sendHS.content);
			    
			    if(head.equals("PEER2PEERCNGROUP280000000000"))
			    {
			    	System.out.println("head confirmed..");
			    	boolean flag = false;
			    	Iterator<Integer> itr2 = PeerProcess.peerIDList.iterator();
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
			    		System.out.println("Identified peer"+rcvdID);
			    		Peer peer = new Peer();
			    		peer.setPersPeerID(persPeerID);
			    		peer.setSock(sock);
			    		peer.setPeerID(rcvdID);
			    		
			    		sendBitField(sock);
			    		byte[] rcvdfield = recieveBitField(sock);
			    		peer.setBitfield(rcvdfield);
			    		//System.out.println("Bitfield done");
			    		
			    		peer.setInterested(false);
			    		
			    		PeerProcess.peersList.add(peer);
			    		
			    		CompleteFile completefile = new CompleteFile();
			    		completefile.setSock(sock);
			    		completefile.setHasFullFile(false);
			    		
			    		PeerProcess.hasFullFile.add(completefile);
			    		//System.out.println("File status checked");
			    		System.out.println("Incoming Connection Request From PeerID: "+rcvdID);
			    		System.out.println();
			    		Logger.makeTCPConnection(rcvdID);
			    	
			    		MessageSender ms = new MessageSender();
			    		ms.start();
			    		
			    		PieceRequest pr = new PieceRequest(rcvdID, nPieces, completeFile, fSize, pSize);
			    		pr.start();
			    		
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
		private void sendBitField(Socket sock) {
			
			try {
				ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
				out.writeObject(BitField.bitfield);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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

}

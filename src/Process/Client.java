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
				
				Socket sock = new Socket(peerIP, PORT);
				HandShake sendHS = new HandShake(persPeerID);
				handShakeSender(sock, sendHS.content);
				
				byte[] recvHSContent = handShakeReceiver(sock);
				String head = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
				System.out.println(head);
				
				String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32));
			    String trimmedPeerID = peerIDStr.trim();
			    int rcvdID = Integer.parseInt(trimmedPeerID);
			    
			    if(head.equals("PEER2PEERCNGROUP280000000000"))
			    {
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
			    		Peer peer = new Peer();
			    		peer.setPersPeerID(persPeerID);
			    		peer.setSock(sock);
			    		peer.setPeerID(Integer.parseInt(infoArr[0]));
			    		
			    		byte[] rcvdfield = recieveBitField(sock);
			    		peer.setBitfield(rcvdfield);
			    		
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
			    		
			    		MessageSender ms = new MessageSender();
			    		ms.start();
			    		
			    		PieceRequest pr = new PieceRequest(Integer.parseInt(infoArr[0]), nPieces, completeFile, fSize, pSize);
			    		pr.start();
			    		
			    		MessageReciever mr = new MessageReciever(sock, pSize);
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

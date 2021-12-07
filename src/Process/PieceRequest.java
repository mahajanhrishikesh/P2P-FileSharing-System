package Process;

import java.net.Socket;
import java.util.Iterator;
import java.util.Random;

import FileManagement.FileMerger;
import FileManagement.Logger;
import Peer.CompleteFile;
import Peer.MsgBody;
import Peer.Peer;
import Peer.PeerProcess;
import Peer.message.BitField;
import Peer.message.Interested;
import Peer.message.NotInterested;
import Peer.message.Request;

public class PieceRequest extends Thread{
	
	private int persPeerID;
	private int nPieces;
	private long fSize;
	private long pSize;
	private int peerID;
	private boolean completeFile;
	private boolean flag = false;
	Socket sock;




	public PieceRequest(int parseInt, int nPieces, boolean completeFile, long fSize, long pSize) {
		this.peerID = peerID;
		this.nPieces = nPieces;
		this.completeFile = completeFile;
		this.fSize = fSize;
		this.pSize = pSize;
	}




	@Override
	public void run()
	{
		
		if(completeFile == false)
		{
			Peer peer = null;
			byte[] field;
			int getPiece;
			
			synchronized(PeerProcess.peersList) {
				Iterator<Peer> itr = PeerProcess.peersList.iterator();
				
				while(itr.hasNext())
				{
					peer = (Peer) itr.next();
					if(peer.getPeerID() == peerID)
					{
						persPeerID = peer.getPersPeerID();
						System.out.println("PersPeerID: "+persPeerID);
						sock = peer.getSock();
						break;
					}
				}
			}
			
			while(true)
			{
				System.out.println("Inside Piece Request.");
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				boolean fullFile = hasFullFile();
				
				if(fullFile)
				{
					if(!Logger.fFlag)
					{
						Logger.fFlag = true;
						System.out.println("Finished Downloading.");
						Logger.downloadComplete();
						
						FileMerger mFile = new FileMerger();
						mFile.reassemble(persPeerID, fSize, pSize, nPieces);
						
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				}
				else
				{
					if(peer.isInterested())
					{
						field = peer.getBitfield();
						getPiece = getPieceInfo(field, BitField.bitfield);
						if(getPiece == 0)
						{
							peer.setInterested(false);
							NotInterested n = new NotInterested();
							
							synchronized (PeerProcess.msgBody)
							{
								MsgBody mBody = new MsgBody();
								mBody.setSocket(sock);
								mBody.setMessage(n.notInterestedMsg);
								PeerProcess.msgBody.add(mBody);
							}
							flag=true;
						}
						else {
							Request r = new Request(getPiece);
							synchronized (PeerProcess.msgBody)
							{
								MsgBody msg = new MsgBody();
								msg.setSocket(sock);
								msg.setMessage(r.request);
								PeerProcess.msgBody.add(msg);
							}
						}
					}
					else
					{
						field = peer.getBitfield();
						getPiece = getPieceInfo(field, BitField.bitfield);
						
						if(getPiece == 0)
						{
							if(flag)
							{
								NotInterested not = new NotInterested();
								synchronized (PeerProcess.msgBody)
								{
									MsgBody msg = new MsgBody();
									msg.setSocket(sock);
									msg.setMessage(not.notInterestedMsg);
									PeerProcess.msgBody.add(msg);
								}
							}
						}
						else
						{
							peer.setInterested(true);
							flag = false;
							
							Interested i = new Interested();
							
							synchronized (PeerProcess.msgBody)
							{
								System.out.println("Interested Here");
								MsgBody mBody = new MsgBody();
								mBody.setSocket(sock);
								mBody.setMessage(i.interestedMsg);
								PeerProcess.msgBody.add(mBody);
							}
							Request request = new Request(getPiece);
							synchronized (PeerProcess.msgBody)
							{
								MsgBody mBody = new MsgBody();
								mBody.setSocket(sock);
								mBody.setMessage(request.request);
								PeerProcess.msgBody.add(mBody);
							}
						}
					}
				}
				
			}
			
		}
		
		byte[] fullFileDownloaded = new byte[5];
		
		for(int j=0; j < fullFileDownloaded.length - 1; j++)
		{
			fullFileDownloaded[j] = 0;
		}
		
		fullFileDownloaded[4] = 8;
		
		sendFullFileDownloaded(fullFileDownloaded);
		
		while(true)
		{
			boolean status = checkAllPeerStatus();
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(status == true && PeerProcess.msgBody.isEmpty())
			{
				break;
			}
		}
		if(!Logger.fDoneFlag)
		{
			Logger.fDoneFlag = true;
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Logger.closeLogger();
		}
		
		System.exit(0);
		
		
		
	}




	private boolean checkAllPeerStatus() {
		boolean allFlag = true;
		Iterator<CompleteFile> itr = PeerProcess.hasFullFile.iterator();
		
		while(itr.hasNext())
		{
			CompleteFile peer = (CompleteFile)itr.next();
			if(peer.hasFullFile())
			{
				allFlag = false;
				break;
			}
		}
		return allFlag;
	}




	private void sendFullFileDownloaded(byte[] fullFileDownloaded) {
		Iterator<CompleteFile> itr = PeerProcess.hasFullFile.iterator();
		
		while(itr.hasNext())
		{
			CompleteFile peer = (CompleteFile) itr.next();
			
			synchronized (PeerProcess.msgBody)
			{
				MsgBody msgBody = new MsgBody();
				msgBody.setSocket(peer.getSocket());
				msgBody.setMessage(fullFileDownloaded);
				PeerProcess.msgBody.add(msgBody);
			}
		}
	}




	private int getPieceInfo(byte[] field, byte[] bitfield) {
		int[] tPieceList = new int[nPieces];
		int k = 0;
		int nMissingPieces = 0;
		int overflow = nPieces % 8;
		
		for(int j = 5; j < bitfield.length; j++)
		{
			int currBFBit = bitfield[j];
			int currFieldBit = field[j];
			
			String x = Integer.toBinaryString(currBFBit & 255 | 256).substring(1);
			char[] y = x.toCharArray();
			int[] z = new int[8];
			
			for(int i = 0; i < y.length; i++)
			{
				z[i] = y[i] - 48;
			}
			
			String x1 = Integer.toBinaryString(currFieldBit & 255 | 256).substring(1);
			char[] y1 = x1.toCharArray();
			int[] z1 = new int[8];
			
			for(int i = 0; i < y.length; i++)
			{
				z1[i] = y1[i] - 48;
			}
			
			if(j < bitfield.length - 1)
			{
				for(int i = 0; i < z1.length; i++)
				{
					if(z[i] == 0 && z1[i] == 1)
					{
						tPieceList[k] = 0;
						k++;
						nMissingPieces ++;
					}
					
					if(z[i] == 0 && z1[i] == 0)
					{
						tPieceList[k] = 1;
						k ++;
					}
					
					if(z[i] == 1)
					{
						tPieceList[k] = 1;
						k ++;
					}
				}
			}
			else
			{
				for(int i = 0; i<overflow; i++)
				{
					if(z[i] == 0 && z1[i] == 1)
					{
						tPieceList[k] = 0;
						k++;
						nMissingPieces ++;
					}
					
					if(z[i] == 0 && z1[i] == 0)
					{
						tPieceList[k] = 1;
						k ++;
					}
					
					if(z[i] == 1)
					{
						tPieceList[k] = 1;
						k ++;
					}
				}
			}
			
		}
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(nMissingPieces == 0)
		{
			return 0;
		}
		
		int[] choices = new int[nMissingPieces];
		
		int x2 = 0;
		for(int i = 0; i < tPieceList.length; i++)
		{
			if(tPieceList[i] == 0)
			{
				choices[x2] = 1;
				x2 ++;
			}
		}
		
		int choice = decideOption(nMissingPieces);
		int piece = choices[choice];
		System.out.println("Requsted Piece "+piece);
		return (piece + 1);
	}




	private int decideOption(int nMissingPieces) {
		
		Random rand = new Random();
		int randomChoice = rand.nextInt(nMissingPieces);
		return randomChoice;
	}




	private boolean hasFullFile() {
		
		boolean completeFlag = true;
		byte[] field = BitField.bitfield;
		for(int j = 5; j < field.length - 1; j++)
		{
			if(field[j] != -1)
			{
				completeFlag = false;
				break;
			}
		}
		System.out.println("Complete flag"+completeFlag);
		if(completeFlag)
		{
			int overflowBits = nPieces % 8;
			int puBit = field[field.length - 1];
			String x = Integer.toBinaryString(puBit & 255 | 256).substring(1);
			char[] y = x.toCharArray();
			int[] z = new int[8];
			
			for(int j = 0; j < y.length; j++)
			{
				z[j] = y[j] - 48;
			}
			
			for(int j = 0; j < overflowBits; j++)
			{
				if(z[j] == 0)
				{
					completeFlag = false;
					break;
				}
			}
		}
		
		return completeFlag;
	}
	
}

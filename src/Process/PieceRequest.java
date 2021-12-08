package Process;

import java.net.Socket;
import java.net.SocketException;
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




	public PieceRequest(int peerID, int nPieces, boolean completeFile, long fSize, long pSize) {
		this.peerID = peerID;
		this.nPieces = nPieces;
		this.completeFile = completeFile;
		this.fSize = fSize;
		this.pSize = pSize;
	}




	@Override
	public void run()
	{
		// If peer does not have complete file then it will extract the peer id and begin
		// requesting pieces.
		if(completeFile == false)
		{
			Peer peer = null;
			byte[] field;
			int getPiece;
			
			synchronized(PeerProcess.peersList) {
				Iterator<Peer> itr = PeerProcess.peersList.iterator();
				for(Peer p: PeerProcess.peersList)
				{
					System.out.println(p.getPersPeerID());
				}
				while(itr.hasNext())
				{
					peer = (Peer) itr.next();
					if(peer.getPeerID() == peerID)
					{
						persPeerID = peer.getPersPeerID();
						sock = peer.getSock();
						break;
					}
				}
			}
			
			while(true)
			{
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// checking if full file has been downloaded for further processing 
				boolean fullFile = hasFullFile();
				
				if(fullFile)
				{
					if(!Logger.fFlag)
					{
						Logger.fFlag = true;
						System.out.println("Finished Downloading.");
						Logger.downloadComplete();
						
						
						//Merging file from the collected pieces
						FileMerger mFile = new FileMerger();
						mFile.reassemble(persPeerID, fSize, pSize, nPieces);
						
						// When last peer is done everything shall shut down.
						for(Peer p: PeerProcess.peersList)
						{
							if (p.getPeerID() == persPeerID)
							{
								System.out.println("Peer "+persPeerID+" is done. It is now a server.");
								p.setImdone(true);
							}
						}
						boolean checker = true;
						for(Peer p: PeerProcess.peersList)
						{
							if(p.isImdone() == false)
							{
								checker = false;
							}
						}
						if(checker)
						{
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
					// If peer is interested in receiving the piece 
					if(peer.isInterested())
					{
						field = peer.getBitfield();
						getPiece = getPieceInfo(field, BitField.bitfield);
						if(getPiece == 0)
						{
							peer.setInterested(false);
							NotInterested n = new NotInterested();
							
							synchronized (PeerProcess.msgPool)
							{
								MsgBody mBody = new MsgBody();
								mBody.setSock(sock);
								mBody.setMessage(n.notInterestedMsg);
								PeerProcess.msgPool.add(mBody);
							}
							flag=true;
						}
						else 
						{
							Request r = new Request(getPiece);
							synchronized (PeerProcess.msgPool)
							{
								MsgBody msg = new MsgBody();
								msg.setSock(sock);
								msg.setMessage(r.request);
								PeerProcess.msgPool.add(msg);
							}
						}
					}
					else
					{
						field = peer.getBitfield();
						getPiece = getPieceInfo(field, BitField.bitfield);
						
						if(getPiece == 0)
						{
							if(!flag)
							{
								NotInterested not = new NotInterested();
								synchronized (PeerProcess.msgPool)
								{
									MsgBody msg = new MsgBody();
									msg.setSock(sock);
									msg.setMessage(not.notInterestedMsg);
									PeerProcess.msgPool.add(msg);
								}
							}
						}
						else
						{
							peer.setInterested(true);
							flag = false;
							
							Interested i = new Interested();
							
							synchronized (PeerProcess.msgPool)
							{
								MsgBody mBody = new MsgBody();
								mBody.setSock(sock);
								mBody.setMessage(i.interestedMsg);
								PeerProcess.msgPool.add(mBody);
							}
							Request request = new Request(getPiece);
							synchronized (PeerProcess.msgPool)
							{
								MsgBody mBody = new MsgBody();
								mBody.setSock(sock);
								mBody.setMessage(request.request);
								PeerProcess.msgPool.add(mBody);
							}
						}
					}
				}
				
			}
			
		}
		
		byte[] contentFull = new byte[5];
		
		for(int j=0; j < contentFull.length - 1; j++)
		{
			contentFull[j] = 0;
		}
		
		contentFull[4] = 8;
		
		sendContentFull(contentFull);
		
	}


	private void sendContentFull(byte[] contentFull) {
		Iterator<CompleteFile> itr = PeerProcess.hasFullFile.iterator();
		
		while(itr.hasNext())
		{
			CompleteFile peer = (CompleteFile) itr.next();
			
			synchronized (PeerProcess.msgPool)
			{
				MsgBody msgBody = new MsgBody();
				msgBody.setSock(peer.getSock());
				msgBody.setMessage(contentFull);
				PeerProcess.msgPool.add(msgBody);
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
			
			for(int i = 0; i < y1.length; i++)
			{
				z1[i] = y1[i] - 48;
			}
			
			if(j < (bitfield.length - 1))
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
				choices[x2] = i;
				x2 ++;
			}
		}
		
		Random rand = new Random();
		
		int choice = rand.nextInt(nMissingPieces);
		int piece = choices[choice];
		System.out.println("Requested Piece "+piece);
		return (piece + 1);
	}

	/**
	 * Checks if full file is present 
	 * @return status
	 */
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

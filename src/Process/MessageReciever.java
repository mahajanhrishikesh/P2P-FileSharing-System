package Process;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import FileManagement.Logger;
import Peer.CompleteFile;
import Peer.MsgBody;
import Peer.Peer;
import Peer.PeerProcess;
import Peer.message.BitField;
import Peer.message.Have;
import Peer.message.Piece;

public class MessageReciever extends Thread{

	private Socket sock;
	private int remPeerID;
	private long pSize;
	
	
	public MessageReciever(int persPeerID, int remID, Socket sock, long pSize) {
		this.sock = sock;
		this.pSize = pSize;
		
		Iterator<Peer> itr = PeerProcess.peersList.listIterator();
		
		remPeerID = remID;
	}


	@Override
	public void run()
	{
		while(true)
		{
			byte[] message = recieveMessage();
			int type = message[4];
			//System.out.println("Type: "+type);
			
			switch (type) {
			case 0: {
				//Choke
				System.out.println("Incoming Choke Message from Peer: " + remPeerID);
				System.out.println();
				Logger.choked(remPeerID);
				break;
			}
			case 1: {
				//UnChoke
				System.out.println("Incoming UnChoke Message from Peer: " + remPeerID);
				System.out.println();
				Logger.unchockingNeighbor(remPeerID);
				break;
			}
			case 2: {
				//Interested
				System.out.println("Incoming Interested Message from Peer: " + remPeerID);
				System.out.println();
				Logger.receiveInterested(remPeerID);
				break;
			}
			case 3: {
				// Not Interested
				System.out.println("Incoming Not Interested Message from Peer: " + remPeerID);
				System.out.println();
				Logger.receiveNotInterested(remPeerID);
				break;
			}
			case 4:{
				// 
				byte[] t = new byte[4];
				int k = 5;
				for(int i = 0; i<t.length; i++)
				{
					t[i] = message[k];
					k ++;
				}
				int nPiece = ByteBuffer.wrap(t).getInt();
				Iterator<Peer> itr = PeerProcess.peersList.iterator();
				
				while(itr.hasNext())
				{
					Peer peer = (Peer) itr.next();
					
					if(peer.getSock().equals(sock))
					{
						byte[] field = peer.getBitfield();
						
						try
						{
							synchronized (field)
							{
								field = updateBitField(field, nPiece);
								peer.setBitfield(field);
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					
				}
				
				System.out.println("Have message received " + remPeerID + " for piece "+nPiece);
				System.out.println();
				Logger.receiveHave(remPeerID, nPiece);
				break;
			}
			case 6: 
			{
				//request for a piece
				byte[] t = new byte[4];
				int k = 5;
				for(int i=0; i<t.length; i++)
				{
					t[i] = message[k];
					k++;
				}
				int nPiece = ByteBuffer.wrap(t).getInt();
				Integer i = new Integer(nPiece);
				Piece p = PeerProcess.enumPieces.get(i);
				
				synchronized(PeerProcess.msgPool)
				{
					MsgBody mBody = new MsgBody();
					mBody.setSock(sock);
					mBody.setMessage(p.pieceData);
					PeerProcess.msgPool.add(mBody);
				}
				break;
			}
			
			case 7:
			{
				byte t[] = new byte[4];
				
				int k = 5;
				for(int i=0; i<t.length; i++)
				{
					t[i] = message[k];
					k ++;
				}
				
				int pIdx = ByteBuffer.wrap(t).getInt();
				Integer n = new Integer(pIdx);
				byte[] p = new byte[message.length - 9];
				for(int i=0; i<p.length; i++)
				{
					p[i] = message[k];
					k++;
				}
				
				if(p.length == pSize && !PeerProcess.enumPieces.containsKey(n))
				{
					Piece piece = new Piece(pIdx, p);
					try {
						synchronized(PeerProcess.enumPieces)
						{
							PeerProcess.enumPieces.put(n, piece);
							Thread.sleep(30);
						}
					} catch(Exception e)
					{
						e.printStackTrace();
					}
					/*******Adding CUC Logic*******
					try {
						synchronized(PeerProcess.peersList)
						{
							Iterator<Peer> itr = PeerProcess.peersList.iterator();
							int tempI=0;
							int remLIdx = 0;
							while(itr.hasNext())
							{
								int next = itr.next().getPeerID();
								if(remPeerID == next)
								{
									remLIdx = tempI;
								}
								tempI++;
							}
							if(!PeerProcess.peersList.get(remLIdx).getDataStats().containsKey(remPeerID))
							{
								HashMap<Integer, Long> temp = PeerProcess.peersList.get(remLIdx).getDataStats();
								temp.put(remPeerID, (long)0);
								PeerProcess.peersList.get(remPeerID).setDataStats(temp);
							}
							HashMap<Integer, Long> temp = PeerProcess.peersList.get(remLIdx).getDataStats();
							temp.put(remPeerID, PeerProcess.peersList.get(remLIdx).getDataStats().get(remPeerID) + pSize);
							PeerProcess.peersList.get(remLIdx).setDataStats(temp);
							System.out.println(PeerProcess.peersList.get(remLIdx).getDataStats());
							Thread.sleep(30);
						}
					} catch(Exception e)
					{
						e.printStackTrace();
					}
					/*******Adding CUC Logic*******/
				}
				
				System.out.println("Piece "+ pIdx + "recieved from "+remPeerID);
				System.out.println();
				Logger.downloadPiece(remPeerID, pIdx);
				try
				{
					synchronized(BitField.bitfield)
					{
						BitField.updateBitField(pIdx);
						Thread.sleep(20);
					}
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				
				Have have = new Have(pIdx);
				Iterator<Peer> itr = PeerProcess.peersList.iterator();
				
				while(itr.hasNext())
				{
					Peer peer = (Peer)itr.next();
					
					synchronized (PeerProcess.msgPool)
					{
						MsgBody mBody = new MsgBody();
						mBody.setSock(peer.getSock());
						mBody.setMessage(have.have);
						PeerProcess.msgPool.add(mBody);
					}
				}
				break;
			}
			
			case 8:
			{
				//System.out.println("Received Full File Signal");
				synchronized(PeerProcess.hasFullFile)
				{
					Iterator<CompleteFile> itr = PeerProcess.hasFullFile.iterator();
					while(itr.hasNext())
					{
						CompleteFile cf = (CompleteFile) itr.next();
						if(cf.getSock().equals(sock))
						{
							cf.setHasFullFile(true);
							break;
						}
					}
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + type);
			}
			
		}
	}


	private byte[] recieveMessage() {
		
		byte[] msg = null;
		try {
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			msg = (byte[]) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Exiting now.");
			System.exit(0);
		}
		return msg;
	}
	
	public byte[] updateBitField(byte[] field, int pieceIndex)
	{
		int li = (pieceIndex - 1)/8;
		int k = 7 - ((pieceIndex - 1)%8);
		field[li+5] = (byte)(field[li+5] | (1<<k));
		return field;
	}
	
}

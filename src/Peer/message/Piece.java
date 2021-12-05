package Peer.message;
import java.nio.ByteBuffer;

public class Piece 
{
	public byte[] pieceData;
	public byte[] messageLen = new byte[4];
	public byte messageType = 7;
	public byte[] pieceIndex = new byte[4];
	public byte[] message;
	
	//Constructor
	public Piece(int index, byte[] data)
	{	
		message = data;
		int dataLength = message.length;
		pieceIndex = ByteBuffer.allocate(4).putInt(index).array();
		messageLen = ByteBuffer.allocate(4).putInt(4+dataLength).array();
		pieceData = new byte[9 + dataLength];
		
		int p;
		for(p = 0; p < messageLen.length; p++)
		{
			pieceData[p] = messageLen[p];
		}
		
		pieceData[p] = messageType;
		p++;
		
		for(int k = 0; k < pieceIndex.length; k++)
		{
			pieceData[p] = pieceIndex[k];
			p++;
		}
		
		for(int j = 0; j < message.length; j++)
		{
			pieceData[p] = message[j];
			p++;
		}	
	}
	
}

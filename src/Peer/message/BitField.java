package Peer.message;
import java.nio.ByteBuffer;

public class BitField
{
	public static byte[] bitfield;
	public int nPiece;
	public boolean completeFile = false;
	public byte[] payload;
	public byte[] messageLen = new byte[4];
	public byte messageType = 5;
	
	public static void setBitField(boolean completeFile, int nPiece)
	{
		
	}
	
	public static void updateBitField(int pID) 
	{
		
	}
}

package Peer.message;
import java.nio.ByteBuffer;

public class BitField
{
	public static byte[] bitfield;
	public static int nPiece;
	public static boolean completeFile = false;
	public static byte[] payload;
	public static byte[] messageLen = new byte[4];
	public static byte messageType = 5;
	
	public static void setBitField(boolean completeFile, int num_of_pieces)
	{
		completeFile = completeFile;
		nPiece = num_of_pieces;
		
		int payload_len = (int)Math.ceil((double)nPiece/8);
		int leftover = nPiece % 8;
		messageLen = ByteBuffer.allocate(4).putInt(payload_len).array();
		payload = new byte[payload_len];
		bitfield = new byte[payload_len+5];
		
		int i;
		for(i=0;i<messageLen.length;i++)
		{
			bitfield[i] = messageLen[i];
		}
		
		bitfield[i] = messageType;
		
		if(!completeFile)
		{
			for(int j=0;j<payload_len;j++)
			{
				i +=1;
				bitfield[j] = 0; 		//initiliaze bitfield to zero if file not present
			}
		}
		else
		{
			for(int j=0;j<payload_len-1;j++)
			{
				i +=1;
				for(int k=0;k<8;k++)
				{
					bitfield[i] = (byte)(bitfield[i] | (1 << k));
				}
			}
			
			i+=1;
			for(int j=0;j<leftover;j++)
			{
				bitfield[i] = (byte) (bitfield[i] | (1 << (7-j)));
			}
		}
	}
	
	public static void updateBitField(int piece_idx) 
	{
		int a = (piece_idx-1)/8;
		int b = 7 - ((piece_idx-1) % 8);
		bitfield[a+5] = (byte) (bitfield[a+5] | (1<<b));
	}
}

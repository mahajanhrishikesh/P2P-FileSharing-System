package Peer.message;
import java.nio.ByteBuffer;

public class Interested
{
	public byte[] interestedMsg = new byte[5];
	public byte[] messageLen = new byte[4];
	public byte messageType = 2;
	
	public Interested()
	{
		messageLen = ByteBuffer.allocate(4).putInt(0).array(); 
		int i;
		for(i=0;i<messageLen.length;i++)
		{
			interestedMsg[i] = messageLen[i];
		}
		
		interestedMsg[i] = messageType;
	}
}

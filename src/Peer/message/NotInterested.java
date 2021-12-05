package Peer.message;
import java.nio.ByteBuffer;

public class NotInterested
{
	public byte[] notInterestedMsg = new byte[5];
	public byte[] messageLen = new byte[4];
	public byte messageType = 3;
	
	public NotInterested()
	{
		messageLen = ByteBuffer.allocate(4).putInt(0).array(); 
		int i;
		for(i=0;i<messageLen.length;i++)
		{
			notInterestedMsg[i] = messageLen[i];
		}
		
		notInterestedMsg[i] = messageType;
	}

}

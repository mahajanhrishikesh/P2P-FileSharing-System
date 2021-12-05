package Peer.message;
import java.nio.ByteBuffer;

public class UnChoke
{
	public byte[] unChokeMsg = new byte[5];
	public byte[] messageLen = new byte[4];
	public byte messageType = 1;
	
	public UnChoke()
	{
		messageLen = ByteBuffer.allocate(4).putInt(0).array(); 
		int i;
		for(i=0;i<messageLen.length;i++)
		{
			unChokeMsg[i] = messageLen[i];
		}
		unChokeMsg[i] = messageType;
	}
}

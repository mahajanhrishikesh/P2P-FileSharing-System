package Peer.message;
import java.nio.ByteBuffer;

public class Choke
{
	public byte[] chokeMsg = new byte[5];
	public byte[] messageLen = new byte[4];
	public byte messageType = 0;
	
	public Choke()
	{
		messageLen = ByteBuffer.allocate(4).putInt(0).array(); 
		int i;
		for(i=0;i<messageLen.length;i++)
		{
			chokeMsg[i] = messageLen[i];
		}
		chokeMsg[i] = messageType;
	}
	

}

package Peer.message;
import java.nio.ByteBuffer;

public class Request
{
	private byte[] messageLen = new byte[4];
	private byte messageType = 6;
	private byte[] payload = new byte[4];
	public byte[] request = new byte[9];
	
	public Request(int index)
	{
		messageLen = ByteBuffer.allocate(4).putInt(4).array();
		payload = ByteBuffer.allocate(4).putInt(index).array();
		
		int i;
		for(i=0;i<messageLen.length;i++)
		{
			request[i] = messageLen[i];
		}
		
		request[i] = messageType;
		i++;
		
		for(int j=0; j<payload.length; j++)
		{
			request[i] = payload[j];
			i++;
		}
	}
}

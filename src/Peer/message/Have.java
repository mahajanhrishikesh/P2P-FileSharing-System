package Peer.message;

import java.nio.ByteBuffer;

public class Have
{

	public byte[] have = new byte[9];
	private byte[] messageLen = new byte[4];
	private byte messageType = 4;
	private byte[] payload = new byte[4];
	
	public Have(int pIdx)
	{
		messageLen = ByteBuffer.allocate(4).putInt(4).array();
		payload = ByteBuffer.allocate(4).putInt(pIdx).array();
		
		int i;
		for(i=0;i<messageLen.length;i++)
		{
			have[i] = messageLen[i];
		}
		
		have[i] = messageType;
		
		for(int j=0; j<payload.length;j++)
		{
			i+=1;
			have[i] = payload[j];
		}	
	
	}

}

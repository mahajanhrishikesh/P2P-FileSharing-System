package Peer;
import java.net.Socket;

public class MsgBody 
{
	private Socket sock;
	private byte[] msg;
	
	public Socket getSock() 
	{
		return sock;
	}
	
	public byte[] getMessage() 
	{
		return msg;
	}
	
	public void setSock(Socket sock) 
	{
		this.sock = sock;
	}
	
	public void setMessage(byte[] Msg) 
	{
		this.msg = Msg;
	}

}

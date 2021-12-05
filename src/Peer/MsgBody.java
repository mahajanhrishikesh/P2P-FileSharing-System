package Peer;
import java.net.Socket;

public class MsgBody 
{
	private Socket sock;
	private byte[] msg;
	
	public Socket getSocket() 
	{
		return sock;
	}
	
	public byte[] getMessage() 
	{
		return msg;
	}
	
	public void setSocket(Socket sock) 
	{
		this.sock = sock;
	}
	
	public void setMessage(byte[] Msg) 
	{
		this.msg = Msg;
	}

}

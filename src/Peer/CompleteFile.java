package Peer;
import java.net.Socket;

public class CompleteFile 
{
	private Socket sock;
	private boolean completeFileDownloaded;

	public void setSock(Socket sock) 
	{
		this.sock = sock;
	}

	public void setHasFullFile(boolean b) 
	{
		this.completeFileDownloaded = b;
	}

	public boolean hasFullFile()
	{
		return completeFileDownloaded;
	}

	public Socket getSocket()
	{
		return sock;
	}

}

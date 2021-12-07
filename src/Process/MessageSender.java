package Process;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Peer.MsgBody;
import Peer.PeerProcess;

public class MessageSender extends Thread{

	@Override
	public void run()
	{
		while(true)
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(!PeerProcess.msgPool.isEmpty())
			{
				synchronized (PeerProcess.msgPool)
				{
					MsgBody mBody = PeerProcess.msgPool.poll();
					Socket sock = mBody.getSock();
					byte[] message = mBody.getMessage();
					sendMessage(sock, message);
					
				}
			}
		}
	}

	private void sendMessage(Socket sock, byte[] message) {
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			synchronized (sock)
			{
				out.writeObject(message);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}

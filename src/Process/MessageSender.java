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

	/**
	 * Sends any message with a valid message type 
	 * @param sock socket of the connection through which the message is to be sent
	 * @param message first message extracted from the message pool of respective thread  message pool
	 */
	private void sendMessage(Socket sock, byte[] message) {
		
		if(checkMessage(message))
		{
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
		else
		{
			System.out.println("Invalid message entered in pool");
		}
		
	}

	private boolean checkMessage(byte[] message) {
		boolean chk = true;
		int msgType = message[4];
		if(msgType>=0 || msgType <9)
		{
			chk=true;
		}
		else
		{
			chk=false;
		}
		return chk;
	}
	
}

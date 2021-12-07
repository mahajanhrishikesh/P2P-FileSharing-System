package FileManagement;

import java.io.*;
import java.util.HashMap;
import Peer.message.Piece;

public class FileParser
{
	private int peerId;
	private long pieceSize;
	private HashMap<Integer,Piece> pieceMap = new HashMap<Integer,Piece>();
	private int pieceNo = 1;
	private String fName;
	
	public FileParser(int id, String fName, long pSize)
	{
		this.peerId = id;
		this.pieceSize = pSize;
		this.fName = fName;
	}
	
	public HashMap<Integer, Piece> fileReader()
	{
		String pathDir = new File(System.getProperty("user.dir")).getParent() + "/peer_" + peerId +"/"+fName;
		File myFile = new File(pathDir);
		try
		{
			InputStream IS = new FileInputStream(myFile);
			byte[] ipBuffer = new byte[(int)pieceSize];
			
			int ipLen=IS.read(ipBuffer); 
			Piece temp;
			while(ipLen > 0)
			{
				temp = new Piece(pieceNo, ipBuffer);
				pieceMap.put(pieceNo, temp);
				pieceNo++;
				ipLen = IS.read(ipBuffer);
			}
			
			IS.close();
		}
		catch(FileNotFoundException e)
		{
			System.err.println(e);
		}
		catch(IOException e)
		{
			System.err.println(e);
		}

		return pieceMap;
	}
}


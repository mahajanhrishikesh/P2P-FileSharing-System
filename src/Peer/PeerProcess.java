package Peer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import FileManagement.CommonConfigParser;
import FileManagement.FileParser;
import FileManagement.Logger;
import FileManagement.PeerInfoFileParser;
import Peer.message.BitField;
import Peer.message.Piece;
import Process.Client;
import Process.Server;

public class PeerProcess {
	public static String fName;
	private boolean completeFile;
	private int PORT;
	private int peerID;
	private int nPieces;
	private long fSize;
	private long pSize;
	public static HashMap<Integer, Piece> enumPieces;
	public static ArrayList<Peer> peersList = new ArrayList<Peer>();
	public static ArrayList<Integer> peerIDList;
	public static ArrayList<Integer> prefPeerIDList;
	public static LinkedList<MsgBody> msgPool = new LinkedList<MsgBody>();
	public static ArrayList<CompleteFile> hasFullFile = new ArrayList<CompleteFile>();
	
	/**
	 * Main PeerProcess starter code
	 * @param args args[0] accepts a single peer ID code, rest of the peer information is 
	 * extracted from PeerInfo.cfg
	 */
	public static void main(String[] args)
	{
		
		//Start Peer Process
		PeerProcess peerProcess = new PeerProcess();
		
		//Read the Common Configuration File 
		CommonConfigParser commonCfgFileParser = new CommonConfigParser();
		HashMap<String, Object> commonFileData = commonCfgFileParser.readCommonFile();
		
		//Read the Peer Info File
		PeerInfoFileParser peerInfo = new PeerInfoFileParser(Integer.parseInt(args[0]));
		peerInfo.readPeerInfoFile();
		peerIDList = peerInfo.getPeerIDs();
		
		//Populate current peer data
		peerProcess.fName= (String)commonFileData.get("FileName");
		peerProcess.fSize = Long.parseLong((String) commonFileData.get("FileSize"));
		peerProcess.pSize = Long.parseLong((String) commonFileData.get("PieceSize"));
		peerProcess.nPieces = (int)Math.ceil((double)peerProcess.fSize/peerProcess.pSize);
		peerProcess.peerID = peerInfo.getPeerID();
		peerProcess.PORT = peerInfo.getPORT();
		peerProcess.completeFile = peerInfo.isCompleteFile();
		BitField.setBitField(peerProcess.completeFile, peerProcess.nPieces);
		
		//Start the logging per peer
		Logger.beginLogger(peerProcess.peerID);
		
		// If peer does not have the complete file the peer will be started up as both,
		// a client and a server.
		if(peerProcess.completeFile == false)
		{
			// Since no file exists a hashmap of enumPieces needs to be made from scratch
			enumPieces = new HashMap<Integer, Piece>();
			
			Server s = new Server(peerProcess.PORT, peerProcess.peerID, peerProcess.completeFile, peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
			s.start();
			
			Client c = new Client(peerProcess.peerID, peerProcess.completeFile, peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
			c.start();			
		}
		else if(peerProcess.completeFile == true)
		{
			// Since server has full file the enumPieces HashMap will be populated with the
			// data from the file present on the server itself.
			FileParser fileParser = new FileParser(peerProcess.peerID, fName, peerProcess.pSize);
			enumPieces = fileParser.fileReader();
			if(checkFileDetails(enumPieces, peerProcess.fSize, peerProcess.pSize))
			{
				System.out.println("Read File "+fName);
				if(peerProcess.peersList.size() > 1)
				{
					for(Peer p: peerProcess.peersList)
					{
						if (p.getPeerID() == peerProcess.peerID)
						{
							System.out.println("Peer "+peerProcess.peerID+" is done. It is now a server.");
							p.setImdone(true);
						}
					}
					boolean checker = true;
					for(Peer p: peerProcess.peersList)
					{
						if(p.isImdone() == false)
						{
							checker = false;
						}
					}
					if(checker)
					{
						System.exit(0);
					}
				}
				
				Server s = new Server(peerProcess.PORT, peerProcess.peerID, peerProcess.completeFile, peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
				s.start();
			}
			else
			{
				System.out.println("File corrupted.");
				System.exit(0);
			}
		}
	}

	/**
	 * Checks if the expected file piece count and the recently received file piece count
	 * match
	 * @param enumPieces2 Enumerated Piece List of all the pieces
	 * @param fSize2 Expected file size
	 * @param pSize2 size per piece
	 * @return
	 */
	private static boolean checkFileDetails(HashMap<Integer, Piece> enumPieces2, long fSize2, long pSize2) {
		
		boolean fileOk = false;
		int numberOfPieces = enumPieces2.size();
		int expectedNPieces = (int)Math.ceil((double)fSize2/pSize2);
		if(numberOfPieces == expectedNPieces)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
}

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
	public static LinkedList<MsgBody> msgBody = new LinkedList<MsgBody>();
	public static ArrayList<CompleteFile> hasFullFile = new ArrayList<CompleteFile>();
	
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
		peerProcess.fSize = Long.parseLong((String) commonFileData.get("FileSize"));
		peerProcess.pSize = Long.parseLong((String) commonFileData.get("PieceSize"));
		peerProcess.nPieces = (int)Math.ceil((double)peerProcess.fSize/peerProcess.pSize);
		peerProcess.peerID = peerInfo.getPeerID();
		peerProcess.PORT = peerInfo.getPORT();
		peerProcess.completeFile = peerInfo.isCompleteFile();
		BitField.setBitField(peerProcess.completeFile, peerProcess.nPieces);
		
		//Start the logging per peer
		Logger.beginLogger(peerProcess.peerID);
		
		if(peerProcess.completeFile == false)
		{
			enumPieces = new HashMap<Integer, Piece>();
			
			Server s = new Server(peerProcess.PORT, peerProcess.peerID, peerProcess.completeFile, peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
			s.start();
			
			Client c = new Client(peerProcess.peerID, peerProcess.completeFile, peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
			c.start();			
		}
		else
		{
			FileParser fileParser = new FileParser(peerProcess.peerID, fName, peerProcess.pSize);
			enumPieces = fileParser.fileReader();
			
			Server s = new Server(peerProcess.PORT, peerProcess.peerID, peerProcess.completeFile, peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
			s.start();
		}
	}
}

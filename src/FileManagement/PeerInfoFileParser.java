package FileManagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class PeerInfoFileParser {
	private int peerID;
	private int persPeerID;
	private String peerIP;
	private int PORT;
	private boolean completeFile;
	
	private String fileName = "\\PeerInfo.cfg";
	private String filePath = new File(System.getProperty("user.dir")).getParent()+fileName;
	
	public PeerInfoFileParser(int peerID)
	{
		this.persPeerID = peerID;
	}
	
	/**
	 * Function will read the PeerInfo file of the format
	 *  
	 * <p>Peer_ID Peer_IP_ADDR Peer_PORT Peer_FullFileStatus</p>
	 * 
	 * Per Line.
	 */
	public void readPeerInfoFile()
	{
		File pifp = new File(filePath);
		try {
			Scanner sc = new Scanner(pifp);
			while(sc.hasNextLine())
			{
				String data = sc.nextLine();
				String[] arr = data.split(" ");
				
				if(persPeerID == Integer.parseInt(arr[0]))
				{
					peerID = Integer.parseInt(arr[0]);
					peerIP = arr[1];
					PORT = Integer.parseInt(arr[2]);
					if(Integer.parseInt(arr[3])==1)
					{
						completeFile = true;
					}
					else
					{
						completeFile = false;
					}
					System.out.println(completeFile);
				}
			}
			sc.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Please load common configuration file before running again. Expected Path: "+ filePath);
		}
	}
	
	/**
	 * Function will read the PeerInfo file and send full ID List 
	 * 
	 * @return idArr List of IDs of the peers is sent back.
	 */
	public ArrayList<Integer> getPeerIDs()
	{
		ArrayList<Integer> idArr = new ArrayList<>();
		File pifp = new File(filePath);
		try
		{
			Scanner sc = new Scanner(pifp);
			while(sc.hasNextLine())
			{
				String data = sc.nextLine();
				String[] arr = data.split(" ");
				idArr.add(Integer.parseInt(arr[0]));				
			}
			sc.close();
			return idArr;
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Please load common configuration file before running again. Expected Path: "+ filePath);
		}
		return null;
	} 
	
	/**
	 * Function will read the PeerInfo file and send complete information regarding 
	 * all peers.
	 * 
	 * @return infoArr List of IDs of the peers is sent back.
	 */
	public ArrayList<String[]> getAllPeerInfo()
	{
		ArrayList<String[]> infoArr = new ArrayList<>();
		File pifp = new File(filePath);
		try
		{
			Scanner sc = new Scanner(pifp);
			while(sc.hasNextLine())
			{
				String data = sc.nextLine();
				String[] arr = data.split(" ");
				if(persPeerID != Integer.parseInt(arr[0]))
				{
					infoArr.add(arr);
				}
				else
				{
					break;
				}
			}
			sc.close();
			return infoArr;
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Please load common configuration file before running again. Expected Path: "+ filePath);
		}
		return null;
	}

	/**
	 * Function will read the PeerInfo file and send full IP List 
	 * 
	 * @return idArr List of IDs of the peers is sent back.
	 */
	public ArrayList<String> getPeerIPs()
	{
		ArrayList<String> ipArr = new ArrayList<>();
		File pifp = new File(filePath);
		try
		{
			Scanner sc = new Scanner(pifp);
			while(sc.hasNextLine())
			{
				String data = sc.nextLine();
				String[] arr = data.split(" ");
				ipArr.add(arr[0]);				
			}
			sc.close();
			return ipArr;
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Please load common configuration file before running again. Expected Path: "+ filePath);
		}
		return null;
	}
	
	/******GETTERS & SETTERS***********/
	public int getPeerID() {
		return peerID;
	}

	public void setPeerID(int peerID) {
		this.peerID = peerID;
	}

	public String getPeerIP() {
		return peerIP;
	}

	public void setPeerIP(String peerIP) {
		this.peerIP = peerIP;
	}

	public int getPORT() {
		return PORT;
	}

	public void setPORT(int pORT) {
		PORT = pORT;
	}

	public boolean isCompleteFile() {
		return completeFile;
	}

	public void setCompleteFile(boolean completeFile) {
		this.completeFile = completeFile;
	} 
	
}

package FileManagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;   
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import Peer.Peer;

import java.time.LocalDateTime;    


@SuppressWarnings("unused")
public class Logger{
	private static int thisPeerID;
	public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
	public static LocalDateTime now = LocalDateTime.now();
	private static BufferedWriter os;
	private static int pieceNums = 0;
	private static File file;
	public static boolean fFlag = false;
	public static boolean fDoneFlag = false;

	public static void beginLogger(int pId) {
		thisPeerID = pId;
		String Name = (new File(System.getProperty("user.dir")).getParent() + "/log_peer_" + thisPeerID + ".log");
		file = new File(Name);
		try {
			os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}

	public static void makeTCPConnection(int pId) {
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " makes a connection to Peer " + pId + ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	public static void madeTCPConnected(int pId) {
		
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " is connected from Peer " + pId + ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	public static void changeInPreferredNeighbors(ArrayList<Peer> neighbours) {
		
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " has the preferred neighbors " + neighbours.toString() + ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch(IOException e) {
			System.err.println(e);
		}
	}
	
	public static void changeofOptiUnchockedNeighbor(int pId) {
		
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " has the optimistically unchocked neighbor " + pId+ ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch(IOException e) {
			System.err.println(e);
		}
	}
	
	public static void unchockingNeighbor(int pId) {
		
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " is unchoked by " + pId+ ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch(IOException e) {
			System.err.println(e);
		}
	}

	public static void downloadComplete() {
		if(fFlag == true) {
			try {
				String date = dtf.format(now);
				String str = date + " : Peer " + thisPeerID + " has downloaded the complete file.";
				os.append(str);
				os.newLine();
				os.flush();
			} catch(IOException e) {
				System.err.println(e);
			}
	}
	}

	public static void closeLogger() {
		try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void receiveInterested(int pId) {
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " received the \'interested\' message from " + pId+ ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch(IOException e) {
			System.err.println(e);
		}
	}

	public static void receiveNotInterested(int pId) {
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " received the \'not interested\' message from " + pId+ ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch(IOException e) {
			System.err.println(e);
		}
	}

	public static void receiveHave(int pId, int pIdx) {
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " received the 'have' message from Peer " + pId + " for the piece " + pIdx + ".";
			os.append(str);
			os.newLine();
			os.flush();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static void downloadPiece(int pId, int pIdx) {
		pieceNums++;
		try {
			String date = dtf.format(now);
			String str = date + " : Peer " + thisPeerID + " has downloaded the piece " + pIdx +" from Peer " + pId + ".";
			os.append(str);
			os.newLine();
			str = "It has these many number of pieces: " + pieceNums;
			os.append(str);
			os.newLine();
			os.flush();
		} catch (IOException e) {
			System.err.println(e);
		}
		
	}

}

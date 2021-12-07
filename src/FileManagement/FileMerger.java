package FileManagement;
import java.io.*;
import java.nio.ByteBuffer;

import Peer.PeerProcess;
import Peer.message.Piece; 	
public class FileMerger {

	/**
	 * 
	 * @param persPeerID
	 * @param fSize
	 * @param pSize
	 * @param nPieces
	 */
	public void reassemble(int persPeerID, long fSize, long pSize, int nPieces) {
		String directory = (new File(System.getProperty("user.dir")).getParent() + "/peer_" + persPeerID); File thisDir = new File(directory);
		if (!thisDir.exists()) {
			
			try {
				thisDir.mkdir();	
			} catch(SecurityException e) {
				System.err.println(e);
			}        
		}
		
		
		// fName
		String fName = (directory + "/" + PeerProcess.fName);
		File file = new File(fName);
		
		try {
			OutputStream out = new FileOutputStream(file);
			int i = 1;
			
			while(i <= nPieces - 1) {
				
				Integer num = new Integer(i);
				Piece p = PeerProcess.enumPieces.get(num);
				byte[] arr = new byte[4];
				int j = 0;
				while(j < 4) {
					arr[j] = p.pieceData[j];
					j++;
				}
				
				int size = ByteBuffer.wrap(arr).getInt();
				size = size - 4;
				
				byte[] buff = new byte[size];
				
				for (int k = 0, z = 9; k < buff.length && z < p.pieceData.length; k++, z++) {
					buff[k] = p.pieceData[z];
				}
				
				out.write(buff);
				i++;
			}
			Integer no = new Integer(nPieces);
			Piece p = PeerProcess.enumPieces.get(no);
			
			int s = (int) (fSize % pSize);
			System.out.println(s);
			
			byte[] buff = new byte[s];
			int k = 0; int z = 9;
			while( k < buff.length && z < p.pieceData.length) {
				buff[k] = p.pieceData[z];
				k++; z++;
			}
			
			out.write(buff);
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
			
	}

}

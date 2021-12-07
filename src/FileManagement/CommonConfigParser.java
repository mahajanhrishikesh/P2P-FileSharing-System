package FileManagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class CommonConfigParser {
	
	/**
	 * Reads the common configuration file of the format 
	 * <pre>
	 * 	NumberOfPreferredNeighbors 
	 *  UnchokingInterval 
	 *  OptimisticUnchokingInterval 
	 *  FileName *.jpg
	 *  FileSize 
	 *  PieceSize 
	 * </pre>
	 * 
	 * @return res Extracts and puts the values of all the fields mentioned above into a
	 * HashMap for easier access.
	 */
	public static HashMap<String, Object> readCommonFile() {
		// TODO Auto-generated method stub
		String fileName = "\\common.cfg";
		String commonFilePath = new File(System.getProperty("user.dir")).getParent() + fileName;
		
		HashMap<String, Object> res = new HashMap<String, Object>();
		
		try {
			File cfp = new File(commonFilePath);
			Scanner sc = new Scanner(cfp);
			
			while(sc.hasNextLine())
			{
				String data = sc.nextLine();
				String[] arr = data.split(" ");
				res.put(arr[0], arr[1]);
			}
			sc.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Please load common configuration file before running again. Expected Path: " + commonFilePath );
		}
		return res;
	}
	
}

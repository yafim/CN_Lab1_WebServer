import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Singleton class holds the necessarily logic to use WebServer program.
 * @author Yafim Vodkov 308973882 , Nir Tahan 305181166
 *
 */
public class Utils {
	/** Read file variables */
	private FileInputStream m_FileInputStream = null;
	private HashMap<String, String> m_FileParmas = null;
	
	private static final Object sf_LockInstance = new Object();
	private static Utils s_Instance = null;
	
	/**
	 * Constructor
	 */
	public Utils(){}
	
	/**
	 * Singleton implementation
	 * @return
	 */
	public static Utils Instance(){
		if (s_Instance == null){
			synchronized(sf_LockInstance){
				if (s_Instance == null){
					s_Instance = new Utils();
				}
			}
		}
		return s_Instance;
	}
	
	public HashMap<String, String> getFileParams(File i_File) throws UnsupportedEncodingException{
		byte[] bFile = readFile(i_File);
		String sFile = byteArrayToString(bFile);
		stringToDictionary(sFile);
		
		return m_FileParmas;
	}
	
	private void stringToDictionary(String i_String){
		m_FileParmas = new HashMap<String, String>();

		for (String s : i_String.split(System.lineSeparator())) {
			m_FileParmas.put(s.split("=")[0], s.split("=")[1]);
		}
	}
	
	/**
	 * Byte[] to String
	 * @param i_Bytes file content in bytes
	 * @return string
	 * @throws UnsupportedEncodingException
	 */
	private String byteArrayToString(byte[] i_Bytes) throws UnsupportedEncodingException{
		return new String(i_Bytes, "UTF-8");
	}
	/**
	 * Read bytes from file
	 * @param i_File file to read
	 * @return file data in bytes
	 */
	private byte[] readFile(File i_File){
		byte[] bFile = null;
		try{
			m_FileInputStream = new FileInputStream(i_File);
			bFile = new byte[(int)i_File.length()];
			
			while(m_FileInputStream.available() != 0){
				m_FileInputStream.read(bFile, 0, bFile.length);
			}
		}
		catch(FileNotFoundException fnf){
			// TODO: Handle exception
			System.err.println(fnf.getMessage()); // debug
		}
		catch (IOException ioe){
			// TODO: Handle exception
			System.err.println(ioe.getMessage()); // debug
		}
		finally{
			try {
				m_FileInputStream.close();
			} catch (Exception e) {
				// TODO: Handle exception
			}
		}

		return bFile;
	}
	
	public static void printDictionary(HashMap<String, String> i_Dictionary){
		for (Map.Entry<String,String> entry : i_Dictionary.entrySet()) {
			  String key = entry.getKey();
			  String value = entry.getValue();
			  
			  System.out.println("KEY : " + key + " - VALUE : " + value);
		}
	}
}

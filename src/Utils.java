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
	/** Errors and messages to the client */
	private final String ERR_NOT_IMPEMENTED = "501 Not Implemented";
	private final String ERR_FILE_NOT_FOUND = "404 Not Found";
	private final String ERR_BAD_REQUEST = "400 Bad Request";
	private final String ERR_INTERNAL_SRV_ERR = "500 Internal Server Error";
	private final String OK_MSG = "200 OK";

	/** HTTP request variables */
	private String m_HTTPRequest = null;
	private String[] m_SplitHTTPRequest = null;	
	private HashMap<String, String> m_HTTPAdditionalInformation = null;
	private HashMap<String, String> m_RequestedVariables = null;

	// http request variables 
	private File m_RequestedFileFullPath;
	private HTTPMethod m_HTTPMethod;
	private String m_HttpVersion;

	/** Response variables */
	// Requested file
	private String m_RequestedFileContent;
	private HashMap<String, String> m_HTTPResponse = null;
	private String m_FileExtension;


	/** Read file variables */	
	private FileInputStream m_FileInputStream = null;
	private HashMap<String, String> m_FileParmas = null;

	/** Root of the server */
	private final String m_Root = "c://serverroot//";
	private final String mf_DefaultPage = "index.html";


	/** Singleton variables */
	private static final Object sf_LockInstance = new Object();
	private static Utils s_Instance = null;

	/**
	 * Empty constructor
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

	/**
	 * Get Config.ini file parameters 
	 * @param i_File
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public HashMap<String, String> getConfigFileParams(File i_File) throws UnsupportedEncodingException{
		byte[] bFile = readFile(i_File);
		String sFile = byteArrayToString(bFile);
		m_FileParmas = stringToDictionary(sFile, "=", new HashMap<String, String>());

		return m_FileParmas;
	}

	/**
	 * Handle http request.
	 * TODO: Send real http request.
	 * @param i_HTTPRequest
	 * @throws UnsupportedEncodingException
	 */
	public void handleHttpRequest(String i_HTTPRequest) throws UnsupportedEncodingException{
		//TODO: DELETE!
//		File testFileRequest = new File("C:\\serverroot\\httpRequest.txt");
//		m_HTTPRequest = byteArrayToString(readFile(testFileRequest));
		m_HTTPRequest = i_HTTPRequest;
		//	System.out.println(sTestFileRequest); // debug
		
		System.out.println(m_HTTPRequest);
		
		/** ACTUAL CODE TO KEEP */
		splitHttpRequest(m_HTTPRequest);
		parseHTTPAdditionalInformation();

		// Print the request
		System.out.println(m_SplitHTTPRequest[0]); 

		initHttpRequestParams();
		//		printHTTPRequestParams(); // debug
		try{
			tryParseVariables();
		} catch (NoVariablesException nve){
			// No variables to parse...
			//System.out.println(nve.getMessage());
		}
		//		if (m_RequestedVariables != null){ //debug!
		//			printDictionary(m_RequestedVariables);
		//		}
		//		printDictionary(m_HTTPAdditionalInformation); // debug
		buildResponseMessage();


		// END
	}

	/**
	 * Split http request into 2 parts: 1. request 2. additional parameters.
	 * @param i_HTTPRequest
	 */
	private void splitHttpRequest(String i_HTTPRequest){
		m_SplitHTTPRequest = i_HTTPRequest.split(System.lineSeparator(), 2);
		verifyGivenPath(m_SplitHTTPRequest[0]);
	}

	/**
	 * Checks if the URL that was given is OK and safe to open.
	 */
	private void verifyGivenPath(String i_Path){
		m_SplitHTTPRequest[0] = i_Path.replaceAll("\\.\\.", "");
	}

	/**
	 * Try parse variables. If exist parse and return true, Otherwise return false.
	 * @return
	 * @throws NoVariablesException 
	 */
	private void tryParseVariables() throws NoVariablesException{

		String fileName = m_RequestedFileFullPath.getName();

		String[] variables = fileName.split("\\?");

		if (variables.length == 1){
			throw new NoVariablesException("No variables to parse");
		}
		else {
			updateRequestedFileFullPath();
			getVariables(variables[1]);
		}
	}

	/**
	 * There must be variables after the file name, so delete it.
	 * @param i_FilePath
	 */
	private void updateRequestedFileFullPath(){
		String sFullPath = m_RequestedFileFullPath.toString().split("\\?")[0];
		m_RequestedFileFullPath = new File(sFullPath);
	}

	/**
	 * Get variables from path as dictionary.
	 * @param i_Variables
	 */
	private void getVariables(String i_Variables){
		// TODO: NEW METHOD
		String[] s = i_Variables.split("&");
		m_RequestedVariables = new HashMap<String, String>();
		for (String str : s){
			m_RequestedVariables = stringToDictionary(str, "=", m_RequestedVariables);
		}
	}


	/**
	 * Initialise HTTP request variables
	 */
	private void initHttpRequestParams(){
		// TODO: Maybe exception?...
		String[] sString = m_SplitHTTPRequest[0].split(" ");
		try{
			// get the method
			m_HTTPMethod = HTTPMethod.valueOf(sString[0]);

			// get the file
			boolean defaultPageGiven = (sString[1].equals("/"));

			m_RequestedFileFullPath = (defaultPageGiven) ? new File(m_Root + mf_DefaultPage)
			: new File(m_Root + sString[1]);

			// get http version
			m_HttpVersion = sString[2];

		} catch (IllegalArgumentException e){
			m_HTTPMethod = HTTPMethod.UNSUPPORTED;
			System.out.println(ERR_NOT_IMPEMENTED);
		} catch(ArrayIndexOutOfBoundsException oobe){
			System.err.println(ERR_BAD_REQUEST);
			//TODO: Handle bad request
		} catch(NullPointerException npe){
			System.out.println(ERR_INTERNAL_SRV_ERR);
		}

	}

	/**
	 * Split string to dictionary
	 * @param i_String
	 */
	private HashMap<String, String> stringToDictionary(String i_String, String i_Separator, HashMap<String, String> i_Dictionary){
		boolean isNull = false;
		boolean requestFlag = false;

		for (String s : i_String.split(System.lineSeparator())) {
			isNull = s == System.lineSeparator() || s.equals("") || s.equals(" ") || s.equals(null);

			/** Check for body */
			if (requestFlag){
				i_Dictionary.put("RequestBody", s);
				break;
			}

			if (!isNull){
				i_Dictionary.put(s.split(i_Separator)[0], s.split(i_Separator)[1]);
			} 
			else {
				requestFlag = true;
			}

		}

		return i_Dictionary;
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

	/**
	 * Start building the response based on the method.
	 */
	private void buildResponseMessage(){

		switch(m_HTTPMethod){
		case GET :
			buildResponseMessage(true, true, false);
			break;
		case POST :
			buildResponseMessage(true, true, false);
			break;
		case HTTP :
			//			System.out.println("HTTP");
			break;
		case HEAD :
			buildResponseMessage(false, true, false);
			//			System.out.println("HEAD");
			//			handleFileRequest();
			//			createResponseHeader();
			//			System.out.println(m_HTTPResponse.get("HEADER"));
			break;
		case TRACE:
			buildResponseMessage(false, false, true);
			break;
			// TODO: Need unsupported method?
		case UNSUPPORTED:
			break;
		}


	}

	/**
	 * Build response message.
	 * 1. Check the requested file and read its content to byte[] 
	 *    m_RequestedFileContent.
	 * 2. Create http response header.
	 * 3. If needed include file content to header.
	 * @param i_PrintFileContent
	 */
	private void buildResponseMessage(boolean i_PrintFileContent, boolean i_IncludeConetnt, boolean i_IncludeHTTPRequest){
		try{
			handleFileRequest();
			createResponseHeader();
			if (i_IncludeConetnt){
				buildResponseContent();
			}
			
			if (i_IncludeHTTPRequest){
				includeHTTPRequestInResponse();
			}
			// TODO: Not here...
			System.out.println(m_HTTPResponse.get("HEADER"));
		//	System.out.println(m_HTTPResponse.get("HTTPRequest"));
			
			if (i_PrintFileContent){
				System.out.println(m_HTTPResponse.get("Content"));
			}
			
			// TODO: END... // 
			
		} catch (UnsupportedEncodingException e) {
			// TODO: Handle error... (bad enum was given...)
		} catch(FileNotFoundException fnfe){
			System.out.println(fnfe.getMessage());
		} catch (NullPointerException npe){
			// No content to get... Not supposed to get here
			System.out.println(ERR_INTERNAL_SRV_ERR);
		} catch (ArrayIndexOutOfBoundsException aofe){
			// TODO: IT COULD BE A FOLDER...
			System.out.println(ERR_FILE_NOT_FOUND);
		}
	}
	
	/**
	 * Usually for TRACE method...
	 */
	private void includeHTTPRequestInResponse(){
		String newHeader = m_HTTPResponse.get("HEADER") + 
				System.lineSeparator() + m_HTTPRequest;
		m_HTTPResponse.put("HEADER" , newHeader);
	}
	
	/**
	 * If file exists and supported by the server open it, 
	 * Otherwise return 404 message
	 * TODO: 404 Exception?
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	private void handleFileRequest() throws ArrayIndexOutOfBoundsException, UnsupportedEncodingException, FileNotFoundException{
		
		//TODO: Maybe folder options...
		m_FileExtension = m_RequestedFileFullPath.getName().split("\\.")[1];

		boolean isSupported = isSupportedFormat(m_FileExtension);
		boolean isImage = false;

		// Check the file
		if (isSupported){
			isImage = isImage(m_FileExtension);
			if (isExists(m_RequestedFileFullPath)){
				// open file...
				//TODO: maybe response byte[] and not stringed content 
				m_RequestedFileContent = byteArrayToString(readFile(m_RequestedFileFullPath));

			}
			else {
				throw new FileNotFoundException(ERR_FILE_NOT_FOUND);
			}
		}
	}
	/**
	 * Returns true if file format supported by the server.
	 * @param i_FileExtension file extension
	 * @return isSupported
	 */
	private boolean isSupportedFormat(String i_FileExtension){
		boolean isSupported = false;
		try{
			SupportedFiles.valueOf(i_FileExtension);		
			isSupported = true;
		}
		catch (IllegalArgumentException iae){
			System.err.println(i_FileExtension + " Not supported file");
			isSupported = false;
		}

		return isSupported;
	}

	/**
	 * Return true if file is image, Otherwise false.
	 * @param i_FileExtension file to check
	 * @return isImage
	 */
	private boolean isImage(String i_FileExtension){
		boolean isImage = false;
		try{
			SupportedFiles.valueOf(i_FileExtension);
			isImage = true;
		}
		catch (IllegalArgumentException iae){
			isImage = false;
		}

		return isImage;
	}

	/**
	 * True if file exists
	 * @param i_FileFullPath file to check
	 * @return
	 */
	private boolean isExists(File i_FileFullPath){
		return i_FileFullPath.exists();
	}

	/**
	 * Get all the additional information in dictionary
	 */
	private void parseHTTPAdditionalInformation(){
		m_HTTPAdditionalInformation = stringToDictionary(m_SplitHTTPRequest[1], ":", new HashMap<String, String>());
	}

	/**
	 * Create HTTP response header.
	 * 1. Set content-type
	 * 2. Set content-length
	 */
	private void createResponseHeader(){
		m_HTTPResponse = new HashMap<String, String>();

		String contentType = getContentType();
		String contentLength = getContentLength();

		buildResponseHeader(contentType, contentLength);
	}

	/**
	 * Attach requested content to the http response
	 */
	private void buildResponseContent(){
		m_HTTPResponse.put("Content", m_RequestedFileContent);
	}

	/**
	 * Build response header
	 */
	private void buildResponseHeader(String i_ContentType, String i_ContentLength){
		String sHeader = String.format(
				"%s %s\ncontent-type: %s\ncontent-length: %s\n\n", 
				m_HttpVersion,
				OK_MSG,
				i_ContentType,
				i_ContentLength
				);

		m_HTTPResponse.put("HEADER", sHeader);

	}

	/**
	 * Get content type
	 * @return content type
	 */
	private String getContentType(){
		String contentType = null;
		try{
			SupportedFiles fileExtension = SupportedFiles.valueOf(m_FileExtension);
			switch(fileExtension ){
			case html:
				contentType = (HTTPMethod.TRACE == m_HTTPMethod) ? "message/http" 
						: SupportedFiles.html.getContentType();
				break;
			case bmp:
				contentType = SupportedFiles.bmp.getContentType();
				break;
			case jpg:
				contentType = SupportedFiles.jpg.getContentType();
				break;
			case gif:
				contentType = SupportedFiles.gif.getContentType();
				break;
			case png:
				contentType = SupportedFiles.png.getContentType();
				break;
			case ico:
				contentType = SupportedFiles.ico.getContentType();
				break;

				// bonus
			case txt:
				contentType = SupportedFiles.txt.getContentType();
				break;
			}
		} catch (IllegalArgumentException iae){
			contentType = "application/octet-stream";
		}

		return contentType;
	}

	/**
	 * Get content length as string
	 * @return content length
	 */
	private String getContentLength(){
		int contentLength = m_RequestedFileContent.length();
		return contentLength + "";
	}

	/*************************** ---  DELETE  ---**********************************/
	/**
	 * Print dictionary<String, String> - DEBUG ONLY! 
	 * TODO: Delete this method.
	 * @param i_Dictionary
	 */
	public static void printDictionary(HashMap<String, String> i_Dictionary){
		for (Map.Entry<String,String> entry : i_Dictionary.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			System.out.println("KEY : " + key + " - VALUE : " + value);
		}
	}

	/**
	 * Debug also...
	 */
	public void printHTTPRequestParams(){
		System.out.println("Http Method: " + m_HTTPMethod);
		System.out.println("FULL FILE PATH: " + m_RequestedFileFullPath);
		System.out.println("HTTP VERSION: " + m_HttpVersion);
	}
	/*************************** ---  DELETE END ---*******************************/
}

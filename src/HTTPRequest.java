import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Singleton class holds the necessarily logic to use WebServer program.
 * @author Yafim Vodkov 308973882 , Nir Tahan 305181166
 *
 */
public class HTTPRequest {
	/** Errors and messages to the client */
	private final String ERR_NOT_IMPEMENTED = "501 Not Implemented";
	private final String ERR_FILE_NOT_FOUND = "404 Not Found";
	private final String ERR_BAD_REQUEST = "400 Bad Request";
	private final String ERR_INTERNAL_SRV_ERR = "500 Internal Server Error";
	private final String OK_MSG = "200 OK";

	/** HTTP request variables */
	private String m_HTTPRequest = null;
	private String[] m_SplitHTTPRequest = null;	
	private HashMap<String, Object> m_HTTPAdditionalInformation = null;
	private HashMap<String, Object> m_RequestedVariables = null;

	// http request variables 
	private File m_RequestedFileFullPath;
	private HTTPMethod m_HTTPMethod;
	private String m_HttpVersion;

	/** Response variables */
	// Requested file
	private byte[]  m_RequestedFileContent;
	private HashMap<String, Object> m_HTTPResponse = null;
	private String m_FileExtension;


	/** Read file variables */	
	private static FileInputStream m_FileInputStream = null;
	private static HashMap<String, Object> m_FileParmas = null;

	/** Root of the server */
	private String m_Root; // real 
	// TODO: SEND DEFAULT PAGE here...
	private String m_DefaultPage;

	private boolean m_IsValidRequest = false;

	// Default is 200
	private String m_ResponseMessage = OK_MSG;
	/**
	 * Constructor
	 */
	public HTTPRequest(String i_Root, String i_DefaultPage){
		// Add '\' in case there isn't
		this.m_Root = (i_Root + "\\"); 
		this.m_DefaultPage = i_DefaultPage;
	}

	/**
	 * Get Config.ini file parameters 
	 * @param i_File
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static HashMap<String, Object> getConfigFileParams(File i_File) throws UnsupportedEncodingException{
		byte[] bFile = readFile(i_File);
		String sFile = byteArrayToString(bFile);
		m_FileParmas = stringToDictionary(sFile, "=", new HashMap<String, Object>());

		return m_FileParmas;
	}

	/**
	 * Get the variables after post message sent
	 */
	public void handlePostVariables(BufferedReader i_In){
		int c;
		int i;
		String sVariables = "";

		String sNumberOfBytesToRead = m_HTTPAdditionalInformation.get("Content-Length").toString().replaceAll("\\s+","");
		int numberOfBytesToRead = Integer.parseInt(sNumberOfBytesToRead);

		try {
			// read numberOfBytes from the current buffer
			for (i = numberOfBytesToRead; i > 0; i--){
				c = i_In.read();
				sVariables += (char)c;
			}

			getVariables(sVariables);
			m_RequestedVariablesLength = numberOfBytesToRead;
//			createResponseHeader();
//			System.out.println(m_RequestedVariables);
		} 
		catch (Exception e) {
			// Shouldn't get here
			System.out.println("Problem with reading from buffer");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Handle http request.
	 * TODO: Send real http request.
	 * @param i_HTTPRequest
	 * @throws Exception 
	 */
	public HashMap<String, Object> handleHttpRequest(String i_HTTPRequest) throws Exception{
		m_HTTPRequest = i_HTTPRequest;
		System.out.println(m_HTTPRequest);


		/** ACTUAL CODE TO KEEP */
		try{
			splitHttpRequest(m_HTTPRequest);
			parseHTTPAdditionalInformation();



			// Print the request
			System.out.println(m_SplitHTTPRequest[0]); 

			initHttpRequestParams();
			//		printHTTPRequestParams(); // debug


			tryParseVariables();
		} catch (NoVariablesException nve){
			// No variables to parse...
		} catch (BadRequestException bre){
			m_ResponseMessage = bre.getMessage();
			createResponseHeader();
		}
		//		if (m_RequestedVariables != null){ //debug!
		//			printDictionary(m_RequestedVariables);
		//		}
		//		printDictionary(m_HTTPAdditionalInformation); // debug

		buildResponseMessage();
		return m_HTTPResponse;

		// END
	}

	/**
	 * Split http request into 2 parts: 1. request 2. additional parameters.
	 * @param i_HTTPRequest
	 * @throws BadRequestException 
	 */
	private void splitHttpRequest(String i_HTTPRequest) throws BadRequestException{
		m_SplitHTTPRequest = i_HTTPRequest.split(System.lineSeparator(), 2);
		verifyGivenPath(m_SplitHTTPRequest[0]);

	}

	/**
	 * Checks if the URL that was given is OK and safe to open.
	 * @throws BadRequestException 
	 */
	private void verifyGivenPath(String i_Path) throws BadRequestException{
		m_SplitHTTPRequest[0] = i_Path.replaceAll("\\.\\.", "");
		if (m_SplitHTTPRequest[0].split(" ").length != 3){
			throw new BadRequestException();
		} else {
			m_IsValidRequest = true;
		}
	}

	/**
	 * Try parse variables. If exist parse and return true, Otherwise return false.
	 * @return
	 * @throws NoVariablesException 
	 */
	private void tryParseVariables() throws NoVariablesException{

		try{
			String fileName = m_RequestedFileFullPath.getName();
			String[] variables = fileName.split("\\?");

			if (variables.length == 1){

				throw new NoVariablesException("No variables to parse");
			}
			else {
				updateRequestedFileFullPath();
				getVariables(variables[1]);
			}
		} catch (Exception e){
			//			System.out.println(e.getMessage());
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

		m_RequestedVariables = new HashMap<String, Object>();
		for (String str : s){
			m_RequestedVariables = stringToDictionary(str, "=", m_RequestedVariables);
			m_RequestedVariablesLength += str.length();
		}
		// TODO: DELETE
		//		System.out.println("VARIABLES : " + m_RequestedVariables);

	}
	private int m_RequestedVariablesLength;

	public HashMap<String, Object> getVariablesAsBytes(){
		return m_RequestedVariables;
	}

	/**
	 * Initialise HTTP request variables
	 * @throws Exception 
	 */
	private void initHttpRequestParams() throws Exception{
		// TODO: Maybe exception?...
		String[] sString = m_SplitHTTPRequest[0].split(" ");
		try{
			// get the method
			m_HTTPMethod = HTTPMethod.valueOf(sString[0]);

			// get the file
			boolean defaultPageGiven = (sString[1].equals("/"));

			m_RequestedFileFullPath = (defaultPageGiven) ? new File(m_Root + m_DefaultPage)
			: new File(m_Root + sString[1]);
			
			
			// get http version
			m_HttpVersion = sString[2];

		} catch (IllegalArgumentException e){
			//			throw new Exception(ERR_NOT_IMPEMENTED);
			m_ResponseMessage = ERR_BAD_REQUEST;
			createResponseHeader();
		} catch(ArrayIndexOutOfBoundsException oobe){
			//			throw new Exception(ERR_BAD_REQUEST);
			m_ResponseMessage = ERR_BAD_REQUEST;
			createResponseHeader();
		} catch(NullPointerException npe){
			throw new Exception(ERR_INTERNAL_SRV_ERR);
		}

	}

	/**
	 * Split string to dictionary
	 * @param i_String
	 */
	private static HashMap<String, Object> stringToDictionary(String i_String, String i_Separator, HashMap<String, Object> i_Dictionary){
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
	private static String byteArrayToString(byte[] i_Bytes) throws UnsupportedEncodingException{
		return new String(i_Bytes, "UTF-8");
	}
	/**
	 * Read bytes from file
	 * @param i_File file to read
	 * @return file data in bytes
	 */
	private static byte[] readFile(File i_File){
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
	 * @throws Exception 
	 */
	private void buildResponseMessage() throws Exception{
		if (m_HTTPMethod != null){
			switch(m_HTTPMethod){
			case GET :
				buildResponseMessage(false, true, false);
				break;
			case POST :
				buildResponseMessage(false, true, false);
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
			}
		}
	}

	/**
	 * Build response message.
	 * 1. Check the requested file and read its content to byte[] 
	 *    m_RequestedFileContent.
	 * 2. Create http response header.
	 * 3. If needed include file content to header.
	 * @param i_PrintFileContent
	 * @throws Exception 
	 */
	private void buildResponseMessage(boolean i_PrintFileContent, boolean i_IncludeConetnt, boolean i_IncludeHTTPRequest) throws Exception{
		try{
			handleFileRequest();
			createResponseHeader();

			if (i_IncludeConetnt){
				buildResponseContent();
			}

			if (i_IncludeHTTPRequest){
				includeHTTPRequestInResponse();
			}

			if (i_PrintFileContent){
				System.out.println(m_HTTPResponse.get("Content"));
			}
			// TODO: END... // 

		} catch (UnsupportedEncodingException e) {
			// TODO: Handle error... (bad enum was given...)

		} catch(FileNotFoundException fnfe){
			System.out.println(fnfe.getMessage());
		} catch (NullPointerException npe){
			throw new Exception(ERR_INTERNAL_SRV_ERR);
		} catch (ArrayIndexOutOfBoundsException aofe){
			// TODO: IT COULD BE A FOLDER...
			//			throw new Exception(ERR_FILE_NOT_FOUND);
		} catch (FileNotSupportedException fnse){
			m_ResponseMessage = ERR_NOT_IMPEMENTED;

			createResponseHeader();
		}
	}

	/**
	 * Usually for TRACE method...
	 */
	private void includeHTTPRequestInResponse(){
		String newHeader = m_HTTPResponse.get("HEADER") 
				+ m_HTTPRequest;
		m_HTTPResponse.put("HEADER" , newHeader);
	}

	/**
	 * If file exists and supported by the server open it, 
	 * Otherwise return 404 message
	 * TODO: 404 Exception?
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 * @throws FileNotSupportedException 
	 */
	private void handleFileRequest() throws ArrayIndexOutOfBoundsException, UnsupportedEncodingException, FileNotFoundException, FileNotSupportedException{

		//TODO: Maybe folder options...
		try{
			m_FileExtension = m_RequestedFileFullPath.getName().split("\\.")[1];
		} catch (Exception e){
			throw new FileNotSupportedException();
		}

		boolean isImage = false;

		// Check the file
		if (isSupportedFormat(m_FileExtension)){
			isImage = isImage(m_FileExtension);
			if (isExists(m_RequestedFileFullPath)){
				// open file...
				m_RequestedFileContent = readFile(m_RequestedFileFullPath);
			}
			else {
				m_ResponseMessage = ERR_FILE_NOT_FOUND;
				m_RequestedFileContent = ERR_FILE_NOT_FOUND.getBytes();
				createResponseHeader();
			}
		} else {
			throw new FileNotSupportedException();
		}
	}
	/**
	 * Returns true if file format supported by the server.
	 * @param i_FileExtension file extension
	 * @return isSupported
	 * @throws FileNotSupportedException 
	 */
	private boolean isSupportedFormat(String i_FileExtension) throws FileNotSupportedException{
		boolean isSupported = false;
		try{
			SupportedFiles.valueOf(i_FileExtension);		
			isSupported = true;
		}
		catch (IllegalArgumentException iae){
			//			System.err.println(i_FileExtension + " Not supported file");
			isSupported = false;
			throw new FileNotSupportedException();
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
		m_HTTPAdditionalInformation = stringToDictionary(m_SplitHTTPRequest[1], ":", new HashMap<String, Object>());
	}

	/**
	 * Create HTTP response header.
	 * 1. Set content-type
	 * 2. Set content-length
	 */
	private void createResponseHeader(){
		m_HTTPResponse = new HashMap<String, Object>();

		String contentType = getContentType();
		String contentLength = getContentLength();

		buildResponseHeader(contentType, contentLength);
	}

	/**
	 * Attach requested content to the http response
	 */
	private void buildResponseContent(){
		//		if (m_RequestedVariables != null){
		//			m_RequestedFileContent = null;
		////			m_RequestedFileContent = m_RequestedVariables[0];
		//			int byteSize = 0;
		//			
		//			for (Map.Entry<String,Object> entry : m_RequestedVariables.entrySet()) {
		//				byte[] key = entry.getKey().getBytes();
		//				byte[] value = entry.getValue().toString().getBytes();
		//				byteSize += key.length + value.length;
		//				m_RequestedFileContent = new byte[byteSize];
		//				System.arraycopy(key, 0, m_RequestedFileContent, 0, key.length);
		//				System.arraycopy(value, 0, m_RequestedFileContent, key.length, value.length);
		//			
		//			}
		//			
		//		}

		m_HTTPResponse.put("Content", m_RequestedFileContent);
	}

	/**
	 * Build response header
	 */
	private void buildResponseHeader(String i_ContentType, String i_ContentLength){

//		String headerResponse = (m_IsValidRequest) ? m_HttpVersion + " " + m_SplitHTTPRequest[0].split(" ")[1] + " " + m_ResponseMessage : 
//			"HTTP/1.1 " + m_ResponseMessage;
//
//		String sHeader = String.format(
//				"%s\r\ncontent-type: %s\r\ncontent-length: %s\r\n\r\n", 
//				headerResponse,
//				i_ContentType,
//				i_ContentLength
//				);
		String headerResponse = (m_IsValidRequest) ? m_HttpVersion + " " + m_ResponseMessage : 
		"HTTP/1.1 " + m_ResponseMessage;
		String sHeader = String.format(
		"%s\r\nDate: %s\r\ncontent-type: %s\r\ncontent-length: %s\r\n\r\n", 
		headerResponse,
		getTimestamp(),
		i_ContentType,
		i_ContentLength
		);
		
		m_HTTPResponse.put("HEADER", sHeader);
	}
	
	/**
	 * Get local time stamp
	 * TODO: Local...
	 * @return
	 */
	private Date getTimestamp(){
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		//Local time zone   
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

		//Time in GMT
		try {
			return dateFormatLocal.parse( dateFormatGmt.format(new Date()) );
		} catch (ParseException e) {
			//TODO: check...
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Update params after post method
	 * @return
	 */
	public HashMap<String, Object> updateParams(){
		createResponseHeader();
		return m_HTTPResponse;
	}

	/**
	 * Get content type
	 * @return content type
	 */
	private String getContentType(){
		String contentType = null;
		try{
			SupportedFiles fileExtension = SupportedFiles.valueOf(m_FileExtension);
			switch(fileExtension){
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
		} catch (Exception iae){
			// TODO: Check the url localhost:8080/h
			contentType = "application/octet-stream";
		}

		return contentType;
	}

	public void clear(){
		/** HTTP request variables */
		m_IsValidRequest = false;
		m_HTTPRequest = "";
		m_SplitHTTPRequest = null;	
		m_HTTPAdditionalInformation.clear();
		if (m_RequestedVariables != null){
			m_RequestedVariables.clear();
			m_RequestedVariables = null;
		}

		// http request variables 
		m_RequestedFileFullPath = null;
		m_HTTPMethod = null;
		m_HttpVersion = "";

		/** Response variables */
		// Requested file
		m_RequestedFileContent = null;
		m_HTTPResponse.clear();
		m_HTTPResponse = null;
		m_FileExtension = "";

		m_ResponseMessage = OK_MSG;



		/** Read file variables */	
		try {
			m_FileInputStream.close();
		} catch (IOException e) {
			System.out.println("cant close filestream");
		}
		if (m_FileParmas != null){
			m_FileParmas.clear();
		}

		m_RequestedVariablesLength = 0;

	}

	/**
	 * Get content length as string
	 * @return content length
	 */
	private String getContentLength(){
		int contentLength = (m_RequestedFileContent != null) ? m_RequestedFileContent.length : 0;
		contentLength += m_RequestedVariablesLength;
//		System.out.println(contentLength);
		return contentLength + "";
	}

	/* Some getters and setters */
	public HTTPMethod getHTTPMethod(){
		return m_HTTPMethod;
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

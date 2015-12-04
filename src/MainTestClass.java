import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

// TODO: DELETE THIS CLASS
public class MainTestClass {
	
	private static final String r_PathToConfigFile = "c:\\serverroot\\config.ini";
	private static final File sf_ConfigFile = new File(r_PathToConfigFile);
	private static Utils m_Utils;
	
	private static HashMap<String, String> i_ConfigFileParams = null;
	
	/** AS IT WOULD BE IN HttpRequest.java file */
	public static int m_Port;
	private static String m_Root;
	private static String m_DefaultPage;
	private static int m_MaxThreads; 
	/** END */
	
	/** This class for test only! */
	public static void main(String[] args){
		//MultiThreadedClass server = new MultiThreadedClass(8080);
		//server.startTheServer(server);
		m_Utils = new Utils();
		
		try {
			i_ConfigFileParams = m_Utils.getConfigFileParams(sf_ConfigFile);
			initParams();
	//		printParams(); // debug
			// server parameters initialised
			m_Utils.handleHttpRequest("http request goes here... ");
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void initParams(){
		m_Port = Integer.parseInt(i_ConfigFileParams.get("port"));
		m_Root = i_ConfigFileParams.get("root");
		m_DefaultPage = i_ConfigFileParams.get("defaultPage");
		m_MaxThreads = Integer.parseInt(i_ConfigFileParams.get("maxThreads"));
	}
	
	private static void printParams() {
		System.out.println("port: " + m_Port);
		System.out.println("root: " + m_Root);
		System.out.println("defaultPage: " + m_DefaultPage);
		System.out.println("maxThreads: " + m_MaxThreads);
	}
}

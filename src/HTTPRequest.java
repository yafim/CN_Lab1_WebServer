import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;


public class HTTPRequest {
	
	/** Server params */
	private int m_Port;
	private String m_Root;
	private String m_DefaultPage;
	private int m_MaxThreads; 
	
	private static final String r_PathToConfigFile = "c:\\serverroot\\config.ini";
	private static final File sf_ConfigFile = new File(r_PathToConfigFile);
	private static Utils m_Utils;
	
	private static HashMap<String, String> i_ConfigFileParams = null;
	
	
	private void initServer(){
		try {
			i_ConfigFileParams = m_Utils.getConfigFileParams(sf_ConfigFile);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initParams();
		
		// TODO: Init multithread class here with ctr -> 
		// new MultihreadedClass(m_Port... )
	}
	
	/**
	 * Initialises server information based on Config.ini
	 */
	private void initParams(){
		m_Port = Integer.parseInt(i_ConfigFileParams.get("port"));
		m_Root = i_ConfigFileParams.get("root");
		m_DefaultPage = i_ConfigFileParams.get("defaultPage");
		m_MaxThreads = Integer.parseInt(i_ConfigFileParams.get("maxThreads"));
	}
}

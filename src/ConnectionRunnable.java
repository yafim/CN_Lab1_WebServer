import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ConnectionRunnable implements Runnable{
	private Socket m_clientSocket = null;
	private boolean m_isRun = true;
	private MyThread myThread;
	private Thread thread;

	private boolean m_IsHTTPRequestReady = false;
	private String m_Root;
	private String m_DefaultPage;

	//!!!!!REMEBER TO DELETE THIS SOCKET AND READER ITS JUST FOR DEBUGING!!!!!!!

	//	private Socket m_socket;
	//!!!!!!! debug!!!!!!
	public HTTPRequest httpRequest;

	public ConnectionRunnable(MyThread myThread, String i_Root, String i_DeafultPage) {
		this.myThread = myThread;
		this.m_Root = i_Root;
		this.m_DefaultPage = i_DeafultPage;
	}

	public synchronized void run() {
		this.thread = Thread.currentThread();
		//While the thread is running if the is no client then it
		//tells him to wait until he will call to handle a client
		while(m_isRun) {
			if (m_clientSocket != null) {
				runTaskForClient();
			}
			else {
				synchronized (thread) {
					try {
						thread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	InputStream input;
	OutputStream output;
	private void runTaskForClient() {		
		try {
			httpRequest = new HTTPRequest(m_Root, m_DefaultPage);
			input  = m_clientSocket.getInputStream();
			output = m_clientSocket.getOutputStream();

			BufferedReader m_In = new BufferedReader (new InputStreamReader(input));
			DataOutputStream outToClient = new DataOutputStream (output);


			//TODO: Yafim decide if you want to delete all this or not
			String lineToRead = "";
			String hTTPRequest = "";

			//			int i;
			StringBuilder response= new StringBuilder();
			String s = "";
			boolean f = true;
			int c;

			while (true){
				while ((c = m_In.read()) != -1 && !m_IsHTTPRequestReady) {
					s += (char)c;
					if ((char) c == '\r'){
						char _c = (char)m_In.read();
						if (_c == '\n'){
							s += _c;
							hTTPRequest = buildHTTPRequest(hTTPRequest, s);
							s = "";
							break;
						}
					}
				}

				if (m_IsHTTPRequestReady){
					System.out.println("============= START =============");
					HashMap<String, Object> hm;

					hm = httpRequest.handleHttpRequest(hTTPRequest);

					if (httpRequest.getHTTPMethod() == HTTPMethod.POST){
						httpRequest.handlePostVariables(m_In);
//						hm = httpRequest.updateParams();
					}

					// TODO: Clean and delete some stuff here.
					String head = (String)hm.get("HEADER");
					byte[] html = (byte[]) hm.get("Content");
					
					
					outToClient.writeBytes(head);


					if (httpRequest.getVariablesAsBytes() != null){
						HashMap<String, Object> requestedVariables = httpRequest.getVariablesAsBytes();

						for (Map.Entry<String,Object> entry : requestedVariables.entrySet()) {
							String key = entry.getKey() + " : ";
							byte[] value = entry.getValue().toString().getBytes();							
							outToClient.writeBytes(key);
							outToClient.write(value);
						}
					} 

					System.out.println(head);
					if (html != null){
						outToClient.write(html);
						System.out.println(html);
					}

					hTTPRequest = "";
					httpRequest.clear();
					m_IsHTTPRequestReady = false;
					outToClient.flush();
					System.out.println("============= END =============");



				}
			}
		} catch (Exception e){
			// General exception with relevant message. There are many 
			// possible exceptions and we let the server handle them.
			System.err.println("ERROR! " + e.getMessage());
		}
		finally{
			try {
				output.close();
				input.close();

				//Finish handling client
				myThread.onClientCommComplete();
				// TODO: DELETE 
				System.out.println("FINISH");
				System.out.println("close!");
			} catch (Exception e) {
				// error in closing streams
				System.out.println(e.getMessage());
			}
		}
	}

	private String buildHTTPRequest(String hTTPRequest, String i_String) throws UnsupportedEncodingException{
		if (i_String.equals("\r\n")){
			m_IsHTTPRequestReady = true;
			return hTTPRequest;
		}
		hTTPRequest += i_String;
		return hTTPRequest;
	}

	public boolean isBusy() {
		return m_clientSocket != null;
	}

	/**
	 * Notifying the sleeping thread that it has a client request to handle 
	 * @param clientSocket
	 * @throws IOException 
	 */
	public void communicate(Socket clientSocket){// throws IOException {		
		this.m_clientSocket = clientSocket;

		synchronized (thread) {
			//TODO: Delete this only before submitting.
			//System.out.println("Thread pool: notify thread " + myNum);
			thread.notify();
		}
	}

	/**
	 * Stops the client request upon calling by changing the running
	 * mode of the variable m_isRun to false 
	 */
	public void stop() {
		this.m_isRun = false;
		synchronized (thread) {
			thread.notify();
		}
	}
}
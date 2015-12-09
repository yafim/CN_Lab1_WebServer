

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;


public class ConnectionRunnable implements Runnable{
	private Socket m_clientSocket = null;
	private boolean m_isRun = true;
	private MyThread myThread;
	private Thread thread;
	
	//!!!!!REMEBER TO DELETE THIS SOCKET AND READER ITS JUST FOR DEBUGING!!!!!!!
	
	//	private Socket m_socket;
	//!!!!!!! debug!!!!!!
	public Utils m_Utils = new Utils();

	public ConnectionRunnable(MyThread myThread) {
		this.myThread = myThread;
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

	private void runTaskForClient() {		
		try {
			InputStream input  = m_clientSocket.getInputStream();
			OutputStream output = m_clientSocket.getOutputStream();
			BufferedReader m_In = new BufferedReader(new InputStreamReader(input));

			//TODO: Yafim decide if you want to delete all this or not
			//Not sure what you are doing here
			try {
				String lineToRead = "";
				String hTTPRequest = "";
				
				while((lineToRead = m_In.readLine()) != null) {
					hTTPRequest = buildHTTPRequest(hTTPRequest, lineToRead);
				}

			} catch (IOException e) {
				//TODO: figure out why we have the following comment line with to do??!
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			output.close();
			input.close();
			//			System.out.println("Request processed: " + time);
		} catch (IOException e) {
			//report exception somewhere.
			e.printStackTrace();
		}

		//Finish handling client
		m_clientSocket = null;
		myThread.onClientCommComplete();
	}

	private String buildHTTPRequest(String hTTPRequest, String i_String) throws UnsupportedEncodingException{
		hTTPRequest += i_String;
		
		if (i_String.equals("")){
			m_Utils.handleHttpRequest(hTTPRequest);
		}
		
		hTTPRequest += System.lineSeparator();
		return hTTPRequest;
	}

	public boolean isBusy() {
		return m_clientSocket != null;
	}

	/**
	 * Notifying the sleeping thread that it has a client request to handle 
	 * @param clientSocket
	 */
	public void communicate(Socket clientSocket) {		
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
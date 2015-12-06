

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;


public class ConnectionRunnable implements Runnable{
	private static int NUM = 0;
	private int myNum;
	
	private Socket m_clientSocket = null;
	private boolean m_isRun = true;
	private MyThread myThread;
	private Thread thread;
	private String m_HTTPRequest = "";
	//!!!!!REMEBER TO DELETE THIS SOCKET AND READER ITS JUST FOR DEBUGING!!!!!!!
	private BufferedReader m_In;
//	private Socket m_socket;
	//!!!!!!! debug!!!!!!
	public Utils m_Utils = new Utils();
	//private Object m_lock = new Object();
	//private boolean isBusy = false;
//	protected String serverText   = null;
	//Hopefully this push will work

	public ConnectionRunnable(MyThread myThread) {
		this.myThread = myThread;
		
		myNum = ++NUM;
//		this.clientSocket = clientSocket;
//		this.serverText   = serverText;
	}

	public synchronized void run() {
		this.thread = Thread.currentThread();
		
		while(m_isRun) {
			if (m_clientSocket != null) {
				runTaskForClient();
			}
			else {
				synchronized (thread) {
					try {
//						thread.sleep(1000);
						//System.out.println("Thread " + myNum + ": wait");
						thread.wait();
						//System.out.println("Thread " + myNum + ": notified");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
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
			//!!!!FOR DEBUGGIN !!!!!!
//			m_socket = new Socket(new String("localhost"), 8080);
			//UNTIL HERE
			 m_In = new BufferedReader(new InputStreamReader(
		                m_clientSocket.getInputStream()));
			//System.out.println("Mile stone 4");
		
			int i = 0;
			while (i < myNum){
				//System.out.println(myNum + " says: message num." + i++);
				Thread.sleep(1 * 1000);
				i++;
			}
			//!!!!FOR DEBUGGIN !!!!!!
			   try {
//				   System.out.println("The line ");
				   int counter = 0;
				   String lineToRead = "";
				   
					while((lineToRead = m_In.readLine()) != null) {
//						m_Utils.handleHttpRequest(lineToRead);
//						System.out.println(counter);
//						System.out.println(lineToRead);
						buildHTTPRequest(lineToRead);
//						m_HTTPRequest += lineToRead;
//						counter++;
//						System.out.println(m_HTTPRequest);
						
					   }
					
//					m_Utils.handleHttpRequest(m_HTTPRequest);
			//		System.out.println(lineToRead);

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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		//Finish handling client
		m_clientSocket = null;
		myThread.onClientCommComplete();
	}
	
	private boolean m_IsDone = false;
	private void buildHTTPRequest(String i_String) throws UnsupportedEncodingException{
		m_HTTPRequest += i_String;
//		System.out.println(i_String.equals(""));
		if (i_String.equals("")){
//			System.out.println(m_HTTPRequest);
			m_Utils.handleHttpRequest(m_HTTPRequest);
		}
		m_HTTPRequest += System.lineSeparator();
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
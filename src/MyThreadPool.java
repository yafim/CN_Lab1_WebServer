import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class MyThreadPool {
	private ArrayList<MyThread> m_availableThreads;
	private ArrayList<MyThread> m_busyThreads;
	private int threadsNum;
	private Queue<Socket> waitingClients;
	
	private String m_Root;
	private String m_DefaultPage;
	
	public MyThreadPool(int threadsNum, String i_Root, String i_DefaultPage) {
		//Number of threads that will be created in the thread pool
		this.threadsNum = threadsNum;
		this.waitingClients = new LinkedList<Socket>();
		this.m_Root = i_Root;
		this.m_DefaultPage = i_DefaultPage;
		initializeThreads();		
	}
		
	private void initializeThreads() {
		//Creating to array list for available and busy threads
		this.m_availableThreads = new ArrayList<MyThread>();
		this.m_busyThreads = new ArrayList<MyThread>();
		
		//Filling the threads array with 10 threads
		for(int counter = 0 ; counter < this.threadsNum; counter++) {
			//Each thread that is add is actually a Mythread object that
			//surrounds the thread, that way i can communicate between the thread pool
			//and manage my threads between being in busy thread list and available 
			//thread list
			this.m_availableThreads.add(new MyThread(this, m_Root, m_DefaultPage));
		}			
	}	
	
	private MyThread getAvailableThread() {		
		//Returning the first available thread
		synchronized (m_availableThreads) {
			if(!m_availableThreads.isEmpty()) {
				return m_availableThreads.remove(0); 
			}	
		}

		return null;
	}

	/**
	 * This method get an available thread for the client who just
	 * got connected to the server, if the is no available thread it adds
	 * him in the waiting client list. After getting a thread for the client
	 * it will send him to connection runnable there his request will be handled)
	 * @param clientSocket
	 */
	public synchronized void execute(Socket clientSocket) {
		MyThread selectedThread = getAvailableThread();
		//If no thread available add the client to the waiting list
		if (selectedThread == null) {
			synchronized (waitingClients) {
				waitingClients.add(clientSocket);
			}
			return;
		}
		//Adding the selected thread to the busy Thread list
		synchronized (m_busyThreads) {
			m_busyThreads.add(selectedThread);
		}
		selectedThread.execute(clientSocket);
	}

	//Bonus feature for shutting down the server in case we want to.
	//I have implemented this for bonus and for future use if we want to 
	//Some how insert shutting down 
	public void shutdown() {
		synchronized (m_availableThreads) {
			for (MyThread thread : m_availableThreads) {
				thread.stop();
			}	
		}
		
		synchronized (m_busyThreads) {
			for (MyThread thread : m_busyThreads) {
				thread.stop();
			}
		}		
	}

	/**
	 * This method happens when a client finish it request, when
	 * that happens we check if there a client waiting in the client list.
	 * if we have one we using the thread that just finished with the current client 
	 * to run the waiting client command. 
	 * @param myThread
	 */
	public void onClientCommComplete(MyThread myThread) {
		synchronized (waitingClients) {
			if(!waitingClients.isEmpty()) {			
				myThread.execute(waitingClients.poll());
				return;
			}
			//No client is waiting so the thread is removed from busy thread list
			synchronized (m_busyThreads) {
				m_busyThreads.remove(myThread);
			}
			//The thread is added to the available thread list
			synchronized (m_availableThreads) {
				m_availableThreads.add(myThread);
			}
			//Telling the thread to be on wait mode until we will call him.
			myThread.waitForClient();
		}
	}
}

import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class MyThreadPool {
	private ArrayList<MyThread> m_availableThreads;
	private ArrayList<MyThread> m_busyThreads;
	private int threadsNum;
	private Queue<Socket> waitingClients;
	
	public MyThreadPool(int threadsNum) {
		this.threadsNum = threadsNum;
		this.waitingClients = new LinkedList<Socket>();
		initializeThreads();		
	}
		
	private void initializeThreads() {
		this.m_availableThreads = new ArrayList<MyThread>();
		this.m_busyThreads = new ArrayList<MyThread>();
		
		//Filling the threads array with 10 threads
		for(int counter = 0 ; counter < this.threadsNum; counter++) {
			this.m_availableThreads.add(new MyThread(this));
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

	public synchronized void execute(Socket clientSocket) {
		MyThread selectedThread = getAvailableThread();
		if (selectedThread == null) {
			synchronized (waitingClients) {
				waitingClients.add(clientSocket);
			}
			return;
		}
		
		synchronized (m_busyThreads) {
			m_busyThreads.add(selectedThread);
		}
		selectedThread.execute(clientSocket);
	}

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

	public void onClientCommComplete(MyThread myThread) {
		synchronized (waitingClients) {
			if(!waitingClients.isEmpty()) {			
				myThread.execute(waitingClients.poll());
				System.out.println("Status: waitingClients - " + waitingClients.size() + ", busyThreads: " + m_busyThreads.size()+ ", availableThreads: " + m_availableThreads.size());
				
				return;
			}
			
			synchronized (m_busyThreads) {
				m_busyThreads.remove(myThread);
			}
			
			synchronized (m_availableThreads) {
				m_availableThreads.add(myThread);
			}
		
			System.out.println("Status: waitingClients - " + waitingClients.size() + ", busyThreads: " + m_busyThreads.size()+ ", availableThreads: " + m_availableThreads.size());
			
			myThread.waitForClient();
		}
	}
}

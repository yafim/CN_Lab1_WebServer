import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedClass implements Runnable {
	protected int serverPort;
	protected ServerSocket serverSocket;
	protected boolean isStopped = false;
	protected MyThreadPool threadPool;

	public MultiThreadedClass(int port){
		this.serverPort = port;
	}

	/**
	 * starting to run the server here after therad.start was activated
	 */
	public void run(){
		openServerSocket();
		while(!isStopped){
			//At the begining no client has connected so the client
			//socket is obviously null
			Socket clientSocket = null;
			try {
				//The thread that running this server stops at this line
				//and waits for a client to connect.
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.out.println("SOCKET CLOSED : " + serverSocket.isClosed());
				if(isStopped) {
					System.out.println("Server Stopped.") ;
					break;
				}
				throw new RuntimeException(
						"Error accepting client connection", e);
			}
			//The moment a client has connected it tells the thread pool 
			//to find an available thread for him from the thread pool
			this.threadPool.execute(clientSocket);            
		}
		//Just in case we would like to give an option to shut down the server i
		//implemented the shut down method.
		this.threadPool.shutdown();
		System.out.println("Server Stopped.") ;
	}

	/**
	 * Creating a thread pool for the 10 threads that will
	 * handle all clients here and starting the server. 
	 */
	public void startTheServer() {
		threadPool = new MyThreadPool(10);
		new Thread(this).start();
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	/**
	 * Creating a socket for the server
	 */
	private void openServerSocket() {
		try {
			//Port in socket is 8080 
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port 8080", e);
		}
	}
}
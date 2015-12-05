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

	public void run(){
		openServerSocket();
		while(!isStopped){
			Socket clientSocket = null;
			try {
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

			this.threadPool.execute(clientSocket);            
		}

		this.threadPool.shutdown();
		System.out.println("Server Stopped.") ;
	}

	//I am starting my server here basically 
	public void startTheServer() {
		threadPool = new MyThreadPool(10);
		new Thread(this).start();
		
		//		try {
		//			Thread.sleep(20 * 1000);
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}
		//System.out.println("Stopping Server");
		//server.stop();
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
			System.out.println("Mile Stone 2");
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port 8080", e);
		}
	}
}
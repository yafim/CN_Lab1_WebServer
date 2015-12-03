import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedClass implements Runnable {

    protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool =
        Executors.newFixedThreadPool(10);

    public MultiThreadedClass(int port){
        this.serverPort = port;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        System.out.println("Mile stone 1");
        openServerSocket();
        System.out.println("Mile stone 3");
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                System.out.println("SOCKET CLOSED : " + serverSocket.isClosed());
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
            	System.err.println(e.getMessage());
                System.out.println("SOCKET CLOSED : " + serverSocket.isClosed());
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            this.threadPool.execute(
                new ConnectionRunnable(clientSocket,
                    "Thread Pooled Server"));
        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }

    //I am starting my server here basically 
    public void startTheServer(MultiThreadedClass server) {
		new Thread(server).start();
		try {
			Thread.sleep(1 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Stopping Server");
	//	server.stop();
    }
    
    private synchronized boolean isStopped() {
        return this.isStopped;
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
import java.net.Socket;


public class MyThread {
	private Thread thread;
	private ConnectionRunnable runnable;
	private MyThreadPool myThreadPool;
	
	public MyThread(MyThreadPool myThreadPool) {
		this.myThreadPool = myThreadPool;
		
		this.runnable = new ConnectionRunnable(this);
		this.thread = new Thread(this.runnable);
		
		this.thread.start();
	}
	
	public boolean isBusy() {
		return runnable.isBusy();
	}

	public void execute(Socket clientSocket) {
		runnable.communicate(clientSocket);
	}

	public void onClientCommComplete() {
		myThreadPool.onClientCommComplete(this);
	}

	public void waitForClient() {
		synchronized (thread) {
			try {
				thread.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		this.runnable.stop();
	}
	
	
}

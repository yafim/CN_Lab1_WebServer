
public class MainTestClass {
	//!!!TEST!!! Trying to make the thread pool work.

	public static void main(String[] args) {
		MultiThreadedClass server = new MultiThreadedClass(8080);
		server.startTheServer(server);

	}

}

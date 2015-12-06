import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {
	private int port;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private String m_HTTPRequest;
	
	
	public Client(int port) {
		this.port = port;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		for(int i = 0; i < 15; i++) {
//			Client client = new Client(8080);
//			client.listenSocket();
//	}
	}
	

	public void listenSocket(){
	//Create socket connection
	   try{
	     socket = new Socket(new String("localhost"), port);
	     out = new PrintWriter(socket.getOutputStream(), 
	                 true);
	     in = new BufferedReader(new InputStreamReader(
	                socket.getInputStream()));
	   } catch (UnknownHostException e) {
	     System.out.println("Unknown host: kq6py");
	     System.exit(1);
	   } catch  (IOException e) {
	     System.out.println("No I/O");
	     System.exit(1);
	   }
	   
//	   try {
//		while((m_HTTPRequest = in.readLine()) != null) {
//			System.out.println("I am Inside while");
//			   System.out.println(m_HTTPRequest);
//		   }
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
	}

}

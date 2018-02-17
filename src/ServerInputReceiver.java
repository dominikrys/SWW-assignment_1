import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerInputReceiver extends Thread {
	
	AtomicBoolean running;
	ServerSocket serverSocket;
	
	ServerInputReceiver(AtomicBoolean _running, ServerSocket _serverSocket) {
		running = _running;
		serverSocket = _serverSocket;
	}
	
    public void run() {
    	BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
    	
        while (running.get() == true) {
        	try {
        		String userInput = inputReader.readLine().toLowerCase();
        		
        		if (userInput.equals("quit")) {
            		running.set(false);
            		serverSocket.close();
            	}
        	} catch (IOException e) {
        		System.out.println("Quitting: " + e.getMessage());
        	}
        }
    }
}

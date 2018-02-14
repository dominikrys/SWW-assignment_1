
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

	private BufferedReader server;

	ClientReceiver(BufferedReader server) {
		this.server = server;
	}

	/**
	 * Run the client receiver thread.
	 */
	public void run() {
		// Print to the user whatever we get from the server:
		try {
			while (true) {
				String s = server.readLine(); // Matches FFFFF in ServerSender.java

				if (s == null) {
					throw new NullPointerException();
				}

				System.out.println(s);
			}
		} catch (SocketException e) { // Matches HHHHH in Client.java
			Report.behaviour("Client receiver ending");
		} catch (NullPointerException | IOException e) {
			Report.errorAndGiveUp("Server seems to have died " + (e.getMessage() == null ? "" : e.getMessage()));
		}
	}
}

/*
 * 
 * The method readLine returns null at the end of the stream
 * 
 * It may throw IoException if an I/O error occurs
 * 
 * See https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html#
 * readLine--
 * 
 * 
 */

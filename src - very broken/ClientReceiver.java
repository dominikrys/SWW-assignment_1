
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

	private BufferedReader server;
	private boolean loggedIn;
	private String nickname;

	ClientReceiver(BufferedReader server) {
		this.server = server;
		loggedIn = false;
		nickname = null;
	}

	/**
	 * Run the client receiver thread.
	 */
	public void run() {
		// Print to the user whatever we get from the server:
		try {
			while (true) {
				String receivedMessage = server.readLine(); // Matches FFFFF in ServerSender.java
				
				// Check if it's a message from the server
				String sender = receivedMessage.substring(5, 11);
				
				if (sender.equals("Server")) {
					if (receivedMessage.substring(13, 16).equals("reg") ) {
						nickname = receivedMessage.substring(16, receivedMessage.length());
						receivedMessage = "Registered user " + nickname;
					}
					else if (receivedMessage.substring(13, 18).equals("login") ) {
						loggedIn = true;
						nickname = receivedMessage.substring(18, receivedMessage.length());
						receivedMessage = "Successfully logged in as " + nickname;
					}
					
				}

				// If null message, some kind of error occured
				if (receivedMessage == null) {
					throw new NullPointerException();
				}

				// Print the message
				System.out.println(receivedMessage);
			}
		} catch (SocketException e) { // Matches HHHHH in Client.java
			Report.behaviour("Client receiver ending");
		} catch (NullPointerException | IOException e) {
			Report.errorAndGiveUp("Server seems to have died " + (e.getMessage() == null ? "" : e.getMessage()));
		}
	}
	
	public boolean getLoggedInStatus() {
		return loggedIn;
	}
	
	public String getNickname() {
		return nickname;
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

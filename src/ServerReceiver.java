
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
	private String myClientsName;
	private BufferedReader myClient;
	private ClientTable clientTable;
	private ServerSender companion;

	/**
	 * Constructs a new server receiver.
	 * 
	 * @param n
	 *            the name of the client with which this server is communicating
	 * @param c
	 *            the reader with which this receiver will read data
	 * @param t
	 *            the table of known clients and connections
	 * @param s
	 *            the corresponding sender for this receiver
	 */
	public ServerReceiver(String n, BufferedReader c, ClientTable t, ServerSender s) {
		myClientsName = n;
		myClient = c;
		clientTable = t;
		companion = s;
	}

	/**
	 * Starts this server receiver.
	 */
	public void run() {
		try {
			while (true) {
				String userInput = myClient.readLine(); // Matches CCCCC in ClientSender.java

				if (userInput == null || userInput.equals("quit")) {
					// Either end of stream reached, just give up, or user wants to quit
					break;
				}

				String text = myClient.readLine(); // Matches DDDDD in ClientSender.java

				if (text != null) {
					Message msg = new Message(myClientsName, text);
					BlockingQueue<Message> recipientsQueue = clientTable.getQueue(userInput); // Matches EEEEE in
																								// ServerSender.java
					if (recipientsQueue != null) {
						recipientsQueue.offer(msg);
					} else {
						Report.error("Message for unexistent client " + userInput + ": " + text);
					}
				} else {
					// No point in closing socket. Just give up.
					return;
				}
			}
		} catch (IOException e) {
			Report.error("Something went wrong with the client " + myClientsName + " " + e.getMessage());
			// No point in trying to close sockets. Just give up.
			// We end this thread (we don't do System.exit(1)).
		}

		Report.behaviour("Server receiver ending");
		companion.interrupt();
		clientTable.remove(myClientsName);
	}
}


import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.LinkedHashMap;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
	private String myClientsName;
	private int myClientsID;
	private BufferedReader myClient;
	private ClientTable clientTable;
	private ServerSender companion;
	private boolean running;
	private ConcurrentHashMap<String, Integer> nicknameToIDMap = new ConcurrentHashMap<String, Integer>();
	private boolean loggedIn;
	private ConcurrentLinkedQueue<String> registeredUsers;
	private String newRegisteredUser;

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
	public ServerReceiver(Integer id, BufferedReader c, ClientTable t, ServerSender s,
			ConcurrentHashMap<String, Integer> _nicknameToIDMap, ConcurrentLinkedQueue<String> _registeredUsers) {
		myClientsID = id;
		myClient = c;
		clientTable = t;
		companion = s;
		nicknameToIDMap = _nicknameToIDMap;
		loggedIn = false;
		running = true;
		registeredUsers = _registeredUsers;
		newRegisteredUser = null;
	}

	public String getClientName() {
		return myClientsName;
	}

	public boolean getLoggedInStatus() {
		return loggedIn;
	}

	public int getClientID() {
		return myClientsID;
	}
	
	public String newUserAdded() {
		return newRegisteredUser;
	}
	
	public void newUserRegistered() {
		newRegisteredUser = null;
	}
	
	public void updateTables(ConcurrentHashMap<String, Integer> _nicknameToIDMap, ConcurrentLinkedQueue<String> _registeredUsers) {
		nicknameToIDMap = _nicknameToIDMap;
		registeredUsers = _registeredUsers;
	}
	
	private void sendServerMessage(String message) {
		Message msg = new Message("Server", message);

		Integer recipientID = myClientsID;

		BlockingQueue<Message> recipientsQueue = clientTable.getQueue(recipientID); // Matches EEEEE in
																					// ServerSender.java
		recipientsQueue.offer(msg);
	}

	/**
	 * Starts this server receiver.
	 */
	public void run() {
		try {
			while (running) {
				String userInput = myClient.readLine(); // Matches CCCCC in ClientSender.java

				switch (userInput) {
				case "":
				case "quit":
					// Either end of stream reached, just give up, or user wants to quit
					running = false;
					break;
				case "register":
					String nickname = myClient.readLine(); // Matches FFFFF in ServerReceiver

					if (!registeredUsers.contains(nickname)) {
						registeredUsers.add(nickname);
						System.out.println("User " + nickname + " registered.");
					} else {
						System.out.println(nickname + " is already registered.");
					}
					break;
				case "login":
					nickname = myClient.readLine(); // Matches FFFFF in ServerReceiver

					if (loggedIn == false) {
						if (registeredUsers.contains(nickname)) {
							myClientsName = nickname;
							nicknameToIDMap.put(nickname, myClientsID);
							loggedIn = true;
							System.out.println("Successfully logged in as " + nickname);
						}
//						else {
//							sendServerMessage(nickname + " isn't registered. Please register first.");
//						}
					} else {
						System.out.println("This client is already logged in to an account");
					}
					break;
				case "logout":
					// TODO
				case "previous":
					// TODO
				case "next":
					// TODO
				case "delete":
					// TODO
					// server.println(userInput); // Matches CCCCC in ServerReceiver
					break;
				case "send":
					String recipient = myClient.readLine(); // Matches DDDDD in ClientSender.java
					String text = myClient.readLine(); // Matches EEEEE in ClientSender.java

					if (text != null && nicknameToIDMap.get(recipient) != null) {
						Message msg = new Message(myClientsName, text);

						// See how many client IDs there are with of the same name but different ID to allow a a user to have multiple copies running
						Integer recipientID = nicknameToIDMap.get(recipient);

						BlockingQueue<Message> recipientsQueue = clientTable.getQueue(recipientID); // Matches EEEEE in
																									// ServerSender.java
						if (recipientsQueue != null) {
							recipientsQueue.offer(msg);
						} else {
							Report.error("Message for unexistent client " + recipient + ": " + text);
						}
					} else {
						// No point in closing socket. Just give up.
						return;
					}
					break;
				default:
					System.out.println("Command not recognised. This should never print, so there's a bug somewhere");
					break;
				}

			}
		} catch (IOException e) {
			Report.error("Something went wrong with the client " + myClientsName + " " + e.getMessage());
			// No point in trying to close sockets. Just give up.
			// We end this thread (we don't do System.exit(1)).
		}

		Report.behaviour("Server receiver ending");
		companion.interrupt();
//		clientTable.remove(myClientsName);
	}
}

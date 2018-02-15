
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
	private ConcurrentHashMap<String, ArrayList<Integer>> nicknameToIDMap = new ConcurrentHashMap<String, ArrayList<Integer>>();
	private ConcurrentHashMap<String, ArrayList<Message>> messageStore = new ConcurrentHashMap<String, ArrayList<Message>>();
	private ConcurrentHashMap<String, Integer> currentMessageMap = new ConcurrentHashMap<String, Integer>();
	private boolean loggedIn;
	private ConcurrentLinkedQueue<String> registeredUsers;

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
			ConcurrentHashMap<String, ArrayList<Integer>> _nicknameToIDMap,
			ConcurrentLinkedQueue<String> _registeredUsers, ConcurrentHashMap<String, ArrayList<Message>> _messageStore,
			ConcurrentHashMap<String, Integer> _currentMessageMap) {
		myClientsID = id;
		myClient = c;
		clientTable = t;
		companion = s;
		nicknameToIDMap = _nicknameToIDMap;
		loggedIn = false;
		running = true;
		registeredUsers = _registeredUsers;
		messageStore = _messageStore;
		currentMessageMap = _currentMessageMap;
	}

	private void sendServerMessage(String message) {
		Message msg = new Message("Server", message);

		Integer recipientID = myClientsID;

		BlockingQueue<Message> recipientsQueue = clientTable.getQueue(recipientID); // Matches EEEEE in
																					// ServerSender.java
		recipientsQueue.offer(msg);
	}

	private void sendExistingMessage(String recipient, Message inputMsg) {
		// See how many client IDs there are with of the same name but different ID to
		// allow a a user to have multiple copies running
		ArrayList<Integer> extractedIDs = new ArrayList<Integer>();
		extractedIDs = nicknameToIDMap.get(recipient);

		for (Integer recipientID : extractedIDs) {
			BlockingQueue<Message> recipientsQueue = clientTable.getQueue(recipientID); // Matches
																						// EEEEE
																						// in
																						// ServerSender.java

			if (recipientsQueue != null) {
				recipientsQueue.offer(inputMsg);
			}
		}
	}

	/**
	 * Starts this server receiver.
	 */
	public void run() {
		try {
			while (running) {
				String userInput = myClient.readLine(); // Matches CCCCC in ClientSender.java

				// In try block in case there's a nullpointer exception - case statements for
				// strings don't allow checking for null, so this is necessary

				try {
					switch (userInput) {
					case "quit":
						// Either end of stream reached, just give up, or user wants to quit
						running = false;
						break;
					case "register":
						String nickname = myClient.readLine(); // Matches FFFFF in ServerReceiver

						if (nickname != null) {
							if (!registeredUsers.contains(nickname)) {
								// Add nickname to registered users
								registeredUsers.add(nickname);

								// Initialise a new arraylist in the messageStore so that it can be used by
								// other parts of the program and won't throw nullpointer exceptions
								ArrayList<Message> initialArrayList = new ArrayList<Message>();
								messageStore.put(nickname, initialArrayList);

								System.out.println("Client " + myClientsID + ": User " + nickname + " registered.");
								sendServerMessage("User " + nickname + " registered.");
							} else {
								System.out.println("Client " + myClientsID + ": " + nickname + " is already registered.");
								sendServerMessage(nickname + " is already registered. You can log in.")
							}
						} else {
							// No point in closing socket. Just give up.
							return;
						}
						break;
					case "login":
						nickname = myClient.readLine(); // Matches FFFFF in ServerReceiver
						if (nickname != null) {
							if (loggedIn == false) {
								if (registeredUsers.contains(nickname)) {
									myClientsName = nickname;

									// Assign the client's ID to an arraylist
									ArrayList<Integer> extractedIDs = new ArrayList<Integer>();

									// If statement to avoid nullpointexception
									if (nicknameToIDMap.get(nickname) != null) {
										extractedIDs = nicknameToIDMap.get(nickname);
									}
									extractedIDs.add(myClientsID);
									nicknameToIDMap.put(nickname, extractedIDs);

									loggedIn = true;
									System.out.println(
											"Client " + myClientsID + " successfully logged in as " + nickname);
									sendServerMessage("Successfully logged in as " + nickname);
								} else {
									System.out.println("Client " + myClientsID + ": " + nickname + " isn't registered. Please register first.");
									sendServerMessage(nickname + " isn't registered. Please register the nickname first.");
								}
							} else {
								System.out.println("Client " + myClientsID + " is already logged in to an account");
								sendServerMessage("");
							}
						} else {
							// No point in closing socket. Just give up.
							return;
						}

						break;
					case "logout":
						if (loggedIn == true) {
							// Remove ID from list so it doesn't get stuff sent to it
							ArrayList<Integer> extractedIDs = nicknameToIDMap.get(myClientsName);
							extractedIDs.remove((Integer) myClientsID);

							// Store the IDs back with the removed clientID
							nicknameToIDMap.put(myClientsName, extractedIDs);

							loggedIn = false;
							System.out.println("Client " + myClientsID + " logged out of account " + myClientsName);
							myClientsName = null;
						} else {
							System.out.println("Client " + myClientsID + " not logged in, so can't be logged out");
						}
					case "previous":
						ArrayList<Message> extractedMessages;
						extractedMessages = messageStore.get(myClientsName);

						if (extractedMessages.size() == 0) {
							System.out.println("Client " + myClientsID
									+ " used command previous however no messages are stored for this nickname");
						} else if (currentMessageMap.get(myClientsName) == 0) {
							System.out.println("Client " + myClientsID
									+ " used command previous however the current message is already on the first message");
						} else {
							// Set current message to previous message
							currentMessageMap.put(myClientsName, currentMessageMap.get(myClientsName) - 1);

							// Print message
							sendExistingMessage(myClientsName,
									extractedMessages.get(currentMessageMap.get(myClientsName)));
						}
						break;
					case "next":
						extractedMessages = messageStore.get(myClientsName);

						if (extractedMessages.size() == 0) {
							System.out.println("Client " + myClientsID
									+ " used command next however no messages are stored for this nickname");
						} else if (currentMessageMap.get(myClientsName) == extractedMessages.size() - 1) {
							System.out.println("Client " + myClientsID
									+ " used command next however the current message is already on the last message");
						} else {
							// Set current message to previous message
							currentMessageMap.put(myClientsName, currentMessageMap.get(myClientsName) + 1);

							// Print message
							sendExistingMessage(myClientsName,
									extractedMessages.get(currentMessageMap.get(myClientsName)));
						}
						break;
					case "delete":
						extractedMessages = messageStore.get(myClientsName);
						int messageAmount = extractedMessages.size();

						if (messageAmount == 0) {
							System.out.println("Client " + myClientsID
									+ " used command delete however no messages are stored for this nickname");
						} else {
							// Remove message
							extractedMessages.remove((int) currentMessageMap.get(myClientsName));
							messageStore.put(myClientsName, extractedMessages);

							// Set current message to the previous value if it's the last value, otherwise
							// leave it so the current message is the next message. If there are no more
							// stored messages it doesn't matter as the value would get overriden
							if (currentMessageMap.get(myClientsName) == messageAmount - 1) {
								currentMessageMap.put(myClientsName, currentMessageMap.get(myClientsName) - 1);
							}

							System.out.println("Message removed in client " + myClientsID);
						}
						break;
					case "send":
						String recipient = myClient.readLine(); // Matches DDDDD in ClientSender.java
						if (recipient != null) {
							String text = myClient.readLine(); // Matches EEEEE in ClientSender.java

							if (text != null) {
								if (nicknameToIDMap.get(recipient) == null) {
									System.out.println("Message " + text + " to unexistent recipient " + recipient);
								} else {
									Message msg = new Message(myClientsName, text);

									// See how many client IDs there are with of the same name but different ID to
									// allow a a user to have multiple copies running
									ArrayList<Integer> extractedIDs = new ArrayList<Integer>();
									extractedIDs = nicknameToIDMap.get(recipient);

									for (Integer recipientID : extractedIDs) {
										BlockingQueue<Message> recipientsQueue = clientTable.getQueue(recipientID); // Matches
																													// EEEEE
																													// in
																													// ServerSender.java

										if (recipientsQueue != null) {
											recipientsQueue.offer(msg);
										}
									}

									// Store message in server
									extractedMessages = messageStore.get(recipient);
									if (extractedMessages.size() == 0) {
										extractedMessages = new ArrayList<Message>();
									}
									extractedMessages.add(msg);
									messageStore.put(recipient, extractedMessages);

									// Set the message that has just been sent to be the current message
									currentMessageMap.put(recipient, extractedMessages.size() - 1);
								}
							} else {
								// No point in closing socket. Just give up.
								return;
							}
						} else {
							// No point in closing socket. Just give up.
							return;
						}
						break;
					default:
						System.out
								.println("Command not recognised. This should never print, so there's a bug somewhere");
						break;
					}
				} catch (NullPointerException e) {
					running = false;
				}
			}
		} catch (IOException e) {
			Report.error("Something went wrong with the client " + myClientsName + " " + e.getMessage());
			// No point in trying to close sockets. Just give up.
			// We end this thread (we don't do System.exit(1)).
		}

		Report.behaviour("Server receiver ending");
		companion.interrupt();
		// clientTable.remove(myClientsName);
	}
}

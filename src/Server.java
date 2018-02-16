import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// The server class
// Stores data which is shared by all clients (lists of users, clients logged in to a certain user
// and starts threads for receiving data from the client and sending data to the client
//
// Usage:
// java Server

public class Server {

	/**
	 * Start the server listening for connections.
	 */
	public static void main(String[] args) {

		// Declare ClientTable which will be responsible for message queues for clients
		ClientTable clientTable = new ClientTable();

		// ConcurrentHashMap of currently logged clients and their usernames
		ConcurrentMap<String, ArrayList<Integer>> nicknameToIDMap = new ConcurrentHashMap<String, ArrayList<Integer>>();

		// ConcurrentHashMap of all registered users, boolean being whether that user is
		// currrently logged in or not
		ConcurrentMap<String, Boolean> registeredUsers = new ConcurrentHashMap<String, Boolean>();

		// ConcurrentHashMap for storing all messages
		ConcurrentMap<String, ArrayList<Message>> messageStore = new ConcurrentHashMap<String, ArrayList<Message>>();

		// ConcurrentHashMap for tracking which message is the "current" one
		ConcurrentMap<String, Integer> currentMessageMap = new ConcurrentHashMap<String, Integer>();

		ServerSocket serverSocket = null;

		// Each client is given an ID, so an initial one is declared here
		int clientID = 1;

		// Set up new server socket
		try {
			serverSocket = new ServerSocket(Port.number);
		} catch (IOException e) {
			Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
		}

		// Try catch block for IO errors
		try {
			// We loop for ever, as servers usually do.
			while (true) {
				// Listen to the socket, accepting connections from new clients:
				Socket socket = serverSocket.accept(); // Matches AAAAA in Client

				// This is so that we can use readLine():
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// Add client ID to the table and send a message to the server
				clientTable.add(clientID);
				Report.behaviour("Client " + clientID + " connected");

				// We create and start a new thread to write to the client:
				PrintStream toClient = new PrintStream(socket.getOutputStream());
				ServerSender serverSender = new ServerSender(clientTable.getQueue(clientID), toClient);
				serverSender.start();

				// We create and start a new thread to read from the client:
				ServerReceiver serverReceiver = new ServerReceiver(clientID, fromClient, clientTable, serverSender,
						nicknameToIDMap, registeredUsers, messageStore, currentMessageMap);
				serverReceiver.start();

				// Increment the client ID so the next client gets set another ID
				clientID++;

			}
		} catch (IOException e) {
			Report.error("IO error " + e.getMessage());
		}
	}
}

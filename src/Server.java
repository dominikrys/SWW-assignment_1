
// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {

	/**
	 * Start the server listening for connections.
	 */
	public static void main(String[] args) {

		// This table will be shared by the server threads:
		ClientTable clientTable = new ClientTable();

		// Map of currently logged clients and their usernames
		ConcurrentHashMap<String, ArrayList<Integer>> nicknameToIDMap = new ConcurrentHashMap<String, ArrayList<Integer>>();

		// List of all registered users
		ConcurrentLinkedQueue<String> registeredUsers = new ConcurrentLinkedQueue<String>();

		// ConcurrentHashMap for storing all messages
		ConcurrentHashMap<String, ArrayList<Message>> messageStore = new ConcurrentHashMap<String, ArrayList<Message>>();

		// ConcurrentHashMap for tracking which message is the "current" one
		ConcurrentHashMap<String, Integer> currentMessageMap = new ConcurrentHashMap<String, Integer>();

		ServerSocket serverSocket = null;

		int clientID = 1;

		try {
			serverSocket = new ServerSocket(Port.number);
		} catch (IOException e) {
			Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
		}

		try {

			// We loop for ever, as servers usually do.
			while (true) {
				// Listen to the socket, accepting connections from new clients:
				Socket socket = serverSocket.accept(); // Matches AAAAA in Client

				// This is so that we can use readLine():
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				boolean loggedIn = false;

				Report.behaviour("Client " + clientID + " connected");
				// We add the client to the table:
				clientTable.add(clientID);

				// We create and start a new thread to write to the client:
				PrintStream toClient = new PrintStream(socket.getOutputStream());
				ServerSender serverSender = new ServerSender(clientTable.getQueue(clientID), toClient);
				serverSender.start();

				// We create and start a new thread to read from the client:
				ServerReceiver serverReceiver = new ServerReceiver(clientID, fromClient, clientTable, serverSender,
						nicknameToIDMap, registeredUsers, messageStore, currentMessageMap);
				serverReceiver.start();

				clientID++;

			}
		} catch (IOException e) {
			// Lazy approach:
			Report.error("IO error " + e.getMessage());
			// A more sophisticated approach could try to establish a new
			// connection. But this is beyond the scope of this simple exercise.
		}
	}
}

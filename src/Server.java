import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
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

    // Initialize server socket
    ServerSocket serverSocket = null;

    // Declare ClientTable which will be responsible for message queues for clients
    ClientTable clientTable = new ClientTable();

    // ConcurrentHashMap of currently logged in clients and their usernames
    ConcurrentHashMap<String, ArrayList<Integer>> nicknameToIDMap =
        new ConcurrentHashMap<String, ArrayList<Integer>>();

    // ConcurrentHashMap of all registered users, boolean being whether that user is
    // currrently logged in or not
    ConcurrentHashMap<String, Boolean> registeredUsers = new ConcurrentHashMap<String, Boolean>();

    // ConcurrentHashMap for storing all messages
    ConcurrentHashMap<String, ArrayList<Message>> messageStore =
        new ConcurrentHashMap<String, ArrayList<Message>>();

    // ConcurrentHashMap for tracking which message is the "current" one
    ConcurrentHashMap<String, Integer> currentMessageMap = new ConcurrentHashMap<String, Integer>();

    // Check if there is there exists a file with registeredUsers, messageStore and
    // currentMessageMap and if there is, read from it
    try {
      File inputFile = new File("serverdata/userData.ser");
      if (inputFile.exists() && !inputFile.isDirectory()) {
        Report.behaviour("Existing userData.ser file found, reading from it now...");

        FileInputStream fileInputStream = new FileInputStream(inputFile);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        // Read each ConcurrentHashMap that has been saved
        registeredUsers = (ConcurrentHashMap<String, Boolean>) objectInputStream.readObject();
        messageStore =
            (ConcurrentHashMap<String, ArrayList<Message>>) objectInputStream.readObject();
        currentMessageMap = (ConcurrentHashMap<String, Integer>) objectInputStream.readObject();
        nicknameToIDMap = (ConcurrentHashMap<String, ArrayList<Integer>>) objectInputStream.readObject();

        // Close streams
        objectInputStream.close();
        fileInputStream.close();
        Report.behaviour("userData.ser file read successfully!");
      } else {
        Report.behaviour("No prior server data detected. Skipping read.");
      }
    } catch (IOException e) {
      Report.error("IOException occured when trying to read userData.ser file: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      Report.error("ClassNotFound exception occured when trying to read userData.ser file: "
          + e.getMessage());
    }

    // Each client is given an ID, so an initial one is declared here
    int clientID = 1;

    // Set an AtomicBoolean as the running flag. AtomicBoolean used here due to the
    // ServerInputReceiver being able to modify it
    AtomicBoolean running = new AtomicBoolean(true);

    // Set up new server socket
    try {
      serverSocket = new ServerSocket(Port.number);
    } catch (IOException e) {
      Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
    }

    // Start the ServerInputReceiver thread for handling user input to server
    (new ServerInputReceiver(running, serverSocket)).start();

    // Try catch block for IO errors
    try {
      Report.behaviour(
          "Server started, listening to connections now. To quit server, type \"quit\".");

      // We loop for ever, as servers usually do.
      while (running.get() == true) {
        // Listen to the socket, accepting connections from new clients:
        Socket socket = serverSocket.accept(); // Matches AAAAA in Client

        // This is so that we can use readLine():
        BufferedReader fromClient =
            new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Add client ID to the table and send a message to the server
        clientTable.add(clientID);
        Report.behaviour("Client " + clientID + " connected");

        // We create and start a new thread to write to the client:
        PrintStream toClient = new PrintStream(socket.getOutputStream());
        ServerSender serverSender = new ServerSender(clientTable.getQueue(clientID), toClient);
        serverSender.start();

        // We create and start a new thread to read from the client:
        ServerReceiver serverReceiver = new ServerReceiver(clientID, fromClient, clientTable,
            serverSender, nicknameToIDMap, registeredUsers, messageStore, currentMessageMap);
        serverReceiver.start();

        // Increment the client ID so the next client gets set another ID
        clientID++;
      }
    } catch (IOException e) {
      Report.error("IO error " + e.getMessage() + ". Server possibly ended by request.");
    }

    // After main method ended, save registeredUsers, messageStore and
    // currentMessageMap to a file
    try {
      // Create the directory and file
      File parentDirectory = new File("serverdata/");
      parentDirectory.mkdirs();
      File outputFile = new File(parentDirectory, "userData.ser");
      outputFile.createNewFile();

      // Write objects to file
      FileOutputStream fileOut = new FileOutputStream(outputFile);
      ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
      outStream.writeObject(registeredUsers);
      outStream.writeObject(messageStore);
      outStream.writeObject(currentMessageMap);
      outStream.writeObject(nicknameToIDMap);
      Report.behaviour(
          "All ConcurrentHashMaps written to serverdata/userdata.ser");

      // Close streams
      outStream.close();
      fileOut.close();
    } catch (IOException e) {
      Report.error("IOException: " + e.getMessage());
    }
  }
}

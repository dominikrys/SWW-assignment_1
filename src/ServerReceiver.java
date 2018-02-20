import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.LinkedHashMap;

// Gets messages from client and puts them in a queue, for another thread to forward to the
// appropriate client.

public class ServerReceiver extends Thread {
  // Declare variables
  private int myClientsID;
  private boolean running;
  private boolean loggedIn;
  private String myClientsName;
  private BufferedReader myClient;
  private ClientTable clientTable;
  private ServerSender companion;
  private ConcurrentHashMap<String, ArrayList<Integer>> nicknameToIDMap =
      new ConcurrentHashMap<String, ArrayList<Integer>>();
  private ConcurrentHashMap<String, ArrayList<Message>> messageStore =
      new ConcurrentHashMap<String, ArrayList<Message>>();
  private ConcurrentHashMap<String, Integer> currentMessageMap =
      new ConcurrentHashMap<String, Integer>();
  private ConcurrentHashMap<String, Boolean> registeredUsers;

  /**
   * The constructor
   * 
   * @param id The ID of the client
   * @param clientReader The BufferedReader which receives messages from ClientSender
   * @param table The ClientTable which stores message queues for all
   * @param serverSender The ServerSender that correspons to this object instance
   * @param _nicknameToIDMap ConcurrentHashMap that stores all client IDs associated with each
   *        nickname
   * @param _registeredUsers ConcurrentHashMap that stores all registered users and whether they're
   *        logged in or not
   * @param _messageStore ConcurrentHashMap that stores the messages of every client
   * @param _currentMessageMap ConcurrentHashMap that stores each nikname's "current" message
   */
  public ServerReceiver(Integer id, BufferedReader clientReader, ClientTable table,
      ServerSender serverSender, ConcurrentHashMap<String, ArrayList<Integer>> _nicknameToIDMap,
      ConcurrentHashMap<String, Boolean> _registeredUsers,
      ConcurrentHashMap<String, ArrayList<Message>> _messageStore,
      ConcurrentHashMap<String, Integer> _currentMessageMap) {
    myClientsID = id;
    myClient = clientReader;
    clientTable = table;
    companion = serverSender;
    nicknameToIDMap = _nicknameToIDMap;
    registeredUsers = _registeredUsers;
    messageStore = _messageStore;
    currentMessageMap = _currentMessageMap;
    loggedIn = false;
    running = true;
  }

  /**
   * Method for sending messages back to the client that has just sent something to the server that
   * would say they're from the server
   */
  private void sendServerMessage(String message) {
    // Construct a message to send to the client
    Message msg = new Message("Server", message);

    // Get the recipient's ID and add the constructed message to its message queue
    Integer recipientID = myClientsID;
    BlockingQueue<Message> recipientsQueue = clientTable.getQueue(recipientID); // Matches EEEEE in
                                                                                // ServerSender.java
    recipientsQueue.offer(msg);
  }

  /*
   * Method for sending a message that has already been sent before and is being stored on the
   * server
   */
  private void sendExistingMessage(String recipient, Message inputMsg) {
    // Get all client IDs currently logged in to the specified nickname
    ArrayList<Integer> extractedIDs = new ArrayList<Integer>();
    extractedIDs = nicknameToIDMap.get(recipient);

    // Send each client which is logged in as the recipient the messaage
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

  /*
   * Method which handles logging the user out. Necessary to be a standalone method as this is
   * called when the user quits and when it logs out
   */
  private void logout() {
    // Remove client ID from the list of client IDs associated with the name this
    // client is logged in to
    ArrayList<Integer> extractedIDs = nicknameToIDMap.get(myClientsName);
    extractedIDs.remove((Integer) myClientsID);
    nicknameToIDMap.put(myClientsName, extractedIDs);

    // Set logged in to false if no more instances clients are connected as this
    // nickname and notify the user and server about behaviour
    if (extractedIDs.size() == 0) {
      registeredUsers.put(myClientsName, false);
    }
    Report.behaviour("Client " + myClientsID + " logged out of account " + myClientsName);
    sendServerMessage("Logged out of account " + myClientsName);
    loggedIn = false;
    myClientsName = null;
  }

  /**
   * Starts this server receiver.
   */
  public void run() {
    // Try catch for IOExceptions which may occur if ClientSender dies
    try {
      // Loop receiving requests from ClientSender
      while (running) {
        // Get the initial command
        String userInput = myClient.readLine(); // Matches CCCCC in ClientSender.java

        // In try block in case there's a nullpointer exception - case statements for
        // strings don't allow checking for null, so this is necessary
        try {
          switch (userInput) {
            case "quit":
              // Log the user out and quit the program
              if (loggedIn == true) {
                logout();
              }
              Report.behaviour("Client " + myClientsID + " has quit by request");
              running = false;
              break;
            case "register":
              // Get the nickname from ClientSender
              String nickname = myClient.readLine(); // Matches FFFFF in ServerReceiver

              // Check if nickname is null in case the stream ended
              if (nickname != null) {
                // Disallow the name "Server" as some messages are sent from the server, and
                // empty strings
                if (!(nickname.equals("") || nickname.toLowerCase().equals("server"))) {
                  if (loggedIn == false) {
                    // Check if user is registered
                    if (!registeredUsers.containsKey(nickname)) {
                      // Add nickname to registered users
                      registeredUsers.put(nickname, false);

                      // Initialise a new arraylist in the messageStore so that it can be used by
                      // other parts of the program and won't throw nullpointer exceptions
                      ArrayList<Message> initialArrayList = new ArrayList<Message>();
                      messageStore.put(nickname, initialArrayList);

                      // Set current message to -1
                      currentMessageMap.put(nickname, -1);

                      // Notify the server and user of behaviour
                      Report.behaviour(
                          "Client " + myClientsID + ": User " + nickname + " registered.");
                      sendServerMessage("User " + nickname + " registered.");
                    } else {
                      // Notify the server and user of error
                      Report.error(
                          "Client " + myClientsID + ": " + nickname + " is already registered.");
                      sendServerMessage(nickname + " is already registered. You can log in.");
                    }
                  } else {
                    // Notify the server and user of error
                    Report.error(
                        "Client " + myClientsID + ": tried to register when already logged in");
                    sendServerMessage("Can't register as as this client is logged in already");
                  }
                } else {
                  // Notify the server and user of error
                  Report.error("Client " + myClientsID
                      + ": tried to register as a nickname that's not allowed");
                  sendServerMessage("The username \"Server\" and empty names are not allowed");
                }
              } else {
                // No point in closing socket. Just give up.
                logout();
                return;
              }
              break;
            case "login":
              // Read the nickname
              nickname = myClient.readLine(); // Matches FFFFF in ServerReceiver

              // Check if nickname is null in case the stream ended
              if (nickname != null) {
                if (loggedIn == false) {
                  // Check if the nickname is registered
                  if (registeredUsers.containsKey(nickname)) {
                    // Set this client's name to the entered name
                    myClientsName = nickname;

                    // Assign the client's ID to an arraylist
                    ArrayList<Integer> extractedIDs = new ArrayList<Integer>();
                    // If statement to avoid nullpointexception
                    if (nicknameToIDMap.get(nickname) != null) {
                      extractedIDs = nicknameToIDMap.get(nickname);
                    }
                    extractedIDs.add(myClientsID);
                    nicknameToIDMap.put(nickname, extractedIDs);

                    // Set logged in to true and add the user to the registered users list
                    loggedIn = true;
                    registeredUsers.put(myClientsName, true);
                    Report.behaviour(
                        "Client " + myClientsID + " successfully logged in as " + nickname);
                    sendServerMessage("Successfully logged in as " + nickname);

                    // Check if any messages have been sent to this client if they have logged in
                    // before but have received messages while being logged out
                    ArrayList<Message> extractedMessages = messageStore.get(myClientsName);
                    // Check if the current message isn't -1 (it gets set to this if it has only
                    // just been registered)
                    if (currentMessageMap.get(myClientsName) != -1) {
                      // Check which the last message that was read was and print any that haven't
                      // been read by placing them in the message queue
                      if (currentMessageMap.get(myClientsName) != extractedMessages.size() - 1) {
                        sendServerMessage("You've missed these messages while being logged out: ");
                        while (currentMessageMap.get(myClientsName) != extractedMessages.size()
                            - 1) {
                          currentMessageMap.put(myClientsName,
                              currentMessageMap.get(myClientsName) + 1);

                          sendExistingMessage(myClientsName,
                              extractedMessages.get(currentMessageMap.get(myClientsName)));
                        }
                      }
                    }
                  } else {
                    // Notify the server and user of any errors
                    Report.error("Client " + myClientsID + ": " + nickname
                        + " isn't registered. Please register first.");
                    sendServerMessage(
                        nickname + " isn't registered. Please register the nickname first.");
                  }
                } else {
                  // Notify the server and user of any errors
                  Report.error("Client " + myClientsID + " is already logged in to an account.");
                  sendServerMessage("This client is already logged in to account " + myClientsName);
                }
              } else {
                // No point in closing socket. Just give up.
                logout();
                return;
              }

              break;
            case "logout":
              if (loggedIn == true) {
                logout();
              } else {
                // Notify the server and user of any errors
                Report.error("Client " + myClientsID + " not logged in, so can't be logged out");
                sendServerMessage("This client isn't logged in, so can't be logged out.");
              }
              break;
            case "previous":
              if (loggedIn == true) {
                // Get the user's mesages stored on the server
                ArrayList<Message> extractedMessages = messageStore.get(myClientsName);

                // Check if it's possible to see the previous message and print out it out,
                // otherwise notify the server and user
                if (extractedMessages.size() == 0) {
                  Report.error("Client " + myClientsID
                      + " used command previous however no messages are stored for this nickname");
                  sendServerMessage(
                      "Can't use the previous command as no messages are currently stored for this user");
                } else if (currentMessageMap.get(myClientsName) == 0) {
                  Report.error("Client " + myClientsID
                      + " used command previous however the current message is already on the oldest message");
                  sendServerMessage("Already on the oldest message, can't go back further");
                } else {
                  // Set current message to previous message
                  currentMessageMap.put(myClientsName, currentMessageMap.get(myClientsName) - 1);

                  // Print message
                  sendExistingMessage(myClientsName,
                      extractedMessages.get(currentMessageMap.get(myClientsName)));
                }
              } else {
                // Notify the server and user of any errors
                Report.error("Client " + myClientsID + ": used command previous but not logged in");
                sendServerMessage("Can't use the command previous as this client is not logged in");
              }
              break;
            case "next":
              if (loggedIn == true) {
                // Get the user's mesages stored on the server
                ArrayList<Message> extractedMessages = messageStore.get(myClientsName);

                // Check if it's possible to see the next message and print out it out,
                // otherwise notify the server and user
                if (extractedMessages.size() == 0) {
                  Report.error("Client " + myClientsID
                      + " used command next however no messages are stored for this nickname");
                  sendServerMessage(
                      "Can't use the next command as no messages are currently stored for this user");
                } else if (currentMessageMap.get(myClientsName) == extractedMessages.size() - 1) {
                  Report.error("Client " + myClientsID
                      + " used command next however the current message is already on the newest message");
                  sendServerMessage("Already on the newest message, can't see any newer ones");
                } else {
                  // Set current message to previous message
                  currentMessageMap.put(myClientsName, currentMessageMap.get(myClientsName) + 1);

                  // Print message
                  sendExistingMessage(myClientsName,
                      extractedMessages.get(currentMessageMap.get(myClientsName)));
                }
              } else {
                // Notify the server and user of any errors
                Report.error("Client " + myClientsID + ": used command next but not logged in");
                sendServerMessage("Can't use the command next as this client is not logged in");
              }
              break;
            case "current":
              if (loggedIn == true) {
                // Get the user's mesages stored on the server
                ArrayList<Message> extractedMessages = messageStore.get(myClientsName);

                // Check if it's possible to see the next message and print out it out,
                // otherwise notify the server and user
                if (extractedMessages.size() == 0) {
                  Report.error("Client " + myClientsID
                      + " used command current however no messages are stored for this nickname");
                  sendServerMessage(
                      "Can't use the current command as no messages are currently stored for this user");
                } else {
                  // Print current message
                  sendExistingMessage(myClientsName,
                      extractedMessages.get(currentMessageMap.get(myClientsName)));
                }
              } else {
                // Notify the server and user of any errors
                Report.error("Client " + myClientsID + ": used command current but not logged in");
                sendServerMessage("Can't use the command current as this client is not logged in");
              }
              break;
            case "delete":
              if (loggedIn == true) {
                // Get the user's mesages stored on the server
                ArrayList<Message> extractedMessages = messageStore.get(myClientsName);
                int messageAmount = extractedMessages.size();

                // Check if it's possibly to delete the message, and delete it if so
                if (messageAmount == 0) {
                  Report.error("Client " + myClientsID
                      + " used command delete however no messages are stored for this nickname");
                  sendServerMessage(
                      "Can't use delete as no messages are currently stored for this user");
                } else {
                  // Remove message
                  extractedMessages.remove((int) currentMessageMap.get(myClientsName));
                  messageStore.put(myClientsName, extractedMessages);

                  // Set current message to the previous value if it's the last value, otherwise
                  // leave it so the current message is the next message. If there are no more
                  // stored messages set the value to -1
                  if (currentMessageMap.get(myClientsName) == messageAmount - 1) {
                    currentMessageMap.put(myClientsName, currentMessageMap.get(myClientsName) - 1);
                  } else if (messageAmount == 1) {
                    currentMessageMap.put(myClientsName, -1);
                  }

                  // Notify the server and user of behaviour
                  Report.behaviour("Message removed in client " + myClientsID);
                  sendServerMessage("Current message removed");
                }
              } else {
                // Notify the server and user of any errors
                Report.error("Client " + myClientsID + ": used command delete but not logged in");
                sendServerMessage("Can't use the command delete as this client is not logged in");
              }
              break;
            case "send":
              // Get recipient from clientSender and check if null in case stream ended
              String recipient = myClient.readLine(); // Matches DDDDD in ClientSender.java

              if (recipient != null) {
                // Get text from clientSender and check if null in case stream ended
                String text = myClient.readLine(); // Matches EEEEE in ClientSender.java

                if (text != null) {
                  if (loggedIn == true) {
                    // Check if the recipient exists
                    if (registeredUsers.containsKey(recipient) == false) {
                      Report.error("Message " + text + " to unexistent recipient " + recipient);
                      sendServerMessage("Message sent to a nonexistent recipient " + recipient);
                    } else {
                      // Create a message object with the appropriate information
                      Message msg = new Message(myClientsName, text);

                      // If the user is logged in extract IDs and put message in blocking queue. See
                      // how many client IDs there are with of the same name but different ID to
                      // allow a a user to have multiple copies running (i.e. all client IDs logged
                      // in to the same account will receive the message)
                      if (registeredUsers.get(recipient) == true) {
                        ArrayList<Integer> extractedIDs = new ArrayList<Integer>();
                        extractedIDs = nicknameToIDMap.get(recipient);

                        for (Integer recipientID : extractedIDs) {
                          BlockingQueue<Message> recipientsQueue =
                              clientTable.getQueue(recipientID); // Matches EEEEE in
                                                                 // ServerSender.java

                          if (recipientsQueue != null) {
                            recipientsQueue.offer(msg);
                          }
                        }
                      }

                      // Store message in server
                      ArrayList<Message> extractedMessages = messageStore.get(recipient);
                      if (extractedMessages.size() == 0) {
                        extractedMessages = new ArrayList<Message>();
                      }
                      extractedMessages.add(msg);
                      messageStore.put(recipient, extractedMessages);

                      // Set the message that has just been sent to be the current message and notify user
                      if (registeredUsers.get(recipient) == true) {
                        currentMessageMap.put(recipient, extractedMessages.size() - 1);
                        
                        sendServerMessage("Message sent to " + recipient);
                      } else {
                        sendServerMessage(recipient + " is currently logged out, the message will be delivered once they log back in.");
                      }
                      
                      // Notify server
                      Report.behaviour(myClientsName + " sent a message to " + recipient);
                      
                    }
                  } else {
                    // Notify server and client of errors
                    Report.error("Client " + myClientsID + ": used command send but not logged in");
                    sendServerMessage("Can't use the command send as this client is not logged in");
                  }

                } else {
                  // No point in closing socket. Just give up.
                  logout();
                  return;
                }
              } else {
                // No point in closing socket. Just give up.
                logout();
                return;
              }
              break;
            default:
              Report.error(
                  "Command not recognised. This should never print, so there's a bug somewhere");
              break;
          }
        } catch (NullPointerException e) {
          Report.error("NullPointer exception: " + e.getMessage());
          logout();
          running = false;
        }
      }
    } catch (IOException e) {
      Report.error("Something went wrong with the client " + myClientsName + " " + e.getMessage());
      logout();
      // No point in trying to close sockets. Just give up.
    }

    // Interrupt the companion thread when this thread ends
    Report.behaviour("Server receiver ending for client " + myClientsID);
    companion.interrupt();
  }
}

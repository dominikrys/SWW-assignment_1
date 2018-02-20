import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * ServerInputReciever class used to handle user input to the server
 */

public class ServerInputReceiver extends Thread {

  AtomicBoolean running;
  ServerSocket serverSocket;

  /**
   * The constructor
   * 
   * @param _running The AtomicBoolean that if it's true, the server runs
   * @param _serverSocket The serverSocket that will be closed if the user enters "quit"
   */
  ServerInputReceiver(AtomicBoolean _running, ServerSocket _serverSocket) {
    running = _running;
    serverSocket = _serverSocket;
  }

  @Override
  public void run() {
    // Set up BufferedReader to get input from the user
    BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    // Keep looping and checking for input
    while (running.get() == true) {
      try {
        String userInput = inputReader.readLine().toLowerCase();

        // If user input is "quit", close the socket the server is currently listening
        // to and set the running atomic boolean to false
        if (userInput.equals("quit")) {
          running.set(false);
          serverSocket.close();
          Report.behaviour(
              "Quit request received, server will quit when all connected clients have quit...");
        } else {
          Report.error("Command " + userInput + " not recognised");
        }
      } catch (IOException e) {
        System.out.println("Quitting: " + e.getMessage());
      }
    }
  }
}


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

// Repeatedly reads recipient's nickname and text from the user in two
// separate lines, sending them to the server (read by ServerReceiver
// thread).

public class ClientSender extends Thread {

  private String nickname;
  private PrintStream server;

  ClientSender(String nickname, PrintStream server) {
    this.nickname = nickname;
    this.server = server;
  }

  /**
   * Start ClientSender thread.
   */
  public void run() {
    // So that we can use the method readLine:
    BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

    try {
      // Then loop forever sending messages to recipients via the server:
      while (true) {
        String userInput = user.readLine();

        if (userInput.toLowerCase().equals("quit")) {
          server.println(userInput);
          break;
        }
        
        //register, login, logout, send, previous, next, delete
        
        // Disallow null recipients
        if (userInput.equals("")) {
        	System.out.println("Recipient cannot be null");
        } else {
        	server.println(userInput); // Matches CCCCC in ServerReceiver
        	String text = user.readLine();
            server.println(text);      // Matches DDDDD in ServerReceiver
        }
        
      }
    } catch (IOException e) {
      Report.errorAndGiveUp("Communication broke in ClientSender"
                        + e.getMessage());
    }

    Report.behaviour("Client sender thread ending"); // Matches GGGGG in Client.java
  }
}

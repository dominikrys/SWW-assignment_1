import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

// Thread for sending data from the client to the server

public class ClientSender extends Thread {

	private PrintStream server;
	private boolean running;

	/**
	 * The constructor
	 * 
	 * @param server
	 *            print stream to send data to
	 */
	ClientSender(PrintStream server) {
		this.server = server;
		running = true;
	}

	/**
	 * Start ClientSender thread.
	 */
	public void run() {
		// So that we can use the method readLine:
		BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

		// Tell the user how to use the program
		System.out.println(
				"Welcome to the chat app! Allowed commands: register, login, logout, send, previous, next, delete, quit");

		// Try block in case communication breaks - i.e. client closed for some reason
		try {
			// Loop sending requests to ServerReceiver
			while (running) {
				// readLine reads the inital command - register, login, logout, send, previous,
				// next, delete, quit. Cases are ignored.
				String userInput = user.readLine().toLowerCase();

				// According to what has been input, ask the user for the right amount of input
				// following the initial command
				switch (userInput) {
				case "quit":
					server.println(userInput); // Matches CCCCC in ServerReceiver
					running = false;
					break;
				case "register":
				case "login":
					server.println(userInput); // Matches CCCCC in ServerReceiver
					String username = user.readLine();
					server.println(username); // Matches FFFFF in ServerReceiver
					break;
				case "logout":
				case "previous":
				case "next":
				case "delete":
					server.println(userInput); // Matches CCCCC in ServerReceiver
					break;
				case "send":
					server.println(userInput); // Matches CCCCC in ServerReceiver
					String recipient = user.readLine();
					server.println(recipient); // Matches DDDDD in ClientSender.java
					String text = user.readLine();
					server.println(text); // Matches EEEEE in ClientSender.java
					break;
				default:
					Report.error("Command not recognised. Please enter one of register, login, logout, send, "
							+ "previous, next, delete, quit");
					break;
				}
			}
		} catch (IOException e) {
			Report.errorAndGiveUp("Communication broke in ClientSender" + e.getMessage());
		}

		Report.behaviour("Client sender thread ending"); // Matches GGGGG in Client.java
	}
}

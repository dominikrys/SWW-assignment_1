
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
	private boolean running;
	private boolean loggedIn;

	ClientSender(PrintStream server) {
		this.server = server;
		running = true;
		loggedIn = false;
	}

	public void setNickname(String _nickname) {
		nickname = _nickname;
		loggedIn = true;
	}

	/**
	 * Start ClientSender thread.
	 */
	public void run() {
		// So that we can use the method readLine:
		BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

		try {
			// Then loop forever sending messages to recipients via the server:
			while (running) {
				// readLine reads the inital command - register, login, logout, send, previous,
				// next, delete, quit
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
					if (loggedIn == true) {
						System.out
								.println("Can't use the command " + userInput + " as there's a user already logged in");
					} else {
						server.println(userInput); // Matches CCCCC in ServerReceiver
						String username = user.readLine();
						server.println(username); // Matches FFFFF in ServerReceiver
						// if (username.equals("")) {
						// System.out.println("User cannot be null. Try another command.");
						// }
						break;
					}
					break;
				case "logout":
				case "previous":
				case "next":
				case "delete":
					if (loggedIn == true) {
						server.println(userInput); // Matches CCCCC in ServerReceiver
					} else {
						System.out.println("No user logged in, thereforce can't run the command " + userInput);
					}
					break;
				case "send":
					if (loggedIn == true) {
						server.println(userInput); // Matches CCCCC in ServerReceiver
						String recipient = user.readLine();
						server.println(recipient); // Matches DDDDD in ClientSender.java
						if (recipient.equals("")) {
							System.out.println("Recipient cannot be null");
						} else {
							String text = user.readLine();
							server.println(text); // Matches EEEEE in ClientSender.java
						}
					} else {
						System.out.println("No user logged in, therefore can't run the command " + userInput);
					}
					break;
				default:
					System.out.println("Command not recognised. Please enter one of register, login, logout, send, "
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

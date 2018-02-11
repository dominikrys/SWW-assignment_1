
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

	ClientSender(String nickname, PrintStream server) {
		this.nickname = nickname;
		this.server = server;
		running = true;
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
					server.println(userInput); // Matches CCCCC in ServerReceiver
					userInput = user.readLine();
					server.println(userInput);
					if (userInput.equals("")) {
						System.out.println("User cannot be null. Try another command.");
					} else {
						userInput = user.readLine();
						server.println(userInput);
					}
				case "logout":
				case "previous":
				case "next":
				case "delete":
					server.println(userInput); // Matches CCCCC in ServerReceiver
				case "send":
					server.println(userInput); // Matches CCCCC in ServerReceiver
					userInput = user.readLine();
					server.println(userInput);
					if (userInput.equals("")) {
						System.out.println("Recipient cannot be null");
					} else {
						String text = user.readLine();
						server.println(text);
					}
				default:
					System.out.println("Command not recognised. Please enter one of register, login, logout, send, "
							+ "previous, next, delete, quit");
				}
			}
		} catch (IOException e) {
			Report.errorAndGiveUp("Communication broke in ClientSender" + e.getMessage());
		}

		Report.behaviour("Client sender thread ending"); // Matches GGGGG in Client.java
	}
}

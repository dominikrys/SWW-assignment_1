import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

// The Client class
// Runs 2 threads, one for receiving data from the server and one for sending data to the server

// Usage:
// java Client server-hostname

class Client {

	public static void main(String[] args) {

		// Check correct usage
		if (args.length != 1) {
			Report.errorAndGiveUp("Usage: java Client server-hostname");
		}

		// Initialize hostname
		String hostname = args[0];

		// Open sockets:
		PrintStream toServer = null;
		BufferedReader fromServer = null;
		Socket server = null;

		try {
			server = new Socket(hostname, Port.number); // Matches AAAAA in Server.java
			toServer = new PrintStream(server.getOutputStream());
			fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
		} catch (UnknownHostException e) {
			Report.errorAndGiveUp("Unknown host: " + hostname);
		} catch (IOException e) {
			Report.errorAndGiveUp("The server doesn't seem to be running " + e.getMessage());
		}

		// Create a thread for sending to and a thread for receiving from the server
		ClientSender sender = new ClientSender(toServer);
		ClientReceiver receiver = new ClientReceiver(fromServer);

		// Run them in parallel
		sender.start();
		receiver.start();

		// Wait for them to end and close sockets.
		try {
			sender.join(); // Waits for ClientSender.java to end. Matches GGGGG.
			Report.behaviour("Client sender ended");
			toServer.close(); // Will trigger SocketException
			fromServer.close(); // (matches HHHHH in ClientServer.java).
			server.close(); // https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html#close()
			receiver.join();
			Report.behaviour("Client receiver ended");
		} catch (IOException e) {
			Report.errorAndGiveUp("Something wrong " + e.getMessage());
		} catch (InterruptedException e) {
			Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
		}
		Report.behaviour("Client ended. Goodbye.");
	}
}


// Usage:
//        java Client user-nickname server-hostname
//
// After initializing and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// A limitation of our implementation is that there is no provision
// for a client to end after we start it. However, we implemented
// things so that pressing ctrl-c will cause the client to end
// gracefully without causing the server to fail.
//
// Another limitation is that there is no provision to terminate when
// the server dies.

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

class Client {

	public static void main(String[] args) {

		// Check correct usage:
		if (args.length != 2) {
			Report.errorAndGiveUp("Usage: java Client user-nickname server-hostname");
		} else if (args[0].toLowerCase().equals("quit")) {
			Report.errorAndGiveUp("Name of client can't be \"quit\"");
		}

		// Initialize information:
		String nickname = args[0];
		String hostname = args[1];

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

		// Tell the server what my nickname is:
		toServer.println(nickname); // Matches BBBBB in Server.java

		// Create two client threads of a diferent nature:
		ClientSender sender = new ClientSender(nickname, toServer);
		ClientReceiver receiver = new ClientReceiver(fromServer);

		// Run them in parallel:
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

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;

// Thread which reads the message queue for each clients and forwards to the appropriate user

public class ServerSender extends Thread {
	private BlockingQueue<Message> clientQueue;
	private PrintStream client;

	/**
	 * Constructs a new server sender.
	 * 
	 * @param q
	 *            messages from this queue will be sent to the client
	 * @param c
	 *            the stream used to send data to the client
	 */
	public ServerSender(BlockingQueue<Message> q, PrintStream c) {
		clientQueue = q;
		client = c;
	}

	/**
	 * Starts this server sender.
	 */
	public void run() {
		
		// Try catch block for checking whether the stream is interrupted
		try {
			// Continuously loop, reading messages from the queue and sending to
			// ClientReceiver
			while (true) {
				Message msg = clientQueue.take(); // Matches EEEEE in ServerReceiver
				client.println(msg); // Matches FFFFF in ClientReceiver
			}
		} catch (InterruptedException e) {
			Report.behaviour("Server sender ending");
		}
	}
}

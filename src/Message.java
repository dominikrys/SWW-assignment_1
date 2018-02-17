import java.io.Serializable;

// The Message class

public class Message implements Serializable{

	// Variables of which each message is made of
	private final String sender;
	private final String text;

	/**
	 * Constructs a new server sender.
	 * 
	 * @param sender
	 *            The client that is sending the message
	 * @param text
	 *            Contents of the message
	 */
	Message(String sender, String text) {
		this.sender = sender;
		this.text = text;
	}

	// Return the sender of the message
	public String getSender() {
		return sender;
	}

	// Return the contents of the message
	public String getText() {
		return text;
	}

	// toString method necessary for when instances of message are passed to a
	// printstream
	public String toString() {
		return "From " + sender + ": " + text;
	}
}

// Each nickname has a different incoming-message queue.

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientTable {

	private ConcurrentMap<Integer, BlockingQueue<Message>> queueTable = new ConcurrentHashMap<>();

	// The following overrides any previously existing nickname, and
	// hence the last client to use this nickname will get the messages
	// for that nickname, and the previously exisiting clients with that
	// nickname won't be able to get messages. Obviously, this is not a
	// good design of a messaging system. So I don't get full marks:

	public void add(Integer clientID) {
		queueTable.put(clientID, new LinkedBlockingQueue<Message>());
	}

	// Returns null if the nickname is not in the table:
	public BlockingQueue<Message> getQueue(Integer clientID) {
		return queueTable.get(clientID);
	}

	// Removes from table:
	public void remove(Integer clientID) {
		queueTable.remove(clientID);
	}
}

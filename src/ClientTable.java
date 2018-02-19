import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

// ClientTable class for handling queues of messages for clients

public class ClientTable {

  // ConcurrentMap of queues shared by all clients
  private ConcurrentMap<Integer, BlockingQueue<Message>> queueTable = new ConcurrentHashMap<>();

  // Method for adding new clients
  public void add(Integer clientID) {
    queueTable.put(clientID, new LinkedBlockingQueue<Message>());
  }

  // Get a client's queue and return null if it doesn't exist
  public BlockingQueue<Message> getQueue(Integer clientID) {
    return queueTable.get(clientID);
  }

  // Remove client from table
  public void remove(Integer clientID) {
    queueTable.remove(clientID);
  }
}

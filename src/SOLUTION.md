# Solution
##### GitLab repo: https://git.cs.bham.ac.uk/dxr714/SWW-assignment_1
 
This explains the different approaches I've taken when writing the program. I will skip talking about the functionality that was already there from the example.



### Client 
Now run using `java Client server-hostname` instead of also specifying the name. The Constructor for `ClientSender` has been changed to not take the client name now.



### ClientSender
* Prints a message when the client is run to tell the user what commands can be used - makes it easier for the user.

* While loop now runs when the `running` boolean is true. This is because all the commands the user can input are checked in a switch block, and a break in those wouldn't be able to break the loop.

* Switch block that checks for what command has been input essentially only handles the amount of input that has to follow up the initial command (e.g. `send` needs 2 more inputs (the recipient and the text) and `previous` doesn't need any extra).



### Server
* The main functionality of sending messages to a certain username has changed. Since the server doesn't know what a client's name is when a client connects, instead each client is assigned an ID. Then, if a message is sent, the recipient's nickname is looked up in a ConcurrentHashMap which stored nicknames and corresponding IDs and sends the message to all the clients which have their ID assigned to that nickname - notice that my implementation allows multiple clients to log into the same username (in the brief it's mention that if this is done properly, extra marks will be awarded).

  * In order to reflect this, ClientTable now instead of assigning a BlockingQueue to a string, it now assigns it to an integer (the client ID).

* A couple more ConcurrentHashMaps have been declared. This is so that each client can share information such as who's logged in or registered.

  * nicknameToIDMap - usernames as keys, and IDs of all the clients connected as this nickname as an ArrayList as the values.

  * registeredUsers - usernames as keys and whether they're currently connected or not as the values (true means connected, false means disconnected).

  * messageStore - usernames as keys, and ArrayLists of messages stored for each user as the values.

  * currentMessageMap - usernames as keys and the index of the "current" message for each client as the value.

### ServerReceiver
* Gets all the `ConcurrentHashMap`s passed to it in its constructor as well as the appropriate clientID instead of the nickname

* While loop now runs when the `running` boolean is true. This is because all the commands the user can input are checked in a switch block, and a break in those wouldn't be able to break the loop.

* Switch block handles commands being passed to the server. It's in a try catch block for NullPointerException in case the user input is null - this can't be handled in the switch statement [as described here](https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.110).

* Every command that takes an input after the first command (those being login, register, send) check if the other inputs are not equal to null in case the input stream has closes

* `register` gets user input and checks if the chosen nickname isn't an empty string or "server". Names like "quit" are allowed due to the new send message syntax, but server is not allowed as some messages will be sent from the server to the client to notify the user of any activity.

  * Then check if this client is already logged in - can't register if already logged in, no other services/websites allow you to do that so I thought it's appropriate.

  * Next check if the name is in the registeredUsers list and if not, add to it, set its status to false (not logged in) and set an empty list of messages for this user. Also set its "current" message to -1 as it has no messages currently stored.

  * After this, notify the user by sending a message from the server (this is good as the user would normally not have access to a server log and can't see if their command worked or what is going on) and print a message on the server.

  * I have chosen to not log in the user after registering automatically, as many other services/websites don't do that, and the user can just easily use the `login` command instead.

* `login` reads the name and checks if the client is currently logged in. If it's not, it checks if the nickname is registered. Then it adds the client's ID to that nickname's list of logged in clients, allowing multiple clients to log in as the same nickname. Next it sets log in to true for the client and the nickname list and notifies the server and client. The next bit of code checks if there have been any messages that have been sent to this nickname while it was logged out and if there have, displays them and sets the "current" message as the latest message.

* `logout` logs the user out if they're logged in by removing the client ID's from the list of IDs associated with the logged in nickname. IF no other clients are logged in to this nickname, the status of the nickname is set to `false` meaning it's logged out. The Client's name is then set to null, and `loggedIn` set to false.

  * The program doesn't quit after being logged out. This makes the user experience more pleasant as if the user wants to e.g. log in to another account, they can without starting the client again. If the user does want to quit, they can just enter the `quit` command after logging out.

* `quit` does everything the same as `logout` if the user is logged in, and then after sets `running` to false which ends the thread, as well as all the other related threads.

* `send` gets the recipient and the message contents. If the user is logged in it then tries to send the message - is the recipient isn't registered, it tells the server that the message is for a nonexistent recipient.

   * I could have also send a message back to the client saying that the user isn't registered, but didn't feel like that was necessary.

* A `message` object is then created and added to the blocking queues of all clients currently logged in as the recipient. The message is then stored on the server in the `messageStore` `ConcurrentHashMap` and if the user is logged in, also sets the recipient's "current" message to this one that has just been sent. At the end notify the server that messages have been sent - this could realistically either be taken out or reduced to just "myClientsName + sent a message" as it may be a privacy concern.
  * The sender isn't notified that the message has been sent as I feel like that would clutter the client's console if a lot of messages are sent.

* `next` and `previous` work similarly in the sense that they both check if the user is logged in, they get the client's stored messages (if they have messages and if it's possible i.e. can't get the next message if the "current" message is already the newest message), set the "current" message to that message and then put it into the client's message queue without storing it. The server is notified of this.

* `delete` gets the current message if messages are being stored for the user, removes it from the `messageStore` `ConcurrentHashMap` and sets the current message to the "next" message, if not possible then to the previous one and if no messages are left, then to -1. The server is notified of this behaviour


`ClientReceiver`, `ServerSender` and `Message` have been left essentially untouched.
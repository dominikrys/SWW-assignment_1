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
* `ServerReceiver` gets all those ConcurrentHashMaps passed to it in its constructor as well as the appropriate clientID instead of the nickname
* Every command that takes an input after the first command (those being login, register, send) check if the other inputs are not equal to null in case the input stream has closes
* `register` gets user input and checks if the chosen nickname isn't an empty string or "server". Names like "quit" are allowed due to the new send message syntax, but server is not allowed as some messages will be sent from the server to the client to notify the user of any activity.
if recipeint != null = check if stream closes back in
for delete command, say next > none
disallow names and ignore cases
whether user is logged in automatically after registering
* login
if a user logs back in, display all its missed messages
allow multiple client connections
* logout
* quit
* send
solution md no "message sent" notifications
when send is called set to current one
allow multiple client connections
* next previous delete
say which message becomes current message after delete
sending messages from server

### ServerReceiver
* While loop now runs when the `running` boolean is true. This is because all the commands the user can input are checked in a switch block, and a break in those wouldn't be able to break the loop.
* Switch block handles commands being passed to the server. It's in a try catch block for NullPointerException in case the user input is null - this can't be handled in the switch statement [as described here](https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.110).

ClientReceiver, ServerSender and Message have been left essentially untouched.

General testing
ignore cases
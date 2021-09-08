# chatroom-with-private-messaging
My solution for Chapter 12 Exercise 7 of “Introduction to Programming Using Java”.
Implementation: as promised in the problem description(see below) this proved to be a difficult exercise for me. 
The issues I encountered were having to figure out ways to work around private implementation. It seemed as though,
without fail, if I wanted to overwrite a method, it was private in the super. However, despite a number of setbacks I was
able to find a solution that I believe works well for what it was intended.

NOTE: This is a javafx program. It requires the javafx library as a dependency. (See bottom of this README for javafx instructions).

Problem Description:
The chat room example from Subsection 12.5.2 can be improved in several ways. First, it
would be nice if the participants in the chat room could be identified by name instead of
by number. Second, it would be nice if one person could send a private message to another
person that would be seen just by that person rather than by everyone. Make these two
changes. You can start with a copy of the package netgame.chat. You will also need the
package netgame.common, which defines the netgame framework.
To make the first change, you will have to implement a subclass of Hub that can keep
track of client names as well as numbers. To get the name of a client to the hub, you
can override the extraHandshake() method both in the Hub subclass and in the Client
subclass. The extraHandshake() method is called as part of setting up the connection
between the client and the hub. It is called after the client has been assigned an ID
number but before the connection is considered to be fully established. It should throw an
IOException if some error occurs during the setup process. Note that any messages that
are sent by the hub as part of the handshake must be read by the client and vice versa.
The extraHandshake() method in the Client is defined as:
protected void extraHandshake(ObjectInputStream in, ObjectOutputStream out)
throws IOException
while in the Hub, there is an extra parameter that tells the ID number of the client whose
connection is being set up:
protected void extraHandshake(in playerID, ObjectInputStream in,
ObjectOutputStream out) throws IOException
In the ChatRoomWindow class, the main() routine asks the user for the name of the
computer where the server is running. You can add some code there to ask the user their
name. (Just imitate the code that asks for the host name.) You will have to decide what
to do if two users want to use the same name.
For the second improvement, personal messages, I suggest writing a new PrivateMessage
class. A PrivateMessage object would include both the string that represents the message
and the ID numbers of the player to whom the message is being sent and the player who
sent the message. The hub will have to be programmed to know how to deal with such
messages. A PrivateMessage should only be sent by the hub to the client who is listed as
the recipient of the message. You need to decide how the user will input a private message
and how the user will select the recipient of the message. Don’t forget that PrivateMessage
needs to be declared to implement Serializable.
If you attempt this exercise, you are likely to find it quite challenging.

Javafx setup instructions:
Download javafx from: https://gluonhq.com/products/javafx/ (I used javafx 12). Save it to a location of your choice.
Unpack the zip folder.
Open my project with your IDE of choice (I use intellij IDEA).
Add the javafx/lib folder as an external library for the project. For intellij, this means going to "project structure" -> "libraries" -> "add library" ->{javafx location}/lib
Add the following as a VM argument for the project: --module-path "{full path to your javafx/lib folder}" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics,javafx.media,javafx.swing,javafx.web
Build and run the project as normal.


package solution.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

import resources.classes.common.PrivateMessage;
import solution.sublclasses.ChatClient;
import solution.sublclasses.ChatForwardedMessage;

/* This class is a demo of the "netgame" package.  It's not exactly a game, but
 * it uses the netgame infrastructure of Hub + Clients to send and receive
 * messages in the chat room.  The chat room server is just a netgame Hub.
 * A ChatRoomWindow has a subclass that represents a Client for that Hub.
 * You must run ChatRoomServer on a known computer.  Several copies of
 * ChatRoomWindow can then connect to that server.
 */

/**
 * This class represents a client for a "chat room" application.  The chat
 * room is hosted by a server running on some computer.  The user of this
 * program must know the host name (or IP address) of the computer that
 * hosts the chat room.  When this program is run, it asks for that
 * information.  Then, it opens a window that has an input box where the
 * user can enter messages to be sent to the chat room.  The message is 
 * sent when the user presses return in the input box or when the
 * user clicks a Send button.  There is also a text area that shows 
 * a transcript of all messages from participants in the chat room.
 * <p>Participants in the chat room are represented only by ID numbers
 * that are assigned to them by the server when they connect.
 */
public class ChatRoomWindow extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }
    //----------------------------------------------------------------------------
    
    private final static int PORT = 37829; // The ChatRoom port number; can't be 
                                           // changed here unless the ChatRoomServer
                                           // program is also changed.

    private TextField messageInput;   // For entering messages to be sent to the chat room.
    private TextField recipientInput;
    private Button sendToOneButton;
    private Button sendToAllButton;        // Sends the contents of the messageInput.
    private Button quitButton;// Leaves the chat room cleanly, by sending a DisconnectMessage.
    
    private TextArea transcript;      // Contains all messages sent by chat room participant, as well
                                      //    as a few additional status messages, 
                                      //    such as when a new user arrives.
    
    private ChatRoomClient connection;      // Represents the connection to the Hub; used to send messages;
                                        // also receives and processes messages from the Hub.
    
    private volatile boolean connected; // This is true while the client is connected to the hub.

    private static String userName;
    
    
    /**
     * Gets the host name (or IP address) of the chat room server from the
     * user and then opens the main window.  The program ends when the user
     * closes the window.
     */
    public void start( Stage stage ) {
        
        TextInputDialog question = new TextInputDialog();
        question.setHeaderText("Enter the host name of the\ncomputer that hosts the chat room.");
        question.setContentText("Host Name:");
        Optional<String> response = question.showAndWait();
        if ( ! response.isPresent() )
            System.exit(0);
        String host = response.get().trim();
        if (host == null || host.trim().length() == 0)
            System.exit(0);

        question.setHeaderText("Enter a name you want others to see.");
        question.setContentText("User name:");
        Optional<String> response2 = question.showAndWait();
        if ( ! response2.isPresent() )
            System.exit(0);
        userName = response2.get().trim();
        if (userName == null || host.trim().length() == 0)
            System.exit(0);


        transcript = new TextArea();
        transcript.setPrefRowCount(30);
        transcript.setPrefColumnCount(60);
        transcript.setWrapText(true);
        transcript.setEditable(false);

        sendToAllButton = new Button("send to all");
        sendToOneButton = new Button("send to one");
        quitButton = new Button("quit");
        recipientInput = new TextField();
        recipientInput.setPrefColumnCount(10);
        messageInput = new TextField();
        messageInput.setPrefColumnCount(40);
        sendToAllButton.setOnAction(e -> doSendToAll() );
        sendToOneButton.setOnAction(e->doSendToOne());
        quitButton.setOnAction( e -> doQuit() );
        sendToAllButton.setDefaultButton(true);
        sendToAllButton.setDisable(true);
        sendToOneButton.setDisable(true);
        messageInput.setEditable(false);
        messageInput.setDisable(true);

        HBox bottom = new HBox(8, new Label("YOU SAY:"), messageInput, recipientInput ,sendToOneButton, sendToAllButton, quitButton);
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        HBox.setMargin(quitButton, new Insets(0,0,0,50));
        bottom.setPadding(new Insets(8));
        bottom.setStyle("-fx-border-color: black; -fx-border-width:2px");
        BorderPane root = new BorderPane(transcript);
        root.setBottom(bottom);

        stage.setScene( new Scene(root) );
        stage.setTitle("Networked Chat");
        stage.setResizable(false);
        stage.setOnHidden( e -> doQuit() );
        stage.show();

        new Thread() {
                // This is a thread that opens the connection to the server.  Since
                // that operation can block, it's not done directly in the constructor.
                // Once the connection is established, the user interface elements are
                // enabled so the user can send messages.  The Thread dies after
                // the connection is established or after an error occurs.
            public void run() {
                try {
                    addToTranscript("Connecting to " + host + " ...");

                    connection = new ChatRoomClient(host);
                    connected = true;
//                    addToTranscript("Connected.");
                    Platform.runLater( () -> {
                        messageInput.setEditable(true);
                        messageInput.setDisable(false);
                        sendToAllButton.setDisable(false);
                        sendToOneButton.setDisable(false);
                        messageInput.requestFocus();
                    });
                }
                catch (IOException e) {
                    addToTranscript("Connection attempt failed.");
                    addToTranscript("Error: " + e);
                }
            }
        }.start();

    }

    public static String getUserName(){
        return userName;
    }


    /**
     * A ChatClient connects to the Hub and is used to send messages to
     * and receive messages from a Hub.  Messages received from the
     * Hub will be of type ForwardedMessage and will contain the
     * ID number of the sender and the string that was sent by
     * that user.
     */
    private class ChatRoomClient extends ChatClient {


        /**
         * Opens a connection to the chat room server on a specified computer.
         */
        ChatRoomClient(String host) throws IOException {
            super(host,PORT);
        }

        @Override
        protected void extraHandshake(ObjectInputStream in, ObjectOutputStream out)throws IOException {

            String userName = ChatRoomWindow.getUserName();
            try{
                String userNameString = in.readObject().toString();
                if(userNameString.equals("Please type a new username (this is what other people will see).")){
                    out.writeObject(userName);
                    ChatRoomWindow.userName = in.readObject().toString();


                }else{
                    throw new IllegalArgumentException("Failed extra handshake.");
                }
            }catch (ClassNotFoundException e){

            }

        }


        /**
         * Responds when a message is received from the server.  It should be
         * a ForwardedMessage representing something that one of the participants
         * in the chat room is saying.  The message is simply added to the
         * transcript, along with the ID number of the sender.
         */
        protected void messageReceived(Object message) {
            if (message instanceof ChatForwardedMessage) {  // (no other message types are expected)
                ChatForwardedMessage bm = (ChatForwardedMessage)message;
                addToTranscript(bm.userName + " says:  " + bm.message);
            } else if(message instanceof PrivateMessage){
                PrivateMessage pm = (PrivateMessage)message;
                addToTranscript(pm.senderName + " says to " + pm.recipientName + ": "+pm.message);
            }
            else{
                addToTranscript(message.toString());
            }
        }

        /**
         * Called when the connection to the client is shut down because of some
         * error message.  (This will happen if the server program is terminated.)
         */
        protected void connectionClosedByError(String message) {
            addToTranscript("Sorry, communication has shut down due to an error:\n     " + message);
            Platform.runLater( () -> {
                sendToAllButton.setDisable(true);
                sendToOneButton.setDisable(true);
                messageInput.setEditable(false);
                messageInput.setDisable(true);
                messageInput.setText("");
            });
            connected = false;
            connection = null;
        }

        /**
         * Posts a message to the transcript when someone joins the chat room.
         */
        protected void playerConnected(int newPlayerID) {
            addToTranscript("Someone new has joined the chat room. Welcome " + userName +"!");
        }

        /**
         * Posts a message to the transcript when someone leaves the chat room.
         */
        protected void playerDisconnected(int departingPlayerID) {
            addToTranscript(userName + " has left the chat room.");
        }

    } // end nested class ChatClient

    
    
    
    
    /**
     * Adds a string to the transcript area, followed by a blank line.
     */
    private void addToTranscript(String message) {
        Platform.runLater( () ->    transcript.appendText(message + "\n\n") );
    }
    
    
    /**
     * Called when the user clicks the Quit button or closes
     * the window by clicking its close box. Called from the
     * application thread.
     */
    private void doQuit() {
        if (connected)
            connection.disconnect();  // Sends a DisconnectMessage to the server.
        try {
            Thread.sleep(500); // Time for DisconnectMessage to actually be sent.
        }
        catch (InterruptedException e) {
        }
        System.exit(0);
    }

    

    /** 
     * Send the string entered by the user as a message
     * to the Hub, using the ChatClient that handles communication
     * for this ChatRoomWindow.  Note that the string is not added
     * to the transcript here.  It will get added after the Hub
     * receives the message and broadcasts it to all clients,
     * including this one.  Called from the application thread.
     */
    private void doSendToAll() {
        String message = messageInput.getText();
        if (message.trim().length() == 0)
            return;
        connection.send(message);
        messageInput.selectAll();
        messageInput.requestFocus();
    }

    private void doSendToOne(){
        String message = messageInput.getText();
        String recipient = recipientInput.getText();
        if (message.trim().length() == 0)
            return;

        PrivateMessage messageObject = new PrivateMessage(userName, recipient, message);
        connection.send(messageObject);

    }
    

}  // end class ChatRoomWindow

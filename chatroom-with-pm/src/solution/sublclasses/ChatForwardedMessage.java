package solution.sublclasses;


import java.io.Serializable;

public class ChatForwardedMessage implements Serializable {
    public final Object message;  // Original message from a client.
    public final int senderID;    // The ID of the client who sent that message.
    public final String userName;

    public ChatForwardedMessage(String senderName, int senderID, Object message) {
        this.senderID = senderID;
        this.message = message;
        this.userName = senderName;
    }
}

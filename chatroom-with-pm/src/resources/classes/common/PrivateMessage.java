package resources.classes.common;

import solution.sublclasses.*;

import java.io.Serializable;

public class PrivateMessage implements Serializable {
    public int senderID;
    public int recipientID;
    public String senderName;
    public Object message;
    public String recipientName;

    public PrivateMessage(String userName, String recipientName, Object message){
        this.message = message;
        this.senderName = userName;
        this.recipientName = recipientName;
    }




}

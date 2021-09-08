package solution.sublclasses;
import resources.classes.common.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;


public class ChatHub extends Hub{
    static HashMap<Integer, String> playerNames = new HashMap<>();
    static HashMap<String, Integer> playerIDs = new HashMap<>();

    public ChatHub(int port) throws IOException {
        super(port);
    }



    protected void messageReceived(int playerID, Object message) {
        String userName = playerNames.get(playerID);
        if(message instanceof PrivateMessage){
            PrivateMessage pm = (PrivateMessage) message;
            pm.senderID = getPlayerByName(pm.senderName);
            pm.recipientID = getPlayerByName(pm.recipientName);
            if(pm.recipientID == -1){
                sendToOne(pm.senderID, "Player " + pm.recipientName + " is not in the database.");
            }else if (pm.senderID == -1){
                throw new IllegalArgumentException("Something went wrong with the send-to-one functionality.");
            }
            else{
                sendToOne(pm.recipientID, message);
                sendToOne(pm.senderID, message);
            }
        }else{
            sendToAll(new ChatForwardedMessage(userName, playerID,message));
        }
    }
//
    public static int getPlayerByName(String name){
        return playerIDs.getOrDefault(name, -1);
    }

    @Override
    public void extraHandshake(int playerId, ObjectInputStream in, ObjectOutputStream out) throws IOException{
        out.writeObject("Please type a new username (this is what other people will see).");
        out.flush();
        String userName;

        try{
                userName = in.readObject().toString();

                if(playerIDs.containsKey(userName)){
                    int counter = 2;
                    while(playerIDs.containsKey(userName+"#"+counter)){
                        counter++;
                    }
                    userName = userName + "#" + counter;
                    playerNames.put(playerId, userName);
                    playerIDs.put(userName, playerId);
                    out.writeObject(userName);
                    out.flush();
                }else{
                    playerNames.put(playerId, userName);
                    playerIDs.put(userName,playerId);
                    out.writeObject(userName);
                    out.flush();
                }

        }catch (ClassNotFoundException e){
            throw new IOException(e);
        }
    }

}

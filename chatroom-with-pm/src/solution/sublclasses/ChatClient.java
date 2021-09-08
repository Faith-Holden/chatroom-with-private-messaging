package solution.sublclasses;
import resources.classes.common.Client;

import java.io.IOException;


public class ChatClient extends Client {
    public ChatClient(String hubHostName, int hubPort) throws IOException {
        super(hubHostName, hubPort);
    }



    @Override
    protected void messageReceived(Object message) {
        System.out.println(message.toString());
    }



}

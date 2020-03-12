package user.client;

import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.net.Socket;

/**
 * Provides a user interface for monitoring weather station data. 
 */
public class UserClient {

    /**
     * Entry point for the user client program.
     * 
     * Parses an IP address and port from the command line arguments and
     * runs the client.
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        String IP = args[0];
        int port = parseInt(args[1]);
        UserClient client = new UserClient();
        client.run(IP, port);
    }

    /**
     * Connects to the server at the specified IP address and port.
     * 
     * @param IP
     * @param port
     * @throws IOException 
     */
    private void run(String IP, int port) throws IOException {
        Socket server = new Socket(IP, port);
        System.out.println("Connected to server at " + server.getInetAddress());
    }
    
}

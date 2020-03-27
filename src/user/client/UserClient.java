package user.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import shared.utilities.UserAuthenticationMessage;
import shared.utilities.AuthenticationResponseMessage;

/**
 * Provides a user interface for monitoring weather station data. 
 */
public class UserClient {
    
    private final Socket server;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    /**
     * Connects to the server at the specified IP address and port.
     * 
     * @param IP
     * @param port
     * @throws IOException 
     */
    public UserClient(String IP, int port) throws IOException {
        server = new Socket(IP, port);
        OutputStream outputStream = server.getOutputStream();
        oos = new ObjectOutputStream(outputStream);
        InputStream inputStream = server.getInputStream();
        ois = new ObjectInputStream(inputStream);
        System.out.println("Connected to server at " + server.getInetAddress());
    }

    /**
     * Sends an authentication message to the server and gets a response back.
     * 
     * @param username
     * @param password
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    void authenticate(String username, String password) throws ClassNotFoundException, IOException {
        sendObject(new UserAuthenticationMessage(username, password));
        try {
            AuthenticationResponseMessage message = (AuthenticationResponseMessage)readObject();
            if(message.isAccepted()) {
                System.out.println("Successfully authenticated!");
            }
            else {
                System.out.println("Authentication request denied!");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Send an object to the server.
     * 
     * @param object
     * @throws IOException 
     */
    private void sendObject(Object object) throws IOException {
        oos.writeObject(object);
        oos.flush();
    }
    
    /**
     * Reads an object from the server.
     * 
     * @return Object
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private Object readObject() throws IOException, ClassNotFoundException {
        Object object = ois.readObject();
        return object;
    }
    
}

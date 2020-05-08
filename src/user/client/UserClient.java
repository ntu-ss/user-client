package user.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import shared.utilities.UserAuthenticationRequest;
import shared.utilities.ClientAuthenticationResponse;
import shared.utilities.FieldUpdateRequest;
import shared.utilities.FieldUpdateResponse;
import shared.utilities.StationDataMessage;
import shared.utilities.StationDataRequest;

public class UserClient {
    
    private final Socket server;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private boolean isLoggedIn;
    private final ClientUI clientUI;
    private String selectedStation;

    public UserClient(String IP, int port) throws IOException {
        server = new Socket(IP, port);
        OutputStream outputStream = server.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = server.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        System.out.println("Connected to server at " + server.getInetAddress());
        isLoggedIn = false;
        clientUI = new ClientUI(this);
        selectedStation = null;
    }

    public static void main(String[] args) throws IOException {
        UserClient client = new UserClient("127.0.0.1", 8080);
        client.run();
    }
    
    public void run() {
        Thread uiThread = new Thread(clientUI);
        uiThread.start();
        while(true) {
            if(isLoggedIn) {
                try {
                    sendObject(new FieldUpdateRequest());
                    FieldUpdateResponse message = (FieldUpdateResponse)readObject();
                    clientUI.updateFieldData("Crop Type: " + message.getCrop() +
                            "\nField Area: " + message.getArea() + " m^2");
                    clientUI.updateStations(message.getStations());
                    System.out.println("Received field update: " +
                            "\nCrop Type: " + message.getCrop() +
                            "\nField Area: " + message.getArea() + " m^2" +
                            "\nStations: " + String.join(", ", message.getStations()));
                } catch (IOException ex) {
                    System.out.println("Error in sending field update request.");
                } catch (ClassNotFoundException ex) {
                    System.out.println("Error in parsing field update response.");
                }
                if(selectedStation != null) {
                    try {
                        requestData(selectedStation);
                    } catch (IOException ex) {
                    } catch (ClassNotFoundException ex) {
                    }
                }
            }
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void authenticate(String username, String password) throws ClassNotFoundException, IOException {
        sendObject(new UserAuthenticationRequest(username, password));
        try {
            ClientAuthenticationResponse message = (ClientAuthenticationResponse)readObject();
            if(message.isAccepted()) {
                isLoggedIn = true;
                clientUI.enableElements();
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

    private void sendObject(Object object) throws IOException {
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }

    private Object readObject() throws IOException, ClassNotFoundException {
        Object object = objectInputStream.readObject();
        return object;
    }

    void requestData(String station) throws IOException, ClassNotFoundException {
        sendObject(new StationDataRequest(station));
        StationDataMessage response = (StationDataMessage)readObject();
        clientUI.updateWeatherData(" GPS Position: " + response.getPosition() + " Lon / Lat" +
                "\n Temperature: " + response.getTemperature() + " C" +
                "\n Humidity: " + response.getHumidity() + "%");
        selectedStation = station;
    }
}

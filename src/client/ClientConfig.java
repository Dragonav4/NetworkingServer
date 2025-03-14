package client;

public class ClientConfig {

    public String Host;
    public int Port;


    public ClientConfig() {
        Host = "localhost";
        Port = 1244;
    }

    public static ClientConfig getDefaultConfig() {
        return new ClientConfig();
    }
}


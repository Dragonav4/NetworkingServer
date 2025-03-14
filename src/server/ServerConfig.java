package server;

import client.ClientConfig;

public class ServerConfig extends ClientConfig {
    public String[] BannedWords = new String[]{};

    public ServerConfig() {
        super();
    }

    public static ServerConfig getDefaultConfig() {
        return new ServerConfig();
    }


}

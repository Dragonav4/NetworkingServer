package server;

import client.Client;
import client.ClientHandler;
import com.google.gson.Gson;
import exceptions.UserNameUsedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Server {

    private final ServerSocket serverSocket;
    private String[] bannedWords;

    public Server(ServerSocket serverSocket, String[] bannedWords) {
        this.serverSocket = serverSocket;
        this.bannedWords = bannedWords;
    }

    public void StartServer() {
        System.out.println("Starting");
        try {

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket, bannedWords);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch (IOException e) {
        } catch (UserNameUsedException e) {
            throw new RuntimeException(e);
        }
    }

    private static ServerConfig serverConfig;

    private static ServerConfig loadConfig() {
        try{
            var path = Client.class.getResource("config/serverConfig.json").getPath();
            var json = Files.readString(Path.of(path));
            var gson = new Gson();
            return gson.fromJson(json, ServerConfig.class);
        } catch (Exception e) {
            return ServerConfig.getDefaultConfig();
        }
    }

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Stop requested");
                ClientHandler.closeAllSockets();
            }
        }));
        serverConfig = loadConfig();
        var serverSocket = new ServerSocket(serverConfig.Port);
        var server = new Server(serverSocket, serverConfig.BannedWords);
        server.StartServer();
    }

}

package client;

import com.google.gson.Gson;
import exceptions.ServerConnectionException;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private boolean tryLogin(String userName) throws IOException {
        sendMessageToServer(userName);
        var input = bufferedReader.readLine();
        if (input.startsWith("Welcome")) {
            System.out.println(input);
            return true;
        }
        else {
            return false;
        }
    }

    private void start() throws IOException {
        listenForMessage();
        sendUserMessages();
    }

    public void sendMessageToServer(String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }


    public void sendUserMessages() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while(true){
            String messageToSend = scanner.nextLine();
            if (socket.isClosed())
                break;
            sendMessageToServer(messageToSend);
        }
        scanner.close();
    }


    public void listenForMessage() {
        var thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroupChat;
                while (!socket.isClosed()) {
                    try {
                        messageFromGroupChat = bufferedReader.readLine();
                        if (messageFromGroupChat == null) {
                            throw new ServerConnectionException();
                        }
                        System.out.println(messageFromGroupChat);
                    } catch (Exception e) {
                        closeEverything(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        });
        thread.start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try{
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(socket!=null) {
                socket.close();
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
    private void closeClientSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                sendMessageToServer("/exit");
                socket.close();
            }
        } catch (IOException e) {
        }
    }

    private static Client client;
    private static ClientConfig clientConfig;

    private static ClientConfig loadConfig() {
        try{
            var path = Client.class.getResource("config/clientConfig.json").getPath(); //getResource try to find in SRC/main/Resource
            var json = Files.readString(Path.of(path)); // reading the config(str)
            var gson = new Gson(); // Obj with methods which needs to work with JSON
            return gson.fromJson(json, ClientConfig.class); // takes json-line and creates a class from Json
        } catch (Exception e) {
            return ClientConfig.getDefaultConfig(); // default config if .....
        }
    }

    public static void main(String[] args) throws IOException {
        clientConfig = loadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {//calls when app shutdown. Uses for 99.9% closing the socket
            @Override
            public void run() {
                if(client != null) {
                    client.closeClientSocket();
                }
            }
        }));
        Scanner scanner = new Scanner(System.in);
        try { //connection to server
            while (true) {
                System.out.println("Enter your username for group chat (or /exit for exit):");
                String username = scanner.next();
                if (username.startsWith("/exit"))
                    break;
                var socket = new Socket(clientConfig.Host, clientConfig.Port);
                client = new Client(socket);
                if (client.tryLogin(username)) {
                    client.start();
                    break;
                } else {
                    System.out.println("User name already used");
                }
            }
        }
        catch(IOException ex) {
            System.out.println("Server connection lost");
        }
    }
}

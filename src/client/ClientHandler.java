package client;

import exceptions.UserNameUsedException;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private static final ConcurrentHashMap<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private String[] bannedWords;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserName;

    private static final String BANNED_WORD_WARNING = "You are not allowed to write this word";

    public ClientHandler(Socket socket, String[] bannedWords) throws UserNameUsedException {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // byte stream -> char stream
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            this.bannedWords = bannedWords;
            synchronized (clientHandlers) {
                if (!clientHandlers.containsKey(clientUserName)) {
                    clientHandlers.put(clientUserName, this);
                    broadcastNewUserJoined();
                }
                else {
                    writeLine(this, "This user name already used"); // not starts with WELCOME :)
                    throw new UserNameUsedException();
                }
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    private boolean isValid(String message) { //word is valid
        for (var word : bannedWords) {
            if (message.contains(word)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null)
                    break;
                messageFromClient = messageFromClient.trim();
                if (messageFromClient.startsWith("/exit"))
                    break;
                if (messageFromClient.startsWith("/ban")) {
                    sendPersonalMessage(clientUserName, "Banned words: " + String.join(", ", bannedWords));
                } else {
                    if (!messageFromClient.isEmpty()) {
                        if (isValid(messageFromClient)) {
                            broadcastMessage(messageFromClient);
                        } else {
                            sendPersonalMessage(clientUserName, BANNED_WORD_WARNING);
                        }
                    }
                }
            } catch (IOException e) {
                break;
            }
        }
        closeEverything();
        System.out.println("Terminating thread of "+clientUserName);
    }

    private void sendPersonalMessage(String username, String message){
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                try {
                    if (clientHandler.clientUserName.equals(username)) {
                        writeLine(clientHandler, message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void broadcastMessage(String messageToSend) {
        var sendToSpecificPersons = messageToSend.startsWith("@");
        var sendToAllExcludingOne = messageToSend.startsWith("-");
        synchronized (clientHandlers) { //only 1 stream can address at once
            for (ClientHandler clientHandler : clientHandlers.values()) {
                try {
                    if (!clientHandler.clientUserName.equals(clientUserName)) { // not the person who send message
                        if (sendToSpecificPersons && !messageToSend.contains("@" + clientHandler.clientUserName)) //only for those who were provided after @
                            continue;
                        if (sendToAllExcludingOne && messageToSend.startsWith("-" + clientHandler.clientUserName)) //only for those who weren't provided after @
                            continue;
                        writeLine(clientHandler, clientUserName + ":" + messageToSend); // sends message to client
                    }
                } catch (IOException e) {
                    closeEverything();
                }
            }
        }
    }

    private void broadcastNewUserJoined() {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                try {
                    if (!clientHandler.clientUserName.equals(clientUserName)) { // new client will not get message that he has entered the chat
                        writeLine(clientHandler, "SERVER: " + clientUserName + " has entered the chat!");
                    } else {
                        writeLine(clientHandler, "Welcome to the chat");
                        writeLine(clientHandler, "To use this chat you can use:");
                        writeLine(clientHandler, "Now you in group chat. Where you can write to all recepients");
                        writeLine(clientHandler, "'@' - to tag someone(one or more people)");
                        writeLine(clientHandler, "'-' - to tag all people without one");
                        writeLine(clientHandler, "Users in the chat:");
                        for (var name : clientHandlers.keySet()) { // all names of users which now in a chat
                            writeLine(clientHandler, "- " + name);
                        }
                    }
                } catch (IOException e) {
                    closeEverything();
                }
            }
        }
    }

    private void writeLine(ClientHandler client, String message) throws IOException {
        client.bufferedWriter.write(message); //write message in a stream
        client.bufferedWriter.newLine(); // shows that this is ended line
        client.bufferedWriter.flush(); // forcibly sends it
    }

    public void removeClientHandler() {
        synchronized (clientHandlers){
            clientHandlers.remove(clientUserName);
        }
        broadcastMessage("SERVER: " + clientUserName + " has left the chat!");
    }

    private void closeEverything() { // when client leaves/exclude attributes of this person will delete
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            System.out.println(clientUserName+" -1"); // -1 -> bufferedReader were closed
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            System.out.println(clientUserName+" -2"); // -2 -> BufferedWriter were closed
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeAllSockets() { // close all connected clients
        Collection<ClientHandler> toRemove;
        synchronized (clientHandlers) { // while executing no one can edit clienthandlers
            toRemove = clientHandlers.values(); // remember all clients
            clientHandlers.clear(); // remove all clienthandler to avoid messaging/refer to this client
        }
        for(var clientHandler: toRemove) {
                clientHandler.closeEverything(); // close all recourses for clients(streams,sockets)
        }
    }
}

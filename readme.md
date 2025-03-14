# **Networking Server**

This project is a **Networking Server** built in **Java**, enabling **real-time communication** between users over a **TCP connection**.  
The server operates through a **command-line interface**, allowing users to connect and send various types of messages using predefined commands.

---

## **Features**
- **TCP-Based communication** – Ensures reliable real-time data transfer.
- **Direct messaging** – Users can send private messages.
- **Exclude messaging** – Ability to exclude specific users from receiving messages.
- **Custom user nicknames** – Users can set their own display names.
- **Banned words & phrases control** – Automatic filtering of restricted words.
- **Clear help & rules section** – Built-in documentation for users.

---
## **Usage**
### **Commands**
- `/ban` — Displays the list of banned words.
- `/nickname <new_nickname>` — Changes your nickname.
- `@<username>` — Tags a specific user in the chat.
- `@<username1>, <username2>` — Tags multiple users.
- `- <username>` — Sends a message to everyone **except** the specified user.
- `/exit` — Leaves the server.

**Note**:  
Writing plain text without a command will send the message to the **global chat**, available to all users.

## **Server Side**
- When the server starts, it **outputs a welcome message** in the console and begins listening for incoming connections.  

## **Client Side**
- After connection to the opened server. User is welcomed with a message and commands he can output, connected clients are shown. 

## **Example of using program:**
![Example of Usage](showingExecutionOfNetWorkingServer.gif)

---
## **Built with**
- Java 
- Flexible user control

## **Project Structure**
- src/server/Server.java - Entry point of the server application and with server classes
- src/client/Client.java - Entry point of the client application and with client classes
- src/config - Directory with config classes
- src/exceptions - handle custom exceptions
- README.md - Project documentation
- LICENSE - Project license

---
## **License**
- This project is licensed under the MIT License
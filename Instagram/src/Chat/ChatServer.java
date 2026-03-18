/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Chat;

import Logica.UserManager;
import Logica.Message;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author Nathan
 */
public final class ChatServer {

    public static final int PORT = 5050;

    private final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private final UserManager userManager = UserManager.getInstance();
    private ServerSocket serverSocket;

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            new Thread(handler, "ChatClientHandler").start();
        }
    }

    private void deliver(String username, ChatEnvelope envelope) {
        ClientHandler handler = onlineUsers.get(username);
        if (handler != null) {
            handler.send(envelope);
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Object obj = in.readObject();
                    if (!(obj instanceof ChatEnvelope)) {
                        continue;
                    }

                    ChatEnvelope env = (ChatEnvelope) obj;

                    if (env.getAction() == ChatAction.REGISTER) {
                        username = env.getUsername();
                        if (username != null && !username.trim().isEmpty()) {
                            onlineUsers.put(username, this);
                        }
                    } else if (env.getAction() == ChatAction.SEND) {
                        Message msg = env.getMessage();
                        if (msg == null) {
                            continue;
                        }

                        if (!userManager.canSendMessage(msg.getFrom(), msg.getTo())) {
                            deliver(msg.getFrom(), ChatEnvelope.error("No puedes enviar mensajes: cuenta privada y solicitud no aprobada."));
                            continue;
                        }

                        userManager.appendDeliveredMessageToInboxes(msg);

                        deliver(msg.getFrom(), ChatEnvelope.incoming(msg));
                        if (!msg.getFrom().equalsIgnoreCase(msg.getTo())) {
                            deliver(msg.getTo(), ChatEnvelope.incoming(msg));
                        }
                    }
                }

            } catch (Exception ignored) {
            } finally {
                try {
                    if (username != null) {
                        onlineUsers.remove(username);
                    }
                    if (socket != null) socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        public synchronized void send(ChatEnvelope envelope) {
            try {
                out.writeObject(envelope);
                out.flush();
            } catch (IOException ignored) {
            }
        }
    }
}
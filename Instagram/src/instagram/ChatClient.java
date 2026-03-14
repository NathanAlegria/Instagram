/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.io.*;
import java.net.Socket;

/**
 *
 * @author Nathan
 */
public class ChatClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ChatListener listener;
    private boolean connected;

    public boolean connect(String username, ChatListener listener) {
        try {
            this.listener = listener;

            socket = new Socket("127.0.0.1", ChatServer.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(ChatEnvelope.register(username));
            out.flush();

            connected = true;
            if (this.listener != null) {
                this.listener.onConnectionStateChanged(true);
            }

            Thread reader = new Thread(this::readLoop, "ChatClientReader");
            reader.setDaemon(true);
            reader.start();

            return true;
        } catch (Exception e) {
            connected = false;
            if (this.listener != null) {
                this.listener.onConnectionStateChanged(false);
            }
            return false;
        }
    }

    private void readLoop() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (!(obj instanceof ChatEnvelope)) {
                    continue;
                }

                ChatEnvelope env = (ChatEnvelope) obj;

                if (env.getAction() == ChatAction.MESSAGE && listener != null) {
                    listener.onIncomingMessage(env.getMessage());
                } else if (env.getAction() == ChatAction.ERROR && listener != null) {
                    listener.onSystemMessage(env.getText());
                }
            }
        } catch (Exception ignored) {
        } finally {
            connected = false;
            if (listener != null) {
                listener.onConnectionStateChanged(false);
            }
        }
    }

    public synchronized boolean sendMessage(Message message) {
        if (!connected) {
            return false;
        }

        try {
            out.writeObject(ChatEnvelope.send(message));
            out.flush();
            return true;
        } catch (IOException e) {
            connected = false;
            if (listener != null) {
                listener.onConnectionStateChanged(false);
            }
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}

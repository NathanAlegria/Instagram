/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Chat;

/**
 *
 * @author Nathan
 */
public final class ChatServerStarter {
    private static ChatServer server;
    private static boolean attempted = false;

    private ChatServerStarter() {
    }

    public static synchronized void startIfNeeded() {
        if (attempted) {
            return;
        }
        attempted = true;

        Thread t = new Thread(() -> {
            try {
                server = new ChatServer();
                server.start();
            } catch (Exception ignored) {
            }
        }, "ChatServerStarter");

        t.setDaemon(true);
        t.start();
    }
}

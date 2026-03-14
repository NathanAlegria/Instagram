/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

/**
 *
 * @author Nathan
 */
public interface ChatListener {
    void onIncomingMessage(Message message);
    void onSystemMessage(String text);
    void onConnectionStateChanged(boolean connected);
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Chat;

import Logica.Message;

/**
 *
 * @author Nathan
 */
public interface ChatListener {
    void onIncomingMessage(Message message);
    void onSystemMessage(String text);
    void onConnectionStateChanged(boolean connected);
}

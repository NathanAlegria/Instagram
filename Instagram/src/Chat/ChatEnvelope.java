/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Chat;

import Logica.Message;
import java.io.Serializable;

/**
 *
 * @author Nathan
 */
public class ChatEnvelope implements Serializable {
    private static final long serialVersionUID = 1L;

    private ChatAction action;
    private String username;
    private Message message;
    private String text;

    public ChatEnvelope(ChatAction action, String username, Message message, String text) {
        this.action = action;
        this.username = username;
        this.message = message;
        this.text = text;
    }

    public static ChatEnvelope register(String username) {
        return new ChatEnvelope(ChatAction.REGISTER, username, null, null);
    }

    public static ChatEnvelope send(Message message) {
        return new ChatEnvelope(ChatAction.SEND, null, message, null);
    }

    public static ChatEnvelope incoming(Message message) {
        return new ChatEnvelope(ChatAction.MESSAGE, null, message, null);
    }

    public static ChatEnvelope error(String text) {
        return new ChatEnvelope(ChatAction.ERROR, null, null, text);
    }

    public ChatAction getAction() {
        return action;
    }

    public String getUsername() {
        return username;
    }

    public Message getMessage() {
        return message;
    }

    public String getText() {
        return text;
    }
}

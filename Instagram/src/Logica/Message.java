/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Nathan
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String from;
    private String to;
    private LocalDateTime dateTime;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private String assetOwner;

    public Message(String from, String to, String content, MessageType type) {
        this(from, to, content, type, null);
    }

    public Message(String from, String to, String content, MessageType type, String assetOwner) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.type = type;
        this.assetOwner = assetOwner;
        this.status = MessageStatus.UNREAD;
        this.dateTime = LocalDateTime.now();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getAssetOwner() {
        return assetOwner;
    }

    public void markRead() {
        this.status = MessageStatus.READ;
    }

    public String getFormattedDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
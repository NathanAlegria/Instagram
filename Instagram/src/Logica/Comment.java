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
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String text;
    private LocalDateTime date;

    public Comment(String username, String text) {
        this.username = username;
        this.text = text;
        this.date = LocalDateTime.now(); 
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
}

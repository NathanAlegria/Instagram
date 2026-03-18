/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica;

/**
 *
 * @author Nathan
 */
public class TextMessage extends Message {

    public TextMessage(String from, String to, String content) {
        super(from, to, content, MessageType.TEXT);
    }
}

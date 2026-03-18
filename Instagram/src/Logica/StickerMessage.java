/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica;

/**
 *
 * @author Nathan
 */
public class StickerMessage extends Message {

    public StickerMessage(String from, String to, String content, MessageType type) {
        super(from, to, content, type);
    }

    public StickerMessage(String from, String to, String content, MessageType type, String assetOwner) {
        super(from, to, content, type, assetOwner);
    }
}

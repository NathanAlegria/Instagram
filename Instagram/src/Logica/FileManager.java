/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author Nathan
 */

/**
 * Manejo de archivos cumpliendo el documento:
 * - Todo va a INSTA_RAIZ
 * - Imágenes de usuario van a INSTA_RAIZ/username/imagenes/
 */
public class FileManager {

    public static String saveUserImage(String username, File sourceFile, String prefix) throws IOException {
        InstaPaths.ensureBaseStructure();
        InstaPaths.ensureUserStructure(username);

        String ext = getExtension(sourceFile.getName());
        String newName = prefix + "_" + System.currentTimeMillis() + ext;

        File dest = new File(InstaPaths.userImagesFolder(username), newName);
        Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return newName;
    }

    public static String saveUserSticker(String username, File sourceFile) throws IOException {
        InstaPaths.ensureBaseStructure();
        InstaPaths.ensureUserStructure(username);

        String ext = getExtension(sourceFile.getName());
        String newName = "sticker_" + System.currentTimeMillis() + ext;

        File dest = new File(InstaPaths.userPersonalStickers(username), newName);
        Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return newName;
    }

    public static String getUserImageAbsolutePath(String username, String imageName) {
        return new File(InstaPaths.userImagesFolder(username), imageName).getAbsolutePath();
    }

    public static String getUserStickerAbsolutePath(String username, String stickerName) {
        return new File(InstaPaths.userPersonalStickers(username), stickerName).getAbsolutePath();
    }

    public static String getGlobalAbsolutePath(String fileName) {
        return new File(InstaPaths.ROOT, fileName).getAbsolutePath();
    }

    private static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i) : "";
    }
}
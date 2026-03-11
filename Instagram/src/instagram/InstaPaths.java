/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Nathan
 */

/**
 * Clase utilitaria para cumplir el requisito del proyecto:
 * Toda persistencia va dentro de INSTA_RAIZ/
 */
public class InstaPaths {

    public static final File ROOT = new File(System.getProperty("user.dir"), "INSTA_RAIZ");

    public static File usersIns() { return new File(ROOT, "users.ins"); }
    public static File usersDat() { return new File(ROOT, "users.dat"); }

    public static File stickersGlobalFolder() { return new File(ROOT, "stickers_globales"); }

    public static File userFolder(String username) { return new File(ROOT, username); }

    // Carpetas obligatorias por usuario
    public static File userImagesFolder(String username) { return new File(userFolder(username), "imagenes"); }
    public static File userPersonalFolders(String username) { return new File(userFolder(username), "folders_personales"); }
    public static File userPersonalStickers(String username) { return new File(userFolder(username), "stickers_personales"); }

    // Archivos obligatorios por usuario
    public static File followersIns(String username) { return new File(userFolder(username), "followers.ins"); }
    public static File followingIns(String username) { return new File(userFolder(username), "following.ins"); }
    public static File instaIns(String username) { return new File(userFolder(username), "insta.ins"); }
    public static File inboxBin(String username) { return new File(userFolder(username), "inbox.bin"); } // guardaremos serializado
    public static File stickersIns(String username) { return new File(userFolder(username), "stickers.ins"); }
    public static File requestsIns(String username) { return new File(userFolder(username), "requests.ins"); } // solicitudes follow (privado)

    public static void ensureBaseStructure() throws IOException {
        if (!ROOT.exists() && !ROOT.mkdirs()) {
            throw new IOException("No se pudo crear INSTA_RAIZ.");
        }
        if (!stickersGlobalFolder().exists()) stickersGlobalFolder().mkdirs();
        if (!usersIns().exists()) usersIns().createNewFile();
    }

    public static void ensureUserStructure(String username) throws IOException {
        File uf = userFolder(username);
        if (!uf.exists() && !uf.mkdirs()) {
            throw new IOException("No se pudo crear carpeta del usuario: " + username);
        }

        // Archivos
        safeCreate(followersIns(username));
        safeCreate(followingIns(username));
        safeCreate(instaIns(username));
        safeCreate(stickersIns(username));
        safeCreate(requestsIns(username));
        // inboxBin se crea al primer envío

        // Carpetas
        userImagesFolder(username).mkdirs();
        userPersonalFolders(username).mkdirs();
        userPersonalStickers(username).mkdirs();
    }

    private static void safeCreate(File f) throws IOException {
        if (!f.exists()) f.createNewFile();
    }
}
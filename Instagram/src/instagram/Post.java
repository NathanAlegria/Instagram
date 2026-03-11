/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
/**
 *
 * @author Nathan
 */

/**
 * Post con:
 * - id único
 * - imageName (solo nombre)
 * - caption
 * - likes y comments
 */
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String imageName; // solo el nombre del archivo
    private String caption;
    private String username;
    private LocalDateTime date;

    private Set<String> likedBy;
    private List<Comment> comments;

    public Post(String username, String imageName, String caption) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.imageName = imageName;
        this.caption = (caption == null) ? "" : caption;
        this.date = LocalDateTime.now();
        this.likedBy = new HashSet<>();
        this.comments = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getAuthorUsername() { return username; }
    public String getUsername() { return username; }
    public String getImageName() { return imageName; }
    public String getCaption() { return caption; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public List<Comment> getComments() { return comments; }

    public int getLikesCount() { return likedBy.size(); }
    public boolean isLikedBy(String user) { return likedBy.contains(user); }
    public void like(String user) { likedBy.add(user); }
    public void unlike(String user) { likedBy.remove(user); }
    public void addComment(Comment comment) { comments.add(comment); }

    /**
     * Ruta real para UI: imagen está guardada dentro de INSTA_RAIZ/author/imagenes/
     */
    public String getImagePath() {
        return FileManager.getUserImageAbsolutePath(username, imageName);
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
}

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
    private List<String> imageNames;
    private String caption;
    private String username;
    private LocalDateTime date;
    private Set<String> likedBy;
    private List<Comment> comments;

    public Post(String username, String imageName, String caption) {
        this(username, Collections.singletonList(imageName), caption);
    }

    public Post(String username, List<String> imageNames, String caption) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.imageNames = new ArrayList<>(imageNames);
        this.caption = normalizeCaption(caption);
        this.date = LocalDateTime.now();
        this.likedBy = new HashSet<>();
        this.comments = new ArrayList<>();
    }

    private String normalizeCaption(String caption) {
        String clean = (caption == null) ? "" : caption.trim().replaceAll("[\\r\\n]+", " ");
        if (!clean.matches(".*#\\w+.*")) {
            clean = clean.isBlank() ? "#general" : clean + " #general";
        }
        return clean;
    }

    public String getId() {
        return id;
    }

    public String getAuthorUsername() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getImageNames() {
        return Collections.unmodifiableList(imageNames);
    }

    public String getImageName() {
        return imageNames.isEmpty() ? "" : imageNames.get(0);
    }

    public String getCaption() {
        return caption;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getLikesCount() {
        return likedBy.size();
    }

    public boolean isLikedBy(String user) {
        return likedBy.contains(user);
    }

    public void like(String user) {
        likedBy.add(user);
    }

    public void unlike(String user) {
        likedBy.remove(user);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public String getImagePath() {
        return FileManager.getUserImageAbsolutePath(username, getImageName());
    }

    public String getImagePath(int index) {
        if (index < 0 || index >= imageNames.size()) {
            return "";
        }
        return FileManager.getUserImageAbsolutePath(username, imageNames.get(index));
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan
 */

/**
 * Usuario con:
 * - estado activo/inactivo
 * - tipo de cuenta PUBLIC/PRIVATE (requisito)
 */
public class User extends AbstractAccount {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private char genero;
    private int edad;
    private String fotoPath;
    private LocalDate joinDate;

    private List<Post> posts;
    private List<String> followers;
    private List<String> followings;

    public User(String nombre, char genero, String username, String password, int edad, String fotoPath) {
        this.nombre = nombre;
        this.genero = genero;
        this.username = username;
        this.password = password;
        this.edad = edad;
        this.fotoPath = fotoPath;
        this.joinDate = LocalDate.now();
        this.isActive = true;
        this.accountType = AccountType.PUBLIC;

        this.posts = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public char getGenero() {
        return genero;
    }

    public int getEdad() {
        return edad;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowings() {
        return followings;
    }

    public void addPost(Post post) {
        this.posts.add(0, post);
    }

    public boolean isFollowing(String targetUsername) {
        return followings.stream().anyMatch(f -> f.equalsIgnoreCase(targetUsername));
    }

    public void follow(String targetUsername) {
        if (!isFollowing(targetUsername)) {
            followings.add(targetUsername);
        }
    }

    public void unfollow(String targetUsername) {
        followings.removeIf(f -> f.equalsIgnoreCase(targetUsername));
    }

    public void addFollower(String followerUsername) {
        boolean exists = followers.stream().anyMatch(f -> f.equalsIgnoreCase(followerUsername));
        if (!exists) {
            followers.add(followerUsername);
        }
    }

    public void removeFollower(String followerUsername) {
        followers.removeIf(f -> f.equalsIgnoreCase(followerUsername));
    }
}
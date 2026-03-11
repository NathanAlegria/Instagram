/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.io.Serializable;
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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private char genero;
    private String username;
    private String password;
    private int edad;
    private String fotoPath;         // solo nombre del archivo
    private LocalDate joinDate;
    private boolean isActive;

    private AccountType accountType; // 🚨 requisito: PUBLIC / PRIVATE

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

        // Por defecto público, luego el usuario lo cambia en su perfil
        this.accountType = AccountType.PUBLIC;

        this.posts = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public char getGenero() { return genero; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getEdad() { return edad; }
    public String getFotoPath() { return fotoPath; }
    public LocalDate getJoinDate() { return joinDate; }

    public List<Post> getPosts() { return posts; }
    public List<String> getFollowers() { return followers; }
    public List<String> getFollowings() { return followings; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public boolean isPrivateAccount() { return accountType == AccountType.PRIVATE; }

    // Acciones
    public void addPost(Post post) { this.posts.add(0, post); }

    public boolean isFollowing(String targetUsername) {
        return followings.contains(targetUsername);
    }

    public void follow(String targetUsername) {
        if (!isFollowing(targetUsername)) followings.add(targetUsername);
    }

    public void unfollow(String targetUsername) {
        followings.remove(targetUsername);
    }

    public void addFollower(String followerUsername) {
        if (!followers.contains(followerUsername)) followers.add(followerUsername);
    }

    public void removeFollower(String followerUsername) {
        followers.remove(followerUsername);
    }
}
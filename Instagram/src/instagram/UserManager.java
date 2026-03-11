/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
/**
 *
 * @author Nathan
 */

/**
 * UserManager:
 * - users.dat serializado (en INSTA_RAIZ)
 * - users.ins (texto) para registro visible
 * - estructura obligatoria por usuario
 * - follow/unfollow con privacidad y solicitudes
 * - inbox (mensajes) en inbox.bin serializado
 */
public class UserManager {
    private static UserManager instance;
    private List<User> users;

    private UserManager() {
        try {
            InstaPaths.ensureBaseStructure();
        } catch (IOException e) {
            System.err.println("No se pudo inicializar INSTA_RAIZ: " + e.getMessage());
        }

        users = loadUsers();
        if (users.isEmpty()) {
            setupTestData();
        }
    }

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    private void setupTestData() {
        try {
            User user1 = new User("Ana García", 'F', "anita123", "1234", 25, "default_user.png");
            User user2 = new User("Carlos Ruiz", 'M', "carlitos_r", "pass", 30, "default_user.png");

            InstaPaths.ensureUserStructure(user1.getUsername());
            InstaPaths.ensureUserStructure(user2.getUsername());

            users.add(user1);
            users.add(user2);
            saveUsers();

        } catch (Exception e) {
            System.err.println("Error creando test data: " + e.getMessage());
        }
    }

    // -------------------- Persistencia users.dat --------------------

    @SuppressWarnings("unchecked")
    private List<User> loadUsers() {
        File f = InstaPaths.usersDat();
        if (!f.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<User>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error al cargar users.dat: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(InstaPaths.usersDat()))) {
            oos.writeObject(users);
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar users.dat: " + e.getMessage());
            return false;
        }
    }

    public boolean saveUser(User user) {
        Optional<User> existing = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(user.getUsername()))
                .findFirst();

        if (existing.isPresent()) {
            int idx = users.indexOf(existing.get());
            users.set(idx, user);
        } else {
            users.add(user);
        }
        return saveUsers();
    }

    public User getUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    // -------------------- Registro / Login --------------------

    public boolean isUsernameUnique(String username) {
        if (users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
            return false;
        }

        // También revisamos users.ins por seguridad
        try (BufferedReader reader = new BufferedReader(new FileReader(InstaPaths.usersIns()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String existingUsername = line.split("#")[0];
                if (existingUsername.equalsIgnoreCase(username)) return false;
            }
        } catch (Exception ignored) {}
        return true;
    }

    public void registrarUsuario(User newUser) throws Exception {
        String username = newUser.getUsername();

        if (!isUsernameUnique(username)) {
            throw new Exception("El nombre de usuario '" + username + "' ya existe.");
        }

        // Crear estructura obligatoria
        InstaPaths.ensureUserStructure(username);

        // Guardar en users.ins (texto)
        // Formato: user#pass#nombre#gen#edad#foto#joinDate#isActive#accountType
        String userRecord = String.format("%s#%s#%s#%c#%d#%s#%s#%b#%s",
                newUser.getUsername(),
                newUser.getPassword(),
                newUser.getNombre(),
                newUser.getGenero(),
                newUser.getEdad(),
                newUser.getFotoPath(),
                newUser.getJoinDate().toString(),
                newUser.isActive(),
                newUser.getAccountType().name()
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(InstaPaths.usersIns(), true))) {
            writer.write(userRecord);
            writer.newLine();
        } catch (IOException e) {
            throw new Exception("Error al guardar users.ins.");
        }

        users.add(newUser);
        saveUsers();
    }

    public User login(String username, String password) throws InvalidCredentialsException {
        User user = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user == null) throw new InvalidCredentialsException("Usuario o contraseña incorrectos.");

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Tu cuenta está desactivada. Actívala desde tu perfil.");
        }
        return user;
    }

    public List<User> searchUsers(String query) {
        List<User> matches = new ArrayList<>();
        String q = query.toLowerCase();

        for (User u : users) {
            // requisito: no mostrar desactivados
            if (u.isActive() && u.getUsername().toLowerCase().contains(q)) {
                matches.add(u);
            }
        }
        return matches;
    }

    // -------------------- Privacidad / Amigos --------------------

    private boolean areFriends(String a, String b) {
        User ua = getUserByUsername(a);
        User ub = getUserByUsername(b);
        if (ua == null || ub == null) return false;

        // Comentario humano: “amigos” = follow mutuo (simple pero cumple regla)
        return ua.isFollowing(b) && ub.isFollowing(a);
    }

    public boolean canViewProfileContent(String viewerUsername, String ownerUsername) {
        User owner = getUserByUsername(ownerUsername);
        if (owner == null) return false;
        if (!owner.isActive()) return false;

        if (!owner.isPrivateAccount()) return true;
        if (viewerUsername == null) return false;

        return viewerUsername.equalsIgnoreCase(ownerUsername) || areFriends(viewerUsername, ownerUsername);
    }

    public boolean canSendMessage(String sender, String receiver) {
        User r = getUserByUsername(receiver);
        if (r == null) return false;
        if (!r.isActive()) return false;

        if (!r.isPrivateAccount()) return true;
        return areFriends(sender, receiver);
    }

    public void togglePrivacy(String username) {
        User u = getUserByUsername(username);
        if (u == null) return;

        if (u.getAccountType() == AccountType.PUBLIC) {
            u.setAccountType(AccountType.PRIVATE);
        } else {
            u.setAccountType(AccountType.PUBLIC);
        }
        saveUser(u);
    }

    // -------------------- Follow / Unfollow + Solicitudes --------------------

    public List<String> getPendingRequests(String username) {
        List<String> pending = new ArrayList<>();
        File f = InstaPaths.requestsIns(username);
        if (!f.exists()) return pending;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) pending.add(line.trim());
            }
        } catch (Exception ignored) {}
        return pending;
    }

    public void requestFollow(String followerUsername, String targetUsername) throws IOException {
        // No duplicados
        File req = InstaPaths.requestsIns(targetUsername);
        writeLineUnique(req, followerUsername);
    }

    public void approveFollowRequest(String targetUsername, String followerUsername) throws IOException {
        // 1) hacemos follow real en archivos
        writeLocalLine(targetUsername, "followers.ins", followerUsername);
        writeLocalLine(followerUsername, "following.ins", targetUsername);

        // 2) actualizamos objetos serializados
        User target = getUserByUsername(targetUsername);
        User follower = getUserByUsername(followerUsername);
        if (target != null && follower != null) {
            target.addFollower(followerUsername);
            follower.follow(targetUsername);
            saveUser(target);
            saveUser(follower);
        }

        // 3) quitamos solicitud
        removeLineAbsolute(InstaPaths.requestsIns(targetUsername), followerUsername);
    }

    public void rejectFollowRequest(String targetUsername, String followerUsername) throws IOException {
        removeLineAbsolute(InstaPaths.requestsIns(targetUsername), followerUsername);
    }

    public void toggleFollow(String followerUsername, String targetUsername) {
        User follower = getUserByUsername(followerUsername);
        User target = getUserByUsername(targetUsername);

        if (follower == null || target == null) return;
        if (followerUsername.equalsIgnoreCase(targetUsername)) return;
        if (!target.isActive()) return;

        boolean isFollowing = follower.isFollowing(targetUsername);

        try {
            if (isFollowing) {
                follower.unfollow(targetUsername);
                target.removeFollower(followerUsername);

                removeLineFromLocalFile(followerUsername, "following.ins", targetUsername);
                removeLineFromLocalFile(targetUsername, "followers.ins", followerUsername);

                saveUser(follower);
                saveUser(target);
                return;
            }

            // FOLLOW nuevo:
            // Si el target es privado y no son amigos -> solicitud
            if (target.isPrivateAccount() && !areFriends(followerUsername, targetUsername)) {
                requestFollow(followerUsername, targetUsername);
                return;
            }

            // público o privado pero ya amigos => follow directo
            follower.follow(targetUsername);
            target.addFollower(followerUsername);

            writeLocalLine(followerUsername, "following.ins", targetUsername);
            writeLocalLine(targetUsername, "followers.ins", followerUsername);

            saveUser(follower);
            saveUser(target);

        } catch (IOException e) {
            System.err.println("Error follow persistencia: " + e.getMessage());
        }
    }

    private void writeLineUnique(File file, String line) throws IOException {
        if (!file.exists()) file.createNewFile();

        List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
        for (String s : lines) {
            if (s.trim().equalsIgnoreCase(line.trim())) return;
        }
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
            w.write(line.trim());
            w.newLine();
        }
    }

    private void writeLocalLine(String username, String filename, String content) throws IOException {
        InstaPaths.ensureUserStructure(username);

        File file = new File(InstaPaths.userFolder(username), filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(content);
            writer.newLine();
        }
    }

    private void removeLineFromLocalFile(String username, String filename, String lineToRemove) throws IOException {
        File inputFile = new File(InstaPaths.userFolder(username), filename);
        File tempFile = new File(InstaPaths.userFolder(username), "temp_" + filename);

        if (!inputFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (!currentLine.trim().equalsIgnoreCase(lineToRemove.trim())) {
                    writer.write(currentLine);
                    writer.newLine();
                }
            }
        }

        if (!inputFile.delete()) throw new IOException("No se pudo borrar: " + inputFile.getName());
        if (!tempFile.renameTo(inputFile)) throw new IOException("No se pudo renombrar temporal.");
    }

    private void removeLineAbsolute(File file, String lineToRemove) throws IOException {
        if (!file.exists()) return;

        File tmp = new File(file.getParentFile(), "tmp_" + file.getName());
        try (BufferedReader r = new BufferedReader(new FileReader(file));
             BufferedWriter w = new BufferedWriter(new FileWriter(tmp))) {
            String cur;
            while ((cur = r.readLine()) != null) {
                if (!cur.trim().equalsIgnoreCase(lineToRemove.trim())) {
                    w.write(cur);
                    w.newLine();
                }
            }
        }
        if (!file.delete()) throw new IOException("No se pudo borrar: " + file.getName());
        if (!tmp.renameTo(file)) throw new IOException("No se pudo renombrar temporal.");
    }

    // -------------------- Posts: publish + load feed --------------------

    private Post parsePostFromLine(String line) {
        try {
            String[] parts = line.split("#", 4);
            if (parts.length < 4) return null;

            String author = parts[0];
            String dateString = parts[1];
            String imageName = parts[2];
            String caption = parts[3];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            LocalDateTime postDate = LocalDateTime.parse(dateString, formatter);

            Post post = new Post(author, imageName, caption);
            post.setDate(postDate);
            return post;

        } catch (Exception e) {
            return null;
        }
    }

    public List<Post> loadPostsFromLocalFile(String username) {
        List<Post> userPosts = new ArrayList<>();
        File file = InstaPaths.instaIns(username);

        if (!file.exists()) return userPosts;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Post p = parsePostFromLine(line);
                if (p != null) userPosts.add(p);
            }
        } catch (IOException e) {
            System.err.println("Error leyendo insta.ins de " + username + ": " + e.getMessage());
        }

        return userPosts;
    }

    private List<String> loadFollowingsFromLocalFile(String username) {
        List<String> followings = new ArrayList<>();
        File file = InstaPaths.followingIns(username);
        if (!file.exists()) return followings;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) followings.add(line.trim());
            }
        } catch (IOException ignored) {}
        return followings;
    }

    public List<Post> getAllRelevantPostsByDate(User loggedUser) {
        if (loggedUser == null) return Collections.emptyList();

        List<Post> all = new ArrayList<>();

        // propios
        if (loggedUser.isActive()) {
            all.addAll(loadPostsFromLocalFile(loggedUser.getUsername()));
        }

        // seguidos
        for (String followed : loadFollowingsFromLocalFile(loggedUser.getUsername())) {
            User fu = getUserByUsername(followed);
            if (fu != null && fu.isActive()) {
                all.addAll(loadPostsFromLocalFile(followed));
            }
        }

        all.sort(Comparator.comparing(Post::getDate).reversed());
        return all;
    }

    public void publishPost(Post post) throws IOException {
        User user = getUserByUsername(post.getUsername());
        if (user == null) throw new IllegalArgumentException("Usuario no encontrado.");

        // 1) actualizar en memoria
        user.addPost(post);
        saveUser(user);

        // 2) persistir insta.ins
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String dateString = post.getDate().format(formatter);

        String record = String.format("%s#%s#%s#%s",
                post.getUsername(),
                dateString,
                post.getImageName(),
                post.getCaption().replaceAll("[\r\n]", " ")
        );

        try (BufferedWriter w = new BufferedWriter(new FileWriter(InstaPaths.instaIns(user.getUsername()), true))) {
            w.write(record);
            w.newLine();
        }
    }

    public List<Post> findMentions(String targetUsername) {
        List<Post> mentioned = new ArrayList<>();
        String cleaned = targetUsername.replace("@", "").toLowerCase();
        String search = "@" + cleaned;

        // Evitar duplicados por ID
        Set<String> seen = new HashSet<>();

        for (User u : users) {
            if (!u.isActive()) continue;

            for (Post p : u.getPosts()) {
                String cap = p.getCaption();
                if (cap == null) continue;

                if (cap.toLowerCase().contains(search)) {
                    if (seen.add(p.getId())) mentioned.add(p);
                }
            }
        }

        mentioned.sort(Comparator.comparing(Post::getDate).reversed());
        return mentioned;
    }

    public boolean addCommentAndSave(Post post, Comment newComment) {
        User author = getUserByUsername(post.getAuthorUsername());
        if (author == null) return false;

        post.addComment(newComment);

        List<Post> posts = author.getPosts();
        boolean replaced = false;
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(post.getId())) {
                posts.set(i, post);
                replaced = true;
                break;
            }
        }

        if (!replaced) return false;
        return saveUser(author);
    }

    // -------------------- INBOX (Instagram-like) --------------------

    @SuppressWarnings("unchecked")
    public List<Message> readInbox(String username) {
        File file = InstaPaths.inboxBin(username);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Message>) ois.readObject();
        } catch (Exception e) {
            // Comentario humano: si el inbox se daña, no tumbamos la app
            return new ArrayList<>();
        }
    }

    private void writeInbox(String username, List<Message> list) throws IOException {
        File file = InstaPaths.inboxBin(username);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(list);
        }
    }

    public void sendMessage(String from, String to, String content, MessageType type) throws IOException {
        if (content == null) content = "";
        if (content.length() > 300) content = content.substring(0, 300);

        if (!canSendMessage(from, to)) {
            throw new IOException("No puedes enviar mensajes: cuenta privada y no son amigos.");
        }

        InstaPaths.ensureUserStructure(from);
        InstaPaths.ensureUserStructure(to);

        Message msg = new Message(from, to, content, type);

        // Guardamos en ambos inbox para que ambos vean la conversación
        List<Message> a = readInbox(from);
        a.add(msg);
        writeInbox(from, a);

        List<Message> b = readInbox(to);
        b.add(msg);
        writeInbox(to, b);
    }

    public void markConversationRead(String owner, String other) throws IOException {
        List<Message> list = readInbox(owner);
        for (Message m : list) {
            boolean sameChat =
                    (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other)) ||
                    (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner));

            if (sameChat && m.getTo().equalsIgnoreCase(owner)) {
                // Solo marco como leído lo que me llegó a mí
                m.markRead();
            }
        }
        writeInbox(owner, list);
    }

    public void deleteConversation(String owner, String other) throws IOException {
        List<Message> list = readInbox(owner);
        list.removeIf(m ->
                (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other)) ||
                (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner))
        );
        writeInbox(owner, list);
    }

    /**
     * Devuelve la lista de "chats" (usuarios con los que tengo conversación),
     * ordenados por el mensaje más reciente.
     */
    public List<String> getChatList(String owner) {
        List<Message> inbox = readInbox(owner);

        Map<String, LocalDateTime> last = new HashMap<>();
        Map<String, Integer> unreadCount = new HashMap<>();

        for (Message m : inbox) {
            String other = m.getFrom().equalsIgnoreCase(owner) ? m.getTo() : m.getFrom();
            last.put(other, m.getDateTime());

            // Contar no leídos que me llegaron a mí
            if (m.getTo().equalsIgnoreCase(owner) && m.getStatus() == MessageStatus.UNREAD) {
                unreadCount.put(other, unreadCount.getOrDefault(other, 0) + 1);
            }
        }

        List<String> chats = new ArrayList<>(last.keySet());
        chats.sort((a, b) -> last.get(b).compareTo(last.get(a)));
        return chats;
    }

    public int getUnreadCount(String owner, String other) {
        int c = 0;
        for (Message m : readInbox(owner)) {
            boolean sameChat =
                    (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other)) ||
                    (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner));
            if (sameChat && m.getTo().equalsIgnoreCase(owner) && m.getStatus() == MessageStatus.UNREAD) c++;
        }
        return c;
    }

    public List<Message> getConversation(String owner, String other) {
        List<Message> all = readInbox(owner);
        List<Message> conv = new ArrayList<>();

        for (Message m : all) {
            boolean sameChat =
                    (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other)) ||
                    (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner));
            if (sameChat) conv.add(m);
        }

        conv.sort(Comparator.comparing(Message::getDateTime));
        return conv;
    }
}
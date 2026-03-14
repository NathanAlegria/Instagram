/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica;

import Exceptions.InvalidCredentialsException;
import Exceptions.AccountInactiveException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @author Nathan
 */

/**
 * UserManager: - users.dat serializado (en INSTA_RAIZ) - users.ins (texto) para
 * registro visible - estructura obligatoria por usuario - follow/unfollow con
 * privacidad y solicitudes - inbox (mensajes) en inbox.bin serializado
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
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private void setupTestData() {
        try {
            User user1 = new User("Nathan Alegria", 'M', "NathanPRO", "1234", 25, "default_user.png");
            User user2 = new User("Carlos Pedrito", 'M', "carlitos", "pass", 30, "default_user.png");
            User user3 = new User("Juanita Alexa", 'F', "Jaun_Al", "Juanita123", 23, "default_user.png");

            InstaPaths.ensureUserStructure(user1.getUsername());
            InstaPaths.ensureUserStructure(user2.getUsername());

            users.add(user1);
            users.add(user2);
            saveUsers();
        } catch (Exception e) {
            System.err.println("Error creando test data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<User> loadUsers() {
        File f = InstaPaths.usersDat();
        if (!f.exists()) {
            return new ArrayList<>();
        }

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

    public boolean isUsernameUnique(String username) {
        return users.stream().noneMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public void registrarUsuario(User newUser) throws Exception {
        String username = newUser.getUsername();

        if (!isUsernameUnique(username)) {
            throw new Exception("El nombre de usuario '" + username + "' ya existe.");
        }

        InstaPaths.ensureUserStructure(username);

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

    public User login(String username, String password) throws InvalidCredentialsException, AccountInactiveException {
        User user = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user == null) {
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos.");
        }

        if (!user.isActive()) {
            throw new AccountInactiveException("Tu cuenta está desactivada.");
        }

        return user;
    }

    public void setActive(String username, boolean active) {
        User user = getUserByUsername(username);
        if (user == null) {
            return;
        }

        if (active) {
            user.activate();
        } else {
            user.deactivate();
        }
        saveUser(user);
    }

    public List<User> searchUsers(String query) {
        List<User> matches = new ArrayList<>();
        String q = query.toLowerCase();

        for (User u : users) {
            if (u.isActive() && u.getUsername().toLowerCase().contains(q)) {
                matches.add(u);
            }
        }
        return matches;
    }

    private boolean isApprovedFollower(String viewerUsername, String ownerUsername) {
        User viewer = getUserByUsername(viewerUsername);
        return viewer != null && viewer.isFollowing(ownerUsername);
    }

    public boolean canViewProfileContent(String viewerUsername, String ownerUsername) {
        User owner = getUserByUsername(ownerUsername);
        if (owner == null || !owner.isActive()) {
            return false;
        }

        if (!owner.isPrivateAccount()) {
            return true;
        }

        if (viewerUsername == null) {
            return false;
        }

        return viewerUsername.equalsIgnoreCase(ownerUsername) || isApprovedFollower(viewerUsername, ownerUsername);
    }

    public boolean canSendMessage(String sender, String receiver) {
        User r = getUserByUsername(receiver);
        if (r == null || !r.isActive()) {
            return false;
        }

        if (!r.isPrivateAccount()) {
            return true;
        }

        return isApprovedFollower(sender, receiver);
    }

    public void togglePrivacy(String username) {
        User u = getUserByUsername(username);
        if (u == null) {
            return;
        }

        u.setAccountType(u.getAccountType() == AccountType.PUBLIC ? AccountType.PRIVATE : AccountType.PUBLIC);
        saveUser(u);
    }

    public List<String> getPendingRequests(String username) {
        List<String> pending = new ArrayList<>();
        File f = InstaPaths.requestsIns(username);

        if (!f.exists()) {
            return pending;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    pending.add(line.trim());
                }
            }
        } catch (Exception ignored) {
        }

        return pending;
    }

    public void requestFollow(String followerUsername, String targetUsername) throws IOException {
        File req = InstaPaths.requestsIns(targetUsername);
        writeLineUnique(req, followerUsername);
    }

    public void approveFollowRequest(String targetUsername, String followerUsername) throws IOException {
        User target = getUserByUsername(targetUsername);
        User follower = getUserByUsername(followerUsername);

        if (target == null || follower == null) {
            return;
        }

        target.addFollower(followerUsername);
        follower.follow(targetUsername);

        writeLineUnique(InstaPaths.followersIns(targetUsername), followerUsername);
        writeLineUnique(InstaPaths.followingIns(followerUsername), targetUsername);

        saveUser(target);
        saveUser(follower);

        removeLineAbsolute(InstaPaths.requestsIns(targetUsername), followerUsername);
    }

    public void rejectFollowRequest(String targetUsername, String followerUsername) throws IOException {
        removeLineAbsolute(InstaPaths.requestsIns(targetUsername), followerUsername);
    }

    public void followBackDirect(String actorUsername, String targetUsername) throws IOException {
        User actor = getUserByUsername(actorUsername);
        User target = getUserByUsername(targetUsername);

        if (actor == null || target == null) {
            return;
        }

        if (actorUsername.equalsIgnoreCase(targetUsername)) {
            return;
        }

        if (!actor.isFollowing(targetUsername)) {
            actor.follow(targetUsername);
            target.addFollower(actorUsername);

            writeLineUnique(InstaPaths.followingIns(actorUsername), targetUsername);
            writeLineUnique(InstaPaths.followersIns(targetUsername), actorUsername);

            saveUser(actor);
            saveUser(target);
        }
    }

    public void toggleFollow(String followerUsername, String targetUsername) {
        User follower = getUserByUsername(followerUsername);
        User target = getUserByUsername(targetUsername);

        if (follower == null || target == null) {
            return;
        }

        if (followerUsername.equalsIgnoreCase(targetUsername)) {
            return;
        }

        if (!target.isActive()) {
            return;
        }

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

            if (target.isPrivateAccount()) {
                requestFollow(followerUsername, targetUsername);
                return;
            }

            follower.follow(targetUsername);
            target.addFollower(followerUsername);

            writeLineUnique(InstaPaths.followingIns(followerUsername), targetUsername);
            writeLineUnique(InstaPaths.followersIns(targetUsername), followerUsername);

            saveUser(follower);
            saveUser(target);

        } catch (IOException e) {
            System.err.println("Error follow persistencia: " + e.getMessage());
        }
    }

    private void writeLineUnique(File file, String line) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }

        List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
        for (String s : lines) {
            if (s.trim().equalsIgnoreCase(line.trim())) {
                return;
            }
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
            w.write(line.trim());
            w.newLine();
        }
    }

    private void removeLineFromLocalFile(String username, String filename, String lineToRemove) throws IOException {
        File inputFile = new File(InstaPaths.userFolder(username), filename);
        File tempFile = new File(InstaPaths.userFolder(username), "temp_" + filename);

        if (!inputFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile)); BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (!currentLine.trim().equalsIgnoreCase(lineToRemove.trim())) {
                    writer.write(currentLine);
                    writer.newLine();
                }
            }
        }

        if (!inputFile.delete()) {
            throw new IOException("No se pudo borrar: " + inputFile.getName());
        }
        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("No se pudo renombrar temporal.");
        }
    }

    private void removeLineAbsolute(File file, String lineToRemove) throws IOException {
        if (!file.exists()) {
            return;
        }

        File tmp = new File(file.getParentFile(), "tmp_" + file.getName());

        try (BufferedReader r = new BufferedReader(new FileReader(file)); BufferedWriter w = new BufferedWriter(new FileWriter(tmp))) {
            String cur;
            while ((cur = r.readLine()) != null) {
                if (!cur.trim().equalsIgnoreCase(lineToRemove.trim())) {
                    w.write(cur);
                    w.newLine();
                }
            }
        }

        if (!file.delete()) {
            throw new IOException("No se pudo borrar: " + file.getName());
        }
        if (!tmp.renameTo(file)) {
            throw new IOException("No se pudo renombrar temporal.");
        }
    }

    public List<Post> loadPostsFromLocalFile(String username) {
        User user = getUserByUsername(username);
        if (user == null || !user.isActive()) {
            return new ArrayList<>();
        }

        List<Post> posts = new ArrayList<>(user.getPosts());
        posts.sort(Comparator.comparing(Post::getDate).reversed());
        return posts;
    }

    public List<Post> getAllRelevantPostsByDate(User loggedUser) {
        if (loggedUser == null || !loggedUser.isActive()) {
            return Collections.emptyList();
        }

        List<Post> all = new ArrayList<>();
        all.addAll(loadPostsFromLocalFile(loggedUser.getUsername()));

        for (String followed : loggedUser.getFollowings()) {
            User u = getUserByUsername(followed);
            if (u != null && u.isActive()) {
                all.addAll(loadPostsFromLocalFile(followed));
            }
        }

        all.sort(Comparator.comparing(Post::getDate).reversed());
        return all;
    }

    public void publishPost(Post post) throws IOException {
        User user = getUserByUsername(post.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        user.addPost(post);
        saveUser(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String dateString = post.getDate().format(formatter);
        String images = String.join("|", post.getImageName());

        String record = String.format("%s#%s#%s#%s",
                post.getUsername(),
                dateString,
                images,
                post.getCaption().replaceAll("[\\r\\n]", " ")
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
        Set<String> seen = new HashSet<>();

        for (User u : users) {
            if (!u.isActive()) {
                continue;
            }

            for (Post p : u.getPosts()) {
                String cap = p.getCaption();
                if (cap != null && cap.toLowerCase().contains(search)) {
                    if (seen.add(p.getId())) {
                        mentioned.add(p);
                    }
                }
            }
        }

        mentioned.sort(Comparator.comparing(Post::getDate).reversed());
        return mentioned;
    }

    public boolean addCommentAndSave(Post post, Comment newComment) {
        User author = getUserByUsername(post.getAuthorUsername());
        if (author == null) {
            return false;
        }

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

        if (!replaced) {
            return false;
        }

        return saveUser(author);
    }

    @SuppressWarnings("unchecked")
    public List<Message> readInbox(String username) {
        File file = InstaPaths.inboxBin(username);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Message>) ois.readObject();
        } catch (Exception e) {
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
        if (content == null) {
            content = "";
        }

        if (content.length() > 300) {
            content = content.substring(0, 300);
        }

        if (!canSendMessage(from, to)) {
            throw new IOException("No puedes enviar mensajes: cuenta privada y solicitud no aprobada.");
        }

        InstaPaths.ensureUserStructure(from);
        InstaPaths.ensureUserStructure(to);

        Message msg = new Message(from, to, content, type);

        List<Message> a = readInbox(from);
        a.add(msg);
        writeInbox(from, a);

        List<Message> b = readInbox(to);
        b.add(msg);
        writeInbox(to, b);
    }

    public void sendStickerImage(String from, String to, String stickerFileName) throws IOException {
        if (!canSendMessage(from, to)) {
            throw new IOException("No puedes enviar mensajes: cuenta privada y solicitud no aprobada.");
        }

        InstaPaths.ensureUserStructure(from);
        InstaPaths.ensureUserStructure(to);

        Message msg = new Message(from, to, stickerFileName, MessageType.STICKER_IMAGE, from);

        List<Message> a = readInbox(from);
        a.add(msg);
        writeInbox(from, a);

        List<Message> b = readInbox(to);
        b.add(msg);
        writeInbox(to, b);
    }

    public List<String> getOwnStickers(String username) {
        File dir = InstaPaths.userPersonalStickers(username);
        List<String> result = new ArrayList<>();

        if (!dir.exists()) {
            return result;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return result;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for (File f : files) {
            if (f.isFile()) {
                result.add(f.getName());
            }
        }

        return result;
    }

    public void markConversationRead(String owner, String other) throws IOException {
        List<Message> list = readInbox(owner);

        for (Message m : list) {
            boolean sameChat
                    = (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other))
                    || (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner));

            if (sameChat && m.getTo().equalsIgnoreCase(owner)) {
                m.markRead();
            }
        }

        writeInbox(owner, list);
    }

    public void deleteConversation(String owner, String other) throws IOException {
        List<Message> list = readInbox(owner);
        list.removeIf(m
                -> (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other))
                || (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner))
        );
        writeInbox(owner, list);
    }

    public List<String> getChatList(String owner) {
        List<Message> inbox = readInbox(owner);
        Map<String, LocalDateTime> last = new HashMap<>();

        for (Message m : inbox) {
            String other = m.getFrom().equalsIgnoreCase(owner) ? m.getTo() : m.getFrom();
            last.put(other, m.getDateTime());
        }

        List<String> chats = new ArrayList<>(last.keySet());
        chats.sort((a, b) -> last.get(b).compareTo(last.get(a)));
        return chats;
    }

    public int getUnreadCount(String owner, String other) {
        int c = 0;

        for (Message m : readInbox(owner)) {
            boolean sameChat
                    = (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other))
                    || (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner));

            if (sameChat && m.getTo().equalsIgnoreCase(owner) && m.getStatus() == MessageStatus.UNREAD) {
                c++;
            }
        }

        return c;
    }

    public List<Message> getConversation(String owner, String other) {
        List<Message> all = readInbox(owner);
        List<Message> conv = new ArrayList<>();

        for (Message m : all) {
            boolean sameChat
                    = (m.getFrom().equalsIgnoreCase(owner) && m.getTo().equalsIgnoreCase(other))
                    || (m.getFrom().equalsIgnoreCase(other) && m.getTo().equalsIgnoreCase(owner));

            if (sameChat) {
                conv.add(m);
            }
        }

        conv.sort(Comparator.comparing(Message::getDateTime));
        return conv;
    }

    public synchronized void appendDeliveredMessageToInboxes(Message msg) throws IOException {
        if (msg == null) {
            return;
        }

        InstaPaths.ensureUserStructure(msg.getFrom());
        InstaPaths.ensureUserStructure(msg.getTo());

        List<Message> senderInbox = readInbox(msg.getFrom());
        senderInbox.add(msg);
        writeInbox(msg.getFrom(), senderInbox);

        List<Message> receiverInbox = readInbox(msg.getTo());
        receiverInbox.add(msg);
        writeInbox(msg.getTo(), receiverInbox);
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO; 
/**
 *
 * @author Nathan
 */

/**
 * InstagramProject (UI)
 * - CardLayout global: LOGIN / REGISTER / ROOT_VIEW
 * - CardLayout centro: MAIN / PROFILE_SEARCH / CREATE_POST / HASHTAG_SEARCH / NOTIFICATIONS / INBOX
 */
public class InstagramProject extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private CardLayout contentLayout;
    private JPanel centerContentPanel;

    private UserManager userManager;
    private User loggedUser;

    // PERFIL / SEARCH
    private JPanel profileCardContainer;
    private JPanel resultsPanel;
    private JTextField txtSearchUser;

    // NOTIFICATIONS (FIX: ahora sí instancia)
    private JPanel notificationsListPanel;

    // INBOX
    private JPanel inboxListPanel;
    private JPanel inboxConversationPanel;
    private JLabel inboxHeaderLabel;
    private String inboxOpenUser = null; // con quién estoy chateando

    // Colores dark
    private final Color BG_COLOR = new Color(0, 0, 0);
    private final Color INPUT_BG = new Color(38, 38, 38);
    private final Color TEXT_COLOR = new Color(250, 250, 250);
    private final Color BORDER_COLOR = new Color(54, 54, 54);
    private final Color BTN_BLUE = new Color(0, 149, 246);
    private final Color POST_BG = new Color(18, 18, 18);

    public InstagramProject() {
        userManager = UserManager.getInstance();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(900, 700));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // ROOT_VIEW (logueado)
        JPanel rootView = new JPanel(new BorderLayout());
        rootView.setBackground(BG_COLOR);

        JPanel sidebar = crearSidebarDesktop();
        rootView.add(sidebar, BorderLayout.WEST);

        contentLayout = new CardLayout();
        centerContentPanel = new JPanel(contentLayout);

        centerContentPanel.add(crearFeedContentWrapper(), "MAIN");
        centerContentPanel.add(crearPanelProfileSearch(), "PROFILE_SEARCH");
        centerContentPanel.add(crearPanelCrearPostContent(), "CREATE_POST");
        centerContentPanel.add(crearPanelHashtagSearchContent(), "HASHTAG_SEARCH");
        centerContentPanel.add(crearPanelNotificacionesContent(), "NOTIFICATIONS");
        centerContentPanel.add(crearPanelInboxContent(), "INBOX");

        rootView.add(centerContentPanel, BorderLayout.CENTER);

        mainPanel.add(crearPanelLogin(), "LOGIN");
        mainPanel.add(crearPanelRegistro(), "REGISTER");
        mainPanel.add(rootView, "ROOT_VIEW");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "LOGIN");
    }

    // ---------------- LOGIN ----------------

    private JPanel crearPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);

        JPanel loginCard = new JPanel(null);
        loginCard.setPreferredSize(new Dimension(400, 600));
        loginCard.setBackground(BG_COLOR);
        loginCard.setBorder(new LineBorder(BORDER_COLOR, 1));

        JLabel title = new JLabel("Instagram", SwingConstants.CENTER);
        title.setFont(new Font("Segoe Script", Font.BOLD, 40));
        title.setForeground(TEXT_COLOR);
        title.setBounds(50, 60, 300, 60);
        loginCard.add(title);

        JTextField txtUser = styledTextField("Usuario");
        txtUser.setBounds(50, 150, 300, 40);

        JPasswordField txtPass = styledPasswordField("Contraseña");
        txtPass.setBounds(50, 200, 300, 40);

        loginCard.add(txtUser);
        loginCard.add(txtPass);

        JButton btnLogin = styledButton("Entrar");
        btnLogin.setBounds(50, 260, 300, 40);
        btnLogin.addActionListener(e -> {
            try {
                String u = txtUser.getText().trim();
                String p = new String(txtPass.getPassword()).trim();

                if (u.isEmpty() || p.isEmpty()) throw new EmptyFieldException("Llena todos los campos");

                loggedUser = userManager.login(u, p);

                cardLayout.show(mainPanel, "ROOT_VIEW");
                contentLayout.show(centerContentPanel, "MAIN");
                rebuildMainFeed();

                JOptionPane.showMessageDialog(this, "Bienvenido " + loggedUser.getNombre(),
                        "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);

                txtUser.setText("");
                txtPass.setText("");

            } catch (Exception ex) {
                int opt = JOptionPane.showConfirmDialog(this,
                        ex.getMessage() + "\n¿Deseas intentar de nuevo (Yes) o Crear cuenta (No)?",
                        "Error de Login", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

                if (opt == JOptionPane.NO_OPTION) {
                    cardLayout.show(mainPanel, "REGISTER");
                }
            }
        });
        loginCard.add(btnLogin);

        JButton btnGoRegister = createLinkButton("¿No tienes una cuenta? Regístrate");
        btnGoRegister.setBounds(50, 550, 300, 30);
        btnGoRegister.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        loginCard.add(btnGoRegister);

        panel.add(loginCard);
        return panel;
    }

    // ---------------- REGISTER ----------------

    private JPanel crearPanelRegistro() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);

        JPanel registerCard = new JPanel(null);
        registerCard.setPreferredSize(new Dimension(400, 600));
        registerCard.setBackground(BG_COLOR);
        registerCard.setBorder(new LineBorder(BORDER_COLOR, 1));

        final String[] photoName = {""};

        JLabel title = new JLabel("Crear Cuenta", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        title.setBounds(50, 30, 300, 30);
        registerCard.add(title);

        JTextField txtNombre = styledTextField("Nombre Completo");
        txtNombre.setBounds(50, 80, 300, 35);

        JTextField txtUser = styledTextField("Username (Único)");
        txtUser.setBounds(50, 125, 300, 35);

        JPasswordField txtPass = styledPasswordField("Contraseña");
        txtPass.setBounds(50, 170, 300, 35);

        JTextField txtEdad = styledTextField("Edad");
        txtEdad.setBounds(50, 215, 140, 35);

        JRadioButton rbM = new JRadioButton("M");
        JRadioButton rbF = new JRadioButton("F");
        styleRadioButton(rbM);
        styleRadioButton(rbF);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbM);
        bg.add(rbF);

        JPanel genderPanel = new JPanel();
        genderPanel.setBackground(BG_COLOR);
        genderPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), "Género", 0, 0, null, Color.GRAY));
        genderPanel.setBounds(210, 215, 140, 45);
        genderPanel.add(rbM);
        genderPanel.add(rbF);
        registerCard.add(genderPanel);

        JButton btnPhoto = new JButton("Seleccionar Foto de Perfil...");
        btnPhoto.setBackground(INPUT_BG);
        btnPhoto.setForeground(Color.WHITE);
        btnPhoto.setBounds(50, 280, 300, 30);

        btnPhoto.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes JPG & PNG", "jpg", "png", "jpeg");
            fileChooser.setFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    // Guardamos al registrar, cuando ya sabemos username (aquí aún no),
                    // así que solo guardamos path temporal y luego lo copiamos con FileManager.
                    photoName[0] = selectedFile.getAbsolutePath();

                    btnPhoto.setText(selectedFile.getName());
                    btnPhoto.setForeground(BTN_BLUE);

                } catch (Exception ex) {
                    btnPhoto.setText("Error al cargar");
                    btnPhoto.setForeground(Color.RED);
                }
            }
        });

        registerCard.add(btnPhoto);
        registerCard.add(txtNombre);
        registerCard.add(txtUser);
        registerCard.add(txtPass);
        registerCard.add(txtEdad);

        JButton btnRegister = styledButton("Registrarte");
        btnRegister.setBounds(50, 330, 300, 40);
        btnRegister.addActionListener(e -> {
            try {
                if (txtNombre.getText().isEmpty() || txtUser.getText().isEmpty()
                        || new String(txtPass.getPassword()).isEmpty() || txtEdad.getText().isEmpty()) {
                    throw new EmptyFieldException("Todos los campos son obligatorios.");
                }

                int edad = Integer.parseInt(txtEdad.getText());
                char genero = rbM.isSelected() ? 'M' : (rbF.isSelected() ? 'F' : ' ');
                if (genero == ' ') throw new EmptyFieldException("Selecciona un género.");

                String username = txtUser.getText().trim();

                // Foto: si no eligió, usamos default
                String finalPhotoName = "default_user.png";
                if (!photoName[0].isEmpty()) {
                    // Copiamos la foto a INSTA_RAIZ/username/imagenes/
                    finalPhotoName = FileManager.saveUserImage(username, new File(photoName[0]), "profile");
                }

                User newUser = new User(
                        txtNombre.getText().trim(),
                        genero,
                        username,
                        new String(txtPass.getPassword()),
                        edad,
                        finalPhotoName
                );

                userManager.registrarUsuario(newUser);
                loggedUser = newUser;

                JOptionPane.showMessageDialog(this, "¡Cuenta creada exitosamente!", "Registro", JOptionPane.INFORMATION_MESSAGE);

                // limpiar
                txtNombre.setText("");
                txtUser.setText("");
                txtPass.setText("");
                txtEdad.setText("");
                photoName[0] = "";
                btnPhoto.setText("Seleccionar Foto de Perfil...");
                btnPhoto.setForeground(Color.WHITE);
                bg.clearSelection();

                cardLayout.show(mainPanel, "ROOT_VIEW");
                contentLayout.show(centerContentPanel, "MAIN");
                rebuildMainFeed();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registro", JOptionPane.ERROR_MESSAGE);
            }
        });
        registerCard.add(btnRegister);

        JButton btnBack = createLinkButton("¿Ya tienes cuenta? Entrar");
        btnBack.setBounds(50, 550, 300, 30);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        registerCard.add(btnBack);

        panel.add(registerCard);
        return panel;
    }

    // ---------------- SIDEBAR ----------------

    private JPanel crearSidebarDesktop() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_COLOR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(new LineBorder(BORDER_COLOR, 1));

        JLabel title = new JLabel("Instagram", SwingConstants.LEFT);
        title.setFont(new Font("Segoe Script", Font.BOLD, 22));
        title.setForeground(TEXT_COLOR);
        title.setBorder(new EmptyBorder(20, 15, 20, 15));
        sidebar.add(title, BorderLayout.NORTH);

        JPanel navLinks = new JPanel();
        navLinks.setLayout(new BoxLayout(navLinks, BoxLayout.Y_AXIS));
        navLinks.setBackground(BG_COLOR);
        navLinks.setBorder(new EmptyBorder(10, 15, 10, 15));

        navLinks.add(createSidebarButton("🏠 Inicio", "MAIN"));
        navLinks.add(createSidebarButton("🔍 Búsqueda", "PROFILE_SEARCH"));
        navLinks.add(createSidebarButton("✨ Crear", "CREATE_POST"));
        navLinks.add(createSidebarButton("💬 Inbox", "INBOX")); // ✅ NUEVO
        navLinks.add(createSidebarButton("🔔 Notificaciones", "NOTIFICATIONS"));
        navLinks.add(createSidebarButton("🔎 Buscar Hashtag", "HASHTAG_SEARCH"));
        navLinks.add(createSidebarButton("👤 Perfil", "MY_PROFILE"));

        navLinks.add(Box.createVerticalGlue());
        navLinks.add(createSidebarButton("🚪 Salir", "LOGOUT"));

        sidebar.add(navLinks, BorderLayout.CENTER);
        return sidebar;
    }

    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(BG_COLOR);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMargin(new Insets(10, 0, 10, 0));

        btn.addActionListener(e -> {
            if (cardName.equals("LOGOUT")) {
                int opt = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    loggedUser = null;
                    cardLayout.show(mainPanel, "LOGIN");
                }
                return;
            }

            // seguridad
            cardLayout.show(mainPanel, "ROOT_VIEW");
            if (loggedUser == null) {
                JOptionPane.showMessageDialog(this, "Debes iniciar sesión.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cardName.equals("MAIN")) {
                rebuildMainFeed();
                contentLayout.show(centerContentPanel, "MAIN");
                return;
            }

            if (cardName.equals("MY_PROFILE")) {
                contentLayout.show(centerContentPanel, "PROFILE_SEARCH");
                mostrarPerfil(loggedUser);
                return;
            }

            if (cardName.equals("PROFILE_SEARCH")) {
                mostrarPanelDeBusqueda();
                contentLayout.show(centerContentPanel, "PROFILE_SEARCH");
                return;
            }

            if (cardName.equals("NOTIFICATIONS")) {
                contentLayout.show(centerContentPanel, "NOTIFICATIONS");
                loadNotifications();
                return;
            }

            if (cardName.equals("INBOX")) {
                contentLayout.show(centerContentPanel, "INBOX");
                reloadInboxChats(); // ✅ cargar chats
                return;
            }

            contentLayout.show(centerContentPanel, cardName);
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(18, 18, 18)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(BG_COLOR); }
        });

        return btn;
    }

    // ---------------- FEED ----------------

    private JPanel crearFeedContentWrapper() {
        JPanel contentAreaWrapper = new JPanel(new GridBagLayout());
        contentAreaWrapper.setBackground(BG_COLOR);

        JPanel feedContent = new JPanel();
        feedContent.setLayout(new BoxLayout(feedContent, BoxLayout.Y_AXIS));
        feedContent.setBackground(BG_COLOR);
        feedContent.setName("FEED_POSTS_INNER_PANEL");

        int feedWidth = 550;

        loadFeedPosts(feedContent, feedWidth);

        JScrollPane scrollPane = new JScrollPane(feedContent);
        scrollPane.setName("FEED_SCROLL_PANE");
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;

        contentAreaWrapper.add(scrollPane, gbc);
        return contentAreaWrapper;
    }

    private void loadFeedPosts(JPanel feedContent, int feedWidth) {
        feedContent.removeAll();

        if (loggedUser == null) {
            JLabel err = new JLabel("Inicia sesión para ver el Feed.", SwingConstants.CENTER);
            err.setForeground(TEXT_COLOR);
            err.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedContent.add(err);
            return;
        }

        List<Post> allPosts = userManager.getAllRelevantPostsByDate(loggedUser);

        if (allPosts.isEmpty()) {
            JLabel emptyMessage = new JLabel("<html><div style='text-align: center; width: " + (feedWidth - 50) + "px;'><b>¡Bienvenido!</b><br>Sigue a tus amigos para ver publicaciones.</div></html>", SwingConstants.CENTER);
            emptyMessage.setForeground(Color.GRAY);
            emptyMessage.setFont(new Font("SansSerif", Font.BOLD, 14));
            emptyMessage.setBorder(new EmptyBorder(50, 0, 0, 0));
            emptyMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedContent.add(emptyMessage);
        } else {
            for (Post post : allPosts) {
                JPanel postPanel = createPostFeedView(post, feedWidth - 50);
                postPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                feedContent.add(postPanel);
                feedContent.add(Box.createVerticalStrut(20));
            }
        }

        feedContent.revalidate();
        feedContent.repaint();
    }

    private JPanel createPostFeedView(Post post, int feedWidth) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(POST_BG);
        panel.setMaximumSize(new Dimension(feedWidth, 420));
        panel.setBorder(new LineBorder(BORDER_COLOR, 1, true));

        JLabel lblImage = new JLabel(cargarImagenCuadrada(post.getImagePath(), 350));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblImage, BorderLayout.CENTER);

        JLabel lblCaption = new JLabel("<html><b>@" + post.getAuthorUsername() + "</b>: " + post.getCaption() + "</html>");
        lblCaption.setForeground(TEXT_COLOR);
        lblCaption.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(lblCaption, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPostDetail(post);
            }
        });

        return panel;
    }

    // ---------------- CREATE POST ----------------

    private JPanel crearPanelCrearPostContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(BG_COLOR);

        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(500, 500));
        formCard.setBackground(POST_BG);
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("✨ Crear Nueva Publicación", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        formCard.add(title, BorderLayout.NORTH);

        JPanel postForm = new JPanel();
        postForm.setLayout(new BoxLayout(postForm, BoxLayout.Y_AXIS));
        postForm.setBackground(POST_BG);
        postForm.setBorder(new EmptyBorder(10, 0, 0, 0));

        JTextArea txtCaption = new JTextArea(5, 20);
        txtCaption.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtCaption.setForeground(TEXT_COLOR);
        txtCaption.setBackground(INPUT_BG);
        txtCaption.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        txtCaption.setCaretColor(TEXT_COLOR);
        txtCaption.setLineWrap(true);
        txtCaption.setWrapStyleWord(true);

        JScrollPane captionScroll = new JScrollPane(txtCaption);
        captionScroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER_COLOR), "Escribe la descripción...", 0, 0, null, Color.GRAY));
        captionScroll.setMaximumSize(new Dimension(460, 150));
        captionScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        postForm.add(captionScroll);
        postForm.add(Box.createVerticalStrut(20));

        JButton btnSelectImage = styledButton("Seleccionar Imagen");
        btnSelectImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSelectImage.setMaximumSize(new Dimension(460, 40));

        final String[] imageName = {""};
        JLabel lblImageStatus = createDetailLabel("Archivo: Ninguno seleccionado");
        lblImageStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSelectImage.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes JPG & PNG", "jpg", "png", "jpeg");
            fileChooser.setFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                try {
                    if (loggedUser == null) throw new Exception("Debes iniciar sesión.");

                    // Guardamos en INSTA_RAIZ/username/imagenes/
                    imageName[0] = FileManager.saveUserImage(loggedUser.getUsername(), selectedFile, "post");

                    lblImageStatus.setText("Imagen guardada: " + imageName[0]);
                    lblImageStatus.setForeground(BTN_BLUE);

                } catch (Exception ex) {
                    lblImageStatus.setText("Error: " + ex.getMessage());
                    lblImageStatus.setForeground(Color.RED);
                }
            }
        });

        postForm.add(btnSelectImage);
        postForm.add(Box.createVerticalStrut(5));
        postForm.add(lblImageStatus);
        postForm.add(Box.createVerticalGlue());

        JButton btnPost = styledButton("Publicar");
        btnPost.setMaximumSize(new Dimension(460, 40));
        btnPost.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPost.setBackground(new Color(255, 105, 180));

        btnPost.addActionListener(e -> {
            try {
                if (loggedUser == null) throw new Exception("Debes iniciar sesión.");
                if (imageName[0].isEmpty()) throw new EmptyFieldException("Debes seleccionar una imagen.");

                Post newPost = new Post(loggedUser.getUsername(), imageName[0], txtCaption.getText().trim());
                userManager.publishPost(newPost);

                // refrescar usuario
                loggedUser = userManager.getUserByUsername(loggedUser.getUsername());

                JOptionPane.showMessageDialog(this, "Publicación creada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                txtCaption.setText("");
                imageName[0] = "";
                lblImageStatus.setText("Archivo: Ninguno seleccionado");
                lblImageStatus.setForeground(TEXT_COLOR);

                contentLayout.show(centerContentPanel, "MAIN");
                rebuildMainFeed();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al publicar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(postForm, BorderLayout.CENTER);
        formCard.add(btnPost, BorderLayout.SOUTH);

        centerWrapper.add(formCard);
        panel.add(centerWrapper, BorderLayout.CENTER);

        return panel;
    }

    // ---------------- PROFILE SEARCH ----------------

    private JPanel crearPanelProfileSearch() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BG_COLOR);

        profileCardContainer = new JPanel(new CardLayout());
        profileCardContainer.setPreferredSize(new Dimension(630, 650));
        profileCardContainer.setBackground(BG_COLOR);

        profileCardContainer.add(crearSearchInputView(), "SEARCH_INPUT");
        profileCardContainer.add(new JPanel(), "PROFILE_VIEW");

        CardLayout cl = (CardLayout) profileCardContainer.getLayout();
        cl.show(profileCardContainer, "SEARCH_INPUT");

        centerPanel.add(profileCardContainer);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearSearchInputView() {
        JPanel searchInputView = new JPanel(new BorderLayout());
        searchInputView.setBackground(BG_COLOR);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Buscar Personas", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtSearchUser = styledTextField("Escribe un username...");
        txtSearchUser.setMaximumSize(new Dimension(400, 40));
        txtSearchUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnSearch = styledButton("Buscar");
        btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSearch.setMaximumSize(new Dimension(400, 40));

        topPanel.add(title);
        topPanel.add(Box.createVerticalStrut(20));
        topPanel.add(txtSearchUser);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(btnSearch);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BG_COLOR);

        JScrollPane scrollResults = new JScrollPane(resultsPanel);
        scrollResults.setBorder(null);
        scrollResults.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        btnSearch.addActionListener(e -> performUserSearch(txtSearchUser.getText()));

        searchInputView.add(topPanel, BorderLayout.NORTH);
        searchInputView.add(scrollResults, BorderLayout.CENTER);

        return searchInputView;
    }

    private void performUserSearch(String query) {
        if (query == null || query.trim().isEmpty() || query.trim().equals("Escribe un username...")) {
            JOptionPane.showMessageDialog(this, "Ingresa un nombre de usuario válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resultsPanel.removeAll();

        List<User> foundUsers = userManager.searchUsers(query.trim());
        if (foundUsers.isEmpty()) {
            JLabel lblNotFound = new JLabel("No se encontraron usuarios.");
            lblNotFound.setForeground(Color.RED);
            lblNotFound.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(lblNotFound);
        } else {
            for (User user : foundUsers) {
                if (!user.getUsername().equalsIgnoreCase(loggedUser.getUsername())) {
                    resultsPanel.add(createUserResultItem(user));
                    resultsPanel.add(Box.createVerticalStrut(10));
                }
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createUserResultItem(User user) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(POST_BG);
        item.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        item.setMaximumSize(new Dimension(500, 60));

        JLabel lblPhoto = new JLabel(cargarImagenCuadrada(FileManager.getUserImageAbsolutePath(user.getUsername(), user.getFotoPath()), 40));
        lblPhoto.setBorder(new EmptyBorder(5, 5, 5, 5));
        item.add(lblPhoto, BorderLayout.WEST);

        JLabel lblInfo = new JLabel("<html><b>@" + user.getUsername() + "</b><br>" + user.getNombre() + "</html>");
        lblInfo.setForeground(TEXT_COLOR);
        item.add(lblInfo, BorderLayout.CENTER);

        JButton btnView = styledButton("Ver Perfil");
        btnView.setPreferredSize(new Dimension(100, 30));
        btnView.addActionListener(e -> mostrarPerfil(user));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setBackground(POST_BG);
        btnWrapper.add(btnView);
        item.add(btnWrapper, BorderLayout.EAST);

        return item;
    }

    private void mostrarPanelDeBusqueda() {
        CardLayout clProfile = (CardLayout) profileCardContainer.getLayout();
        clProfile.show(profileCardContainer, "SEARCH_INPUT");

        if (resultsPanel != null) {
            resultsPanel.removeAll();
            JLabel lblWelcome = new JLabel("Escribe un username para buscar.");
            lblWelcome.setForeground(Color.GRAY);
            lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(lblWelcome);
            resultsPanel.revalidate();
            resultsPanel.repaint();
        }

        if (txtSearchUser != null) txtSearchUser.setText("Escribe un username...");
    }

    // ---------------- PROFILE VIEW (con PRIVACIDAD + SOLICITUDES + DM) ----------------

    private void mostrarPerfil(User targetUser) {
        User refreshed = userManager.getUserByUsername(targetUser.getUsername());
        if (refreshed == null) {
            JOptionPane.showMessageDialog(this, "Error al cargar perfil.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel profileView = buildProfileView(refreshed);

        // Limpieza de vista anterior (para no acumular)
        for (Component comp : profileCardContainer.getComponents()) {
            if ("PROFILE_VIEW_CONTENT".equals(comp.getName())) {
                profileCardContainer.remove(comp);
                break;
            }
        }

        profileView.setName("PROFILE_VIEW_CONTENT");
        profileCardContainer.add(profileView, "PROFILE_VIEW");

        CardLayout cl = (CardLayout) profileCardContainer.getLayout();
        cl.show(profileCardContainer, "PROFILE_VIEW");

        profileCardContainer.revalidate();
        profileCardContainer.repaint();
    }

    private JPanel buildProfileView(User targetUser) {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(BG_COLOR);
        profilePanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Foto
        int photoSize = 120;
        JLabel lblPhoto = new JLabel();
        lblPhoto.setPreferredSize(new Dimension(photoSize, photoSize));
        lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhoto.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 39), 3));

        // En fotos: ahora está en INSTA_RAIZ/target/imagenes/
        String absPhoto = FileManager.getUserImageAbsolutePath(targetUser.getUsername(), targetUser.getFotoPath());
        lblPhoto.setIcon(cargarImagenCuadrada(absPhoto, photoSize - 6));

        headerPanel.add(lblPhoto, BorderLayout.WEST);

        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(BG_COLOR);

        JLabel lblUsername = new JLabel("@" + targetUser.getUsername() + " ✅");
        lblUsername.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblUsername.setForeground(TEXT_COLOR);
        infoPanel.add(lblUsername);

        JLabel lblPrivacy = new JLabel("Cuenta: " + (targetUser.isPrivateAccount() ? "🔒 PRIVADA" : "🌐 PÚBLICA"));
        lblPrivacy.setForeground(Color.GRAY);
        infoPanel.add(lblPrivacy);
        infoPanel.add(Box.createVerticalStrut(8));

        // Stats
        int postCount = userManager.loadPostsFromLocalFile(targetUser.getUsername()).size();
        JPanel stats = new JPanel();
        stats.setBackground(BG_COLOR);
        stats.setLayout(new BoxLayout(stats, BoxLayout.X_AXIS));
        stats.add(createStatPanel(String.valueOf(postCount), "publicaciones"));
        stats.add(Box.createHorizontalStrut(25));
        stats.add(createStatPanel(String.valueOf(targetUser.getFollowers().size()), "seguidores"));
        stats.add(Box.createHorizontalStrut(25));
        stats.add(createStatPanel(String.valueOf(targetUser.getFollowings().size()), "seguidos"));

        infoPanel.add(stats);
        infoPanel.add(Box.createVerticalStrut(10));

        infoPanel.add(createDetailLabel("Nombre: " + targetUser.getNombre()));
        infoPanel.add(createDetailLabel("Género: " + targetUser.getGenero()));
        infoPanel.add(createDetailLabel("Edad: " + targetUser.getEdad()));
        infoPanel.add(createDetailLabel("Miembro desde: " + targetUser.getJoinDate()));

        headerPanel.add(infoPanel, BorderLayout.CENTER);

        // Action panel
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(BG_COLOR);

        boolean isOwnProfile = loggedUser != null && targetUser.getUsername().equalsIgnoreCase(loggedUser.getUsername());

        if (isOwnProfile) {
            // ✅ BOTÓN PRIVACIDAD (requisito)
            JButton btnPrivacy = styledButton(targetUser.isPrivateAccount() ? "Cambiar a PÚBLICA 🌐" : "Cambiar a PRIVADA 🔒");
            btnPrivacy.setBackground(new Color(54, 54, 54));
            btnPrivacy.addActionListener(e -> {
                userManager.togglePrivacy(loggedUser.getUsername());
                loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                mostrarPerfil(loggedUser);
            });
            actionPanel.add(btnPrivacy);
            actionPanel.add(Box.createVerticalStrut(10));

            // ✅ SOLICITUDES PENDIENTES (si privado)
            if (targetUser.isPrivateAccount()) {
                actionPanel.add(createFollowRequestsPanel(targetUser));
                actionPanel.add(Box.createVerticalStrut(10));
            }

        } else {
            // Follow / request follow
            boolean isFollowing = loggedUser.isFollowing(targetUser.getUsername());

            JButton btnFollow = styledButton(isFollowing ? "DEJAR DE SEGUIR" : "SEGUIR");
            if (isFollowing) btnFollow.setBackground(new Color(54, 54, 54));
            btnFollow.addActionListener(e -> {
                userManager.toggleFollow(loggedUser.getUsername(), targetUser.getUsername());
                loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                mostrarPerfil(targetUser);
                rebuildMainFeed();
            });

            actionPanel.add(btnFollow);
            actionPanel.add(Box.createVerticalStrut(10));

            // ✅ DM (inbox): solo si permitido por privacidad
            JButton btnMessage = styledButton("Enviar Mensaje 💬");
            btnMessage.setBackground(new Color(255, 105, 180));
            btnMessage.addActionListener(e -> {
                if (!userManager.canSendMessage(loggedUser.getUsername(), targetUser.getUsername())) {
                    JOptionPane.showMessageDialog(this, "No puedes enviar DM: cuenta privada y no son amigos.", "Privacidad", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Abrimos inbox directamente con ese usuario
                inboxOpenUser = targetUser.getUsername();
                contentLayout.show(centerContentPanel, "INBOX");
                openConversation(inboxOpenUser);
            });
            actionPanel.add(btnMessage);
        }

        headerPanel.add(actionPanel, BorderLayout.EAST);

        profilePanel.add(headerPanel, BorderLayout.NORTH);

        // Contenido de posts
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);

        boolean canView = userManager.canViewProfileContent(
                loggedUser != null ? loggedUser.getUsername() : null,
                targetUser.getUsername()
        );

        if (!canView) {
            JLabel locked = new JLabel("🔒 Esta cuenta es privada. Solo amigos pueden ver sus posts.", SwingConstants.CENTER);
            locked.setForeground(Color.GRAY);
            locked.setFont(new Font("SansSerif", Font.BOLD, 14));
            contentPanel.add(locked, BorderLayout.CENTER);

        } else {
            JPanel postsGrid = crearPostsGrid(targetUser);
            JScrollPane scrollPane = new JScrollPane(postsGrid);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            contentPanel.add(scrollPane, BorderLayout.CENTER);
        }

        profilePanel.add(contentPanel, BorderLayout.CENTER);
        return profilePanel;
    }

    private JPanel createFollowRequestsPanel(User me) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(POST_BG);
        box.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        box.setMaximumSize(new Dimension(260, 220));

        JLabel title = new JLabel("Solicitudes pendientes");
        title.setForeground(TEXT_COLOR);
        title.setBorder(new EmptyBorder(8, 8, 8, 8));
        box.add(title);

        List<String> pending = userManager.getPendingRequests(me.getUsername());
        if (pending.isEmpty()) {
            JLabel empty = new JLabel("No hay solicitudes.");
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(0, 8, 8, 8));
            box.add(empty);
            return box;
        }

        for (String follower : pending) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(POST_BG);
            row.setBorder(new EmptyBorder(4, 8, 4, 8));

            JLabel lbl = new JLabel("@" + follower);
            lbl.setForeground(TEXT_COLOR);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            btns.setBackground(POST_BG);

            JButton ok = styledButton("Aceptar");
            ok.setPreferredSize(new Dimension(90, 28));
            ok.addActionListener(e -> {
                try {
                    userManager.approveFollowRequest(me.getUsername(), follower);
                    loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                    mostrarPerfil(loggedUser);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton no = styledButton("Rechazar");
            no.setPreferredSize(new Dimension(90, 28));
            no.setBackground(new Color(80, 80, 80));
            no.addActionListener(e -> {
                try {
                    userManager.rejectFollowRequest(me.getUsername(), follower);
                    mostrarPerfil(loggedUser);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btns.add(ok);
            btns.add(no);

            row.add(lbl, BorderLayout.WEST);
            row.add(btns, BorderLayout.EAST);

            box.add(row);
        }

        return box;
    }

    private JPanel crearPostsGrid(User targetUser) {
        List<Post> posts = userManager.loadPostsFromLocalFile(targetUser.getUsername());

        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(BG_COLOR);
        gridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        if (posts.isEmpty()) {
            JLabel noPosts = new JLabel("No hay publicaciones aún.", SwingConstants.CENTER);
            noPosts.setForeground(Color.GRAY);
            gridPanel.add(noPosts);
            return gridPanel;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(1, 1, 1, 1);

        int cols = (Config.MODO == VisualMode.MOBILE) ? 3 : 3;
        int thumbnailSize = (Config.MODO == VisualMode.MOBILE) ? 115 : 200;

        int col = 0, row = 0;

        for (Post post : posts) {
            JButton thumb = new JButton();
            thumb.setPreferredSize(new Dimension(thumbnailSize, thumbnailSize));
            thumb.setBorder(null);
            thumb.setBackground(POST_BG);
            thumb.setCursor(new Cursor(Cursor.HAND_CURSOR));

            ImageIcon icon = cargarImagenCuadrada(post.getImagePath(), thumbnailSize);
            if (icon != null) thumb.setIcon(icon);
            else {
                thumb.setText("IMG");
                thumb.setForeground(Color.RED);
            }

            thumb.addActionListener(e -> showPostDetail(post));

            gbc.gridx = col;
            gbc.gridy = row;
            gridPanel.add(thumb, gbc);

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.weighty = 1.0;
        gbc.gridwidth = cols;
        gridPanel.add(Box.createVerticalGlue(), gbc);

        return gridPanel;
    }

    // ---------------- HASHTAG SEARCH (simple) ----------------

    private JPanel crearPanelHashtagSearchContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBackground(POST_BG);
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtHashtag = new JTextField(25);
        txtHashtag.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtHashtag.setText("#Buscar");

        JButton btnSearch = styledButton("Buscar");
        btnSearch.setPreferredSize(new Dimension(100, 30));

        searchPanel.add(txtHashtag);
        searchPanel.add(btnSearch);
        panel.add(searchPanel, BorderLayout.NORTH);

        JPanel resultsWrapper = new JPanel(new BorderLayout());
        resultsWrapper.setBackground(BG_COLOR);
        panel.add(resultsWrapper, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            String input = txtHashtag.getText().trim();
            if (input.isEmpty() || input.equals("#Buscar")) {
                JOptionPane.showMessageDialog(this, "Ingresa un hashtag válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String tag = input.startsWith("#") ? input : ("#" + input);

            // Mostrar resultados simples en feed style: recorremos users.dat posts serializados en memoria
            // Nota humana: para mantenerlo simple, buscamos en los posts del feed actual
            resultsWrapper.removeAll();

            List<Post> found = new ArrayList<>();
            List<Post> all = userManager.getAllRelevantPostsByDate(loggedUser);
            for (Post p : all) {
                if (p.getCaption() != null && p.getCaption().toLowerCase().contains(tag.toLowerCase())) {
                    found.add(p);
                }
            }

            if (found.isEmpty()) {
                JLabel no = new JLabel("No se encontró: " + tag, SwingConstants.CENTER);
                no.setForeground(Color.GRAY);
                resultsWrapper.add(no, BorderLayout.CENTER);
            } else {
                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                container.setBackground(BG_COLOR);

                for (Post p : found) {
                    container.add(createPostFeedView(p, 520));
                    container.add(Box.createVerticalStrut(10));
                }

                JScrollPane sp = new JScrollPane(container);
                sp.setBorder(null);
                sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                resultsWrapper.add(sp, BorderLayout.CENTER);
            }

            resultsWrapper.revalidate();
            resultsWrapper.repaint();
        });

        txtHashtag.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(FocusEvent evt) { if (txtHashtag.getText().equals("#Buscar")) txtHashtag.setText(""); }
            @Override public void focusLost(FocusEvent evt) { if (txtHashtag.getText().isEmpty()) txtHashtag.setText("#Buscar"); }
        });

        return panel;
    }

    // ---------------- NOTIFICATIONS (FIX) ----------------

    private JPanel crearPanelNotificacionesContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("🔔 Notificaciones (Menciones)", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setBorder(new EmptyBorder(15, 0, 15, 0));
        panel.add(title, BorderLayout.NORTH);

        // ✅ FIX: esta sí es la variable de instancia que usa loadNotifications()
        notificationsListPanel = new JPanel();
        notificationsListPanel.setLayout(new BoxLayout(notificationsListPanel, BoxLayout.Y_AXIS));
        notificationsListPanel.setBackground(BG_COLOR);
        notificationsListPanel.setBorder(new EmptyBorder(10, 50, 10, 50));

        JScrollPane scrollPane = new JScrollPane(notificationsListPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void loadNotifications() {
        if (notificationsListPanel == null || loggedUser == null) return;

        notificationsListPanel.removeAll();

        String me = loggedUser.getUsername();
        List<Post> mentions = userManager.findMentions(me);

        if (mentions.isEmpty()) {
            JLabel emptyMsg = new JLabel("No tienes menciones recientes (@" + me + ").", SwingConstants.CENTER);
            emptyMsg.setForeground(Color.GRAY);
            emptyMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
            notificationsListPanel.add(Box.createVerticalStrut(50));
            notificationsListPanel.add(emptyMsg);
        } else {
            for (Post post : mentions) {
                JPanel item = createMentionItem(post);
                item.setAlignmentX(Component.CENTER_ALIGNMENT);
                notificationsListPanel.add(item);
                notificationsListPanel.add(Box.createVerticalStrut(10));
            }
        }

        notificationsListPanel.revalidate();
        notificationsListPanel.repaint();
    }

    private JPanel createMentionItem(Post post) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(POST_BG);
        item.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        item.setMaximumSize(new Dimension(520, 90));

        String author = post.getAuthorUsername();

        JLabel lblText = new JLabel("<html><b>@" + author + "</b> te mencionó: <i>"
                + safeSub(post.getCaption(), 50) + "</i></html>");
        lblText.setForeground(TEXT_COLOR);
        lblText.setBorder(new EmptyBorder(10, 10, 10, 10));
        item.add(lblText, BorderLayout.CENTER);

        JLabel lblImage = new JLabel(cargarImagenCuadrada(post.getImagePath(), 60));
        lblImage.setBorder(new EmptyBorder(5, 5, 5, 10));
        item.add(lblImage, BorderLayout.EAST);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showPostDetail(post); }
        });

        return item;
    }

    private String safeSub(String s, int max) {
        if (s == null) return "";
        return s.substring(0, Math.min(s.length(), max)) + (s.length() > max ? "..." : "");
    }

    // ---------------- INBOX (Instagram-like) ----------------

    private JPanel crearPanelInboxContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(12, 12, 12, 12));

        inboxHeaderLabel = new JLabel("💬 Inbox", SwingConstants.LEFT);
        inboxHeaderLabel.setForeground(TEXT_COLOR);
        inboxHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        header.add(inboxHeaderLabel, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Split: lista chats (izq) + conversación (der)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(320);
        split.setResizeWeight(0.0);

        // Left: chats
        inboxListPanel = new JPanel();
        inboxListPanel.setLayout(new BoxLayout(inboxListPanel, BoxLayout.Y_AXIS));
        inboxListPanel.setBackground(BG_COLOR);
        inboxListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane chatScroll = new JScrollPane(inboxListPanel);
        chatScroll.setBorder(null);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Right: conversation
        inboxConversationPanel = new JPanel(new BorderLayout());
        inboxConversationPanel.setBackground(BG_COLOR);
        inboxConversationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel hint = new JLabel("Selecciona un chat para ver mensajes.", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        inboxConversationPanel.add(hint, BorderLayout.CENTER);

        split.setLeftComponent(chatScroll);
        split.setRightComponent(inboxConversationPanel);

        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void reloadInboxChats() {
        if (loggedUser == null || inboxListPanel == null) return;

        inboxListPanel.removeAll();

        List<String> chats = userManager.getChatList(loggedUser.getUsername());

        // Barra "Nuevo mensaje"
        JPanel newMsgBar = new JPanel(new BorderLayout(5, 5));
        newMsgBar.setBackground(POST_BG);
        newMsgBar.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        newMsgBar.setMaximumSize(new Dimension(280, 44));

        JTextField toField = new JTextField();
        toField.setBackground(INPUT_BG);
        toField.setForeground(TEXT_COLOR);
        toField.setCaretColor(TEXT_COLOR);
        toField.setBorder(new EmptyBorder(8, 8, 8, 8));
        toField.setToolTipText("Escribe un username para abrir chat");

        JButton btnOpen = styledButton("Abrir");
        btnOpen.setPreferredSize(new Dimension(90, 36));
        btnOpen.addActionListener(e -> {
            String target = toField.getText().trim();
            if (target.isEmpty()) return;
            if (target.equalsIgnoreCase(loggedUser.getUsername())) return;

            User u = userManager.getUserByUsername(target);
            if (u == null || !u.isActive()) {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.", "Inbox", JOptionPane.WARNING_MESSAGE);
                return;
            }

            inboxOpenUser = target;
            openConversation(inboxOpenUser);
        });

        newMsgBar.add(toField, BorderLayout.CENTER);
        newMsgBar.add(btnOpen, BorderLayout.EAST);

        inboxListPanel.add(newMsgBar);
        inboxListPanel.add(Box.createVerticalStrut(12));

        if (chats.isEmpty()) {
            JLabel empty = new JLabel("No tienes conversaciones aún.");
            empty.setForeground(Color.GRAY);
            inboxListPanel.add(empty);
        } else {
            for (String other : chats) {
                inboxListPanel.add(createChatItem(other));
                inboxListPanel.add(Box.createVerticalStrut(8));
            }
        }

        inboxListPanel.revalidate();
        inboxListPanel.repaint();
    }

    private JPanel createChatItem(String otherUsername) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(POST_BG);
        item.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        item.setMaximumSize(new Dimension(280, 60));

        JLabel name = new JLabel("@" + otherUsername);
        name.setForeground(TEXT_COLOR);
        name.setBorder(new EmptyBorder(10, 10, 10, 10));

        int unread = userManager.getUnreadCount(loggedUser.getUsername(), otherUsername);
        JLabel badge = new JLabel(unread > 0 ? ("  " + unread + "  ") : "");
        badge.setForeground(Color.WHITE);
        badge.setOpaque(unread > 0);
        badge.setBackground(BTN_BLUE);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        right.setBackground(POST_BG);
        right.add(badge);

        item.add(name, BorderLayout.WEST);
        item.add(right, BorderLayout.EAST);

        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                inboxOpenUser = otherUsername;
                openConversation(otherUsername);
            }
        });

        return item;
    }

    private void openConversation(String otherUsername) {
        if (loggedUser == null) return;

        // Marcar leídos al abrir
        try {
            userManager.markConversationRead(loggedUser.getUsername(), otherUsername);
        } catch (Exception ignored) {}

        inboxHeaderLabel.setText("💬 @" + otherUsername);

        inboxConversationPanel.removeAll();
        inboxConversationPanel.setLayout(new BorderLayout());

        // Mensajes
        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BG_COLOR);

        List<Message> conv = userManager.getConversation(loggedUser.getUsername(), otherUsername);

        for (Message m : conv) {
            messagesPanel.add(createMessageBubble(m));
            messagesPanel.add(Box.createVerticalStrut(6));
        }

        JScrollPane sp = new JScrollPane(messagesPanel);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Input
        JPanel input = new JPanel(new BorderLayout(6, 6));
        input.setBackground(BG_COLOR);
        input.setBorder(new EmptyBorder(10, 0, 0, 0));

        JTextField txt = new JTextField();
        txt.setBackground(INPUT_BG);
        txt.setForeground(TEXT_COLOR);
        txt.setCaretColor(TEXT_COLOR);
        txt.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnSticker = styledButton("😀");
        btnSticker.setPreferredSize(new Dimension(60, 40));

        JButton btnSend = styledButton("Enviar");
        btnSend.setPreferredSize(new Dimension(90, 40));

        JButton btnDelete = styledButton("🗑");
        btnDelete.setPreferredSize(new Dimension(60, 40));
        btnDelete.setBackground(new Color(80, 80, 80));

        btnDelete.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this, "¿Borrar conversación con @" + otherUsername + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    userManager.deleteConversation(loggedUser.getUsername(), otherUsername);
                    inboxOpenUser = null;
                    reloadInboxChats();
                    inboxConversationPanel.removeAll();
                    JLabel hint = new JLabel("Conversación borrada.", SwingConstants.CENTER);
                    hint.setForeground(Color.GRAY);
                    inboxConversationPanel.add(hint, BorderLayout.CENTER);
                    inboxConversationPanel.revalidate();
                    inboxConversationPanel.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnSend.addActionListener(e -> {
            String text = txt.getText().trim();
            if (text.isEmpty()) return;

            try {
                userManager.sendMessage(loggedUser.getUsername(), otherUsername, text, MessageType.TEXT);
                txt.setText("");
                openConversation(otherUsername);
                reloadInboxChats();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Privacidad", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnSticker.addActionListener(e -> {
            // Mini picker simple (cumple "tipo Sticker")
            String[] stickers = {"❤️", "😂", "👏", "😢", "😊"};
            String pick = (String) JOptionPane.showInputDialog(this, "Elige un sticker", "Stickers",
                    JOptionPane.PLAIN_MESSAGE, null, stickers, stickers[0]);
            if (pick == null) return;

            try {
                userManager.sendMessage(loggedUser.getUsername(), otherUsername, pick, MessageType.STICKER);
                openConversation(otherUsername);
                reloadInboxChats();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Privacidad", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftBtns.setBackground(BG_COLOR);
        leftBtns.add(btnSticker);
        leftBtns.add(btnDelete);

        input.add(leftBtns, BorderLayout.WEST);
        input.add(txt, BorderLayout.CENTER);
        input.add(btnSend, BorderLayout.EAST);

        inboxConversationPanel.add(sp, BorderLayout.CENTER);
        inboxConversationPanel.add(input, BorderLayout.SOUTH);

        inboxConversationPanel.revalidate();
        inboxConversationPanel.repaint();

        // Scroll al final (como IG)
        SwingUtilities.invokeLater(() -> sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum()));
    }

    private JPanel createMessageBubble(Message m) {
        boolean mine = m.getFrom().equalsIgnoreCase(loggedUser.getUsername());

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_COLOR);

        JLabel bubble = new JLabel();
        bubble.setOpaque(true);
        bubble.setBorder(new EmptyBorder(10, 12, 10, 12));
        bubble.setFont(new Font("SansSerif", Font.PLAIN, 14));

        String content = m.getType() == MessageType.STICKER ? (m.getContent() + "  ") : m.getContent();

        bubble.setText("<html>" + content + "<br><small style='color: #cccccc;'>" + m.getFormattedDateTime() + "</small></html>");

        if (mine) {
            bubble.setBackground(new Color(0, 149, 246));
            bubble.setForeground(Color.WHITE);
            row.add(bubble, BorderLayout.EAST);
        } else {
            bubble.setBackground(new Color(50, 50, 50));
            bubble.setForeground(Color.WHITE);
            row.add(bubble, BorderLayout.WEST);
        }

        return row;
    }

    // ---------------- POST DETAIL (simple) ----------------

    private void showPostDetail(Post post) {
        // Minimal: abrir diálogo de comentarios como ya manejabas
        JOptionPane.showMessageDialog(this, "Post de @" + post.getAuthorUsername() + "\n\n" + post.getCaption(),
                "Post", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- REBUILD FEED ----------------

    public void rebuildMainFeed() {
        if (loggedUser != null) loggedUser = userManager.getUserByUsername(loggedUser.getUsername());

        // Buscar el scroll pane
        JScrollPane scrollPane = (JScrollPane) getComponentByName(mainPanel, "FEED_SCROLL_PANE");
        if (scrollPane == null) {
            contentLayout.show(centerContentPanel, "MAIN");
            return;
        }

        JPanel feedContentPanel = (JPanel) getComponentByName(scrollPane.getViewport(), "FEED_POSTS_INNER_PANEL");
        if (feedContentPanel == null) return;

        loadFeedPosts(feedContentPanel, 550);
        contentLayout.show(centerContentPanel, "MAIN");

        mainPanel.revalidate();
        mainPanel.repaint();

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    private Component getComponentByName(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) return comp;
            if (comp instanceof Container) {
                Component found = getComponentByName((Container) comp, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    // ---------------- IMAGEN (robusto) ----------------

    private ImageIcon cargarImagenCuadrada(String absolutePath, int size) {
        try {
            File imgFile = new File(absolutePath);
            if (!imgFile.exists()) {
                // fallback humano: si no existe, devuelvo placeholder vacío pero no rompo UI
                return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
            }

            BufferedImage original = ImageIO.read(imgFile);
            if (original == null) return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));

            Image scaled = original.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            return new ImageIcon(out);

        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
        }
    }

    // ---------------- HELPERS UI ----------------

    private JLabel createDetailLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_COLOR);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
        return lbl;
    }

    private JPanel createStatPanel(String count, String label) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);

        JLabel lblCount = new JLabel(count, SwingConstants.CENTER);
        lblCount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCount.setForeground(TEXT_COLOR);
        lblCount.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLabel = new JLabel(label, SwingConstants.CENTER);
        lblLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblLabel.setForeground(Color.GRAY);
        lblLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(lblCount);
        p.add(lblLabel);
        return p;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setBackground(INPUT_BG);
        field.setCaretColor(TEXT_COLOR);
        field.setText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        field.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setBackground(INPUT_BG);
        field.setCaretColor(TEXT_COLOR);
        field.setText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        field.setEchoChar((char) 0);

        field.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('*');
                    field.setForeground(TEXT_COLOR);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).isEmpty()) {
                    field.setEchoChar((char) 0);
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BTN_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 10, 10, 10));
        return btn;
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(BTN_BLUE);
        btn.setBackground(BG_COLOR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private void styleRadioButton(JRadioButton rb) {
        rb.setBackground(BG_COLOR);
        rb.setForeground(TEXT_COLOR);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rb.setFocusPainted(false);
    }
}
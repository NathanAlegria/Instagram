/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan
 */
public class InstagramProject extends JPanel {

    private final Color BG = new Color(0, 0, 0);
    private final Color PANEL = new Color(18, 18, 18);
    private final Color INPUT = new Color(38, 38, 38);
    private final Color BORDER = new Color(54, 54, 54);
    private final Color TEXT = new Color(250, 250, 250);
    private final Color MUTED = new Color(160, 160, 160);
    private final Color BLUE = new Color(0, 149, 246);

    private CardLayout rootLayout;
    private JPanel rootPanel;

    private CardLayout contentLayout;
    private JPanel contentPanel;
    private String currentContentCard = "FEED";

    private CardLayout searchViewLayout;
    private JPanel searchViewPanel;
    private JPanel profileViewWrapper;
    private String currentProfileUsername;

    private JPanel bannerHost;
    private JPanel confirmHost;

    private UserManager userManager;
    private User loggedUser;

    private JPanel feedInner;

    private JPanel searchResultsPanel;
    private JTextField txtSearchUser;

    private JPanel notificationsListPanel;

    private JPanel hashtagResultsPanel;

    private JPanel chatListPanel;
    private JPanel chatConversationPanel;
    private JLabel chatTitleLabel;
    private String openedChatUser;

    private ChatClient chatClient;

    public InstagramProject() {
        userManager = UserManager.getInstance();

        setLayout(new BorderLayout());
        setBackground(BG);

        bannerHost = new JPanel();
        bannerHost.setOpaque(false);
        bannerHost.setLayout(new BoxLayout(bannerHost, BoxLayout.Y_AXIS));
        bannerHost.setBorder(new EmptyBorder(8, 8, 8, 8));

        confirmHost = new JPanel();
        confirmHost.setOpaque(false);
        confirmHost.setLayout(new BoxLayout(confirmHost, BoxLayout.Y_AXIS));
        confirmHost.setBorder(new EmptyBorder(0, 8, 8, 8));

        JPanel topStack = new JPanel();
        topStack.setOpaque(false);
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.add(bannerHost);
        topStack.add(confirmHost);

        add(topStack, BorderLayout.NORTH);

        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);
        rootPanel.setBackground(BG);

        rootPanel.add(createLoginPanel(), "LOGIN");
        rootPanel.add(createRegisterPanel(), "REGISTER");
        rootPanel.add(createAppView(), "APP");

        add(rootPanel, BorderLayout.CENTER);
        rootLayout.show(rootPanel, "LOGIN");
    }

    private JPanel createAppView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(createSidebar(), BorderLayout.WEST);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(BG);

        contentPanel.add(createFeedPanel(), "FEED");
        contentPanel.add(createSearchPanel(), "SEARCH");
        contentPanel.add(createCreatePostPanel(), "CREATE");
        contentPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        contentPanel.add(createHashtagPanel(), "HASHTAG");
        contentPanel.add(createInboxPanel(), "INBOX");

        root.add(contentPanel, BorderLayout.CENTER);

        return root;
    }

    private void showContent(String card) {
        currentContentCard = card;
        contentLayout.show(contentPanel, card);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG);

        JPanel card = new JPanel(null);
        card.setPreferredSize(new Dimension(420, 520));
        card.setBackground(BG);
        card.setBorder(new LineBorder(BORDER, 1));

        JLabel title = new JLabel("Instagram", SwingConstants.CENTER);
        title.setBounds(60, 45, 300, 60);
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe Script", Font.BOLD, 40));
        card.add(title);

        JTextField txtUser = styledTextField("Usuario");
        txtUser.setBounds(60, 150, 300, 42);
        card.add(txtUser);

        JPasswordField txtPass = styledPasswordField("Contraseña");
        txtPass.setBounds(60, 205, 300, 42);
        card.add(txtPass);

        JButton btnLogin = styledButton("Entrar");
        btnLogin.setBounds(60, 265, 300, 42);
        btnLogin.addActionListener(e -> {
            clearConfirmHost();

            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword()).trim();

            try {
                if (u.isEmpty() || u.equals("Usuario") || p.isEmpty() || p.equals("Contraseña")) {
                    throw new EmptyFieldException("Llena todos los campos.");
                }

                loggedUser = userManager.login(u, p);
                connectChat();
                refreshAll();
                rootLayout.show(rootPanel, "APP");
                showContent("FEED");
                showInlineInfo("Bienvenido @" + loggedUser.getUsername());

            } catch (AccountInactiveException ex) {
                showInlineConfirm(
                        "La cuenta está desactivada. ¿Deseas activarla ahora?",
                        "Activar",
                        () -> {
                            userManager.setActive(u, true);
                            try {
                                loggedUser = userManager.login(u, p);
                                connectChat();
                                refreshAll();
                                rootLayout.show(rootPanel, "APP");
                                showContent("FEED");
                                showInlineInfo("Cuenta activada.");
                            } catch (Exception ex2) {
                                showInlineError(ex2.getMessage());
                            }
                        }
                );
            } catch (Exception ex) {
                showInlineError(ex.getMessage());
            }
        });
        card.add(btnLogin);

        JButton btnRegister = createLinkButton("¿No tienes cuenta? Regístrate");
        btnRegister.setBounds(80, 455, 260, 28);
        btnRegister.addActionListener(e -> {
            clearBannerHost();
            clearConfirmHost();
            rootLayout.show(rootPanel, "REGISTER");
        });
        card.add(btnRegister);

        panel.add(card);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG);

        JPanel card = new JPanel(null);
        card.setPreferredSize(new Dimension(470, 640));
        card.setBackground(BG);
        card.setBorder(new LineBorder(BORDER, 1));

        JLabel title = new JLabel("Crear cuenta", SwingConstants.CENTER);
        title.setBounds(60, 25, 340, 35);
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        card.add(title);

        JTextField txtNombre = styledTextField("Nombre completo");
        txtNombre.setBounds(60, 80, 340, 38);
        card.add(txtNombre);

        JTextField txtUser = styledTextField("Username");
        txtUser.setBounds(60, 128, 340, 38);
        card.add(txtUser);

        JPasswordField txtPass = styledPasswordField("Contraseña");
        txtPass.setBounds(60, 176, 340, 38);
        card.add(txtPass);

        JTextField txtEdad = styledTextField("Edad");
        txtEdad.setBounds(60, 224, 160, 38);
        card.add(txtEdad);

        JRadioButton rbM = new JRadioButton("M");
        JRadioButton rbF = new JRadioButton("F");
        styleRadio(rbM);
        styleRadio(rbF);

        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(rbM);
        bgGender.add(rbF);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        genderPanel.setBounds(240, 224, 160, 38);
        genderPanel.setBackground(BG);
        genderPanel.setBorder(new LineBorder(BORDER, 1));
        genderPanel.add(rbM);
        genderPanel.add(rbF);
        card.add(genderPanel);

        JRadioButton rbPublic = new JRadioButton("Pública");
        JRadioButton rbPrivate = new JRadioButton("Privada");
        styleRadio(rbPublic);
        styleRadio(rbPrivate);
        rbPublic.setSelected(true);

        ButtonGroup bgPrivacy = new ButtonGroup();
        bgPrivacy.add(rbPublic);
        bgPrivacy.add(rbPrivate);

        JPanel privacyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 2));
        privacyPanel.setBounds(60, 276, 340, 38);
        privacyPanel.setBackground(BG);
        privacyPanel.setBorder(new LineBorder(BORDER, 1));
        privacyPanel.add(rbPublic);
        privacyPanel.add(rbPrivate);
        card.add(privacyPanel);

        JButton btnPhoto = styledButton("Seleccionar foto de perfil");
        btnPhoto.setBounds(60, 330, 340, 38);
        card.add(btnPhoto);

        final String[] tempPhotoPath = {""};

        btnPhoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));

            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                tempPhotoPath[0] = chooser.getSelectedFile().getAbsolutePath();
                btnPhoto.setText("Foto: " + chooser.getSelectedFile().getName());
                showInlineInfo("Foto seleccionada.");
            }
        });

        JButton btnCreate = styledButton("Crear cuenta");
        btnCreate.setBounds(60, 390, 340, 42);
        btnCreate.addActionListener(e -> {
            clearConfirmHost();

            try {
                String nombre = txtNombre.getText().trim();
                String user = txtUser.getText().trim();
                String pass = new String(txtPass.getPassword()).trim();
                String edadText = txtEdad.getText().trim();

                if (nombre.isEmpty() || nombre.equals("Nombre completo")
                        || user.isEmpty() || user.equals("Username")
                        || pass.isEmpty() || pass.equals("Contraseña")
                        || edadText.isEmpty() || edadText.equals("Edad")) {
                    throw new EmptyFieldException("Todos los campos son obligatorios.");
                }

                char genero = rbM.isSelected() ? 'M' : (rbF.isSelected() ? 'F' : ' ');
                if (genero == ' ') {
                    throw new EmptyFieldException("Selecciona un género.");
                }

                int edad = Integer.parseInt(edadText);

                String photoName = "default_user.png";
                if (!tempPhotoPath[0].isEmpty()) {
                    photoName = FileManager.saveUserImage(user, new File(tempPhotoPath[0]), "profile");
                }

                User newUser = new User(nombre, genero, user, pass, edad, photoName);
                newUser.setAccountType(rbPrivate.isSelected() ? AccountType.PRIVATE : AccountType.PUBLIC);

                userManager.registrarUsuario(newUser);
                loggedUser = newUser;
                connectChat();
                refreshAll();
                rootLayout.show(rootPanel, "APP");
                showContent("FEED");
                showInlineInfo("Cuenta creada correctamente.");

            } catch (NumberFormatException ex) {
                showInlineError("La edad debe ser numérica.");
            } catch (Exception ex) {
                showInlineError(ex.getMessage());
            }
        });
        card.add(btnCreate);

        JButton btnBack = createLinkButton("¿Ya tienes cuenta? Entrar");
        btnBack.setBounds(100, 580, 250, 24);
        btnBack.addActionListener(e -> {
            clearBannerHost();
            clearConfirmHost();
            rootLayout.show(rootPanel, "LOGIN");
        });
        card.add(btnBack);

        panel.add(card);
        return panel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 768));
        sidebar.setBackground(BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(22, 18, 18, 18));

        JLabel title = new JLabel("Instagram");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe Script", Font.BOLD, 24));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(32));

        sidebar.add(createSidebarButton("🏠 Inicio", () -> {
            currentProfileUsername = null;
            refreshFeed();
            showContent("FEED");
        }));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(createSidebarButton("🔍 Buscar", () -> {
            resetSearchView();
            showContent("SEARCH");
        }));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(createSidebarButton("✨ Crear", () -> {
            currentProfileUsername = null;
            showContent("CREATE");
        }));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(createSidebarButton("💬 Inbox", () -> {
            currentProfileUsername = null;
            refreshInboxChats();
            showContent("INBOX");
        }));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(createSidebarButton("🔔 Notificaciones", () -> {
            currentProfileUsername = null;
            loadNotifications();
            showContent("NOTIFICATIONS");
        }));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(createSidebarButton("🔎 Hashtags", () -> {
            currentProfileUsername = null;
            showContent("HASHTAG");
        }));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(createSidebarButton("👤 Mi perfil", () -> {
            if (loggedUser != null) {
                showProfile(loggedUser);
            }
        }));
        sidebar.add(Box.createVerticalStrut(22));

        sidebar.add(createSidebarButton("🚪 Salir", () -> {
            disconnectChat();
            loggedUser = null;
            currentProfileUsername = null;
            openedChatUser = null;
            clearBannerHost();
            clearConfirmHost();
            rootLayout.show(rootPanel, "LOGIN");
        }));

        return sidebar;
    }

    private JButton createSidebarButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 52));
        btn.setPreferredSize(new Dimension(210, 52));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(TEXT);
        btn.setBackground(BG);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(12, 14, 12, 14));

        btn.addActionListener(e -> action.run());

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PANEL);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(BG);
            }
        });

        return btn;
    }

    private JPanel createFeedPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);

        feedInner = new JPanel();
        feedInner.setLayout(new BoxLayout(feedInner, BoxLayout.Y_AXIS));
        feedInner.setBackground(BG);
        feedInner.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane sp = new JScrollPane(feedInner);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    private void refreshFeed() {
        if (feedInner == null) {
            return;
        }

        feedInner.removeAll();

        if (loggedUser == null) {
            feedInner.revalidate();
            feedInner.repaint();
            return;
        }

        List<Post> posts = userManager.getAllRelevantPostsByDate(loggedUser);

        if (posts.isEmpty()) {
            JLabel empty = new JLabel("No hay publicaciones todavía.");
            empty.setForeground(MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedInner.add(Box.createVerticalStrut(60));
            feedInner.add(empty);
        } else {
            for (Post post : posts) {
                JPanel card = createPostCard(post, 620, 520);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                feedInner.add(card);
                feedInner.add(Box.createVerticalStrut(20));
            }
        }

        feedInner.revalidate();
        feedInner.repaint();
    }

    private JPanel createPostCard(Post post, int width, int imageSize) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL);
        card.setMaximumSize(new Dimension(width, imageSize + 260));
        card.setBorder(new LineBorder(BORDER, 1, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(PANEL);

        User author = userManager.getUserByUsername(post.getAuthorUsername());
        String profilePath = (author != null)
                ? FileManager.getUserImageAbsolutePath(author.getUsername(), author.getFotoPath())
                : "";

        JLabel avatar = new JLabel(cargarImagenCircular(profilePath, 36));
        JLabel info = new JLabel("<html><b>@" + post.getAuthorUsername() + "</b><br><span style='color:#aaaaaa;'>" + post.getFormattedDate() + "</span></html>");
        info.setForeground(TEXT);

        left.add(avatar);
        left.add(info);

        header.add(left, BorderLayout.WEST);
        card.add(header, BorderLayout.NORTH);

        card.add(createCarouselPanel(post, imageSize), BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(PANEL);
        footer.setBorder(new EmptyBorder(12, 12, 14, 12));

        JLabel lblCaption = new JLabel("<html><b>@" + post.getAuthorUsername() + "</b> " + escapeHtml(post.getCaption()) + "</html>");
        lblCaption.setForeground(TEXT);
        footer.add(lblCaption);
        footer.add(Box.createVerticalStrut(8));

        List<Comment> comments = post.getComments();
        if (comments.isEmpty()) {
            JLabel noComments = new JLabel("Sin comentarios todavía.");
            noComments.setForeground(MUTED);
            footer.add(noComments);
        } else {
            int limit = Math.min(3, comments.size());
            for (int i = 0; i < limit; i++) {
                Comment c = comments.get(i);
                JLabel lbl = new JLabel("<html><b>@" + c.getUsername() + "</b> "
                        + escapeHtml(c.getText())
                        + " <span style='color:#999999;'>(" + c.getFormattedDate() + ")</span></html>");
                lbl.setForeground(TEXT);
                footer.add(lbl);
            }
        }

        footer.add(Box.createVerticalStrut(10));

        JPanel commentRow = new JPanel(new BorderLayout(8, 0));
        commentRow.setBackground(PANEL);

        JTextField txtComment = new JTextField();
        txtComment.setBackground(INPUT);
        txtComment.setForeground(TEXT);
        txtComment.setCaretColor(TEXT);
        txtComment.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnCommentSend = createMiniButton("Comentar");
        btnCommentSend.setPreferredSize(new Dimension(110, 40));

        btnCommentSend.addActionListener(e -> {
            String txt = txtComment.getText().trim();
            if (txt.isEmpty()) {
                return;
            }

            boolean ok = userManager.addCommentAndSave(post, new Comment(loggedUser.getUsername(), txt));
            if (ok) {
                txtComment.setText("");
                refreshAll();

                if ("SEARCH".equals(currentContentCard) && currentProfileUsername != null) {
                    refreshCurrentProfileView();
                }
                showInlineInfo("Comentario agregado.");
            } else {
                showInlineError("No se pudo guardar el comentario.");
            }
        });

        commentRow.add(txtComment, BorderLayout.CENTER);
        commentRow.add(btnCommentSend, BorderLayout.EAST);

        footer.add(commentRow);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCarouselPanel(Post post, int imageSize) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(PANEL);

        List<String> imgs = post.getImageNames();
        if (imgs.isEmpty()) {
            return container;
        }

        CardLayout layout = new CardLayout();
        JPanel cards = new JPanel(layout);
        cards.setBackground(PANEL);

        for (int i = 0; i < imgs.size(); i++) {
            JLabel img = new JLabel(cargarImagenRectangular(post.getImagePath(i), imageSize, imageSize));
            img.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(PANEL);
            p.add(img, BorderLayout.CENTER);
            cards.add(p, String.valueOf(i));
        }

        container.add(cards, BorderLayout.CENTER);

        if (imgs.size() > 1) {
            JButton prev = createRoundNavButton("<");
            JButton next = createRoundNavButton(">");

            JLabel indicator = new JLabel("1 / " + imgs.size(), SwingConstants.CENTER);
            indicator.setForeground(TEXT);

            final int[] index = {0};

            prev.addActionListener(e -> {
                index[0] = (index[0] - 1 + imgs.size()) % imgs.size();
                layout.show(cards, String.valueOf(index[0]));
                indicator.setText((index[0] + 1) + " / " + imgs.size());
            });

            next.addActionListener(e -> {
                index[0] = (index[0] + 1) % imgs.size();
                layout.show(cards, String.valueOf(index[0]));
                indicator.setText((index[0] + 1) + " / " + imgs.size());
            });

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setBackground(PANEL);
            bottom.setBorder(new EmptyBorder(8, 10, 8, 10));
            bottom.add(prev, BorderLayout.WEST);
            bottom.add(indicator, BorderLayout.CENTER);
            bottom.add(next, BorderLayout.EAST);
            container.add(bottom, BorderLayout.SOUTH);
        }

        return container;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        searchViewLayout = new CardLayout();
        searchViewPanel = new JPanel(searchViewLayout);
        searchViewPanel.setBackground(BG);

        JPanel searchPage = new JPanel(new BorderLayout());
        searchPage.setBackground(BG);

        JPanel top = new JPanel();
        top.setBackground(BG);
        top.setBorder(new EmptyBorder(20, 20, 10, 20));
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Buscar usuarios");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtSearchUser = styledTextField("Escribe un username...");
        txtSearchUser.setMaximumSize(new Dimension(420, 42));
        txtSearchUser.setPreferredSize(new Dimension(420, 42));
        txtSearchUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnSearch = styledButton("Buscar");
        btnSearch.setMaximumSize(new Dimension(420, 42));
        btnSearch.setPreferredSize(new Dimension(420, 42));
        btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSearch.addActionListener(e -> performUserSearch(txtSearchUser.getText().trim()));

        top.add(title);
        top.add(Box.createVerticalStrut(16));
        top.add(txtSearchUser);
        top.add(Box.createVerticalStrut(10));
        top.add(btnSearch);

        searchResultsPanel = new JPanel();
        searchResultsPanel.setLayout(new BoxLayout(searchResultsPanel, BoxLayout.Y_AXIS));
        searchResultsPanel.setBackground(BG);
        searchResultsPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JScrollPane resultsScroll = new JScrollPane(searchResultsPanel);
        resultsScroll.setBorder(null);
        resultsScroll.getViewport().setBackground(BG);
        resultsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        searchPage.add(top, BorderLayout.NORTH);
        searchPage.add(resultsScroll, BorderLayout.CENTER);

        profileViewWrapper = new JPanel(new BorderLayout());
        profileViewWrapper.setBackground(BG);

        searchViewPanel.add(searchPage, "RESULTS");
        searchViewPanel.add(profileViewWrapper, "PROFILE");

        panel.add(searchViewPanel, BorderLayout.CENTER);

        resetSearchView();
        return panel;
    }

    private void resetSearchView() {
        currentProfileUsername = null;

        if (searchResultsPanel == null || searchViewLayout == null) {
            return;
        }

        searchResultsPanel.removeAll();

        JLabel lbl = new JLabel("Escribe un username para buscar.");
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        searchResultsPanel.add(Box.createVerticalStrut(30));
        searchResultsPanel.add(lbl);

        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();

        searchViewLayout.show(searchViewPanel, "RESULTS");
    }

    private void performUserSearch(String query) {
        if (query == null || query.trim().isEmpty() || query.equals("Escribe un username...")) {
            showInlineWarning("Escribe un username válido.");
            return;
        }

        currentProfileUsername = null;
        searchResultsPanel.removeAll();

        List<User> found = new ArrayList<>(userManager.searchUsers(query.trim()));

        if (found.isEmpty()) {
            JLabel empty = new JLabel("No se encontraron usuarios.");
            empty.setForeground(MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            searchResultsPanel.add(Box.createVerticalStrut(20));
            searchResultsPanel.add(empty);
        } else {
            for (User u : found) {
                searchResultsPanel.add(createUserSearchItem(u));
                searchResultsPanel.add(Box.createVerticalStrut(10));
            }
        }

        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();
        searchViewLayout.show(searchViewPanel, "RESULTS");
    }

    private JPanel createUserSearchItem(User user) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(PANEL);
        item.setBorder(new LineBorder(BORDER, 1, true));
        item.setMaximumSize(new Dimension(560, 68));

        String path = FileManager.getUserImageAbsolutePath(user.getUsername(), user.getFotoPath());
        JLabel avatar = new JLabel(cargarImagenCircular(path, 44));
        avatar.setBorder(new EmptyBorder(10, 10, 10, 0));
        item.add(avatar, BorderLayout.WEST);

        JLabel info = new JLabel("<html><b>@" + user.getUsername() + "</b><br>" + user.getNombre() + "</html>");
        info.setForeground(TEXT);
        item.add(info, BorderLayout.CENTER);

        JButton btnView = styledButton("Ver perfil");
        btnView.setPreferredSize(new Dimension(120, 34));
        btnView.addActionListener(e -> showProfile(user));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        right.setBackground(PANEL);
        right.add(btnView);

        item.add(right, BorderLayout.EAST);
        return item;
    }

    private void showProfile(User targetUser) {
        if (targetUser == null) {
            return;
        }

        User target = userManager.getUserByUsername(targetUser.getUsername());
        if (target == null) {
            return;
        }

        currentProfileUsername = target.getUsername();
        refreshCurrentProfileView();
        searchViewLayout.show(searchViewPanel, "PROFILE");
        showContent("SEARCH");
    }

    private void refreshCurrentProfileView() {
        if (currentProfileUsername == null || profileViewWrapper == null) {
            return;
        }

        User target = userManager.getUserByUsername(currentProfileUsername);
        if (target == null) {
            return;
        }

        JPanel profile = buildProfilePanel(target);

        profileViewWrapper.removeAll();

        JScrollPane profileScroll = new JScrollPane(profile);
        profileScroll.setBorder(null);
        profileScroll.getViewport().setBackground(BG);
        profileScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        profileViewWrapper.add(profileScroll, BorderLayout.CENTER);
        profileViewWrapper.revalidate();
        profileViewWrapper.repaint();
    }

    private JPanel buildProfilePanel(User target) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(BG);

        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setBackground(BG);
        avatarWrap.setPreferredSize(new Dimension(180, 160));
        avatarWrap.add(new JLabel(cargarImagenCircular(
                FileManager.getUserImageAbsolutePath(target.getUsername(), target.getFotoPath()), 120)));
        header.add(avatarWrap, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setBackground(BG);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel username = new JLabel("@" + target.getUsername());
        username.setForeground(TEXT);
        username.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel details = new JLabel("Cuenta: " + (target.isPrivateAccount() ? "Privada" : "Pública"));
        details.setForeground(MUTED);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 0));
        stats.setBackground(BG);
        stats.add(createStatLabel(String.valueOf(target.getPosts().size()), "publicaciones"));
        stats.add(createStatLabel(String.valueOf(target.getFollowers().size()), "seguidores"));
        stats.add(createStatLabel(String.valueOf(target.getFollowings().size()), "seguidos"));

        center.add(username);
        center.add(Box.createVerticalStrut(6));
        center.add(details);
        center.add(Box.createVerticalStrut(12));
        center.add(stats);
        center.add(Box.createVerticalStrut(10));
        center.add(createMutedLabel("Nombre: " + target.getNombre()));
        center.add(createMutedLabel("Edad: " + target.getEdad()));
        center.add(createMutedLabel("Miembro desde: " + target.getJoinDate()));

        header.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setBackground(BG);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        boolean own = loggedUser != null && loggedUser.getUsername().equalsIgnoreCase(target.getUsername());

        if (own) {
            JButton btnPrivacy = styledButton(target.isPrivateAccount() ? "Cambiar a pública" : "Cambiar a privada");
            btnPrivacy.setMaximumSize(new Dimension(180, 42));
            btnPrivacy.addActionListener(e -> {
                userManager.togglePrivacy(loggedUser.getUsername());
                loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                refreshCurrentProfileView();
                showInlineInfo("Privacidad actualizada.");
            });

            JButton btnDeactivate = styledButton("Desactivar cuenta");
            btnDeactivate.setMaximumSize(new Dimension(180, 42));
            btnDeactivate.setBackground(new Color(160, 60, 60));
            btnDeactivate.addActionListener(e -> {
                showInlineConfirm(
                        "Si desactivas tu cuenta no aparecerás en búsquedas, publicaciones ni podrás iniciar sesión.",
                        "Desactivar",
                        () -> {
                            userManager.setActive(loggedUser.getUsername(), false);
                            disconnectChat();
                            loggedUser = null;
                            currentProfileUsername = null;
                            openedChatUser = null;
                            rootLayout.show(rootPanel, "LOGIN");
                            showInlineInfo("Cuenta desactivada.");
                        }
                );
            });

            actions.add(btnPrivacy);
            actions.add(Box.createVerticalStrut(10));
            actions.add(btnDeactivate);

            if (target.isPrivateAccount()) {
                actions.add(Box.createVerticalStrut(14));
                actions.add(createRequestsPanel(target));
            }
        } else {
            boolean following = loggedUser != null && loggedUser.isFollowing(target.getUsername());
            boolean requested = loggedUser != null && userManager.getPendingRequests(target.getUsername())
                    .stream()
                    .anyMatch(r -> r.equalsIgnoreCase(loggedUser.getUsername()));

            JButton btnFollow = styledButton(
                    following ? "Dejar de seguir" : (requested ? "Solicitud enviada" : "Seguir")
            );
            btnFollow.setMaximumSize(new Dimension(180, 42));

            if (requested) {
                btnFollow.setEnabled(false);
            }

            btnFollow.addActionListener(e -> {
                boolean wasFollowing = loggedUser.isFollowing(target.getUsername());
                boolean isPrivate = target.isPrivateAccount();

                userManager.toggleFollow(loggedUser.getUsername(), target.getUsername());
                loggedUser = userManager.getUserByUsername(loggedUser.getUsername());

                if (!wasFollowing && isPrivate && !loggedUser.isFollowing(target.getUsername())) {
                    showInlineInfo("Solicitud enviada.");
                } else if (wasFollowing) {
                    showInlineInfo("Has dejado de seguir a @" + target.getUsername());
                } else {
                    showInlineInfo("Ahora sigues a @" + target.getUsername());
                }

                refreshCurrentProfileView();
                refreshFeed();
            });

            JButton btnMsg = styledButton("Enviar mensaje");
            btnMsg.setMaximumSize(new Dimension(180, 42));
            btnMsg.setBackground(new Color(255, 105, 180));
            btnMsg.addActionListener(e -> {
                if (!userManager.canSendMessage(loggedUser.getUsername(), target.getUsername())) {
                    showInlineWarning("No puedes enviar mensajes todavía. Debes ser seguidor aprobado en una cuenta privada.");
                    return;
                }

                openedChatUser = target.getUsername();
                refreshInboxChats();
                openConversation(openedChatUser);
                showContent("INBOX");
            });

            actions.add(btnFollow);
            actions.add(Box.createVerticalStrut(10));
            actions.add(btnMsg);
        }

        header.add(actions, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 3, 4, 4));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(25, 0, 0, 0));

        boolean canView = userManager.canViewProfileContent(loggedUser != null ? loggedUser.getUsername() : null, target.getUsername());

        if (!canView) {
            JPanel locked = new JPanel(new GridBagLayout());
            locked.setBackground(BG);
            JLabel lbl = new JLabel("🔒 Esta cuenta es privada.");
            lbl.setForeground(MUTED);
            locked.add(lbl);
            root.add(locked, BorderLayout.CENTER);
            return root;
        }

        List<Post> posts = userManager.loadPostsFromLocalFile(target.getUsername());
        if (posts.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(BG);
            JLabel lbl = new JLabel("No hay publicaciones.");
            lbl.setForeground(MUTED);
            empty.add(lbl);
            root.add(empty, BorderLayout.CENTER);
            return root;
        }

        for (Post p : posts) {
            JButton thumb = new JButton(cargarImagenRectangular(p.getImagePath(0), 220, 220));
            thumb.setBackground(PANEL);
            thumb.setBorder(null);
            thumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            thumb.addActionListener(e -> showPostDialog(p));
            body.add(thumb);
        }

        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel createRequestsPanel(User me) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(PANEL);
        box.setBorder(new LineBorder(BORDER, 1, true));
        box.setMaximumSize(new Dimension(260, 240));

        JLabel title = new JLabel("Solicitudes pendientes");
        title.setForeground(TEXT);
        title.setBorder(new EmptyBorder(8, 8, 8, 8));
        box.add(title);

        List<String> pending = userManager.getPendingRequests(me.getUsername());

        if (pending.isEmpty()) {
            JLabel empty = new JLabel("No hay solicitudes.");
            empty.setForeground(MUTED);
            empty.setBorder(new EmptyBorder(0, 8, 8, 8));
            box.add(empty);
            return box;
        }

        for (String follower : pending) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(PANEL);
            row.setBorder(new EmptyBorder(4, 8, 4, 8));

            JLabel lbl = new JLabel("@" + follower);
            lbl.setForeground(TEXT);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            btns.setBackground(PANEL);

            JButton ok = createMiniButton("Aceptar");
            JButton no = createMiniButton("Rechazar");

            ok.addActionListener(e -> {
                try {
                    userManager.approveFollowRequest(me.getUsername(), follower);
                    loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                    refreshCurrentProfileView();
                    refreshFeed();
                    showInlineInfo("Solicitud aceptada.");

                    showInlineConfirm(
                            "Solicitud aceptada. ¿Deseas seguir también a @" + follower + "?",
                            "Seguir",
                            () -> {
                                try {
                                    userManager.followBackDirect(me.getUsername(), follower);
                                    loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                                    refreshCurrentProfileView();
                                    refreshFeed();
                                    showInlineInfo("Ahora también sigues a @" + follower);
                                } catch (Exception ex) {
                                    showInlineError(ex.getMessage());
                                }
                            }
                    );

                } catch (Exception ex) {
                    showInlineError(ex.getMessage());
                }
            });

            no.addActionListener(e -> {
                try {
                    userManager.rejectFollowRequest(me.getUsername(), follower);
                    refreshCurrentProfileView();
                    showInlineInfo("Solicitud rechazada.");
                } catch (Exception ex) {
                    showInlineError(ex.getMessage());
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

    private JPanel createCreatePostPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(760, 560));
        card.setBackground(PANEL);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Crear publicación");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea txtCaption = new JTextArea(6, 20);
        txtCaption.setBackground(INPUT);
        txtCaption.setForeground(TEXT);
        txtCaption.setCaretColor(TEXT);
        txtCaption.setBorder(new EmptyBorder(12, 12, 12, 12));
        txtCaption.setLineWrap(true);
        txtCaption.setWrapStyleWord(true);
        txtCaption.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JScrollPane spCaption = new JScrollPane(txtCaption);
        spCaption.setMaximumSize(new Dimension(620, 180));
        spCaption.setPreferredSize(new Dimension(620, 180));
        spCaption.setAlignmentX(Component.CENTER_ALIGNMENT);
        spCaption.setBorder(new LineBorder(BORDER, 1));

        JLabel lblInfo = new JLabel("0 imágenes seleccionadas");
        lblInfo.setForeground(MUTED);
        lblInfo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnSelect = styledButton("Seleccionar imágenes");
        btnSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSelect.setMaximumSize(new Dimension(240, 44));
        btnSelect.setPreferredSize(new Dimension(240, 44));

        final List<String> selectedImages = new ArrayList<>();

        btnSelect.addActionListener(e -> {
            if (loggedUser == null) {
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));
            chooser.setMultiSelectionEnabled(true);

            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                try {
                    selectedImages.clear();
                    File[] files = chooser.getSelectedFiles();

                    for (File file : files) {
                        selectedImages.add(FileManager.saveUserImage(loggedUser.getUsername(), file, "post"));
                    }

                    lblInfo.setText(selectedImages.size() + " imagen(es) seleccionada(s)");
                    showInlineInfo("Imágenes cargadas.");
                } catch (Exception ex) {
                    showInlineError(ex.getMessage());
                }
            }
        });

        JButton btnPublish = styledButton("Publicar");
        btnPublish.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPublish.setMaximumSize(new Dimension(180, 44));
        btnPublish.setPreferredSize(new Dimension(180, 44));

        btnPublish.addActionListener(e -> {
            try {
                if (loggedUser == null) {
                    throw new Exception("Debes iniciar sesión.");
                }

                if (selectedImages.isEmpty()) {
                    throw new Exception("Debes seleccionar al menos una imagen.");
                }

                Post post = new Post(loggedUser.getUsername(), selectedImages, txtCaption.getText().trim());
                userManager.publishPost(post);

                loggedUser = userManager.getUserByUsername(loggedUser.getUsername());

                txtCaption.setText("");
                selectedImages.clear();
                lblInfo.setText("0 imágenes seleccionadas");

                refreshFeed();
                showContent("FEED");
                showInlineInfo("Publicación creada.");

            } catch (Exception ex) {
                showInlineError(ex.getMessage());
            }
        });

        card.add(title);
        card.add(Box.createVerticalStrut(26));
        card.add(spCaption);
        card.add(Box.createVerticalStrut(22));
        card.add(btnSelect);
        card.add(Box.createVerticalStrut(12));
        card.add(lblInfo);
        card.add(Box.createVerticalGlue());
        card.add(btnPublish);
        card.add(Box.createVerticalStrut(10));

        outer.add(card);
        return outer;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JLabel title = new JLabel("Notificaciones", SwingConstants.CENTER);
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(18, 0, 18, 0));
        panel.add(title, BorderLayout.NORTH);

        notificationsListPanel = new JPanel();
        notificationsListPanel.setLayout(new BoxLayout(notificationsListPanel, BoxLayout.Y_AXIS));
        notificationsListPanel.setBackground(BG);
        notificationsListPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JScrollPane sp = new JScrollPane(notificationsListPanel);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void loadNotifications() {
        if (notificationsListPanel == null) {
            return;
        }

        notificationsListPanel.removeAll();

        if (loggedUser == null) {
            notificationsListPanel.revalidate();
            notificationsListPanel.repaint();
            return;
        }

        List<Post> mentions = userManager.findMentions(loggedUser.getUsername());

        if (mentions.isEmpty()) {
            JLabel empty = new JLabel("No tienes menciones.");
            empty.setForeground(MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            notificationsListPanel.add(Box.createVerticalStrut(40));
            notificationsListPanel.add(empty);
        } else {
            for (Post p : mentions) {
                JPanel item = new JPanel(new BorderLayout(12, 0));
                item.setBackground(PANEL);
                item.setBorder(new LineBorder(BORDER, 1, true));
                item.setMaximumSize(new Dimension(620, 80));

                JLabel img = new JLabel(cargarImagenRectangular(p.getImagePath(0), 64, 64));
                img.setBorder(new EmptyBorder(8, 8, 8, 8));
                item.add(img, BorderLayout.WEST);

                JLabel txt = new JLabel("<html><b>@" + p.getAuthorUsername() + "</b> te mencionó.<br><span style='color:#aaaaaa;'>" + p.getFormattedDate() + "</span></html>");
                txt.setForeground(TEXT);
                item.add(txt, BorderLayout.CENTER);

                JButton open = createMiniButton("Ver");
                open.addActionListener(e -> showPostDialog(p));

                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
                right.setBackground(PANEL);
                right.add(open);

                item.add(right, BorderLayout.EAST);

                notificationsListPanel.add(item);
                notificationsListPanel.add(Box.createVerticalStrut(10));
            }
        }

        notificationsListPanel.revalidate();
        notificationsListPanel.repaint();
    }

    private JPanel createHashtagPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 16));
        top.setBackground(BG);

        JTextField txtTag = styledTextField("#general");
        txtTag.setPreferredSize(new Dimension(320, 40));

        JButton btnSearch = styledButton("Buscar");
        top.add(txtTag);
        top.add(btnSearch);

        hashtagResultsPanel = new JPanel();
        hashtagResultsPanel.setLayout(new BoxLayout(hashtagResultsPanel, BoxLayout.Y_AXIS));
        hashtagResultsPanel.setBackground(BG);
        hashtagResultsPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JScrollPane sp = new JScrollPane(hashtagResultsPanel);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        btnSearch.addActionListener(e -> {
            String tag = txtTag.getText().trim();
            if (tag.isEmpty()) {
                showInlineWarning("Escribe un hashtag.");
                return;
            }
            if (!tag.startsWith("#")) {
                tag = "#" + tag;
            }

            final String searchTag = tag.toLowerCase();

            hashtagResultsPanel.removeAll();

            List<Post> found = new ArrayList<>();
            for (User u : userManager.searchUsers("")) {
                found.addAll(userManager.loadPostsFromLocalFile(u.getUsername()));
            }

            found.removeIf(p -> p.getCaption() == null || !p.getCaption().toLowerCase().contains(searchTag));

            if (found.isEmpty()) {
                JLabel empty = new JLabel("No se encontraron publicaciones para " + tag);
                empty.setForeground(MUTED);
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                hashtagResultsPanel.add(empty);
            } else {
                found.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                for (Post p : found) {
                    JPanel card = createPostCard(p, 620, 500);
                    card.setAlignmentX(Component.CENTER_ALIGNMENT);
                    hashtagResultsPanel.add(card);
                    hashtagResultsPanel.add(Box.createVerticalStrut(20));
                }
            }

            hashtagResultsPanel.revalidate();
            hashtagResultsPanel.repaint();
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInboxPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(16, 16, 16, 16));

        chatTitleLabel = new JLabel("Inbox");
        chatTitleLabel.setForeground(TEXT);
        chatTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.add(chatTitleLabel, BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(320);
        split.setResizeWeight(0);
        split.setBorder(null);

        chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatListPanel.setBackground(BG);
        chatListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane spLeft = new JScrollPane(chatListPanel);
        spLeft.setBorder(null);
        spLeft.getViewport().setBackground(BG);

        chatConversationPanel = new JPanel(new BorderLayout());
        chatConversationPanel.setBackground(BG);

        JLabel hint = new JLabel("Selecciona un chat.", SwingConstants.CENTER);
        hint.setForeground(MUTED);
        chatConversationPanel.add(hint, BorderLayout.CENTER);

        split.setLeftComponent(spLeft);
        split.setRightComponent(chatConversationPanel);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void refreshInboxChats() {
        if (chatListPanel == null || loggedUser == null) {
            return;
        }

        chatListPanel.removeAll();

        JPanel newBar = new JPanel(new BorderLayout(6, 0));
        newBar.setBackground(PANEL);
        newBar.setBorder(new LineBorder(BORDER, 1, true));
        newBar.setMaximumSize(new Dimension(290, 42));

        JTextField txtTarget = new JTextField();
        txtTarget.setBackground(INPUT);
        txtTarget.setForeground(TEXT);
        txtTarget.setCaretColor(TEXT);
        txtTarget.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton btnOpen = createMiniButton("Abrir");
        btnOpen.addActionListener(e -> {
            String target = txtTarget.getText().trim();
            if (target.isEmpty() || target.equalsIgnoreCase(loggedUser.getUsername())) {
                return;
            }

            User u = userManager.getUserByUsername(target);
            if (u == null || !u.isActive()) {
                showInlineWarning("Usuario no encontrado.");
                return;
            }

            openedChatUser = target;
            openConversation(target);
        });

        newBar.add(txtTarget, BorderLayout.CENTER);
        newBar.add(btnOpen, BorderLayout.EAST);

        chatListPanel.add(newBar);
        chatListPanel.add(Box.createVerticalStrut(12));

        List<String> chats = userManager.getChatList(loggedUser.getUsername());

        if (chats.isEmpty()) {
            JLabel empty = new JLabel("No tienes conversaciones.");
            empty.setForeground(MUTED);
            chatListPanel.add(empty);
        } else {
            for (String other : chats) {
                chatListPanel.add(createChatItem(other));
                chatListPanel.add(Box.createVerticalStrut(8));
            }
        }

        chatListPanel.revalidate();
        chatListPanel.repaint();
    }

    private JPanel createChatItem(String otherUsername) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(PANEL);
        item.setBorder(new LineBorder(BORDER, 1, true));
        item.setMaximumSize(new Dimension(290, 60));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        User u = userManager.getUserByUsername(otherUsername);
        String path = (u != null)
                ? FileManager.getUserImageAbsolutePath(u.getUsername(), u.getFotoPath())
                : "";

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        left.setBackground(PANEL);

        JLabel avatar = new JLabel(cargarImagenCircular(path, 40));
        JLabel name = new JLabel("@" + otherUsername);
        name.setForeground(TEXT);

        left.add(avatar);
        left.add(name);

        int unread = userManager.getUnreadCount(loggedUser.getUsername(), otherUsername);
        JLabel badge = new JLabel(unread > 0 ? (" " + unread + " ") : "");
        badge.setOpaque(unread > 0);
        badge.setBackground(BLUE);
        badge.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        right.setBackground(PANEL);
        right.add(badge);

        item.add(left, BorderLayout.WEST);
        item.add(right, BorderLayout.EAST);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openedChatUser = otherUsername;
                openConversation(otherUsername);
            }
        });

        return item;
    }

    private void openConversation(String otherUsername) {
        if (loggedUser == null) {
            return;
        }

        try {
            userManager.markConversationRead(loggedUser.getUsername(), otherUsername);
        } catch (Exception ignored) {
        }

        chatTitleLabel.setText("Inbox - @" + otherUsername);

        chatConversationPanel.removeAll();
        chatConversationPanel.setLayout(new BorderLayout());

        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BG);
        messagesPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        List<Message> conv = userManager.getConversation(loggedUser.getUsername(), otherUsername);
        for (Message m : conv) {
            messagesPanel.add(createMessageBubble(m));
            messagesPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane sp = new JScrollPane(messagesPanel);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel pickerPanel = new JPanel(new BorderLayout());
        pickerPanel.setBackground(BG);
        pickerPanel.setBorder(new EmptyBorder(0, 10, 6, 10));

        JPanel composer = new JPanel(new BorderLayout(8, 0));
        composer.setBackground(BG);
        composer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftButtons.setBackground(BG);

        JButton btnEmoji = createMiniButton("😀");
        JButton btnSticker = createMiniButton("Sticker");
        JButton btnUploadSticker = createMiniButton("Subir");
        JButton btnDeleteChat = createMiniButton("Borrar");

        JTextField txt = new JTextField();
        txt.setBackground(INPUT);
        txt.setForeground(TEXT);
        txt.setCaretColor(TEXT);
        txt.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnSend = styledButton("Enviar");
        btnSend.setPreferredSize(new Dimension(100, 40));

        btnSend.addActionListener(e -> {
            String text = txt.getText().trim();
            if (text.isEmpty()) {
                return;
            }

            if (chatClient == null || !chatClient.isConnected()) {
                showInlineError("El chat no está conectado.");
                return;
            }

            Message msg = new Message(loggedUser.getUsername(), otherUsername, text, MessageType.TEXT);
            boolean sent = chatClient.sendMessage(msg);

            if (sent) {
                txt.setText("");
            } else {
                showInlineError("No se pudo enviar el mensaje.");
            }
        });

        btnEmoji.addActionListener(e -> togglePicker(pickerPanel, buildEmojiPicker(otherUsername, pickerPanel)));

        btnSticker.addActionListener(e -> {
            JPanel stickerPicker = buildStickerPicker(otherUsername, pickerPanel);
            if (stickerPicker != null) {
                togglePicker(pickerPanel, stickerPicker);
            }
        });

        btnUploadSticker.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));

            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                try {
                    String stickerName = FileManager.saveUserSticker(loggedUser.getUsername(), chooser.getSelectedFile());
                    showInlineInfo("Sticker guardado: " + stickerName);
                } catch (Exception ex) {
                    showInlineError(ex.getMessage());
                }
            }
        });

        btnDeleteChat.addActionListener(e -> {
            showInlineConfirm("¿Borrar la conversación con @" + otherUsername + "?", "Borrar", () -> {
                try {
                    userManager.deleteConversation(loggedUser.getUsername(), otherUsername);
                    openedChatUser = null;
                    refreshInboxChats();

                    chatConversationPanel.removeAll();
                    JLabel hint = new JLabel("Conversación borrada.", SwingConstants.CENTER);
                    hint.setForeground(MUTED);
                    chatConversationPanel.add(hint, BorderLayout.CENTER);
                    chatConversationPanel.revalidate();
                    chatConversationPanel.repaint();

                    showInlineInfo("Conversación eliminada.");
                } catch (Exception ex) {
                    showInlineError(ex.getMessage());
                }
            });
        });

        leftButtons.add(btnEmoji);
        leftButtons.add(btnSticker);
        leftButtons.add(btnUploadSticker);
        leftButtons.add(btnDeleteChat);

        composer.add(leftButtons, BorderLayout.WEST);
        composer.add(txt, BorderLayout.CENTER);
        composer.add(btnSend, BorderLayout.EAST);

        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.setBackground(BG);
        southWrap.add(pickerPanel, BorderLayout.NORTH);
        southWrap.add(composer, BorderLayout.SOUTH);

        chatConversationPanel.add(sp, BorderLayout.CENTER);
        chatConversationPanel.add(southWrap, BorderLayout.SOUTH);
        chatConversationPanel.revalidate();
        chatConversationPanel.repaint();

        SwingUtilities.invokeLater(() -> sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum()));
    }

    private void togglePicker(JPanel host, JPanel panel) {
        if (host.getComponentCount() > 0) {
            host.removeAll();
            host.revalidate();
            host.repaint();
            return;
        }

        host.removeAll();
        host.add(panel, BorderLayout.CENTER);
        host.revalidate();
        host.repaint();
    }

    private JPanel buildEmojiPicker(String otherUsername, JPanel host) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBackground(PANEL);
        panel.setBorder(new LineBorder(BORDER, 1, true));

        String[] emojis = {"❤️", "😂", "👏", "😢", "😊", "🔥", "👍", "😍"};

        for (String emoji : emojis) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 20));
            btn.setBackground(INPUT);
            btn.setForeground(TEXT);
            btn.setBorder(new LineBorder(BORDER, 1));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                if (chatClient == null || !chatClient.isConnected()) {
                    showInlineError("El chat no está conectado.");
                    return;
                }

                Message msg = new Message(loggedUser.getUsername(), otherUsername, emoji, MessageType.STICKER_EMOJI);
                boolean sent = chatClient.sendMessage(msg);

                if (!sent) {
                    showInlineError("No se pudo enviar el sticker.");
                }

                host.removeAll();
                host.revalidate();
                host.repaint();
            });

            panel.add(btn);
        }

        return panel;
    }

    private JPanel buildStickerPicker(String otherUsername, JPanel host) {
        List<String> own = userManager.getOwnStickers(loggedUser.getUsername());
        if (own.isEmpty()) {
            showInlineWarning("No tienes stickers propios. Sube uno primero.");
            return null;
        }

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row.setBackground(PANEL);

        for (String sticker : own) {
            JButton btn = new JButton(cargarImagenRectangular(
                    FileManager.getUserStickerAbsolutePath(loggedUser.getUsername(), sticker), 70, 70));
            btn.setBackground(INPUT);
            btn.setBorder(new LineBorder(BORDER, 1));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                if (chatClient == null || !chatClient.isConnected()) {
                    showInlineError("El chat no está conectado.");
                    return;
                }

                Message msg = new Message(
                        loggedUser.getUsername(),
                        otherUsername,
                        sticker,
                        MessageType.STICKER_IMAGE,
                        loggedUser.getUsername()
                );

                boolean sent = chatClient.sendMessage(msg);
                if (!sent) {
                    showInlineError("No se pudo enviar el sticker.");
                }

                host.removeAll();
                host.revalidate();
                host.repaint();
            });

            row.add(btn);
        }

        JScrollPane scroll = new JScrollPane(row);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(PANEL);
        scroll.setPreferredSize(new Dimension(520, 96));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createMessageBubble(Message m) {
        boolean mine = m.getFrom().equalsIgnoreCase(loggedUser.getUsername());

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, 10, 8, 10));
        bubble.setBackground(mine ? BLUE : new Color(50, 50, 50));
        bubble.setMaximumSize(new Dimension(360, 220));

        if (m.getType() == MessageType.TEXT) {
            JLabel lbl = new JLabel("<html>" + escapeHtml(m.getContent()) + "</html>");
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
            bubble.add(lbl);

        } else if (m.getType() == MessageType.STICKER_EMOJI) {
            JLabel lblEmoji = new JLabel(m.getContent());
            lblEmoji.setForeground(Color.WHITE);

            // En Windows normalmente este font renderiza mejor emojis
            lblEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

            lblEmoji.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.add(lblEmoji);

        } else if (m.getType() == MessageType.STICKER_IMAGE) {
            String owner = m.getAssetOwner() != null ? m.getAssetOwner() : m.getFrom();
            String path = FileManager.getUserStickerAbsolutePath(owner, m.getContent());

            JLabel img = new JLabel(cargarImagenRectangular(path, 130, 130));
            img.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.add(img);
        }

        bubble.add(Box.createVerticalStrut(5));

        JLabel time = new JLabel(m.getFormattedDateTime());
        time.setForeground(new Color(230, 230, 230));
        time.setFont(new Font("SansSerif", Font.PLAIN, 10));
        time.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.add(time);

        if (mine) {
            row.add(bubble, BorderLayout.EAST);
        } else {
            row.add(bubble, BorderLayout.WEST);
        }

        return row;
    }

    private void showPostDialog(Post post) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Publicación", true);
        dialog.setSize(820, 760);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG);

        Post freshPost = post;
        User author = userManager.getUserByUsername(post.getAuthorUsername());
        if (author != null) {
            for (Post p : author.getPosts()) {
                if (p.getId().equals(post.getId())) {
                    freshPost = p;
                    break;
                }
            }
        }

        JPanel card = createPostCard(freshPost, 760, 520);
        dialog.add(card, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void refreshAll() {
        if (loggedUser != null) {
            loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
        }

        refreshFeed();
        loadNotifications();
        refreshInboxChats();

        if ("SEARCH".equals(currentContentCard) && currentProfileUsername != null) {
            refreshCurrentProfileView();
        }
    }

    private void connectChat() {
        disconnectChat();

        if (loggedUser == null) {
            return;
        }

        chatClient = new ChatClient();

        boolean ok = chatClient.connect(loggedUser.getUsername(), new ChatListener() {
            @Override
            public void onIncomingMessage(Message message) {
                SwingUtilities.invokeLater(() -> handleIncomingSocketMessage(message));
            }

            @Override
            public void onSystemMessage(String text) {
                SwingUtilities.invokeLater(() -> showInlineError(text));
            }

            @Override
            public void onConnectionStateChanged(boolean connected) {
                SwingUtilities.invokeLater(() -> {
                    if (!connected) {
                        showInlineWarning("Chat desconectado.");
                    }
                });
            }
        });

        if (!ok) {
            showInlineWarning("No se pudo conectar el chat en tiempo real.");
        }
    }

    private void disconnectChat() {
        if (chatClient != null) {
            chatClient.disconnect();
            chatClient = null;
        }
    }

    private void handleIncomingSocketMessage(Message message) {
        if (loggedUser == null || message == null) {
            return;
        }

        refreshInboxChats();

        String other = message.getFrom().equalsIgnoreCase(loggedUser.getUsername())
                ? message.getTo()
                : message.getFrom();

        if (openedChatUser != null && openedChatUser.equalsIgnoreCase(other)) {
            openConversation(openedChatUser);
        } else if (!message.getFrom().equalsIgnoreCase(loggedUser.getUsername())) {
            showInlineInfo("Nuevo mensaje de @" + message.getFrom());
        }

        if ("SEARCH".equals(currentContentCard) && currentProfileUsername != null) {
            refreshCurrentProfileView();
        }

        refreshFeed();
    }

    private void clearBannerHost() {
        if (bannerHost != null) {
            bannerHost.removeAll();
            bannerHost.revalidate();
            bannerHost.repaint();
        }
    }

    private void clearConfirmHost() {
        if (confirmHost != null) {
            confirmHost.removeAll();
            confirmHost.revalidate();
            confirmHost.repaint();
        }
    }

    private void showInlineMessage(String text, Color bgColor, int millis) {
        if (bannerHost == null) {
            return;
        }

        clearBannerHost();

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(10, 16, 10, 16));
        panel.setMaximumSize(new Dimension(700, 44));

        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton close = new JButton("✕");
        close.setForeground(Color.WHITE);
        close.setBackground(bgColor);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setContentAreaFilled(false);
        close.setCursor(new Cursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> clearBannerHost());

        panel.add(lbl, BorderLayout.CENTER);
        panel.add(close, BorderLayout.EAST);

        wrap.add(panel);
        bannerHost.add(wrap);
        bannerHost.revalidate();
        bannerHost.repaint();

        if (millis > 0) {
            Timer timer = new Timer(millis, e -> clearBannerHost());
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void showInlineInfo(String text) {
        showInlineMessage(text, new Color(0, 149, 246), 3000);
    }

    private void showInlineError(String text) {
        showInlineMessage(text, new Color(180, 60, 60), 4500);
    }

    private void showInlineWarning(String text) {
        showInlineMessage(text, new Color(190, 140, 40), 4000);
    }

    private void showInlineConfirm(String text, String yesText, Runnable onYes) {
        if (confirmHost == null) {
            return;
        }

        clearConfirmHost();

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setMaximumSize(new Dimension(760, 56));

        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnYes = new JButton(yesText);
        btnYes.setBackground(new Color(0, 149, 246));
        btnYes.setForeground(Color.WHITE);
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnNo = new JButton("Cancelar");
        btnNo.setBackground(new Color(90, 90, 90));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFocusPainted(false);
        btnNo.setBorderPainted(false);
        btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnYes.addActionListener(e -> {
            clearConfirmHost();
            if (onYes != null) {
                onYes.run();
            }
        });

        btnNo.addActionListener(e -> clearConfirmHost());

        actions.add(btnYes);
        actions.add(btnNo);

        panel.add(lbl, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);

        wrap.add(panel);
        confirmHost.add(wrap);
        confirmHost.revalidate();
        confirmHost.repaint();
    }

    private JLabel createMutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(MUTED);
        return lbl;
    }

    private JPanel createStatLabel(String number, String label) {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel n = new JLabel(number, SwingConstants.CENTER);
        n.setForeground(TEXT);
        n.setFont(new Font("SansSerif", Font.BOLD, 16));
        n.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setForeground(MUTED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(n);
        p.add(l);
        return p;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setForeground(MUTED);
        field.setBackground(INPUT);
        field.setCaretColor(TEXT);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(MUTED);
                }
            }
        });

        return field;
    }

    private JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(placeholder);
        field.setForeground(MUTED);
        field.setBackground(INPUT);
        field.setCaretColor(TEXT);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setEchoChar((char) 0);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('*');
                    field.setForeground(TEXT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setEchoChar((char) 0);
                    field.setForeground(MUTED);
                }
            }
        });

        return field;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        return btn;
    }

    private JButton createMiniButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(INPUT);
        btn.setForeground(TEXT);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(BORDER, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        return btn;
    }

    private JButton createRoundNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(42, 28));
        btn.setBackground(INPUT);
        btn.setForeground(TEXT);
        btn.setBorder(new LineBorder(BORDER, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(BLUE);
        btn.setBackground(BG);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleRadio(JRadioButton rb) {
        rb.setBackground(BG);
        rb.setForeground(TEXT);
        rb.setFocusPainted(false);
    }

    private ImageIcon cargarImagenRectangular(String absolutePath, int width, int height) {
        try {
            File imgFile = new File(absolutePath);
            if (!imgFile.exists()) {
                return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
            }

            BufferedImage original = ImageIO.read(imgFile);
            if (original == null) {
                return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
            }

            Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();

            return new ImageIcon(out);
        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private ImageIcon cargarImagenCircular(String absolutePath, int size) {
        try {
            File imgFile = new File(absolutePath);
            if (!imgFile.exists()) {
                return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
            }

            BufferedImage original = ImageIO.read(imgFile);
            if (original == null) {
                return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
            }

            BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gResize = resized.createGraphics();
            gResize.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gResize.drawImage(original, 0, 0, size, size, null);
            gResize.dispose();

            BufferedImage circle = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circle.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new Ellipse2D.Double(0, 0, size, size));
            g2.drawImage(resized, 0, 0, null);
            g2.dispose();

            return new ImageIcon(circle);
        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

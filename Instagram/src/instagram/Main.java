/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import javax.swing.*;

/**
 *
 * @author Nathan
 */

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("INSTA - Red Social Visual");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            InstagramProject app = new InstagramProject();
            frame.setContentPane(app);

            if (Config.MODO == VisualMode.MOBILE) {
                frame.setSize(390, 844);
            } else {
                frame.setSize(1366, 768);
            }

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

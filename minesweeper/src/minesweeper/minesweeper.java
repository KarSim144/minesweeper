package minesweeper;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class minesweeper extends JFrame {

    private JLabel statusbar1; // First status bar
    private JLabel statusbar2; // Second status bar

    public minesweeper() {
        initUI();
    }

    private void initUI() {
        // panel to hold multiple status bars
        JPanel statusPanel = new JPanel(new BorderLayout());
        
        // Initialize the first status bar
        statusbar1 = new JLabel("Mines Left: 20");
        statusPanel.add(statusbar1, BorderLayout.WEST);

        // statusbar2
        statusbar2 = new JLabel("Open a tile first");
        statusPanel.add(statusbar2, BorderLayout.EAST);

        // status panel to the bottom of the frame
        add(statusPanel, BorderLayout.SOUTH);

        // game board
        add(new board(statusbar1)); // Pass the first status bar to the board

        setResizable(false);
        pack();

        setTitle("Minesweeper");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            var ex = new minesweeper();
            ex.setVisible(true);
        });
    }
}

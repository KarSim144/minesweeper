package minesweeper;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class board extends JPanel {

    private final int NUM_IMAGES = 13;
    private final int SIZE = 15;

    private final int COVERCELL = 10;
    private final int MARKCELL = 10;
    private final int EMPTYCELL = 0;
    private final int MINE = 9;
    private final int COVEREDMINE = MINE + COVERCELL;
    private final int MARKED_MINE_CELL = COVEREDMINE + MARKCELL;

    private final int DRAWMINE = 9;
    private final int DRAWCOVER = 10;
    private final int DRAWMARK = 11;
    private final int DRAWWRONGMARK = 12;

    private final int N_MINES = 1200;
    private final int N_ROWS = 80;
    private final int N_COLS = 80;

    private final int BOARDWIDTH = N_COLS * SIZE + 1;
    private final int BOARDHEIGHT = N_ROWS * SIZE + 1;

    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private Image[] img;

    private int allCells;
    private final JLabel statusbar;
    private boolean firstClick; // Tracks if it's the first click

    public board(JLabel statusbar) {
        this.statusbar = statusbar;
        initBoard();
    }

    private void initBoard() {
        setPreferredSize(new Dimension(BOARDWIDTH, BOARDHEIGHT));
        img = new Image[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
            var path = "src/resources/" + i + ".png";
            img[i] = (new ImageIcon(path)).getImage();
        }

        addMouseListener(new MinesAdapter());
        newGame();
    }

    private void newGame() {
        firstClick = true;
        inGame = true;
        minesLeft = N_MINES;
        allCells = N_ROWS * N_COLS;
        field = new int[allCells];

        for (int i = 0; i < allCells; i++) {
            field[i] = COVERCELL;
        }

        statusbar.setText(Integer.toString(minesLeft));
    }

    private void placeMines(int safeIndex) {
        var random = new Random();
        int i = 0;

        while (i < N_MINES) {
            int position = random.nextInt(allCells);

            // Avoid placing mines in the clicked area or where mines already exist
            if (position == safeIndex || field[position] == COVEREDMINE) continue;

            int current_col = position % N_COLS;
            int safe_col = safeIndex % N_COLS;

            // Ensure safe surrounding cells on first click
            if (Math.abs(position / N_COLS - safeIndex / N_COLS) <= 1 &&
                Math.abs(current_col - safe_col) <= 1) continue;

            field[position] = COVEREDMINE;
            i++;

            // Increment adjacent cell counts
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    int neighbor = position + dx + dy * N_COLS;
                    int neighborCol = neighbor % N_COLS;

                    if (neighbor >= 0 && neighbor < allCells &&
                        Math.abs(neighborCol - current_col) <= 1 &&
                        field[neighbor] != COVEREDMINE) {
                        field[neighbor]++;
                    }
                }
            }
        }
    }

    private void findEmptyCells(int index) {
        int current_col = index % N_COLS;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int neighbor = index + dx + dy * N_COLS;
                int neighborCol = neighbor % N_COLS;

                if (neighbor >= 0 && neighbor < allCells &&
                    Math.abs(neighborCol - current_col) <= 1 &&
                    field[neighbor] > MINE) {
                    field[neighbor] -= COVERCELL;

                    if (field[neighbor] == EMPTYCELL) {
                        findEmptyCells(neighbor);
                    }
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        int uncover = 0;

        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                int cell = field[(i * N_COLS) + j];

                if (!inGame) {
                    if (cell == COVEREDMINE) {
                        cell = DRAWMINE; // Show mines
                    } else if (cell == MARKED_MINE_CELL) {
                        cell = DRAWMARK; // Show flagged mines
                    } else if (cell > COVEREDMINE) {
                        cell = DRAWWRONGMARK; // Show wrongly flagged cells
                    } else if (cell > MINE) {
                        cell = DRAWCOVER; // Keep covered cells
                    }
                } else {
                    if (cell == MARKED_MINE_CELL) { 
                        cell = DRAWMARK; // Properly draw flagged cells
                    } else if (cell > COVEREDMINE) {
                        cell = DRAWMARK; // Draw flag on marked cells
                    } else if (cell > MINE) {
                        cell = DRAWCOVER; // Draw covered cells
                        uncover++;
                    }
                }

                g.drawImage(img[cell], (j * SIZE), (i * SIZE), this);
            }
        }

        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            statusbar.setText("Game lost");
        }
    }

    private class MinesAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cCol = x / SIZE;
            int cRow = y / SIZE;
            int clickedCellIndex = cRow * N_COLS + cCol;

            if (!inGame) {
                newGame();
                repaint();
                return;
            }

            if (firstClick) {
                placeMines(clickedCellIndex);
                firstClick = false;

                // Automatically reveal empty cells around the first click
                if (field[clickedCellIndex] == EMPTYCELL) {
                    findEmptyCells(clickedCellIndex);
                }
            }

            if (x < N_COLS * SIZE && y < N_ROWS * SIZE) {
                if (e.getButton() == MouseEvent.BUTTON3) { // Right-click to mark
                    // Check if the cell can be marked or unmarked
                    if (field[clickedCellIndex] > MINE) {
                        if (field[clickedCellIndex] <= COVEREDMINE) {
                            // Add a flag
                            if (minesLeft > 0) {
                                field[clickedCellIndex] += MARKCELL;
                                minesLeft--;
                                statusbar.setText(Integer.toString(minesLeft));
                            } else {
                                statusbar.setText("No marks left");
                            }
                        } else if (field[clickedCellIndex] > COVEREDMINE) {
                            // Remove the flag
                            field[clickedCellIndex] -= MARKCELL;
                            minesLeft++;
                            statusbar.setText(Integer.toString(minesLeft));
                        }
                    }
                    repaint(); // Repaint after marking or unmarking
                } else { // Left-click to uncover
                    if (field[clickedCellIndex] > COVEREDMINE) return;

                    if (field[clickedCellIndex] == COVEREDMINE) {
                        inGame = false; // Hit a mine
                    } else if (field[clickedCellIndex] > MINE) {
                        field[clickedCellIndex] -= COVERCELL;

                        if (field[clickedCellIndex] == EMPTYCELL) {
                            findEmptyCells(clickedCellIndex);
                        }
                    }
                
                    repaint();
                }
            }
        }
    }
}
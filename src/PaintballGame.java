import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class PaintballGame extends JPanel implements ActionListener, KeyListener {

    // Game settings
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // Player settings
    private static final int PLAYER_SIZE = 50;
    private static final int PLAYER_SPEED = 5;

    // Paintball settings
    private static final int PAINTBALL_SIZE = 10;
    private static final int PAINTBALL_SPEED = 10;

    // Players
    private Player player1, player2;

    // Timer for game loop
    private Timer timer;

    // Scores
    private int score1, score2;

    // Winning score
    private static final int WINNING_SCORE = 40;

    // Background image
    private Image backgroundImage;

    public PaintballGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Load background image
        backgroundImage = new ImageIcon("bg2.jpeg").getImage();

        // Initialize players with images
        player1 = new Player(100, HEIGHT / 2 - PLAYER_SIZE / 2, "red.jpeg", KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, Color.BLUE);
        player2 = new Player(700, HEIGHT / 2 - PLAYER_SIZE / 2, "blue.jpeg", KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER, Color.RED);

        // Initialize scores
        score1 = 0;
        score2 = 0;

        // Set up game loop
        timer = new Timer(1000 / 60, this); // 60 FPS
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image
        g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, this);

        // Draw players
        player1.draw(g);
        player2.draw(g);

        // Draw paintballs
        player1.drawPaintballs(g);
        player2.drawPaintballs(g);

        // Draw scores
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Player 1 Score: " + score1, 50, 30);
        g.drawString("Player 2 Score: " + score2, WIDTH - 200, 30);

        // Check for winner
        if (score1 >= WINNING_SCORE) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Player 1 Wins!", WIDTH / 2 - 150, HEIGHT / 2);
            timer.stop(); // Stop the game
        } else if (score2 >= WINNING_SCORE) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Player 2 Wins!", WIDTH / 2 - 150, HEIGHT / 2);
            timer.stop(); // Stop the game
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update players
        player1.update();
        player2.update();

        // Repaint the game
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        player1.handleKeyPress(e.getKeyCode(), true);
        player2.handleKeyPress(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player1.handleKeyPress(e.getKeyCode(), false);
        player2.handleKeyPress(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // Main method to start the game
    public static void main(String[] args) {
        JFrame frame = new JFrame("Paintball Game");
        PaintballGame game = new PaintballGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Player class
    private class Player {
        private int x, y;
        private Image image;
        private int upKey, downKey, leftKey, rightKey, shootKey;
        private boolean upPressed, downPressed, leftPressed, rightPressed, shootPressed;
        private ArrayList<Paintball> paintballs;
        private int dir; // 1 for right, -1 for left
        private Color color;

        public Player(int x, int y, String imagePath, int upKey, int downKey, int leftKey, int rightKey, int shootKey, Color color) {
            this.x = x;
            this.y = y;
            this.image = new ImageIcon(imagePath).getImage();
            this.upKey = upKey;
            this.downKey = downKey;
            this.leftKey = leftKey;
            this.rightKey = rightKey;
            this.shootKey = shootKey;
            this.paintballs = new ArrayList<>();
            this.dir = 1; // Default direction is right
            this.color = color;
        }

        public void draw(Graphics g) {
            g.drawImage(image, x, y, PLAYER_SIZE, PLAYER_SIZE, null);
        }

        public void drawPaintballs(Graphics g) {
            for (Paintball p : paintballs) {
                p.draw(g);
            }
        }

        public void update() {
            // Save the previous position to check for collisions
            int prevX = x;
            int prevY = y;

            // Move player
            if (upPressed && y - PLAYER_SPEED > 0) y -= PLAYER_SPEED;
            if (downPressed && y + PLAYER_SIZE + PLAYER_SPEED < HEIGHT) y += PLAYER_SPEED;
            if (leftPressed && x - PLAYER_SPEED > 0) {
                x -= PLAYER_SPEED;
                dir = -1; // Set direction to left
            }
            if (rightPressed && x + PLAYER_SIZE + PLAYER_SPEED < WIDTH) {
                x += PLAYER_SPEED;
                dir = 1; // Set direction to right
            }

            // Collision detection with the other player
            if (collisionWithPlayer(player1, player2)) {
                x = prevX; // Revert position if collision detected
                y = prevY;
            }

            // Update paintballs
            for (int i = 0; i < paintballs.size(); i++) {
                Paintball p = paintballs.get(i);
                if (p.update()) {
                    paintballs.remove(i);
                    i--;
                } else {
                    // Check for collision with other player
                    if (p.getBounds().intersects(player1.getBounds()) && p.getColor() != player1.getColor()) {
                        score2++;
                        paintballs.remove(i);
                        i--;
                    } else if (p.getBounds().intersects(player2.getBounds()) && p.getColor() != player2.getColor()) {
                        score1++;
                        paintballs.remove(i);
                        i--;
                    }
                }
            }

            // Shoot paintball
            if (shootPressed) {
                shoot();
                shootPressed = false;
            }
        }

        private boolean collisionWithPlayer(Player p1, Player p2) {
            Rectangle r1 = new Rectangle(p1.x, p1.y, PLAYER_SIZE, PLAYER_SIZE);
            Rectangle r2 = new Rectangle(p2.x, p2.y, PLAYER_SIZE, PLAYER_SIZE);
            return r1.intersects(r2);
        }

        public void handleKeyPress(int keyCode, boolean pressed) {
            if (keyCode == upKey) upPressed = pressed;
            if (keyCode == downKey) downPressed = pressed;
            if (keyCode == leftKey) leftPressed = pressed;
            if (keyCode == rightKey) rightPressed = pressed;
            if (keyCode == shootKey) shootPressed = pressed;
        }

        private void shoot() {
            int paintballX = x + PLAYER_SIZE / 2 - PAINTBALL_SIZE / 2;
            int paintballY = y + PLAYER_SIZE / 2 - PAINTBALL_SIZE / 2;
            paintballs.add(new Paintball(paintballX, paintballY, dir));
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, PLAYER_SIZE, PLAYER_SIZE);
        }

        public Color getColor() {
            return color; // Return the color assigned to the player
        }
    }

    // Paintball class
    private class Paintball {
        private int x, y;
        private int direction;
        private Color color;

        public Paintball(int x, int y, int direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.color = direction == 1 ? Color.BLUE : Color.RED; // Blue for player1, Red for player2
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval(x, y, PAINTBALL_SIZE, PAINTBALL_SIZE);
        }

        public boolean update() {
            // Move paintball
            x += PAINTBALL_SPEED * direction;
            return x > WIDTH || x < 0; // Return true if paintball goes out of bounds
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, PAINTBALL_SIZE, PAINTBALL_SIZE);
        }

        public Color getColor() {
            return color;
        }
    }
}

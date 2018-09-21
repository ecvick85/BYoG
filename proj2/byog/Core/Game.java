package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import java.awt.Color;
import java.awt.Font;
import edu.princeton.cs.introcs.StdDraw;
import java.io.Serializable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class Game implements Serializable {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 50;
    public static final int HEIGHT = 50;
    private static Player player1;
    private static Player player2;
    private static long seed = 0;
    private static TETile[][] finalWorldFrame;
    private static Random rand;
    private static TETile[][] movingFrame;
    private static int flowersLeft = 0;
    private static Font font = new Font("Times New Roman", Font.BOLD, 60);


    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        drawMainMenu();
        char key = waitForMenuKey();
        while (true) {
            if (key == 'n') {
                Game.seed = askForSeed();
                countdown();
                movingFrame = playWithInputString(Long.toString(Game.seed));
                renderWorld(movingFrame);
                play();
            } else if (key == 'l') {
                countdown();
                World w = loadWorld();
                movingFrame = w.frame;
                finalWorldFrame = movingFrame;
                player1 = w.player1;
                player2 = w.player2;
                flowersLeft = w.flowersLeft;
                renderWorld(movingFrame);
                play();
            } else if (key == 'q') {
                System.exit(0);
                break;
            }
            key = waitForMenuKey();
        }
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */

    public TETile[][] playWithInputString(String input) {
        // DONE: Fill out this method to run the game using the input passed in,
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().
        Game.rand = setGameSeed(input);

        createEmptyWorld();

        int numrooms = rand.nextInt(5) + 5;
        Room[] rooms = new Room[numrooms];
        createRooms(rooms);

        for (int count = 0; count < numrooms; count += 1) {
            if (intersection(rooms[count])) {
                rooms[count] = new Room();
                count -= 1;
            } else {
                drawRoom(rooms[count]);
            }
        }

        drawAllHallways(rooms);

        // Draw a room in the bottom left
        drawRoom(1, 1, 5, 5);
        drawRoom(WIDTH - 6, HEIGHT - 8, 5, 5);
        player1 = new Player(3, 3, Tileset.PLAYER);
        player2 = new Player(WIDTH - 4, HEIGHT - 6, Tileset.PLAYER2);
        finalWorldFrame[player1.x][player1.y] = Tileset.PLAYER;
        finalWorldFrame[player2.x][player2.y] = Tileset.PLAYER2;

        drawWalls();
        String movestr = moveString(input);
        moveNoDraw(movestr);
        return finalWorldFrame;
    }

    // The Room class consists of: x-coordinate, y-coordinate, width, height
    private class Room {
        int roomwidth;
        int roomheight;
        int roomx;
        int roomy;
        Room() {
            roomwidth = rand.nextInt(Math.min(HEIGHT, WIDTH) / 5) + 2;
            roomheight = rand.nextInt(Math.min(HEIGHT, WIDTH) / 5) + 2;
            roomx = rand.nextInt(WIDTH - roomwidth - 2) + 1;
            roomy = rand.nextInt(HEIGHT - roomheight - 2) + 1;
        }
        Room(int w, int h, int x, int y) {
            roomwidth = w;
            roomheight = h;
            roomx = x;
            roomy = y;
        }
    }

    // Fills an array of rooms with properties
    private void createRooms(Room[] rooms) {
        for (int i = 0; i < rooms.length; i++) {
            int roomwidth = rand.nextInt(Math.min(HEIGHT, WIDTH) / 5) + 2;
            int roomheight = rand.nextInt(Math.min(HEIGHT, WIDTH) / 5) + 2;
            int roomx = rand.nextInt(WIDTH - roomwidth - 2) + 1;
            int roomy = rand.nextInt(HEIGHT - roomheight - 5) + 1;
            rooms[i] = new Room(roomwidth, roomheight, roomx, roomy);
        }
    }

    // Set the game seed and return a Random
    private Random setGameSeed(String input) {
        seed = 0;
        for (int i = 0; i < input.length(); i += 1) {
            if (Character.isDigit(input.charAt(i))) {
                seed = 10 * seed + Long.parseLong("" + input.charAt(i));
            }
        }
        return new Random(seed);
    }

    // Create an empty world and fill each array element with Tileset.NOTHING
    private void createEmptyWorld() {
        finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }
    }

    // Draw a given room with grass and flowers
    private void drawRoom(Room r) {

        for (int i = 0; i < r.roomwidth; i += 1) {
            for (int j = 0; j < r.roomheight; j += 1) {
                finalWorldFrame[r.roomx + i][r.roomy + j] = randomTile();
            }
        }
    }

    // Draw a room at the given coordinates of given dimensions
    private void drawRoom(int x, int y, int w, int h) {

        for (int i = 0; i < w; i += 1) {
            for (int j = 0; j < h; j += 1) {
                finalWorldFrame[x + i][y + j] = Tileset.GRASS;
            }
        }
    }

    private static TETile randomTile() {
        int tileNum = rand.nextInt(10);
        if (tileNum == 8) {
            return Tileset.FLOWER;
        } else {
            return Tileset.GRASS;
        }
    }

    // Draw a hallway of given length at position (x, y) in a given direction
    private void drawHallway(int x, int y, int len, char dir) {
        if (dir == 'n') {
            for (int i = 0; i < len && (y + i) < HEIGHT; i += 1) {
                finalWorldFrame[x][y + i] = randomTile();
            }
        } else if (dir == 's') {
            for (int i = 0; i < len && (y - i) > 0; i += 1) {
                finalWorldFrame[x][y - i] = randomTile();
            }
        } else if (dir == 'w') {
            for (int i = 0; i < len && (x - i) > 0; i += 1) {
                finalWorldFrame[x - i][y] = randomTile();
            }
        } else if (dir == 'e') {
            for (int i = 0; i < len && (x + i) < WIDTH; i += 1) {
                finalWorldFrame[x + i][y] = randomTile();
            }
        } else {
            throw new RuntimeException("Not a valid direction");
        }
    }

    // Create and draw hallways between two given rooms
    private void drawHallway(Room a, Room b) {
        int[] pointa = randomPoint(a);
        int[] pointb = randomPoint(b);

        if (RandomUtils.bernoulli(rand)) {
            if (pointb[1] - pointa[1] > 0) {
                drawHallway(pointa[0], pointa[1], pointb[1] - pointa[1], 'n');
                if (pointb[0] - pointa[0] > 0) {
                    drawHallway(pointa[0], pointa[1] + (pointb[1] - pointa[1]),
                            pointb[0] - pointa[0], 'e');
                } else {
                    drawHallway(pointa[0], pointa[1] + (pointb[1] - pointa[1]),
                            pointa[0] - pointb[0], 'w');
                }
            } else {
                drawHallway(pointa[0], pointa[1], pointa[1] - pointb[1], 's');
                if (pointb[0] - pointa[0] > 0) {
                    drawHallway(pointa[0], pointa[1] - (pointa[1] - pointb[1]),
                            pointb[0] - pointa[0], 'e');
                } else {
                    drawHallway(pointa[0], pointa[1] - (pointa[1] - pointb[1]),
                            pointa[0] - pointb[0], 'w');
                }
            }
        } else {
            if (pointb[0] - pointa[0] > 0) {
                drawHallway(pointa[0], pointa[1], pointb[0] - pointa[0], 'e');
                if (pointb[1] - pointa[1] > 0) {
                    drawHallway(pointa[0] + (pointb[0] - pointa[0]), pointa[1],
                            pointb[1] - pointa[1], 'n');
                } else {
                    drawHallway(pointa[0] + (pointb[0] - pointa[0]), pointa[1],
                            pointa[1] - pointb[1], 's');
                }
            } else {
                drawHallway(pointa[0], pointa[1], pointa[0] - pointb[0], 'w');
                if (pointb[1] - pointa[1] > 0) {
                    drawHallway(pointa[0] - (pointa[0] - pointb[0]), pointa[1],
                            pointb[1] - pointa[1], 'n');
                } else {
                    drawHallway(pointa[0] - (pointa[0] - pointb[0]), pointa[1],
                            pointa[1] - pointb[1], 's');
                }
            }

        }
    }

    // Choose a random point in a given room
    private int[] randomPoint(Room r) {
        return new int[] {rand.nextInt(r.roomwidth) + r.roomx,
                rand.nextInt(r.roomheight) + r.roomy};
    }

    // Draw all the hallways for a given array of rooms, including the starting room
    private void drawAllHallways(Room[] rooms) {
        drawHallway(new Room(5, 5, WIDTH - 6, HEIGHT - 8), rooms[0]);
        for (int i = 0; i < rooms.length - 1; i += 1) {
            drawHallway(rooms[i], rooms[i + 1]);
        }
        drawHallway(rooms[rooms.length - 1], new Room(5, 5, 1, 1));
    }

    // Ensure the top right and bottom left rooms have no intersections
    private boolean intersection(Room r) {
        if ((r.roomx <= 7) && (r.roomy <= 7)) {
            return true;
        } else if ((r.roomx + r.roomwidth >= WIDTH - 7) && (r.roomy + r.roomheight >= HEIGHT - 9)) {
            return true;
        }
        return false;
    }

    // Draw a wall if adjacent to a floor tile
    private void drawWalls() {
        for (int i = 1; i < WIDTH - 1; i += 1) {
            for (int j = 1; j < HEIGHT - 1; j += 1) {
                for (int k = -1; k <= 1; k += 1) {
                    for (int l = -1; l <= 1; l += 1) {
                        if ((finalWorldFrame[i + k][j + l] == Tileset.NOTHING)
                                && ((finalWorldFrame[i][j] == Tileset.GRASS)
                                || finalWorldFrame[i][j] == Tileset.FLOWER)) {
                            finalWorldFrame[i + k][j + l] = Tileset.WALL;
                        }
                    }
                }
                if (finalWorldFrame[i][j] == Tileset.FLOWER) {
                    flowersLeft += 1;
                }
            }
        }
    }

    // Draw and display the world
    private void renderWorld(TETile[][] f) {
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(f);
        StdDraw.show();
    }

    // Create a string for movements
    private String moveString(String input) {
        String movestr = input;
        if ((movestr.charAt(0) == 'n')) {
            movestr = movestr.substring(1);
        } else if ((movestr.charAt(0) == 'l')) {
            World w = loadWorld();
            movingFrame = w.frame;
            finalWorldFrame = movingFrame;
            player1 = w.player1;
            player2 = w.player2;
            flowersLeft = w.flowersLeft;
            movestr = movestr.substring(1);
        } else if ((movestr.charAt(0) == 'q')) {
            saveWorld(new World(finalWorldFrame, player1, player2, flowersLeft));
        }
        movestr = movestr.replaceAll("\\d", "");
        return movestr;
    }

    // Move and draw according to a string
    private void move(String str) {
        for (int i = 0; i < str.length(); i += 1) {
            char c = str.charAt(i);
            if (c == 'w') {
                player1.moveN();
            } else if (c == 'a') {
                player1.moveW();
            } else if (c == 'd') {
                player1.moveE();
            } else if (c == 's') {
                player1.moveS();
            } else if (c == 'i') {
                player2.moveN();
            } else if (c == 'j') {
                player2.moveW();
            } else if (c == 'l') {
                player2.moveE();
            } else if (c == 'k') {
                player2.moveS();
            }
        }
    }

    // Move according to a string, but don't draw
    private void moveNoDraw(String str) {
        for (int i = 0; i < str.length(); i += 1) {
            char c = str.charAt(i);
            if (c == 'w') {
                player1.moveNoDrawN();
            } else if (c == 'a') {
                player1.moveNoDrawW();
            } else if (c == 'd') {
                player1.moveNoDrawE();
            } else if (c == 's') {
                player1.moveNoDrawS();
            } else if (c == 'i') {
                player2.moveNoDrawN();
            } else if (c == 'j') {
                player2.moveNoDrawW();
            } else if (c == 'l') {
                player2.moveNoDrawE();
            } else if (c == 'k') {
                player2.moveNoDrawS();
            } else if (c == 'q') {
                saveWorld(new World(finalWorldFrame, player1, player2, flowersLeft));
            }
        }
    }

    // Check if a space is clear
    private boolean isClear(int x, int y) {
        if (!(finalWorldFrame[x][y].equals(Tileset.WALL))) {
            return true;
        }
        return false;
    }

    // Start the game with a main menu
    private void drawMainMenu() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);

        StdDraw.enableDoubleBuffering();
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.7, "Flower Power");
        StdDraw.show();

        Font font2 = new Font("Times New Roman", Font.BOLD, 20);
        StdDraw.setFont(font2);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.4, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.35, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.3, "Quit (Q)");

        StdDraw.show();
    }

    // Ask for a new game seed
    private long askForSeed() {
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.show();
        StdDraw.text(WIDTH / 2, HEIGHT * 0.6,
                "Please enter a seed, or press 'r' for a random seed. Press s to confirm: ");
        StdDraw.show();
        char s = waitForMenuKey();
        seed = 0;
        while (s != 's') {
            if (s == 'r') {
                seed = System.currentTimeMillis();
                break;
            }
            if (Character.isDigit(s)) {
                StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
                StdDraw.text(WIDTH / 2, HEIGHT * 0.6,
                        "Please enter a seed, or press 'r' for a random seed. "
                                + "Press s to confirm: ");
                StdDraw.show();
                seed = 10 * seed + Long.parseLong("" + s);
                StdDraw.text(WIDTH / 2, HEIGHT * 0.5, "Seed: " + seed);
                StdDraw.show();
            } else {
                StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
                StdDraw.text(WIDTH / 2, HEIGHT * 0.6,
                        "Please enter a seed, or press 'r' for a random seed. "
                                + "Press s to confirm: ");
                StdDraw.show();
                StdDraw.text(WIDTH / 2, HEIGHT * 0.5, "Seed: " + seed);
                StdDraw.text(WIDTH / 2, HEIGHT * 0.4, "numbers only pls");
                StdDraw.show();
            }
            s = waitForMenuKey();
        }
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        return seed;
    }

    // Wait for a key and return it
    private char waitForKey() {
        while (!StdDraw.hasNextKeyTyped()) {
            StdDraw.pause(10);
            mouseTile();
        }
        return StdDraw.nextKeyTyped();
    }

    // Wait for a menu key and return it
    private char waitForMenuKey() {
        while (!StdDraw.hasNextKeyTyped()) {
            StdDraw.pause(10);
        }
        return StdDraw.nextKeyTyped();
    }

    // Prints tile at mouse location and flower counts
    private void mouseTile() {
        double x = StdDraw.mouseX();
        double y = StdDraw.mouseY();
        int w = (int) Math.floorDiv((long) x, 1);
        int h = (int) Math.floorDiv((long) y, 1);
        if (h >= 50) {
            h = 49;
        }
        if (w >= 50) {
            w = 49;
        }
        TETile tile = movingFrame[w][h];
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2, HEIGHT - 1, WIDTH / 2, 1);
        StdDraw.setPenColor(Color.PINK);
        StdDraw.textLeft(1, HEIGHT - 1, tile.description());
        StdDraw.textRight(WIDTH - 1, HEIGHT - 1,
                "Flowers left: " + flowersLeft + "   Player 1: " + player1.flowers
                        + "   Player 2: " + player2.flowers);
        StdDraw.show(10);

    }

    // A countdown screen
    private void countdown() {
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.setPenColor(Color.PINK);
        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 200));
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "3");
        StdDraw.show(1000);
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "2");
        StdDraw.show(1000);
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "1");
        StdDraw.show(1000);
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
    }

    // Play the game
    private void play() {
        while (true) {
            char k = waitForKey();
            if (k == 'q') {
                saveWorld(new World(movingFrame, player1, player2, flowersLeft));
                playWithKeyboard();
            } else {
                move(Character.toString(k));
            }
            if (noFlowers()) {
                endGame();
            }
        }
    }

    // Check if there are flowers left
    private boolean noFlowers() {
        return flowersLeft == 0;
    }

    // A class to represent a player
    public class Player implements Serializable {
        private int x;
        private int y;
        private int flowers = 0;
        private TETile player;
        Player(int xloc, int yloc, TETile t) {
            x = xloc;
            y = yloc;
            player = t;
        }

        // Move player north
        public void moveN() {
            if (isClear(x, y + 1)) {
                movingFrame[x][y] = Tileset.GRASS;
                if (movingFrame[x][y + 1] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                movingFrame[x][y + 1] = player;
                movingFrame[x][y].draw(x, y);
                y += 1;
                movingFrame[x][y].draw(x, y);
            }
        }

        // Move player west
        public void moveW() {
            if (isClear(x - 1, y)) {
                movingFrame[x][y] = Tileset.GRASS;
                if (movingFrame[x - 1][y] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                movingFrame[x - 1][y] = player;
                movingFrame[x][y].draw(x, y);
                x -= 1;
                movingFrame[x][y].draw(x, y);
            }
        }

        // Move player east
        public void moveE() {
            if (isClear(x + 1, y)) {
                movingFrame[x][y] = Tileset.GRASS;
                if (movingFrame[x + 1][y] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                movingFrame[x + 1][y] = player;
                movingFrame[x][y].draw(x, y);
                x += 1;
                movingFrame[x][y].draw(x, y);
            }
        }

        // Move player south
        public void moveS() {
            if (isClear(x, y - 1)) {
                movingFrame[x][y] = Tileset.GRASS;
                if (movingFrame[x][y - 1] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                movingFrame[x][y - 1] = player;
                movingFrame[x][y].draw(x, y);
                y -= 1;
                movingFrame[x][y].draw(x, y);
            }
        }

        // Move player north
        public void moveNoDrawN() {
            if (isClear(x, y + 1)) {
                finalWorldFrame[x][y] = Tileset.GRASS;
                if (finalWorldFrame[x][y + 1] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                finalWorldFrame[x][y + 1] = player;
                y += 1;
            }
        }

        // Move player west
        public void moveNoDrawW() {
            if (isClear(x - 1, y)) {
                finalWorldFrame[x][y] = Tileset.GRASS;
                if (finalWorldFrame[x - 1][y] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                finalWorldFrame[x - 1][y] = player;
                x -= 1;
            }
        }

        // Move player east
        public void moveNoDrawE() {
            if (isClear(x + 1, y)) {
                finalWorldFrame[x][y] = Tileset.GRASS;
                if (finalWorldFrame[x + 1][y] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                finalWorldFrame[x + 1][y] = player;
                x += 1;
            }
        }

        // Move player south
        public void moveNoDrawS() {
            if (isClear(x, y - 1)) {
                finalWorldFrame[x][y] = Tileset.GRASS;
                if (finalWorldFrame[x][y - 1] == Tileset.FLOWER) {
                    flowers += 1;
                    flowersLeft -= 1;
                }
                finalWorldFrame[x][y - 1] = player;
                y -= 1;
            }
        }
    }

    // End the game
    private void endGame() {
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.setPenColor(Color.PINK);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 10, "Final score:");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 5, "Player 1 - " + player1.flowers);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Player 2 - " + player2.flowers);
        if (player1.flowers > player2.flowers) {
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, "Player 1 wins!");
        } else if (player2.flowers > player1.flowers) {
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, "Player 2 wins!");
        } else {
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, "It's a tie!");
        }
        StdDraw.show();
        while (true) {
            StdDraw.pause(100);
            if (StdDraw.hasNextKeyTyped()) {
                char k = StdDraw.nextKeyTyped();
                if ((k == 'n') || (k == 'q')) {
                    break;
                }
            }
        }
        StdDraw.clear(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 5, "Play again? ");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "(Q)uit or (N)ew game?");
        StdDraw.show();
        char k = waitForMenuKey();
        while (true) {
            if (k == 'q') {
                System.exit(0);
            } else if (k == 'n') {
                playWithKeyboard();
            }
        }
    }

    // Loads a saved world or generates a random world if nothing is saved
    public World loadWorld() {
        File f = new File("./world.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (World) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }

        /* In the case no World has been saved yet, we return a new one. */
        return new World(playWithInputString(Long.toString(System.currentTimeMillis())),
                new Player(3, 3, Tileset.PLAYER), new Player(WIDTH - 4,
                HEIGHT - 6, Tileset.PLAYER), flowersLeft);
    }

    // Saves the current world, including player positions
    private void saveWorld(World w) {
        File f = new File("./world.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(w);
        }  catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    // A world class that can be serialized, might not be necessary
    public class World implements Serializable {
        Player player1;
        Player player2;
        int flowersLeft;
        TETile[][] frame;
        World(TETile[][] f, Player p1, Player p2, int flo) {
            player1 = p1;
            player2 = p2;
            frame = f;
            flowersLeft = flo;
        }
    }
}

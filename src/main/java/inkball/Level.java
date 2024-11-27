package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a level in the InkBall game.
 * Handles the loading, parsing, and rendering of the level layout.
 */
public class Level {
    /** Reference to the main App instance */
    private App p;
    /** The file path or string containing the level layout */
    private String layout;
    /** Array of wall images */
    private PImage[] walls;
    /** Array of hole images */
    private PImage[] holes;
    /** Image of the background tile */
    private PImage tile;
    /** Image of the entry point */
    private PImage entrypoint;
    /** Array of ball images */
    private PImage[] ballImages;
    /** Array of strings representing each line in the level layout */
    private String[] lines;
    /** 2D array representing the grid of characters from the level layout */
    private char[][] grid;
    /** List of Hole objects in the level */
    private List<Hole> holeList = new ArrayList<>();
    /** List of spawn points for balls */
    private List<PVector> spawnPoints;
    /** Map tracking the hit counts for breakable bricks */
    private Map<PVector, Integer> brickHitCounts = new HashMap<>();

    /**
     * Constructs a new Level object.
     *
     * @param p           Reference to the main App instance
     * @param layout      The level layout as a string or file path
     * @param walls       Array of wall images
     * @param holes       Array of hole images
     * @param tile        Image of the background tile
     * @param entrypoint  Image of the entry point
     * @param ballImages  Array of ball images
     */
    public Level(App p, String layout, PImage[] walls, PImage[] holes, PImage tile, PImage entrypoint, PImage[] ballImages) {
        this.p = p;
        this.layout = layout;
        this.walls = walls;
        this.holes = holes;
        this.tile = tile;
        this.entrypoint = entrypoint;
        this.ballImages = ballImages;
        this.lines = p.loadStrings(layout);
        this.spawnPoints = new ArrayList<>();

        grid = new char[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            grid[i] = lines[i].toCharArray();
        }
        initializeLevel();
    }

    /**
     * Initializes the level by parsing the grid and setting up game elements.
     */
    private void initializeLevel() {

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                float x = col * App.CELLSIZE;
                float y = row * App.CELLSIZE + App.TOPBAR;  // Avoids the top bar area
                char tileChar = grid[row][col];

                switch (tileChar) {
                    case 'B':
                        // Handle pre-set balls in the level
                        if (col + 1 < grid[row].length) {
                            int colorIndex = Character.getNumericValue(grid[row][col + 1]);
                            PVector position = new PVector(col * App.CELLSIZE, row * App.CELLSIZE + App.TOPBAR);

                            // Creates a pre-set ball
                            float vx = App.random.nextFloat() < 0.5 ? 2 : -2;
                            float vy = App.random.nextFloat() < 0.5 ? 2 : -2;

                            Ball prePlacedBall = new Ball(p, position.x, position.y, vx, vy, ballImages[colorIndex], this, colorIndex, ballImages);
                            p.addBall(prePlacedBall);

                            // Sets the current character and the one to the right to a blank space
                            grid[row][col] = ' ';
                            grid[row][col + 1] = ' ';
                        }
                        break;

                    case 'H':
                        // Handle holes in the level
                        if (col + 1 < grid[row].length) {
                            char holeColorChar = grid[row][col + 1];
                            grid[row][col + 1] = ' ';
                            int holeColor = Character.getNumericValue(holeColorChar);

                            // Stores the hole’s position and color
                            holeList.add(new Hole(col * App.CELLSIZE, row * App.CELLSIZE + App.TOPBAR, holeColor));
                            // Skips the next character
                            col++;
                        }
                        break;

                    case 'S':
                        // Adds the 'S' position to the spawn points list
                        float sx = col * App.CELLSIZE + App.CELLSIZE / 2;
                        float sy = row * App.CELLSIZE + App.CELLSIZE / 2 + App.TOPBAR;
                        spawnPoints.add(new PVector(sx, sy));
                        break;

                    case '5':
                        // Initialize breakable brick of type '5'
                        brickHitCounts.put(new PVector(col, row), 0);
                        break;

                    case '6':
                        // Initialize breakable brick of type '6'
                        brickHitCounts.put(new PVector(col, row), 0);
                        break;

                    default:
                        // Does nothing for other characters
                        break;
                }
            }
        }
    }

    /**
     * Gets a random spawn point from the list of spawn points.
     *
     * @return A PVector representing the spawn point coordinates
     */
    public PVector getRandomSpawnPoint() {
        int index = App.random.nextInt(spawnPoints.size());
        return spawnPoints.get(index);
    }

    /**
     * Parses and draws the level on the screen.
     */
    // Parses and draws the level
    public void draw() {
        // Draw background tiles
        for (int row = 0; row < lines.length; row++) {
            String line = lines[row];
            for (int col = 0; col < line.length(); col++) {
                float x = col * App.CELLSIZE;
                float y = row * App.CELLSIZE + App.TOPBAR;  // Avoids the top bar area

                // Draws the background tile
                p.image(tile, x, y, App.CELLSIZE, App.CELLSIZE);
            }
        }

        // Draw game elements based on grid characters
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                float x = col * App.CELLSIZE;
                float y = row * App.CELLSIZE + App.TOPBAR;  // Avoids the top bar area

                char tileChar = grid[row][col];

                switch (tileChar) {
                    case 'X':
                        // Draws a wall (default gray wall)
                        p.image(walls[0], x, y, App.CELLSIZE, App.CELLSIZE);
                        break;
                    case '1':
                        // Draws an orange wall
                        p.image(walls[1], x, y, App.CELLSIZE, App.CELLSIZE);
                        break;
                    case '2':
                        // Draws a blue wall
                        p.image(walls[2], x, y, App.CELLSIZE, App.CELLSIZE);
                        break;
                    case '3':
                        // Draws a green wall
                        p.image(walls[3], x, y, App.CELLSIZE, App.CELLSIZE);
                        break;
                    case '4':
                        // Draws a yellow wall
                        p.image(walls[4], x, y, App.CELLSIZE, App.CELLSIZE);
                        break;
                    case '5':
                        // Draws breakable wall type '5' if it hasn't been destroyed
                        PVector wallPosition = new PVector(col, row);
                        if (brickHitCounts.containsKey(wallPosition)) {
                            p.image(walls[5], x, y, App.CELLSIZE, App.CELLSIZE);
                        }
                        break;
                    case '6':
                        // Draws breakable wall type '6' if it hasn't been destroyed
                        PVector wallPosition1 = new PVector(col, row);
                        if (brickHitCounts.containsKey(wallPosition1)) {
                            p.image(walls[6], x, y, App.CELLSIZE, App.CELLSIZE);
                        }
                        break;
                    case 'H':
                        // Draws the hole
                        if (col + 1 < grid[0].length) {
                            char holeColor = lines[row].charAt(col + 1);
                            int holeIndex = Character.getNumericValue(holeColor);

                            p.image(holes[holeIndex], x, y, App.CELLSIZE * 2, App.CELLSIZE * 2);

                            // Skips the next character (i.e., the color number for the hole)
                            col++;
                        }
                        break;

                    case 'S':
                        // Draws the spawn point
                        p.image(entrypoint, x, y, App.CELLSIZE, App.CELLSIZE);
                        break;

                    default:
                        // Only draws the background tile; other characters are ignored
                        break;
                }
            }
        }
    }

    /**
     * Determines if a given position is occupied by a wall.
     *
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @return True if the position is a wall, false otherwise
     */
    public boolean isWall(float x, float y) {
        // Converts coordinates to row and column in the grid
        int col = (int) (x / App.CELLSIZE);
        int row = (int) ((y - App.TOPBAR) / App.CELLSIZE);

        char tileChar = grid[row][col];
        return tileChar == 'X' || tileChar == '1' || tileChar == '2' || tileChar == '3' || tileChar == '4' || tileChar == '5' || tileChar == '6';
    }

    /**
     * Handles collision with breakable bricks and updates their hit counts.
     *
     * @param x          The x-coordinate of the collision
     * @param y          The y-coordinate of the collision
     * @param colorIndex The color index of the ball that hit the brick
     */
    public void checkBrickCollision(float x, float y, int colorIndex) {
        int wallPosition = getWallColor(x, y);
        if (wallPosition >= 5) {
            if (colorIndex == wallPosition - 4) {
                int col = (int) (x / App.CELLSIZE);
                int row = (int) ((y - App.TOPBAR) / App.CELLSIZE);
                PVector temp = new PVector(col, row);
                int hitCount = brickHitCounts.get(temp) + 1;
                System.out.println(hitCount);
                brickHitCounts.put(temp, hitCount);
                if (hitCount >= 3) {
                    // Wall hit three times, remove it
                    brickHitCounts.remove(temp);
                    grid[row][col] = ' ';
                }
            }
        }
    }

    /**
     * Gets the color code of the wall at a given position.
     *
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @return An integer representing the wall's color code
     */
    public int getWallColor(float x, float y) {
        int col = (int) (x / App.CELLSIZE);
        int row = (int) ((y - App.TOPBAR) / App.CELLSIZE);

        char tileChar = grid[row][col];

        // Checks if it is a colored wall and returns the color code
        switch (tileChar) {
            case '1': return 1; // Orange wall
            case '2': return 2; // Blue wall
            case '3': return 3; // Green wall
            case '4': return 4; // Yellow wall
            case '5': return 5; // Breakable wall type 5
            case '6': return 6; // Breakable wall type 6
            default: return 0;  // No color or invalid character
        }
    }

    /**
     * Checks if a ball at a given position collides with any hole.
     *
     * @param x The x-coordinate of the ball
     * @param y The y-coordinate of the ball
     * @return The Hole object if collision occurs, null otherwise
     */
    public Hole checkHoleCollision(float x, float y) {
        for (Hole hole : holeList) {
            if (hole.isInside(x, y)) {
                return hole;  // Returns the matching hole
            }
        }
        return null;  // No matching hole found
    }

    /**
     * Retrieves the positions of all walls in the level.
     *
     * @return A list of PVector objects representing wall positions
     */
    public List<PVector> getWallPositions() {
        List<PVector> wallPositions = new ArrayList<>();

        int rows = grid.length;
        int cols = grid[0].length;

        // Traverse the border of the grid to find walls
        for (int col = 0; col < cols; col++) {
            char tile = grid[0][col];
            if (isWallCharacter(tile)) {
                wallPositions.add(new PVector(col, 0));
            }
        }

        for (int row = 1; row < rows; row++) {
            char tile = grid[row][cols - 1];
            if (isWallCharacter(tile)) {
                wallPositions.add(new PVector(cols - 1, row));
            }
        }

        for (int col = cols - 2; col >= 0; col--) {
            char tile = grid[rows - 1][col];
            if (isWallCharacter(tile)) {
                wallPositions.add(new PVector(col, rows - 1));
            }
        }

        for (int row = rows - 2; row > 0; row--) {
            char tile = grid[row][0];
            if (isWallCharacter(tile)) {
                wallPositions.add(new PVector(0, row));
            }
        }

        return wallPositions;  // Returns all wall coordinates
    }

    /**
     * Checks if a character represents a wall.
     *
     * @param tile The character to check
     * @return True if the character represents a wall, false otherwise
     */
    private boolean isWallCharacter(char tile) {
        return tile == 'X' || tile == '1' || tile == '2' || tile == '3' || tile == '4';
    }
}

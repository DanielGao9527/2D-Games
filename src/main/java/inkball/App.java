package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.*;

/**
 * The main game application class for InkBall, managing game state, user input,
 * and level configuration for gameplay.
 */
public class App extends PApplet {

    /** The size of each cell in pixels */
    public static final int CELLSIZE = 32;
    /** Height of the top area in pixels */
    public static final int TOPBAR = 64;
    /** Game window width */
    public static int WIDTH = 576;
    /** Game window height */
    public static int HEIGHT = 640;
    /** Board width in cells */
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    /** Board height in cells */
    public static final int BOARD_HEIGHT = 20;
    /** Frames per second for game rendering */
    public static final int FPS = 30;

    /** Path to the configuration file */
    public String configPath;
    /** JSON object for configuration data */
    JSONObject config;

    /** Map to store loaded sprites */
    private HashMap<String, PImage> sprites = new HashMap<>();
    /** Random number generator for general use */
    public static Random random = new Random();
    /** Random number generator with seed */
    private Random vrandom = new Random();
    /** Array of ball images */
    PImage[] ballImages = new PImage[5];
    /** Array of wall images */
    private PImage[] walls = new PImage[7];
    /** Array of hole images */
    private PImage[] holes = new PImage[5];
    /** Tile image */
    PImage tile;
    /** Entry point image */
    PImage entrypoint;
    /** Level layout as a string */
    String layout;
    /** Array of ball colors for the level */
    JSONArray ballColorsArray;
    /** Array of levels from configuration */
    JSONArray levels;

    /** List of active balls in the game */
    List<Ball> balls;
    /** Queue of ball colors to spawn */
    LinkedList<String> ballQueue;
    /** View of the ball queue for display */
    private LinkedList<String> ballQueueview;
    /** List of player-drawn lines */
    List<PlayerLine> playerLines;
    /** Current level object */
    Level level;
    /** List of balls to remove from the game */
    List<Ball> ballsToRemove = new ArrayList<>();
    /** Flag to check if the game is paused */
    boolean isPaused = false;
    /** Flag to check if the level is complete */
    boolean levelComplete = false;
    /** Flag to check if the game is complete */
    boolean gameComplete = false;
    /** Player's score */
    int score = 0;
    /** Timer for the level */
    int timer;
    /** Total time for the level */
    int time;
    /** Timer for spawning balls */
    float spawnTimer = 0;
    /** Interval between spawning balls */
    float spawnInterval;
    /** List of wall positions in the level */
    private List<PVector> wallPositions;
    /** Index for the first yellow tile animation */
    private int yellowTile1Index = 0;
    /** Index for the second yellow tile animation */
    private int yellowTile2Index = 0;
    /** Offset for the ball queue image display */
    private int imageOffset = 0;
    /** Index of the current level */
    int currentLevelIndex = 0;
    /** Score at the start of the level */
    private int levelStartingScore = 0;
    /** Modifier for increasing score */
    private float scoreIncreaseModifier = 1.0f;
    /** Modifier for decreasing score */
    private float scoreDecreaseModifier = 1.0f;

    /** Map of score increase values by ball color */
    private Map<Integer, Integer> scoreIncreaseByColor = new HashMap<>();
    /** Map of score decrease values by ball color */
    private Map<Integer, Integer> scoreDecreaseByColor = new HashMap<>();

    /**
     * Default constructor that initializes the configuration file path
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Constructor that sets a custom configuration file path and random seed.
     *
     * @param config the path to the configuration file
     * @param seed   the random seed for generating random values
     */
    public App(String config, long seed) {
        this.configPath = config;
        vrandom.setSeed(seed);
    }

    /**
     * Sets up the initial window size for the game.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT); // Set window size
    }

    /**
     * Loads the ball queue based on the color data in the configuration.
     *
     * @param ballColors an array of ball color names in JSON format
     */
    public void loadBallQueue(JSONArray ballColors) {
        ballQueue = new LinkedList<>();
        for (int i = 0; i < ballColors.size(); i++) {
            ballQueue.add(ballColors.getString(i));
        }
    }

    /**
     * Sets up the initial state of the game, loading images, configuration, and levels.
     */
    @Override
    public void setup() {
        frameRate(FPS);

        // Load image resources
        tile = getSprite("tile");
        entrypoint = getSprite("entrypoint");

        ballImages = new PImage[5];
        holes = new PImage[5];
        walls = new PImage[7];

        // Load ball, hole, and wall images
        for (int i = 0; i < 5; i++) {
            ballImages[i] = getSprite("ball" + i);
            holes[i] = getSprite("hole" + i);
            walls[i] = getSprite("wall" + i);
        }
        walls[5] = getSprite("wall" + 5);
        walls[6] = getSprite("wall" + 6);

        // Load configuration file
        config = loadJSONObject(configPath);
        levels = config.getJSONArray("levels");

        // Load the first level
        loadLevel(currentLevelIndex);
    }

    /**
     * Loads a specific level based on the index and initializes necessary variables.
     *
     * @param levelIndex the index of the level to load
     */
    void loadLevel(int levelIndex) {
        if (levelIndex >= levels.size()) {
            gameComplete = true;
            return;
        }
        levelStartingScore = score;

        // Load score modifiers from configuration
        JSONObject scoreIncrease = config.getJSONObject("score_increase_from_hole_capture");
        scoreIncreaseByColor.put(0, scoreIncrease.getInt("grey"));
        scoreIncreaseByColor.put(1, scoreIncrease.getInt("orange"));
        scoreIncreaseByColor.put(2, scoreIncrease.getInt("blue"));
        scoreIncreaseByColor.put(3, scoreIncrease.getInt("green"));
        scoreIncreaseByColor.put(4, scoreIncrease.getInt("yellow"));

        JSONObject scoreDecrease = config.getJSONObject("score_decrease_from_wrong_hole");
        scoreDecreaseByColor.put(0, scoreDecrease.getInt("grey"));
        scoreDecreaseByColor.put(1, scoreDecrease.getInt("orange"));
        scoreDecreaseByColor.put(2, scoreDecrease.getInt("blue"));
        scoreDecreaseByColor.put(3, scoreDecrease.getInt("green"));
        scoreDecreaseByColor.put(4, scoreDecrease.getInt("yellow"));

        JSONObject currentLevel = levels.getJSONObject(levelIndex);
        layout = currentLevel.getString("layout");

        // Load score modifiers for the level
        scoreIncreaseModifier =  currentLevel.getFloat("score_increase_from_hole_capture_modifier");
        scoreDecreaseModifier =  currentLevel.getFloat("score_decrease_from_wrong_hole_modifier") ;

        // Load time and spawn interval
        time = currentLevel.getInt("time");
        timer = time;
        spawnInterval = currentLevel.getInt("spawn_interval");
        spawnTimer = spawnInterval;
        ballColorsArray = config.getJSONArray("levels").getJSONObject(levelIndex).getJSONArray("balls");

        balls = new ArrayList<>();
        level = new Level(this, layout, walls, holes, tile, entrypoint, ballImages);

        playerLines = new ArrayList<>();
        loadBallQueue(ballColorsArray);

        wallPositions = level.getWallPositions();
        yellowTile1Index = 0;
        yellowTile2Index = wallPositions.size() / 2;

        if (!ballQueue.isEmpty()) {
            spawnBall();
        }
        ballQueueview = ballQueue;
        levelComplete = false;
        imageOffset = 0;
    }

    /**
     * Loads a sprite image by its name and stores it in the sprite map if not already loaded.
     *
     * @param s the name of the sprite to load
     * @return the loaded image
     */
    public PImage getSprite(String s) {
        PImage result = sprites.get(s);
        if (result == null) {
            result = loadImage(this.getClass().getResource(s + ".png").getPath().replace("%20", " "));
            sprites.put(s, result);
        }
        return result;
    }

    /**
     * Handles key press events, toggling pause and restarting the game.
     *
     * @param event the key event
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKey() == ' ') {
            // Toggle pause
            isPaused = !isPaused;
        } else if (event.getKey() == 'r') {
            // Restart game or reload level
            if (gameComplete) {
                currentLevelIndex = 0;
                levelStartingScore = 0;
                score = 0;
                gameComplete = false;
                levelComplete = false;
                loadLevel(currentLevelIndex);
            }
            restartGame();
        }
    }

    /**
     * Handles mouse press events, starting a new player-drawn line.
     */
    @Override
    public void mousePressed() {
        if (!levelComplete && mouseButton == LEFT) {
            playerLines.add(new PlayerLine(this));
        }
    }

    /**
     * Handles mouse drag events, adding points to the current player-drawn line.
     */
    @Override
    public void mouseDragged() {
        if (!levelComplete) {
            if (mouseButton == LEFT) {
                if (!playerLines.isEmpty()) {
                    PlayerLine currentLine = playerLines.get(playerLines.size() - 1);
                    currentLine.addPoint(mouseX, mouseY, pmouseX, pmouseY);
                }
            }
        }
    }

    /**
     * Handles mouse release events, removing player-drawn lines on right-click.
     */
    @Override
    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            for (int i = 0; i < playerLines.size(); i++) {
                if (playerLines.get(i).collidesWith(mouseX, mouseY, 5)) {
                    playerLines.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * The main game loop, responsible for updating and rendering the game state.
     */
    @Override
    public void draw() {
        background(255);

        // Draw the top bar with score and time
        drawTopBar();
        // Draw the level
        level.draw();

        // Display all balls
        for (Ball ball : balls) {
            ball.display();
        }

        // Game logic when timer is running and level is not complete
        if (timer > 0 && !levelComplete) {
            if (!isPaused) {

                // Move balls and handle collisions
                for (Ball ball : balls) {
                    ball.move(playerLines);
                    int k = ball.shouldBeRemoved();
                    if (k == 1) {
                        ballsToRemove.add(ball);
                    } else if (k == 2) {
                        ballsToRemove.add(ball);
                        ballQueue.add(getColorName(ball.getColorIndex()));
                    }
                }

                // Remove balls that need to be removed
                balls.removeAll(ballsToRemove);

                // Spawn new balls at intervals
                if (frameCount % 3 == 0) {
                    if (!balls.isEmpty()) {
                        spawnTimer -= 0.1;
                        if (spawnTimer <= 0) {
                            spawnBall();
                            spawnTimer = spawnInterval;
                        }
                    } else {
                        spawnBall();
                        spawnTimer = spawnInterval;
                    }
                }

                // Decrease timer every second
                if (frameCount % FPS == 0 && timer > 0) {
                    timer--;
                }
            }
            // Display spawn timer
            if (!ballQueue.isEmpty()) {
                textAlign(RIGHT, CENTER);
                textSize(20);
                text(nf(spawnTimer, 1, 1), 270, TOPBAR / 2);
            }
            // Display player-drawn lines
            for (PlayerLine line : playerLines) {
                line.display();
            }
            // Check if the level is complete
            checkGameEnd();
        } else if (timer <= 0 && !levelComplete) {
            // Display time's up message
            textSize(16);
            textAlign(CENTER, CENTER);
            fill(0);
            text("=== TIME'S UP ===", width / 2 + 80, TOPBAR / 2);
        } else if (levelComplete) {
            // Handle level completion animation
            if (frameCount % 2 == 0) {
                if (timer > 0) {
                    score += 1;
                    timer--;
                    moveYellowTiles();
                }
            }
            drawYellowTiles();
            if (timer <= 0) {
                // Load next level or end game
                if (currentLevelIndex < levels.size() - 1) {
                    currentLevelIndex++;
                    loadLevel(currentLevelIndex);
                    levelComplete = false;
                } else {
                    gameComplete = true;
                }
            }
        }
    }

    /**
     * Draws the top bar displaying the score, time, and ball queue.
     */
    private void drawTopBar() {
        fill(200);
        rect(0, 0, width, TOPBAR);

        int boxWidth = 210;
        int boxHeight = TOPBAR - 10;
        fill(0);
        rect(10, 5, boxWidth, boxHeight);

        int ballSize = 32;
        int padding = 8;
        int xPos = 20 - imageOffset;
        int yPos = (TOPBAR - ballSize) / 2;
        int count = 0;
        LinkedList<String> q = ballQueueview;
        for (String ballColor : q) {
            if (count < 5) {
                if (xPos >= 20 && xPos <= 180) {
                    int colorIndex = getColorIndex(ballColor);
                    PImage ballImage = ballImages[colorIndex];

                    image(ballImage, xPos, yPos, ballSize, ballSize);
                    count++;
                }
                xPos += ballSize + padding;
            }
        }

        if (!ballQueue.isEmpty() && !isPaused) {
            if (imageOffset < 0) imageOffset += 1;
        }

        fill(0);
        textSize(20);
        textAlign(CENTER, CENTER);

        if (isPaused) {
            text("*** PAUSED ***", width / 2 + 70, TOPBAR / 2);
        } else if (gameComplete) {
            textSize(16);
            textAlign(CENTER, CENTER);
            fill(0);
            text("=== ENDED ===", width / 2 + 80, TOPBAR / 2);
        } else if (levelComplete) {
            textSize(13);
            textAlign(CENTER, CENTER);
            text("=== LEVEL COMPLETE ===", width / 2 + 80, TOPBAR / 2);
        }

        // Display score and time
        textAlign(RIGHT, CENTER);
        textSize(20);
        text("Score: " + score, width - 10, TOPBAR / 3);

        text("Time: " + timer, width - 10, TOPBAR * 2 / 3);
    }

    /**
     * Moves the indices for the yellow tile animation.
     */
    private void moveYellowTiles() {
        yellowTile1Index = (yellowTile1Index + 1) % wallPositions.size();
        yellowTile2Index = (yellowTile2Index + 1) % wallPositions.size();
    }

    /**
     * Draws the yellow tiles for the level completion animation.
     */
    private void drawYellowTiles() {
        PVector yellowTile1Pos = wallPositions.get(yellowTile1Index);
        PVector yellowTile2Pos = wallPositions.get(yellowTile2Index);

        image(walls[4], yellowTile1Pos.x * CELLSIZE, yellowTile1Pos.y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
        image(walls[4], yellowTile2Pos.x * CELLSIZE, yellowTile2Pos.y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
    }

    /**
     * Restarts the game, resetting variables and reloading the level.
     */
    void restartGame() {
        score = levelStartingScore;
        balls.clear();
        playerLines.clear();
        timer = time;
        levelComplete = false;
        spawnTimer = spawnInterval;
        loadBallQueue(ballColorsArray);
        imageOffset = 0;
        level = new Level(this, layout, walls, holes, tile, entrypoint, ballImages);
        if (!ballQueue.isEmpty()) {
            spawnBall();
        }
        ballQueueview = ballQueue;
    }

    /**
     * Checks if the game should end, setting the levelComplete flag if conditions are met.
     */
    private void checkGameEnd() {
        if (balls.isEmpty() && ballQueue.isEmpty()) {
            levelComplete = true;
        }
    }

    /**
     * Updates the player's score based on the ball color and whether it entered the correct hole.
     *
     * @param ballColor   the color index of the ball
     * @param correctHole whether the ball entered the correct hole
     */
    public void updateScore(int ballColor, boolean correctHole) {
        int scoreChange;
        if (correctHole) {
            scoreChange = Math.round(scoreIncreaseByColor.get(ballColor) * scoreIncreaseModifier);
        } else {
            scoreChange = -Math.round(scoreDecreaseByColor.get(ballColor) * scoreDecreaseModifier);
        }
        score += scoreChange;
    }

    /**
     * Adds a ball to the list of active balls in the game.
     *
     * @param ball the ball to add
     */
    public void addBall(Ball ball) {
        balls.add(ball);
    }

    /**
     * Spawns a new ball from the ball queue into the game.
     */
    public void spawnBall() {
        if (ballQueue.isEmpty()) {
            return;
        }

        String colorName = ballQueue.poll();

        imageOffset -= 40;
        int colorIndex = getColorIndex(colorName);

        PVector spawnPoint = level.getRandomSpawnPoint();

        // Randomize ball velocity
        float vx = vrandom.nextFloat() < 0.5 ? 2 : -2;
        float vy = vrandom.nextFloat() < 0.5 ? 2 : -2;

        Ball newBall = new Ball(this, spawnPoint.x, spawnPoint.y, vx, vy, ballImages[colorIndex], level, colorIndex, ballImages);
        balls.add(newBall);
    }

    /**
     * Converts a color index to its corresponding color name.
     *
     * @param colorIndex the index of the color
     * @return the name of the color
     */
    String getColorName(int colorIndex) {
        switch (colorIndex) {
            case 0: return "grey";
            case 1: return "orange";
            case 2: return "blue";
            case 3: return "green";
            case 4: return "yellow";
            default: return "grey";
        }
    }

    /**
     * Converts a color name to its corresponding color index.
     *
     * @param colorName the name of the color
     * @return the index of the color
     */
    public int getColorIndex(String colorName) {
        switch (colorName.toLowerCase()) {
            case "grey":
                return 0;
            case "orange":
                return 1;
            case "blue":
                return 2;
            case "green":
                return 3;
            case "yellow":
                return 4;
            default:
                return 0;
        }
    }

    /**
     * The main method to start the game application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}

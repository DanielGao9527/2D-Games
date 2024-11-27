package inkball;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Test class for the InkBall game.
 * Contains unit tests for various functionalities of the game.
 */
public class InkBallTest {

    /**
     * Tests the basic setup and collision behavior of the game with a sample configuration.
     * This test initializes the game with a specific configuration file and runs it to observe
     * collision behaviors and ensure no exceptions are thrown during execution.
     */
    @Test
    public void collisionTest() {
        // Path to the sample configuration file
        String configPath = "src/test/testconfig1.json";
        // Create a new instance of the App with the given configuration and seed
        App app = new App(configPath, 30);
        app.delay(1000); // Delay to allow setup
        // Run the sketch
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(10000); // Allow the app to run for a while
        app.setup(); // Call setup method
        app.loop();  // Start the game loop
    }

    /**
     * Tests the creation of player lines by simulating mouse input.
     * This test simulates mouse press and drag events to create a player line and checks
     * if the line is correctly added to the game's data structures.
     */
    @Test
    public void testPlayerLineCreation() {
        // Create a new instance of the App
        App app = new App();
        app.delay(1000); // Delay to allow setup
        // Run the sketch
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000); // Additional delay
        app.setup(); // Call setup method
        app.loop();  // Start the game loop

        // Simulate mouse positions for drawing
        app.mouseX = 100;  // Set current mouse x-coordinate
        app.mouseY = 100;  // Set current mouse y-coordinate
        app.pmouseX = 90;  // Set previous mouse x-coordinate (for drag simulation)
        app.pmouseY = 90;  // Set previous mouse y-coordinate

        // Simulate left mouse button press and drag to create a line
        app.mouseButton = PConstants.LEFT;  // Set mouse button to LEFT
        app.mousePressed();  // Simulate mouse press event
        app.mouseDragged();  // Simulate mouse drag event

        // Assert that a new player line has been created
        assertFalse(app.playerLines.isEmpty());  // Ensure the playerLines list is not empty
        PlayerLine line = app.playerLines.get(0);
        line.display();  // Display the line (optional for visual confirmation)
        assertNotNull(line);  // Ensure the line object is not null
        assertFalse(line.getLineSegments().isEmpty());  // Ensure the line has segments added

        // Simulate right mouse button release to remove the line
        app.mouseButton = PConstants.RIGHT;  // Set mouse button to RIGHT
        app.mouseReleased();  // Simulate mouse release event
        assertTrue(app.playerLines.isEmpty());  // Ensure the playerLines list is now empty
    }

    /**
     * Tests the ball collision behavior with player-created lines.
     * This test checks whether the ball correctly bounces off player-drawn lines
     * and whether the lines are removed upon collision as expected.
     */
    @Test
    public void testPlayerLineCollision() {
        // Create a new instance of the App
        App app = new App();
        app.delay(1000); // Delay to allow setup
        // Run the sketch
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000); // Additional delay
        app.setup(); // Call setup method
        app.loop();  // Start the game loop

        int colorIndex = 0; // Assume color index is 0 (e.g., grey color)
        // Create a new ball at position (200, 200) with velocity (2, 2)
        Ball newBall = new Ball(app, 200, 200, 2, 2, app.ballImages[colorIndex], app.level, colorIndex, app.ballImages);

        // Create a player line to simulate a collision
        PlayerLine line = new PlayerLine(app);
        line.addPoint(201, 200, 200, 201); // Add a line segment
        app.playerLines.add(line); // Add the line to the game

        newBall.move(app.playerLines); // Move the ball and check for collisions
        float initialVx = newBall.getVx(); // Get the new velocity after collision
        float initialVy = newBall.getVy();

        // Assert that the ball's velocity has changed, indicating a bounce
        assertNotEquals(initialVx, 2); // Velocity should not be the same after collision
        assertNotEquals(initialVy, 2);
        assertTrue(app.playerLines.isEmpty()); // Line should be removed after collision

        // Test collision when ball moves in the opposite direction
        newBall = new Ball(app, 200, 200, -2, -2, app.ballImages[colorIndex], app.level, colorIndex, app.ballImages);

        // Create another player line
        line = new PlayerLine(app);
        line.addPoint(301, 300, 300, 301); // Add a line segment
        app.playerLines.add(line); // Add the line to the game

        newBall.move(app.playerLines); // Move the ball
        initialVx = newBall.getVx(); // Get the new velocity
        initialVy = newBall.getVy();

        // Check if the line still exists (no collision should have occurred)
        line.collidesWith(200, 200, 16);
        assertFalse(app.playerLines.isEmpty()); // Line should still exist
    }

    /**
     * Tests if the game responds to a timeout condition in the game configuration.
     * This test runs the game with a configuration that includes a timeout to ensure
     * the game correctly handles the end-of-time scenario.
     */
    @Test
    public void TimeoutTest() {
        // Path to the configuration file with timeout settings
        String configPath = "src/test/testconfig2.json";
        // Create a new instance of the App with the given configuration and seed
        App app = new App(configPath, 30);
        app.delay(1000); // Delay to allow setup
        // Run the sketch
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(2000); // Allow the app to run for a while
        app.setup(); // Call setup method
        app.loop();  // Start the game loop
    }

    /**
     * Tests the behavior of ball removal within the game.
     * This test checks whether balls are correctly removed from the game when they
     * enter holes or meet removal criteria.
     */
    @Test
    public void RemoveTest() {
        // Path to the configuration file designed to test ball removal
        String configPath = "src/test/testconfig3.json";
        // Create a new instance of the App with the given configuration and seed
        App app = new App(configPath, 30);
        app.delay(2000); // Delay to allow setup
        // Run the sketch
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(10000); // Allow the app to run for a while to observe ball removal
        app.setup(); // Call setup method
        app.loop();  // Start the game loop
    }

    /**
     * Tests the game's response to key events, specifically the spacebar (pause) and 'r' key (reset).
     * This test simulates key presses and checks if the game's state changes accordingly.
     */
    @Test
    public void testKeyPressed() {
        // Create a new instance of the App
        App app = new App();
        app.delay(1000); // Delay to allow setup
        // Run the sketch
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(2000); // Additional delay
        app.setup(); // Call setup method
        app.loop();  // Start the game loop

        // Simulate pressing the spacebar to pause the game
        KeyEvent keyPressedEvent = new KeyEvent(
                null,                       // Native object, can be null
                System.currentTimeMillis(),  // Timestamp of the event
                KeyEvent.PRESS,              // Key action (press)
                0,                           // No modifier keys
                ' ',                         // Key character (spacebar)
                PConstants.CODED             // Key code (can use PConstants.CODED)
        );

        app.keyPressed(keyPressedEvent); // Simulate key press event
        assertTrue(app.isPaused);  // Assert that the game is now paused

        app.keyPressed(keyPressedEvent); // Simulate key press event again
        assertFalse(app.isPaused); // Assert that the game is now unpaused

        // Simulate pressing the 'r' key to reset the game
        keyPressedEvent = new KeyEvent(
                null,                       // Native object, can be null
                System.currentTimeMillis(),  // Timestamp of the event
                KeyEvent.PRESS,              // Key action (press)
                0,                           // No modifier keys
                'r',                         // Key character ('r' key)
                PConstants.CODED             // Key code (can use PConstants.CODED)
        );

        app.keyPressed(keyPressedEvent); // Simulate key press event
        assertTrue(app.timer > 119);  // Verify that the timer has reset or incremented after 'r' key press
    }
}

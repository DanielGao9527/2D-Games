package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a hole in the InkBall game.
 * A hole can capture balls, and it has a position, size, and color.
 */
public class Hole {
    /** X-coordinate of the top-left corner of the hole */
    private float x;
    /** Y-coordinate of the top-left corner of the hole */
    private float y;
    /** Color index of the hole */
    private int color;
    /** Width of the hole (64 pixels) */
    private final int width = 64;  // Width of the hole (64 pixels)
    /** Height of the hole (64 pixels) */
    private final int height = 64; // Height of the hole (64 pixels)

    /**
     * Constructs a Hole object with specified position and color.
     *
     * @param x     The x-coordinate of the top-left corner of the hole
     * @param y     The y-coordinate of the top-left corner of the hole
     * @param color The color index of the hole
     */
    public Hole(float x, float y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    /**
     * Checks if a point (ball's position) is inside the hole's area.
     * The hole occupies a 2x2 grid, corresponding to 64x64 pixels.
     *
     * @param ballX The x-coordinate of the ball
     * @param ballY The y-coordinate of the ball
     * @return      True if the ball is inside the hole, false otherwise
     */
    // Checks if the ball is inside the hole's area (2x2 hole)
    public boolean isInside(float ballX, float ballY) {
        // Hole size is 64x64 pixels for a 2x2 grid
        return ballX > x && ballX < x + width && ballY > y && ballY < y + height;
    }

    /**
     * Returns the color index of the hole.
     *
     * @return The color index of the hole
     */
    // Returns the color of the hole
    public int getColor() {
        return color;
    }

    /**
     * Gets the x-coordinate of the hole's center.
     *
     * @return The x-coordinate of the hole's center
     */
    // Gets the X-coordinate of the hole's center
    public float getCenterX() {
        return x + width / 2;
    }

    /**
     * Gets the y-coordinate of the hole's center.
     *
     * @return The y-coordinate of the hole's center
     */
    // Gets the Y-coordinate of the hole's center
    public float getCenterY() {
        return y + height / 2;
    }

}

package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.Iterator;
import java.util.List;

/**
 * Represents a ball in the InkBall game.
 * Handles the ball's movement, collision detection, and interactions with the game environment.
 */
public class Ball {
    /** Reference to the main PApplet instance for rendering and utility functions */
    private PApplet p;
    /** Reference to Level class for wall detection and interactions */
    private Level level; // Reference to Level class for wall detection
    /** Ball's position on the x-axis */
    private float x;
    /** Ball's position on the y-axis */
    private float y;
    /** Ball's velocity on the x-axis */
    private float vx;
    /** Ball's velocity on the y-axis */
    private float vy;
    /** Current image representing the ball */
    private PImage image;
    /** Array of ball images for different colors */
    private PImage[] ballImages; // Array of ball images
    /** Index representing the ball's current color */
    private int colorIndex;
    /** Radius of the ball */
    private float radius = 16;
    /** Indicates if the ball is in the process of being removed */
    private boolean isBeingRemoved = false; // Indicates if the ball is being removed
    /** Tracks time for removal animation */
    private float removeAnimationTime = 0;  // Tracks time for removal animation
    /** Maximum time for the removal animation */
    private final float maxRemoveAnimationTime = 16;
    /** Indicates if the ball was removed correctly (entered the correct hole) */
    private boolean correct;

    /**
     * Constructs a new Ball object.
     *
     * @param p           Reference to the main PApplet instance
     * @param x           Initial x-position of the ball
     * @param y           Initial y-position of the ball
     * @param vx          Initial velocity of the ball on the x-axis
     * @param vy          Initial velocity of the ball on the y-axis
     * @param image       Initial image of the ball
     * @param level       Reference to the Level object for environment interactions
     * @param colorIndex  Index representing the ball's color
     * @param ballImages  Array of ball images for different colors
     */
    // Constructor
    public Ball(PApplet p, float x, float y, float vx, float vy, PImage image, Level level, int colorIndex, PImage[] ballImages) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.image = image;
        this.level = level; // Passes Level object
        this.colorIndex = colorIndex;
        this.ballImages = ballImages;
    }

    /**
     * Moves the ball and checks for collisions with walls and player-drawn lines.
     *
     * @param playerLines List of player-drawn lines in the game
     */
    // Moves the ball and checks for wall collision
    public void move(List<PlayerLine> playerLines) {
        checkWallCollision();

        // Check collision with player-drawn lines and remove lines upon collision
        Iterator<PlayerLine> iterator = playerLines.iterator();
        while (iterator.hasNext()) {
            PlayerLine line = iterator.next();
            if (collideWithPlayerLine(line)) {
                iterator.remove(); // Safely remove the entire line
            }
        }

        fixBallPositionIfStuck();
    }

    /**
     * Adjusts the ball's position if it gets stuck inside a wall.
     */
    private void fixBallPositionIfStuck() {
        // Check if the ball is stuck inside the wall on the x-axis
        if (level.isWall(x - radius, y)) {
            x += 2;  // Move the ball right until no longer overlapping with the wall
        } else if (level.isWall(x + radius, y)) {
            x -= 2;  // Move the ball left
        }

        // Check if the ball is stuck inside the wall on the y-axis
        if (level.isWall(x, y - radius)) {
            y += 2;  // Move the ball down
        } else if (level.isWall(x, y + radius)) {
            y -= 2;  // Move the ball up
        }
    }

    /**
     * Checks for collision between the ball and walls by sampling points around the ball's circumference.
     *
     * @param ballX The x-coordinate to check
     * @param ballY The y-coordinate to check
     * @return True if a collision is detected, false otherwise
     */
    private boolean checkCircularWallCollision(float ballX, float ballY) {
        // Checks multiple angles around the ball for wall collision
        int numPoints = 16;  // Divide the circumference into 16 points to check
        for (int i = 0; i < numPoints; i++) {
            float angle = PApplet.TWO_PI * i / numPoints;  // Calculate angle
            float checkX = ballX + radius * PApplet.cos(angle);  // Calculate x-coordinate on the circumference
            float checkY = ballY + radius * PApplet.sin(angle);  // Calculate y-coordinate on the circumference

            // Return true if this point collides with the wall
            if (level.isWall(checkX, checkY)) {
                level.checkBrickCollision(checkX, checkY, colorIndex); // Handle brick collision if applicable
                return true;
            }
        }
        return false;  // No collision detected
    }

    /**
     * Checks for collisions with walls and handles ball movement and bouncing.
     */
    void checkWallCollision() {
        int steps = 5;  // Divide movement into 5 steps for smoother collision detection
        float stepX = vx / steps;
        float stepY = vy / steps;

        for (int i = 0; i < steps; i++) {
            // Use circular collision detection for x-axis movement
            if (!checkCircularWallCollision(x + stepX, y)) {
                x += stepX;  // Update x position
            } else {
                vx = -vx;  // Horizontal bounce
                break;
            }

            // Use circular collision detection for y-axis movement
            if (!checkCircularWallCollision(x, y + stepY)) {
                y += stepY;  // Update y position
            } else {
                vy = -vy;  // Vertical bounce
                break;
            }
        }

        // Check for color-changing walls around the ball
        int leftColor = level.getWallColor(x + vx - radius, y + vy);
        int rightColor = level.getWallColor(x + vx + radius, y + vy);
        int topColor = level.getWallColor(x + vx, y + vy - radius);
        int bottomColor = level.getWallColor(x + vx, y + vy + radius);

        // Update ball color if it hits a colored wall
        if (leftColor >= 1 && leftColor <= 4) {
            colorIndex = leftColor;
            image = ballImages[colorIndex];
        } else if (rightColor >= 1 && rightColor <= 4) {
            colorIndex = rightColor;
            image = ballImages[colorIndex];
        } else if (topColor >= 1 && topColor <= 4) {
            colorIndex = topColor;
            image = ballImages[colorIndex];
        } else if (bottomColor >= 1 && bottomColor <= 4) {
            colorIndex = bottomColor;
            image = ballImages[colorIndex];
        }
    }

    /**
     * Checks for collision between the ball and a player-drawn line.
     * If a collision occurs, updates the ball's velocity to reflect off the line.
     *
     * @param line The player-drawn line to check for collision
     * @return True if a collision occurred, false otherwise
     */
    boolean collideWithPlayerLine(PlayerLine line) {
        for (PlayerLine.Line segment : line.getLineSegments()) {
            PVector closest = getClosestPointOnLine(segment);
            float distance = PVector.dist(new PVector(x, y), closest);

            if (distance <= radius) {
                // Calculate bounce direction
                PVector normal = PVector.sub(new PVector(x, y), closest).normalize();
                PVector velocity = new PVector(vx, vy);
                PVector reflection = PVector.sub(velocity, PVector.mult(normal, 2 * PVector.dot(velocity, normal)));
                vx = reflection.x;
                vy = reflection.y;
                return true; // Collision occurred
            }
        }
        return false; // No collision
    }

    /**
     * Calculates the closest point on a line segment to the ball's position.
     *
     * @param segment The line segment to calculate against
     * @return The closest point on the line segment to the ball
     */
    // Calculates the closest point on a line segment to the ball
    private PVector getClosestPointOnLine(PlayerLine.Line segment) {
        PVector start = new PVector(segment.start.x, segment.start.y);
        PVector end = new PVector(segment.end.x, segment.end.y);
        PVector ballPosition = new PVector(x, y);

        PVector lineVector = PVector.sub(end, start);
        PVector ballToStart = PVector.sub(ballPosition, start);
        float t = PVector.dot(ballToStart, lineVector) / PVector.dot(lineVector, lineVector);
        t = PApplet.constrain(t, 0, 1);  // Restrict t to the range [0, 1]
        return PVector.add(start, PVector.mult(lineVector, t));  // Closest point
    }

    /**
     * Determines if the ball should be removed from the game.
     * Handles interactions with holes and removal animations.
     *
     * @return An integer indicating the removal status:
     *         0 - Ball should not be removed yet
     *         1 - Ball has been successfully removed
     *         2 - Ball should be reset (e.g., wrong hole)
     */
    public int shouldBeRemoved() {
        // Checks if the ball has entered a hole or meets other removal criteria
        if (isBeingRemoved) {
            removeAnimationTime += 1;  // Increment animation time

            // Gradually shrink the ball
            float scaleFactor = PApplet.map(removeAnimationTime, 0, maxRemoveAnimationTime, 0, 1);
            this.radius = 7 * scaleFactor;
            this.vx *= scaleFactor;
            this.vy *= scaleFactor;

            // Remove ball after animation completes
            if (removeAnimationTime + 12 >= maxRemoveAnimationTime && correct) {
                return 1;  // Removal complete
            } else if (removeAnimationTime + 12 >= maxRemoveAnimationTime) {
                return 2; // Ball should be reset
            }
            return 0;  // Animation in progress, keep the ball on screen
        }
        Hole hole = level.checkHoleCollision(x, y);
        if (hole != null) {
            // Calculate the distance from the ball to the hole
            PVector holeCenter = new PVector(hole.getCenterX(), hole.getCenterY());
            PVector ballPosition = new PVector(x, y);
            PVector attraction = PVector.sub(holeCenter, ballPosition);
            float distanceToHole = attraction.mag();

            // If the ball is within 32 pixels, start attracting and shrinking it
            if (distanceToHole < 32) {
                // Apply attraction force, adding proportional to the distance
                attraction.normalize();  // Normalize the vector to ensure correct attraction direction
                attraction.mult(Math.max(0.008f * distanceToHole, 0.1f));
                this.vx += attraction.x;
                this.vy += attraction.y;

                // Gradually shrink the ball's size as it gets closer
                float scaleFactor = PApplet.map(distanceToHole, 0, 32, 0, 1);
                this.radius = 16 * scaleFactor;

                // Mark the ball for removal when it reaches the center of the hole
                if (distanceToHole < 16) {
                    isBeingRemoved = true;
                    if (hole.getColor() == colorIndex || colorIndex == 0 || hole.getColor() == 0) {  // Color match or neutral
                        ((App) p).updateScore(hole.getColor(), true);  // Update score positively
                        correct = true;  // Ball is removed correctly
                    } else {
                        ((App) p).updateScore(hole.getColor(), false);  // Update score negatively
                        correct = false;  // Ball should be reset
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Renders the ball on the screen at its current position.
     */
    // Displays the ball
    public void display() {
        p.image(image, x - radius, y - radius, radius * 2, radius * 2); // Draw ball image, centered
    }

    /**
     * Gets the ball's current velocity on the x-axis.
     *
     * @return The x-component of the ball's velocity
     */
    // Gets the ball's x-velocity
    public float getVx() {
        return vx;
    }

    /**
     * Gets the ball's current velocity on the y-axis.
     *
     * @return The y-component of the ball's velocity
     */
    // Gets the ball's y-velocity
    public float getVy() {
        return vy;
    }

    /**
     * Gets the ball's current color index.
     *
     * @return The index representing the ball's color
     */
    // Gets the ball's color index
    public int getColorIndex() {
        return colorIndex;
    }
}

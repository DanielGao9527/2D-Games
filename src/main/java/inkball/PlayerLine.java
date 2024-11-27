package inkball;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Represents a player-drawn line in the InkBall game.
 * The line is composed of multiple line segments that the player draws by dragging the mouse.
 */
public class PlayerLine {
    /**
     * Inner class representing a single line segment between two points.
     */
    static class Line {
        /** Starting point of the line segment */
        public PVector start;
        /** Ending point of the line segment */
        public PVector end;
        /** Thickness of the line segment */
        public float lineThickness;

        /**
         * Constructs a line segment with a start point, end point, and thickness.
         *
         * @param start         Starting point of the line segment
         * @param end           Ending point of the line segment
         * @param lineThickness Thickness of the line segment
         */
        public Line(PVector start, PVector end, float lineThickness) {
            this.start = start;
            this.end = end;
            this.lineThickness = lineThickness;
        }
    }

    /** List of line segments composing the player line */
    private ArrayList<Line> lineSegments;
    /** Reference to the PApplet for drawing and utility methods */
    private PApplet p;
    /** Thickness of the line */
    private final float lineThickness = 10;  // Thickness of the line

    /**
     * Constructs a new PlayerLine.
     *
     * @param p Reference to the PApplet instance
     */
    public PlayerLine(PApplet p) {
        this.p = p;
        lineSegments = new ArrayList<>();
    }

    /**
     * Adds a new line segment to the player line based on mouse positions.
     *
     * @param mouseX   Current x-coordinate of the mouse
     * @param mouseY   Current y-coordinate of the mouse
     * @param pmouseX  Previous x-coordinate of the mouse
     * @param pmouseY  Previous y-coordinate of the mouse
     */
    // Adds a line segment
    public void addPoint(float mouseX, float mouseY, float pmouseX, float pmouseY) {
        lineSegments.add(new Line(new PVector(mouseX, mouseY), new PVector(pmouseX, pmouseY), lineThickness));
    }

    /**
     * Displays all line segments of the player line on the screen.
     */
    // Displays all line segments
    public void display() {
        p.stroke(0);
        p.strokeWeight(lineThickness);
        for (Line segment : lineSegments) {
            p.line(segment.start.x, segment.start.y, segment.end.x, segment.end.y);
        }
    }

    /**
     * Gets the list of line segments composing the player line.
     *
     * @return An ArrayList of Line objects
     */
    // Gets the list of line segments for use by other classes
    public ArrayList<Line> getLineSegments() {
        return lineSegments;
    }

    /**
     * Checks if there is a collision between the player line and a point (e.g., the mouse).
     *
     * @param mouseX x-coordinate of the point to check
     * @param mouseY y-coordinate of the point to check
     * @param radius Radius around the point to consider for collision
     * @return True if there is a collision, false otherwise
     */
    // Checks if there is a collision with the ball
    public boolean collidesWith(float mouseX, float mouseY, float radius) {
        for (Line segment : lineSegments) {
            if (distToSegment(mouseX, mouseY, segment) < radius) {
                return true;  // Collision occurred
            }
        }
        return false;  // No collision
    }

    /**
     * Calculates the shortest distance from a point to a line segment.
     *
     * @param px      x-coordinate of the point
     * @param py      y-coordinate of the point
     * @param segment The line segment to calculate distance to
     * @return The shortest distance from the point to the line segment
     */
    // Calculates the shortest distance from the ball to the line segment
    float distToSegment(float px, float py, Line segment) {
        float vx = segment.end.x - segment.start.x;
        float vy = segment.end.y - segment.start.y;
        float wx = px - segment.start.x;
        float wy = py - segment.start.y;

        float c1 = wx * vx + wy * vy;
        if (c1 <= 0) return PApplet.dist(px, py, segment.start.x, segment.start.y);

        float c2 = vx * vx + vy * vy;
        if (c2 <= c1) return PApplet.dist(px, py, segment.end.x, segment.end.y);

        float b = c1 / c2;
        float pbx = segment.start.x + b * vx;
        float pby = segment.start.y + b * vy;
        return PApplet.dist(px, py, pbx, pby);
    }
}

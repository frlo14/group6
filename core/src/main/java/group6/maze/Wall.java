package group6.maze;

public class Wall {
    Cell a;
    Cell b;

    public Wall(Cell a, Cell b) {
        this.a = a;
        this.b = b;
    }

    public void removeWall() {
        if (a.x == b.x) {
            // checks if theyre in the same column, then removes the shared wall if they're neighbours
            if (a.y > b.y) { a.walls[2] = false; b.walls[0] = false; }
            else { a.walls[0] = false; b.walls[2] = false; }
        } else if (a.y == b.y) {
            // checks if theyre in the same row, then removes the shared wall if they're neighbours
            if (a.x > b.x) { a.walls[3] = false; b.walls[1] = false; }
            else { a.walls[1] = false; b.walls[3] = false; }
        }
    }

    // will at some point need to write something that uses walls[] to decide orientation of wall assets
}

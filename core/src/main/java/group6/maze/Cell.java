package group6.maze;

public class Cell {
    int x;
    int y;
    boolean[] walls = {true, true, true, true}; 

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
}


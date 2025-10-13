package group6.maze;

import java.util.Objects;

public class Chunk extends Maze {
    private final int coordX;
    private final int coordY;
    private final long seed;

    public Chunk(int coordX, int coordY, long globalSeed, int width, int height) {
        super(width, height);
        this.coordX = coordX;
        this.coordY = coordY;
        this.seed = Objects.hash(coordX, coordY, globalSeed);

        random.setSeed(seed);

        this.generate();
    }

    public int getChunkX() {
        return coordX;
    }

    public int getChunkY() {
        return coordY;
    }

    public long getSeed() {
        return seed;
    }

    // makes sure that maze openings for each chunk align with the neighbouring chunk to create a more natural maze feel
    public void alignBorders(Chunk neighbor) {
        CellType[][] grid = getGrid();
        CellType[][] neighborGrid = neighbor.getGrid();

        // checks that the chunks are neighbours (this method is called on the left chunk) and iterates through the rightmost column
        if (neighbor.getChunkX() == coordX + 1 && neighbor.getChunkY() == coordY) {
            for (int y = 1; y < getHeight() - 1; y += 2) {
                boolean open = (Objects.hash(coordX, coordY, y, seed) & 1) == 0; // deterministic hashing algorithm to decide which walls should be 'opened' based on if the hash is odd or even
                if (open) {
                    grid[getWidth() - 1][y] = CellType.PASSAGE;
                    neighborGrid[0][y] = CellType.PASSAGE;
                }
            }
        }
    }
}



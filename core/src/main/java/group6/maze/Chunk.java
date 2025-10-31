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

        // right neighbor
        if (neighbor.getChunkX() == coordX + 1 && neighbor.getChunkY() == coordY) {
            for (int y = 1; y < getHeight() - 1; y += 2) {
                boolean open = (Objects.hash(coordX, coordY, y, seed) & 1) == 0; // deterministic hashing algorithm to decide which walls should be 'opened' based on if the hash is odd or even
                if (open) {
                    grid[getWidth() - 1][y] = CellType.PASSAGE;
                    neighborGrid[0][y] = CellType.PASSAGE;
                }
            }
        }

        // left neighbor
        if (neighbor.getChunkX() == coordX - 1 && neighbor.getChunkY() == coordY) {
            for (int y = 1; y < getHeight() - 1; y += 2) {
                boolean open = (Objects.hash(coordX, coordY, y, seed) & 1) == 0;
                if (open) {
                    grid[0][y] = CellType.PASSAGE;
                    neighborGrid[getWidth() - 1][y] = CellType.PASSAGE;
                }
            }
        }

        // top neighbor
        if (neighbor.getChunkY() == coordY + 1 && neighbor.getChunkX() == coordX) {
            for (int x = 1; x < getWidth() - 1; x += 2) {
                boolean open = (Objects.hash(coordX, coordY, x, seed) & 1) == 0;
                if (open) {
                    grid[x][getHeight() - 1] = CellType.PASSAGE;
                    neighborGrid[x][0] = CellType.PASSAGE;
                }
            }
        }

        // bottom neighbor
        if (neighbor.getChunkY() == coordY - 1 && neighbor.getChunkX() == coordX) {
            for (int x = 1; x < getWidth() - 1; x += 2) {
                boolean open = (Objects.hash(coordX, coordY, x, seed) & 1) == 0;
                if (open) {
                    grid[x][0] = CellType.PASSAGE;
                    neighborGrid[x][getHeight() - 1] = CellType.PASSAGE;
                }
            }
        }
    }
}




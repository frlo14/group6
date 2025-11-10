package group6.maze;

import java.util.Objects;
import java.util.*;
import com.badlogic.gdx.utils.Array;
import java.util.Random;
import com.badlogic.gdx.Gdx;

public class Chunk extends Maze {
    private final int coordX;
    private final int coordY;
    private final long globalSeed; // global seed for the whole maze to ensure consistency
    private final long seed;
    private Array<Powerups> powerups = new Array<>();
    private Powerups powerup;

    public Chunk(int coordX, int coordY, long globalSeed, int width, int height) {
        super(width, height);
        this.coordX = coordX;
        this.coordY = coordY;
        this.globalSeed = globalSeed;
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

    public long getGlobalSeed() {
        return globalSeed;
    }

    public Array<Powerups> getPowerups() {
        return powerups;
    }

    // makes sure that maze openings for each chunk align with the neighbouring chunk to create a more natural maze feel
    public void alignBorders(Chunk neighbor) {
        CellType[][] grid = getGrid();
        CellType[][] neighborGrid = neighbor.getGrid();

        long globalSeed = this.getGlobalSeed();

        // manual spatial hashing algorithm to ensure consistency between chunks and make sure each wall always has the same ID
        long minX = Math.min(coordX, neighbor.getChunkX());
        long minY = Math.min(coordY, neighbor.getChunkY());
        long maxX = Math.max(coordX, neighbor.getChunkX());
        long maxY = Math.max(coordY, neighbor.getChunkY());

        // normalizes values with my world seed using XOR
        long base = splitmix64((minX * 73856093L) ^ (minY * 19349663L) ^ (maxX * 83492791L) ^ (maxY * 29348917L) ^ globalSeed);

        // determines direction of the wall
        int dirX = neighbor.getChunkX() - coordX;
        int dirY = neighbor.getChunkY() - coordY;


        // right neighbour
        if (dirX == 1 && dirY == 0) {
            boolean isOpen = false;
            for (int y = 1; y < getHeight() - 1; y++) {
                boolean open = ((splitmix64(base ^ minX ^ minY ^ maxX ^ maxY ^ y) & 1L) == 0L);
                if (open) {
                    grid[getWidth() - 1][y] = CellType.PASSAGE;
                    neighborGrid[0][y] = CellType.PASSAGE;
                    isOpen = true;
                }
            }
            /* failsafe
            due to the nature of the hashing algorithm, the algorithm computes walls that should be open as closed, this just opens every other wall as a safety mechanism*/ 
            if (!isOpen) {
                for (int y = 1; y < getHeight() - 1; y += 2) {
                    grid[getWidth() - 1][y] = CellType.PASSAGE;
                    neighborGrid[0][y] = CellType.PASSAGE;
                }
            }
        }

        // left neighbour
        if (dirX == -1 && dirY == 0) {
            boolean isOpen = false;
            for (int y = 1; y < getHeight() - 1; y++) {
                boolean open = ((splitmix64(base ^ minX ^ minY ^ maxX ^ maxY ^ y) & 1L) == 0L);
                if (open) {
                    grid[0][y] = CellType.PASSAGE;
                    neighborGrid[getWidth() - 1][y] = CellType.PASSAGE;
                    isOpen = true;
                }
            }
            if (!isOpen) {
                for (int y = 1; y < getHeight() - 1; y += 2) {
                    grid[0][y] = CellType.PASSAGE;
                    neighborGrid[getWidth() - 1][y] = CellType.PASSAGE;
                }
            }
        }

        // top neighbour
        if (dirY == 1 && dirX == 0) {
            boolean isOpen = false;
            int thisY = getHeight() - 1;
            int neighbourY = 0;
            for (int x = 1; x < getWidth() - 1; x++) {
                boolean open = ((splitmix64(base ^ x) & 1L) == 0L);
                if (open) {
                    grid[x][thisY] = CellType.PASSAGE;
                    neighborGrid[x][neighbourY] = CellType.PASSAGE;
                    isOpen = true;
                }
            }
            if (!isOpen) {
                for (int x = 1; x < getWidth() - 1; x += 2) {
                    grid[x][thisY] = CellType.PASSAGE;
                    neighborGrid[x][neighbourY] = CellType.PASSAGE;
                }
            }
        }

        // bottom neighbour
        if (dirY == -1 && dirX == 0) {
            boolean isOpen = false;
            int thisY = 0;
            int neighbourY = getHeight() - 1;
            for (int x = 1; x < getWidth() - 1; x++) {
                boolean open = ((splitmix64(base ^ x) & 1L) == 0L);
                if (open) {
                    grid[x][thisY] = CellType.PASSAGE;
                    neighborGrid[x][neighbourY] = CellType.PASSAGE;
                    isOpen = true;
                }
            }
            if (!isOpen) {
                for (int x = 1; x < getWidth() - 1; x += 2) {
                    grid[x][thisY] = CellType.PASSAGE;
                    neighborGrid[x][neighbourY] = CellType.PASSAGE;
                }
            }
        }
    }

    // pesudo random method mixes bits using right shifts, XOR and multiplies by golden ratio and other 'magic' constants
    private static long splitmix64(long v) {
        v += 0x9e3779b97f4a7c15L;
        v = (v ^ (v >>> 30)) * 0xbf58476d1ce4e5b9L;
        v = (v ^ (v >>> 27)) * 0x94d049bb133111ebL;
        v = v ^ (v >>> 31);
        return v;
    }

        
    // randomly chooses a location and a powerup type then creates it
    public void spawnPowerups(Main world, int count, Random rng) {
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (grid[x][y] != Maze.CellType.PASSAGE);

            Powerups.Type type = Powerups.Type.values()[rng.nextInt(Powerups.Type.values().length-1)]; // omits the key event from random generation

            Powerups p = new Powerups(type, x, y, Powerups.getPowerupTexture(type));
            powerups.add(p);
        }
    }



}

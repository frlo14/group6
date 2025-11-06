// chunk.java — revised
package group6.maze;

import java.util.Objects;

import com.badlogic.gdx.Gdx;

public class Chunk extends Maze {
    private final int coordX;
    private final int coordY;
    private final long globalSeed; // global seed for the whole maze to ensure consistency
    private final long seed;

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

    // makes sure that maze openings for each chunk align with the neighbouring chunk to create a more natural maze feel
    public void alignBorders(Chunk neighbor) {
        CellType[][] grid = getGrid();
        CellType[][] neighborGrid = neighbor.getGrid();

        long globalSeed = this.getGlobalSeed();

        // manual spatial hashing algorithm to ensure consistency between chunks
        java.util.function.LongSupplier keyBase = () -> {
        long minX = Math.min(coordX, neighbor.getChunkX());
        long maxX = Math.max(coordX, neighbor.getChunkX());
        long minY = Math.min(coordY, neighbor.getChunkY());
        long maxY = Math.max(coordY, neighbor.getChunkY());

        return (minX * 73856093L) ^ (maxX * 19349663L) ^
            (minY * 83492791L) ^ (maxY * 29348917L) ^ globalSeed; // normalizes values with my world seed using XOR
    };


        // right neighbor
        if (neighbor.getChunkX() == coordX + 1 && neighbor.getChunkY() == coordY) {
            long base = keyBase.getAsLong();
            for (int y = 1; y < getHeight() - 1; y += 2) {
                boolean open = (Objects.hash(base, y, coordX, coordY, neighbor.getChunkX(), neighbor.getChunkY()) & 1) == 0; // uses the hash from earlier to determine whether walls should be open or not
                if (open) {
                    grid[getWidth() - 1][y] = CellType.PASSAGE;
                    neighborGrid[0][y] = CellType.PASSAGE;
                }
            }
        }


        // left neighbor
        if (neighbor.getChunkX() == coordX - 1 && neighbor.getChunkY() == coordY) {
            long base = keyBase.getAsLong();
            for (int y = 1; y < getHeight() - 1; y += 2) {
                boolean open = (Objects.hash(base, y, coordX, coordY, neighbor.getChunkX(), neighbor.getChunkY()) & 1) == 1; // opposite hash value for opposite side (breaks if changed)
                if (open) {
                    grid[0][y] = CellType.PASSAGE;
                    neighborGrid[getWidth() - 1][y] = CellType.PASSAGE;
                }
            }
        }

        // top neighbor
        if (neighbor.getChunkY() == coordY + 1 && neighbor.getChunkX() == coordX) {
            System.out.println("running top");
            long base = keyBase.getAsLong();
            for (int x = 1; x < getWidth() - 1; x += 2) {
                boolean open = (Objects.hash(base, x, coordX, coordY, neighbor.getChunkX(), neighbor.getChunkY()) & 1) == 0;
                Gdx.app.log("alignBorders Debugging", String.format("open/base=(%b,%d)",open, base));
                if (open) {
                    grid[x][getHeight() - 1] = CellType.PASSAGE;
                    neighborGrid[x][0] = CellType.PASSAGE;
                }
            }
        }

        // bottom neighbor
        if (neighbor.getChunkY() == coordY - 1 && neighbor.getChunkX() == coordX) {
            System.out.println("running bottom");
            long base = keyBase.getAsLong();
            for (int x = 1; x < getWidth() - 1; x += 2) {
                boolean open = (Objects.hash(base, x, coordX, coordY, neighbor.getChunkX(), neighbor.getChunkY()) & 1) == 1;
                Gdx.app.log("alignBorders Debugging", String.format("open/base=(%b,%d)",open, base));
                if (open) {
                    grid[x][0] = CellType.PASSAGE;
                    neighborGrid[x][getHeight() - 1] = CellType.PASSAGE;
                }
            }
        }
        Gdx.app.log("alignDebug", String.format(
    "This=(%d,%d), Neighbor=(%d,%d)",
    coordX, coordY, neighbor.getChunkX(), neighbor.getChunkY()
));


    }
}

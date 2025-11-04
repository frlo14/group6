package group6.maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maze {
    // cells are either walls or passages
    public enum CellType { BLOCKED, PASSAGE }

    private final int width;
    private final int height;
    private final CellType[][] grid;
    public final Random random;

    public Maze(int width, int height) {
        // makes sure dimensions are odd so walls fall on even coords and passages fall on odd coords
        this.width = (width % 2 == 0) ? width + 1 : width;  
        this.height = (height % 2 == 0) ? height + 1 : height; 
        this.grid = new CellType[this.width][this.height];
        this.random = new Random(System.currentTimeMillis());

        // initialises all cells as walls
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                grid[x][y] = CellType.BLOCKED;
            }
        }
    }

    // uses prims algorithm to procedurally generate a chunk of the maze
    public void generate() {
        // picks random starting cell and turns it into a passage
        int sx = randomOdd(width);
        int sy = randomOdd(height);
        grid[sx][sy] = CellType.PASSAGE;

        // adds blocked cells 2 steps away as potential frontiers
        List<int[]> frontier = new ArrayList<>();
        addFrontier(sx, sy, frontier);

        while (!frontier.isEmpty()) {
            int[] cell = frontier.remove(random.nextInt(frontier.size()));
            int fx = cell[0];
            int fy = cell[1];

            // chooses a random frontier cell and finds a valid neighbouring passage
            List<int[]> neighbors = new ArrayList<>();
            for (int[] dir : dirs) {
                int nx = fx + dir[0] * 2;
                int ny = fy + dir[1] * 2;
                if (inBounds(nx, ny) && grid[nx][ny] == CellType.PASSAGE) {
                    neighbors.add(new int[]{nx, ny}); 
                }
            }

            // carves a passage between the frontier cell and a random neighbouring passage
            if (!neighbors.isEmpty()) {
                int[] chosen = neighbors.get(random.nextInt(neighbors.size()));
                int bx = (fx + chosen[0]) / 2;
                int by = (fy + chosen[1]) / 2;
                grid[fx][fy] = CellType.PASSAGE;
                grid[bx][by] = CellType.PASSAGE;

                addFrontier(fx, fy, frontier);
            }
        }
    }

    // defines the cardinal directions
    private static final int[][] dirs = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private void addFrontier(int x, int y, List<int[]> frontier) {
        for (int[] d : dirs) {
            int nx = x + d[0] * 2;
            int ny = y + d[1] * 2;
            if (inBounds(nx, ny) && grid[nx][ny] == CellType.BLOCKED) {
                frontier.add(new int[]{nx, ny});
                grid[nx][ny] = CellType.BLOCKED; 
            }
        }
    }

    private boolean inBounds(int x, int y) {
        return x > 0 && y > 0 && x < width - 1 && y < height - 1;
    }

    private int randomOdd(int limit) {
        int n = random.nextInt(limit / 2) * 2 + 1;
        return Math.min(n, limit - 2);
    }

    public int getWidth() { 
        return width; 
    }

    public int getHeight() { 
        return height; 
    }
    
    public CellType[][] getGrid() { 
        return grid; 
    }

    public boolean isCellBlocked(float x, float y, int tileSize) {
        int tileX = (int)(x / tileSize);
        int tileY = (int)(y / tileSize);

        if (tileX < 0 || tileY < 0 || tileX >= grid.length || tileY >= grid[0].length) {
            return true; 
        }

        return grid[tileX][tileY] == CellType.BLOCKED;
}

}



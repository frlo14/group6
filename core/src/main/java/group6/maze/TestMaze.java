package group6.maze;

// need to delete after maze is generated with assets in LocalMap class
public class TestMaze {
    public static void main(String[] args) {
        long seed = 12345L;

        Chunk chunk = new Chunk(0, 0, seed, 49, 49);
        Chunk chunk2 = new Chunk(1, 0, seed, 49, 49);
        chunk.alignBorders(chunk2);

        System.out.println("Chunk (0,0):");
        print(chunk);

        System.out.println("Chunk (1,0):");
        print(chunk2);
    }

    public static void print(Maze maze) {
        Maze.CellType[][] grid = maze.getGrid();
        int width = maze.getWidth();
        int height = maze.getHeight();

        for (int y = height - 1; y >= 0; y--) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < width; x++) {
                line.append(grid[x][y] == Maze.CellType.BLOCKED ? "-!" : "  ");
            }
            System.out.println(line);
        }
    }
}



package group6.maze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import group6.maze.game.AssetData;

import java.util.HashMap;
import java.util.Map;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Player player;

    private final int mazeWidth = 31;
    private final int mazeHeight = 31;
    private final int tileSize = 64;
    private final int proximityThreshold = 8;
    private final long globalSeed = System.currentTimeMillis();

    private TextureRegion floor;
    private TextureRegion wall;

    // map of chunk coordinates to maze chunks
    private final Map<ChunkCoord, Chunk> chunks = new HashMap<>();
    private ChunkCoord currentChunkCoord;

    private final Rectangle reusableRectangle = new Rectangle();

    @Override
    // constructor class ran once on startup
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        AssetData.load();
        floor = AssetData.floor;
        wall = AssetData.wall;

        // generates initial maze chunk
        Chunk initialChunk = new Chunk(0, 0, globalSeed, mazeWidth, mazeHeight);
        chunks.put(new ChunkCoord(0, 0), initialChunk);
        currentChunkCoord = new ChunkCoord(0, 0);

        // places player in the center
        float startX = (mazeWidth / 2f) * tileSize;
        float startY = (mazeHeight / 2f) * tileSize;

        // splits player assets into direction based animations then creates said player
        TextureRegion[] upFrames    = new TextureRegion[]{ AssetData.playerSprites[0], AssetData.playerSprites[1] };
        TextureRegion[] downFrames  = new TextureRegion[]{ AssetData.playerSprites[4], AssetData.playerSprites[5] };
        TextureRegion[] leftFrames  = new TextureRegion[]{ AssetData.playerSprites[6], AssetData.playerSprites[7] };
        TextureRegion[] rightFrames = new TextureRegion[]{ AssetData.playerSprites[2], AssetData.playerSprites[3] };

        player = new Player(upFrames, downFrames, leftFrames, rightFrames, startX, startY);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        player.update(delta);
        checkPlayerCollision();

        // determines which chunk player is currently in
        int playerTileX = (int) (player.x / tileSize);
        int playerTileY = (int) (player.y / tileSize);

        int currentChunkX = Math.floorDiv(playerTileX, mazeWidth);
        int currentChunkY = Math.floorDiv(playerTileY, mazeHeight);

        int localX = Math.floorMod(playerTileX, mazeWidth);
        int localY = Math.floorMod(playerTileY, mazeHeight);

        currentChunkCoord = new ChunkCoord(currentChunkX, currentChunkY);

        // generates neighboring chunk if player is close to edge
        checkProximityAndGenerate(localX, localY, currentChunkX, currentChunkY);

        // camera follows player
        camera.position.set(player.x, player.y, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // draws all loaded maze chunks
        for (Map.Entry<ChunkCoord, Chunk> entry : chunks.entrySet()) {
            ChunkCoord coord = entry.getKey();
            Chunk chunk = entry.getValue();

            // adjusts offset so that chunks overlap correctly
            float offsetX = coord.x * (mazeWidth - 1) * tileSize;
            float offsetY = coord.y * (mazeHeight - 1) * tileSize;

            Maze.CellType[][] grid = chunk.getGrid();
            for (int y = 0; y < mazeHeight; y++) {
                for (int x = 0; x < mazeWidth; x++) {
                    float sx = offsetX + x * tileSize;
                    float sy = offsetY + y * tileSize;
                    TextureRegion tex = (grid[x][y] == Maze.CellType.PASSAGE) ? floor : wall;
                    batch.draw(tex, sx, sy, tileSize, tileSize);
                }
            }
        }

        // draw player on top
        player.draw(batch);
        batch.end();
    }

    private void checkProximityAndGenerate(int localX, int localY, int currentChunkX, int currentChunkY) {
        // right edge
        if (localX >= mazeWidth - proximityThreshold) generateChunkIfAbsent(currentChunkX + 1, currentChunkY);
        // left edge
        if (localX < proximityThreshold) generateChunkIfAbsent(currentChunkX - 1, currentChunkY);
        // top edge
        if (localY >= mazeHeight - proximityThreshold) generateChunkIfAbsent(currentChunkX, currentChunkY + 1);
        // bottom edge
        if (localY < proximityThreshold) generateChunkIfAbsent(currentChunkX, currentChunkY - 1);
    }

    private void generateChunkIfAbsent(int chunkX, int chunkY) {
        ChunkCoord coord = new ChunkCoord(chunkX, chunkY);
        if (!chunks.containsKey(coord)) {
            Chunk newChunk = new Chunk(chunkX, chunkY, globalSeed, mazeWidth, mazeHeight);

            // aligns borders with existing neighbors in all directions
            Chunk neighbor;

            neighbor = chunks.get(new ChunkCoord(chunkX - 1, chunkY)); // left
            if (neighbor != null) neighbor.alignBorders(neighbor);

            neighbor = chunks.get(new ChunkCoord(chunkX + 1, chunkY)); // right
            if (neighbor != null) newChunk.alignBorders(neighbor);

            neighbor = chunks.get(new ChunkCoord(chunkX, chunkY - 1)); // bottom
            if (neighbor != null) neighbor.alignBorders(neighbor);

            neighbor = chunks.get(new ChunkCoord(chunkX, chunkY + 1)); // top
            if (neighbor != null) newChunk.alignBorders(neighbor);

            chunks.put(coord, newChunk);
        }
    }

    //Helper method to get a chunk from your map using the ChunkCoord record.
    private Chunk getChunkAt(int chunkX, int chunkY) {
        return chunks.get(new ChunkCoord(chunkX, chunkY));
    }

    //Checks for collision between the player and the maze walls.
    private void checkPlayerCollision() {

        //find the tile range the player is currently touching.
        int startTileX = (int) Math.floor(player.bounds.x / tileSize);
        int startTileY = (int) Math.floor(player.bounds.y / tileSize);
        int endTileX = (int) Math.floor((player.bounds.x + player.bounds.width) / tileSize);
        int endTileY = (int) Math.floor((player.bounds.y + player.bounds.height) / tileSize);

        final int effectiveWidth = mazeWidth - 1;
        final int effectiveHeight = mazeHeight - 1;

        //Loop only through a small range of tiles
        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {

                //Find which Chunk this tile (tileX, tileY) belongs to.
                int chunkX = Math.floorDiv(tileX, effectiveWidth);
                int chunkY = Math.floorDiv(tileY, effectiveHeight);

                //Find the local tile coordinate within that chunk.
                int localTileX = Math.floorMod(tileX, effectiveWidth);
                int localTileY = Math.floorMod(tileY, effectiveHeight);

                //Get the chunk
                Chunk chunk = getChunkAt(chunkX, chunkY);
                if (chunk == null || chunk.getGrid() == null) {
                    continue; // This chunk isn't loaded, treat as empty space
                }

                //This is the collision check.
                if (chunk.getGrid()[localTileX][localTileY] == Maze.CellType.BLOCKED) {

                    //Set our reusableRect to the wall's position.
                    reusableRectangle.set(
                        tileX * tileSize, tileY * tileSize, tileSize, tileSize
                    );

                    //Check for an overlap
                    if (player.bounds.overlaps(reusableRectangle)) {
                        player.resolveCollision(reusableRectangle);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        AssetData.dispose();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();
    }

    private record ChunkCoord(int x, int y) {}
}

package group6.maze;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import group6.maze.game.AssetData;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Player player;

    private final int mazeWidth = 31;
    private final int mazeHeight = 31;
    private final int tileSize = 64;
    private final int proximityThreshold = 6;
    private final long globalSeed = System.currentTimeMillis();

    private TextureRegion floor;
    private TextureRegion wall;

    private float timeElapsed;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private BitmapFont font;
    private DecimalFormat timerFormatting;


    // map of chunk coordinates to maze chunks
    public final Map<ChunkCoord, Chunk> chunks = new HashMap<>();
    public ChunkCoord currentChunkCoord;

    @Override
    // constructor class ran once on startup
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        AssetData.load();
        floor = AssetData.floor;
        wall = AssetData.wall; 

        // binds the timer text to the top of the screen
        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), uiCamera);
        font = new BitmapFont();
        font.getData().setScale(2f);
        timerFormatting = new DecimalFormat("00.0");
        timeElapsed = 0;

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
        Chunk currentChunk = chunks.get(currentChunkCoord);
        player.update(delta, this);

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

        // increments timer once per frame
        timeElapsed += delta;

        // displays the formatted time
        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        String displayTime = "Time elapsed: " + timerFormatting.format(timeElapsed);
        font.draw(batch, displayTime, 20, uiViewport.getWorldHeight() - 20);

        batch.end();
    }

    private void checkProximityAndGenerate(int localX, int localY, int currentChunkX, int currentChunkY) {
        if (localX >= mazeWidth - proximityThreshold) generateChunkIfAbsent(currentChunkX + 1, currentChunkY);
        if (localX < proximityThreshold) generateChunkIfAbsent(currentChunkX - 1, currentChunkY);
        if (localY >= mazeHeight - proximityThreshold) generateChunkIfAbsent(currentChunkX, currentChunkY + 1);
        if (localY < proximityThreshold) generateChunkIfAbsent(currentChunkX, currentChunkY - 1);
    }

    private void generateChunkIfAbsent(int chunkX, int chunkY) {
        ChunkCoord coord = new ChunkCoord(chunkX, chunkY);
        if (!chunks.containsKey(coord)) {
            Chunk newChunk = new Chunk(chunkX, chunkY, globalSeed, mazeWidth, mazeHeight);

            chunks.put(coord, newChunk);

            // for each neighbor direction
            ChunkCoord[] neighborOffsets = {
                new ChunkCoord(chunkX - 1, chunkY), 
                new ChunkCoord(chunkX + 1, chunkY), 
                new ChunkCoord(chunkX, chunkY - 1), 
                new ChunkCoord(chunkX, chunkY + 1)  
            };

            for (ChunkCoord neighborCoord : neighborOffsets) {
                Chunk neighbor = chunks.get(neighborCoord);
                if (neighbor != null) {
                    // ran twice for symmetry and consistency
                    newChunk.alignBorders(neighbor);
                    neighbor.alignBorders(newChunk);
                }
            }

            //chunks.put(coord, newChunk);
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
        uiViewport.update(width, height, true);
        camera.update();
    }

    protected record ChunkCoord(int x, int y) {}

    
    public boolean isCellBlocked(float worldX, float worldY, int tileSize) {
    // converts overall coordinates to local tile coords
    int tileX = (int) Math.floor(worldX / tileSize);
    int tileY = (int) Math.floor(worldY / tileSize);

    // determines tile span of a given chunk 
    int chunkTileWidth = mazeWidth - 1; 
    int chunkTileHeight = mazeHeight - 1;

    // finds the chunk the relevant tile is located in
    int chunkX = Math.floorDiv(tileX, chunkTileWidth);
    int chunkY = Math.floorDiv(tileY, chunkTileHeight);

    // finds the local coordinates of the tile within the current chunk
    int localX = Math.floorMod(tileX, chunkTileWidth);
    int localY = Math.floorMod(tileY, chunkTileHeight);

    ChunkCoord coord = new ChunkCoord(chunkX, chunkY);
    Chunk chunk = chunks.get(coord);
    if (chunk == null) {
        return true; // ungenerated chunks are blocked by default (strictly not needed as walls should block it but is safe)
    }

    int gridX = localX;
    int gridY = localY;

    // ensures no out of bounds exceptions
    if (gridX < 0 || gridY < 0 || gridX >= chunk.getWidth() || gridY >= chunk.getHeight()) {
        return true;
    }

    // returns state of cell as bool
    return chunk.getGrid()[gridX][gridY] == Maze.CellType.BLOCKED;
}
}
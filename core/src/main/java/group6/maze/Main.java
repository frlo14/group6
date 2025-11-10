package group6.maze;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import group6.maze.game.AssetData;

import group6.maze.game.AssetData;
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Player player;

    protected final int mazeWidth = 31;
    protected final int mazeHeight = 31;
    protected final int tileSize = 64;
    private int proximityThreshold = 12;
    private final long globalSeed = System.currentTimeMillis();
    public float multiplier = 1f;
    public float startX;
    public float startY;

    private TextureRegion floor;
    private TextureRegion floorWithDesk;
    protected TextureRegion finalFloor;
    private TextureRegion wall;

    private float timeElapsed;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private BitmapFont font;
    private DecimalFormat timerFormatting;

    private EnemyAttack activeAttack;
    private int timesHit;
    protected boolean hasKey;
    protected float displacement;

    public boolean keySpawned;
    public boolean finalChunkSpawned = false;
    private boolean finalScoreCalculated = false;
    private float finalScore;
    public ChunkCoord finalChunkCoord = null;
    


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
        floor = AssetData.woodenFloor;
        floorWithDesk = AssetData.woodenFloorWithDesk;
        finalFloor = AssetData.finalFloor;
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
        startX = (mazeWidth / 2f) * tileSize;
        startY = (mazeHeight / 2f) * tileSize;

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

            // gives blocked cells a wall and passages a type of floor
            Maze.CellType[][] grid = chunk.getGrid();
            for (int y = 0; y < mazeHeight; y++) {
                for (int x = 0; x < mazeWidth; x++) {
                    float sx = offsetX + x * tileSize;
                    float sy = offsetY + y * tileSize;
                    TextureRegion tex;
                    if (chunk.isFinalChunk) {           
                        tex = chunk.finalFloor;             
                    } else if (grid[x][y] == Maze.CellType.BLOCKED) {
                        tex = wall;
                    } else {
                        tex = chunk.getFloorTexture(x, y, floor, floorWithDesk);
                    }

                    batch.draw(tex, sx, sy, tileSize, tileSize);

                }
            }

            // safety, ensures transparent images appear transparent
            batch.enableBlending(); 
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            // draws all active powerups
            for (Powerups p : chunk.getPowerups()) {
                if (p.isActive()) {
                    float px = offsetX + p.x * tileSize;
                    float py = offsetY + p.y * tileSize;
                    batch.draw(Powerups.getPowerupTexture(p.type), px, py, tileSize, tileSize);
                }
            }
        }

        // draw player on top
        player.draw(batch);

        // increments timer once per frame
        incrementTimer(delta, multiplier);

        // displays the formatted ui 
        if (!finalChunkSpawned) {
            uiViewport.apply();
            batch.setProjectionMatrix(uiCamera.combined);

            GlyphLayout layout = new GlyphLayout();

            String displayTime = "Time elapsed: " + timerFormatting.format(timeElapsed);
            font.draw(batch, displayTime, 20, uiViewport.getWorldHeight() - 20);

            layout.setText(font, displayTime);
            float timeWidth = layout.width;

            String displayHits = "Times hit: " + timerFormatting.format(timesHit);
            font.draw(batch, displayHits, 20 + timeWidth + 40, uiViewport.getWorldHeight() - 20);
        }

        // listens for attacks
        if (activeAttack != null) {
            activeAttack.update(delta);
            activeAttack.render(batch);

            if (activeAttack.collidingWithPlayer(player.x, player.y)) {
                timesHit += 1;
                System.out.println("hit");
            }

            if (activeAttack.isFinished()) {
                activeAttack = null;
            }
        }

        // checks how far the player has moved
        this.displacement = this.calcDisplacement(player.x, player.y, this.startX, this.startY);
        
        if (finalChunkCoord != null && currentChunkCoord.equals(finalChunkCoord)) {
            calcScore();
            drawFinalChunkText(batch, font, camera);
        }

        batch.end();
    }

    private void checkProximityAndGenerate(int localX, int localY, int currentChunkX, int currentChunkY) {
        boolean exitSatisfied = this.exitConditionSatisfied(hasKey);
        // normal gameplay loop
        if (!exitSatisfied) {
            if (localX >= mazeWidth - proximityThreshold) generateChunkIfAbsent(currentChunkX + 1, currentChunkY);
            if (localX < proximityThreshold) generateChunkIfAbsent(currentChunkX - 1, currentChunkY);
            if (localY >= mazeHeight - proximityThreshold) generateChunkIfAbsent(currentChunkX, currentChunkY + 1);
            if (localY < proximityThreshold) generateChunkIfAbsent(currentChunkX, currentChunkY - 1);
        }
        // once the game ends generates the last chunk, then stops being able to generate
        else if (!finalChunkSpawned && exitSatisfied) {
            if (localX >= mazeWidth - proximityThreshold) generateFinalChunk(currentChunkX + 1, currentChunkY);
            if (localX < proximityThreshold) generateFinalChunk(currentChunkX - 1, currentChunkY);
            if (localY >= mazeHeight - proximityThreshold) generateFinalChunk(currentChunkX, currentChunkY + 1);
            if (localY < proximityThreshold) generateFinalChunk(currentChunkX, currentChunkY - 1);
        }
        else {
            return;
        }
    }


    private void generateChunkIfAbsent(int chunkX, int chunkY) {
        ChunkCoord coord = new ChunkCoord(chunkX, chunkY);
        if (!chunks.containsKey(coord)) {
            Chunk newChunk = new Chunk(chunkX, chunkY, globalSeed, mazeWidth, mazeHeight);

            Random rng = new Random(globalSeed + chunkX * 10007L + chunkY * 7919L); // deterministic seeding for powerup generation
            newChunk.spawnPowerups(this, 8, rng);
            newChunk.spawnKey(this, rng, displacement);

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
                    //neighbor.alignBorders(newChunk);
                }
            }


            proximityThreshold += 1;
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

    public void incrementTimer(float delta, float multiplier) {
        timeElapsed += (delta * multiplier);
    }

    // same logic as isCellBlocked, finding the local coordinates and then returning the relevant info, in this case, powerup
    public Powerups getPowerupAt(float worldX, float worldY, int tileSize) {
        int tileX = (int) Math.floor(worldX / tileSize);
        int tileY = (int) Math.floor(worldY / tileSize);

        int chunkTileWidth = mazeWidth - 1;
        int chunkTileHeight = mazeHeight - 1;

        int chunkX = Math.floorDiv(tileX, chunkTileWidth);
        int chunkY = Math.floorDiv(tileY, chunkTileHeight);

        int localX = Math.floorMod(tileX, chunkTileWidth);
        int localY = Math.floorMod(tileY, chunkTileHeight);

        ChunkCoord coord = new ChunkCoord(chunkX, chunkY);
        Chunk chunk = chunks.get(coord);
        if (chunk == null) return null; 

        for (Powerups p : chunk.getPowerups()) {
            if (!p.isActive()) {
                continue; 
            }
            if (p.x == localX && p.y == localY) {
                return p; 
            }
        }

        return null;
    }

    public void doAttack(float y) {
        if (activeAttack == null || activeAttack.isFinished()) {
            activeAttack = new EnemyAttack(y, AssetData.lecturer, AssetData.beamTexture, camera);
        }

        if (activeAttack != null && activeAttack.collidingWithPlayer(player.x, player.y)) {
            timesHit += 1;
            System.out.println("hit");
        }
    }

    // similar logic to generateChunkIfAbsent, only ever ran once
    public void generateFinalChunk(int chunkX, int chunkY) {
        if (finalChunkSpawned) return;

        ChunkCoord coord = new ChunkCoord(chunkX, chunkY);
        if (!chunks.containsKey(coord)) {
            Chunk finalChunk = new Chunk(chunkX, chunkY, globalSeed, mazeWidth, mazeHeight);

            // change flag
            finalChunk.isFinalChunk = true;
            finalChunk.finalFloor = AssetData.finalFloor;

            // removes all interior walls
            for (int x = 0; x < mazeWidth; x++) {
                for (int y = 0; y < mazeHeight; y++) {
                    finalChunk.getGrid()[x][y] = Maze.CellType.PASSAGE;
                }
            }

            chunks.put(coord, finalChunk);

            ChunkCoord[] neighborOffsets = {
                new ChunkCoord(chunkX - 1, chunkY),
                new ChunkCoord(chunkX + 1, chunkY),
                new ChunkCoord(chunkX, chunkY - 1),
                new ChunkCoord(chunkX, chunkY + 1)
            };

            for (ChunkCoord neighborCoord : neighborOffsets) {
                Chunk neighbor = chunks.get(neighborCoord);
                if (neighbor != null) {
                    finalChunk.alignBorders(neighbor);
                }
            }

            finalChunkCoord = coord;
            finalChunkSpawned = true;
        }
    }


    public boolean exitConditionSatisfied(boolean hasKey) {
        if (hasKey) {
            return true;
        }
        return false;
    }

    // uses pythagoras theorem to calculate displacement
    public float calcDisplacement(float x, float y, float startX, float startY) {
        float displacement = (float) Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        return displacement;
    }

    public void drawFinalChunkText(SpriteBatch batch, BitmapFont font, OrthographicCamera camera) {
        Chunk finalChunk = null;
        for (Chunk chunk : chunks.values()) {
            if (chunk.isFinalChunk) {
                finalChunk = chunk;
                break;
            }
        }
        if (finalChunk == null) {
            return;
        } 

        // find centre of the chunk
        float chunkWorldX = finalChunk.getChunkX() * (mazeWidth - 1) * tileSize;
        float chunkWorldY = finalChunk.getChunkY() * (mazeHeight - 1) * tileSize;

        float centerX = chunkWorldX + (mazeWidth * tileSize) / 2f;
        float centerY = chunkWorldY + (mazeHeight * tileSize) / 2f;

        String line1 = "Congratulations, you escaped uni and made it to the real world!";
        String line2 = "Your score: " + finalScore;

        font.getData().setScale(3.5f); 

        GlyphLayout layout1 = new GlyphLayout(font, line1);
        GlyphLayout layout2 = new GlyphLayout(font, line2);

        float textX1 = centerX - layout1.width / 2f;
        float textY1 = centerY + layout1.height / 2f + layout2.height / 2f; 
        float textX2 = centerX - layout2.width / 2f;
        float textY2 = centerY - layout1.height / 2f + layout2.height / 2f; 

        batch.setProjectionMatrix(camera.combined);
        font.draw(batch, layout1, textX1, textY1);
        font.draw(batch, layout2, textX2, textY2);

    }

    public void calcScore() {
        if (!finalScoreCalculated) {
            /*score is calculated by:
            subtracting time from 5 and a half minutes in seconds
            subtracting e^ times hit from that value
             */
            finalScore = (330f - timeElapsed) - (float)Math.exp(timesHit);
            if (finalScore < 0) finalScore = 0f;
            finalScoreCalculated = true;
        }
    }

}
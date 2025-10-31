package group6.maze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import group6.maze.game.AssetData;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Maze maze;
    private Player player;

    private final int mazeWidth = 31;
    private final int mazeHeight = 31;
    private final int tileSize = 64;

    private TextureRegion floor;
    private TextureRegion wall;

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
        maze = new Maze(mazeWidth, mazeHeight);
        maze.generate();

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

        // camera follows player
        camera.position.set(player.x, player.y, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // draws maze tiles
        Maze.CellType[][] grid = maze.getGrid();
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                float sx = x * tileSize;
                float sy = y * tileSize;
                TextureRegion tex = (grid[x][y] == Maze.CellType.PASSAGE) ? floor : wall;
                batch.draw(tex, sx, sy, tileSize, tileSize);
            }
        }

        // draw player on top
        player.draw(batch);
        batch.end();
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
}

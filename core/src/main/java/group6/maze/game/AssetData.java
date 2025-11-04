package group6.maze.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class AssetData {
    public static Texture floorTexture;
    public static Texture wallTexture;
    public static Texture playerTexture;

    public static TextureRegion floor;
    public static TextureRegion wall;
    public static TextureRegion[] playerSprites;

    public static void load() {
        floorTexture = new Texture("assets/greyFloor.png");
        wallTexture = new Texture("assets/greyBrickWall.png");
        playerTexture = new Texture("assets/character.png");

        floor = new TextureRegion(floorTexture);
        wall = new TextureRegion(wallTexture);

        int spriteCount = 8;
        int frameWidth = 16;
        int frameHeight = playerTexture.getHeight();
        playerSprites = new TextureRegion[spriteCount];

        for (int i = 0; i < spriteCount; i++) {
            playerSprites[i] = new TextureRegion(playerTexture, i * frameWidth, 0, frameWidth, frameHeight);
        }
    } 

    public static void dispose() {
        floorTexture.dispose();
        wallTexture.dispose();
    }
}

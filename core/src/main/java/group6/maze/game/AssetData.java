package group6.maze.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class AssetData {
    public static Texture floorTexture;
    public static Texture wallTexture;
    public static Texture playerTexture;
    public static Texture guitarPowerupTexture;
    public static Texture speedBoostPowerupTexture;
    public static Texture attackPowerupTexture;
    public static Texture slowTimePowerupTexture;
    public static Texture speedTimePowerupTexture;
    public static Texture keyPowerupTexture;
    public static Texture lecturerTexture;
    public static Texture beamTexture;

    public static Music soundtrack;
    public static Sprite guitarSprite;

    public static TextureRegion floor;
    public static TextureRegion wall;
    public static TextureRegion guitarPowerup;
    public static TextureRegion speedBoostPowerup;
    public static TextureRegion attackPowerup;
    public static TextureRegion slowTimePowerup;
    public static TextureRegion speedTimePowerup;
    public static TextureRegion keyPowerup;
    public static TextureRegion lecturer;
    public static TextureRegion beam;
    public static TextureRegion[] playerSprites;

    public static void load() {
        floorTexture = new Texture("assets/greyFloor.png");
        wallTexture = new Texture("assets/greyBrickWall.png");
        playerTexture = new Texture("assets/character.png");
        guitarPowerupTexture = new Texture("assets/guitarSG.png");
        speedBoostPowerupTexture = new Texture("assets/speed2.png");
        attackPowerupTexture = new Texture("assets/gpt.png");
        slowTimePowerupTexture = new Texture("assets/slowClock.png");
        speedTimePowerupTexture = new Texture("assets/fastClock.png");
        keyPowerupTexture = new Texture("assets/key.png");
        lecturerTexture = new Texture("assets/lecturer.png");
        beamTexture = new Texture("assets/tellBeam.png");

        soundtrack = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/the-only-thing-they-fear-is-you.mp3"));

        floor = new TextureRegion(floorTexture);
        wall = new TextureRegion(wallTexture);
        guitarPowerup = new TextureRegion(guitarPowerupTexture);
        speedBoostPowerup = new TextureRegion(speedBoostPowerupTexture);
        attackPowerup = new TextureRegion(attackPowerupTexture);
        slowTimePowerup = new TextureRegion(slowTimePowerupTexture);
        speedTimePowerup = new TextureRegion(speedTimePowerupTexture);
        keyPowerup = new TextureRegion(keyPowerupTexture);
        lecturer = new TextureRegion(lecturerTexture);
        beam = new TextureRegion(beamTexture);
        

        guitarSprite = new Sprite(guitarPowerupTexture);

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
        playerTexture.dispose();
        guitarPowerupTexture.dispose();
        speedBoostPowerupTexture.dispose();
        attackPowerupTexture.dispose();
        slowTimePowerupTexture.dispose();
        speedTimePowerupTexture.dispose();
        keyPowerupTexture.dispose();
        soundtrack.dispose();
    }
}

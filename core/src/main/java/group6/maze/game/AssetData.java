package group6.maze.game;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import group6.maze.LocalMap;


@SuppressWarnings("PointlessArithmeticExpression")
public final class AssetData {
    public static Texture charSpriteSheet;
    public static Texture roadTileSheet;
    public static Texture altRoadTileSheet;
    public static Texture buildingTileSheet;
    public static Texture grassyTileSheet;
    public static Texture miscDecorTileSheet;
    public static Texture waterTileSheet;
    public static Texture indoorTileSheet;
    public static TextureRegion[][] frames;

    public static void load() {
        charSpriteSheet = new Texture("Small-8-Direction-Characters_by_AxulArt.png");
        roadTileSheet = new Texture("isometric street tiles 1.png");
        altRoadTileSheet = new Texture("isometric street tiles 2 .png");
        buildingTileSheet = new Texture("isometric four buildings 2.png");
        grassyTileSheet = new Texture("isometric grass 128x64 tiles .png");
        miscDecorTileSheet = new Texture("isometric assets .png");
        waterTileSheet = new Texture("isometric 128x64 water tiles .png");
        indoorTileSheet = new Texture("isometric 64x128 floor and 64x walls tiles .png");
        frames = TextureRegion.split(charSpriteSheet, 36, 32);
    }

    public static void dispose() {
        charSpriteSheet.dispose();
    }

}
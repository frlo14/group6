package group6.maze;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import group6.maze.game.AssetData;

public class Powerups {
    public enum Type {
        GUITAR,
        SPEED_BOOST,
        ATTACK,
        SLOW_TIME,
        SPEED_TIME,
        EXIT_ITEM
    }

    public Type type;
    public int x, y;
    public boolean active;
    public TextureRegion texture;
    public Main game;

    public Powerups(Type type, int x, int y, TextureRegion texture) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.active = true;
    }

    public void draw(SpriteBatch batch, int tileSize) {
        if (active) {
            batch.draw(texture, x * tileSize, y * tileSize);
        }
    }

    // runs given case on pickup, applying effect based on the current enum
    public void pickUp(Player player, Main world) {
        active = false;

        switch (type) {
            case GUITAR -> { 
                AssetData.soundtrack.play();
            }
            case SPEED_BOOST -> {
                player.setSpeed(300f);
            }
            case ATTACK -> {
                world.doAttack((player.y + 0.5f));
            }
            case SLOW_TIME -> {
                world.multiplier = 0.5f;
            }
            case SPEED_TIME -> {
                world.multiplier = 2f;
            }
            case EXIT_ITEM -> {
                // add key spawn, do last when sorting completion
            }
        }
    }

    public static TextureRegion getPowerupTexture(Type type) {
        switch (type) {
            case GUITAR -> { 
                return AssetData.guitarSprite;
            }
            case SPEED_BOOST -> {
                return AssetData.speedBoostPowerup;
            }
            case ATTACK -> {
                return AssetData.attackPowerup;
            }
            case SLOW_TIME -> {
                return AssetData.slowTimePowerup;
            }
            case SPEED_TIME -> {
                return AssetData.speedTimePowerup;
            }
            case EXIT_ITEM -> {
                return AssetData.keyPowerup;
            }
            default -> {
                return AssetData.floor;
            }

        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

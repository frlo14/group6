package group6.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    public float x, y; 
    public float speed = 200f; 
    private float stateTime = 0f; 
    private boolean moving = false;
    private Chunk currentChunk;

    // directional animations
    private Animation<TextureRegion> animUp, animDown, animLeft, animRight;
    private Animation<TextureRegion> currentAnim;

    private float scale = 2.5f; 

    public Player(TextureRegion[] upFrames, TextureRegion[] downFrames, TextureRegion[] leftFrames, TextureRegion[] rightFrames, float startX, float startY) {
        this.x = startX;
        this.y = startY;

        // creates the walking animation by alternating between 2 sprites
        animUp = new Animation<>(0.1f, upFrames);
        animUp.setPlayMode(Animation.PlayMode.LOOP);

        animDown = new Animation<>(0.1f, downFrames);
        animDown.setPlayMode(Animation.PlayMode.LOOP);

        animLeft = new Animation<>(0.1f, leftFrames);
        animLeft.setPlayMode(Animation.PlayMode.LOOP);

        animRight = new Animation<>(0.1f, rightFrames);
        animRight.setPlayMode(Animation.PlayMode.LOOP);

        currentAnim = animDown; // default facing down
    }

    // ran once per frame
    public void update(float delta, Main world) {
        float moveX = 0, moveY = 0;

        // handles movement by checking keypress 
        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))) moveY += 1;
        if ((Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))) moveY -= 1;
        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))) moveX -= 1;
        if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))) moveX += 1;

        moving = (moveX != 0 || moveY != 0);

        // selects animation based on movement direction
        if (moveY > 0) currentAnim = animUp;
        else if (moveY < 0) currentAnim = animDown;
        else if (moveX < 0) currentAnim = animLeft;
        else if (moveX > 0) currentAnim = animRight;

        if (!moving) {
            stateTime = 0;
            return;
        }

        // normalize diagonal movement
        float len = (float)Math.sqrt(moveX * moveX + moveY * moveY);
        float dirX = moveX / len;
        float dirY = moveY / len;

        // computes the delta time for the movement 
        float deltaX = dirX * speed * delta;
        float deltaY = dirY * speed * delta;

        // works out target position
        float targetX = x + deltaX;
        float targetY = y + deltaY;

        // checks if tile is blocked, then defers it to single axis movement to be sure
        boolean blockedFull = world.isCellBlocked(targetX, targetY, 64);
        boolean moved = false;

        if (!blockedFull) {
            x = targetX;
            y = targetY;
            moved = true;
        } else {
            boolean blockedX = world.isCellBlocked(x + deltaX, y, 64);
            if (!blockedX) {
                x += deltaX;
                moved = true;
            } else {
                boolean blockedY = world.isCellBlocked(x, y + deltaY, 64);
                if (!blockedY) {
                    y += deltaY;
                    moved = true;
                }
            }
        }

        if (moved) {
            stateTime += delta;
        } else {
            stateTime = 0;
        }

        // once you walk over powerup, it is removed from the screen and then the effect is applied
        Powerups powerup = world.getPowerupAt(x, y, world.tileSize);
        if (powerup != null) {
            powerup.setActive(false);
            powerup.pickUp(this, world);
        }
    }


    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = currentAnim.getKeyFrame(stateTime, true);

        float spriteW = currentFrame.getRegionWidth() * scale;
        float spriteH = currentFrame.getRegionHeight() * scale;

        // draws sprite centered on position
        batch.draw(currentFrame, x - spriteW / 2f, y - spriteH / 2f, spriteW, spriteH);
    }


    public void setCurrentChunk(Chunk chunk) {
        this.currentChunk = chunk;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
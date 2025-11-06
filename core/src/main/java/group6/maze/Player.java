package group6.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;

public class Player {
    public float x, y;
    private float speed = 200f;
    private float stateTime = 0f;
    private boolean moving = false;
    public Rectangle bounds;

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

        //set Rectangle
        bounds = new Rectangle(0, 0,32 , 42);
        updateBoundsPosition();
    }

    // ran once per frame
    public void update(float delta) {
        float moveX = 0, moveY = 0;

        // handles movement by checking keypress
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1;

        moving = (moveX != 0 || moveY != 0);

        // selects animation based on movement direction
        if (moveY > 0) currentAnim = animUp;
        else if (moveY < 0) currentAnim = animDown;
        else if (moveX < 0) currentAnim = animLeft;
        else if (moveX > 0) currentAnim = animRight;

        if (moving) {
            // normalize diagonal movement
            float len = (float)Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= len;
            moveY /= len;

            // updates position
            x += moveX * speed * delta;
            y += moveY * speed * delta;

            stateTime += delta; // advances animation
        } else {
            stateTime = 0; // makes sure idle at first frame
        }
        //update player's rectangle to position
        updateBoundsPosition();
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = currentAnim.getKeyFrame(stateTime, true);

        float spriteW = currentFrame.getRegionWidth() * scale;
        float spriteH = currentFrame.getRegionHeight() * scale;

        // draws sprite centered on position
        batch.draw(currentFrame, x - spriteW / 2f, y - spriteH / 2f, spriteW, spriteH);
    }

    public void resolveCollision(Rectangle wall) {

        // Calculate how much we're overlapping on each side
        float overlapLeft = (bounds.x + bounds.width) - wall.x;
        float overlapRight = (wall.x + wall.width) - bounds.x;
        float overlapBottom = (bounds.y + bounds.height) - wall.y;
        float overlapTop = (wall.y + wall.height) - bounds.y;

        // Find the smallest overlap
        float minOverlapX = Math.min(overlapLeft, overlapRight);
        float minOverlapY = Math.min(overlapBottom, overlapTop);

        // Push the player out by the smallest amount
        if (minOverlapX < minOverlapY) {
            // We are colliding more horizontally, so push horizontally
            if (overlapLeft < overlapRight) {
                // Player is overlapping from the right, push left
                x -= overlapLeft;
            } else {
                // Player is overlapping from the left, push right
                x += overlapRight;
            }
        } else {
            // We are colliding more vertically, so push vertically
            if (overlapBottom < overlapTop) {
                // Player is overlapping from the top, push down
                y -= overlapBottom;
            } else {
                // Player is overlapping from the bottom, push up
                y += overlapTop;
            }
        }

        // After pushing, we must update the bounding box's position
        // so the *next* collision check in the same frame is accurate
        updateBoundsPosition();
    }

    private void updateBoundsPosition() {
        //center the rectangle on the player
        float rX = this.x - (bounds.width / 2f);
        float rY = this.y - (bounds.height / 2f);
        bounds.setPosition(rX, rY);
    }

    public float getx() {return x;}

    public float gety() {return y;}
}

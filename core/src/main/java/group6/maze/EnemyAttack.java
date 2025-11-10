package group6.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class EnemyAttack {
    private enum State { TELL, DASH, DONE }

    private State state = State.TELL;
    private float elapsed = 0f;

    private final float TELLTime = 0.15f;   // beam reveal duration
    private final float dashDuration = 1.2f;     // enemy dash duration
    private final float enemyScaleTiles = 1.2f;
    private final float dashScale = 1.4f;
    private final float bobAmplitude = 8f;
    private final float tiltDegrees = 30f;
    private final float tileSize = 64f;

    private final OrthographicCamera camera;
    private final TextureRegion enemyRegion;
    private final Texture beamTexture;

    private float beamWorldY;
    private float enemyX;
    private float enemyY;
    private float dashStartX;
    private float dashEndX;
    private final float direction = 1f; // left to right
    protected boolean isHit = false;

    private float beamHeight;
    private final float visibleFraction = 0.85f; // 85% of the beam is visible

    public EnemyAttack(float worldY, TextureRegion enemyRegion, Texture beamTexture, OrthographicCamera camera) {
        this.beamWorldY = worldY;
        this.enemyRegion = enemyRegion;
        this.beamTexture = beamTexture;
        this.camera = camera;

        float enemySize = tileSize * enemyScaleTiles;
        this.enemyX = -enemySize;
        this.enemyY = beamWorldY;

        this.beamHeight = tileSize * 1.5f; 
    }

    public void update(float delta) {
        elapsed += delta;

        switch (state) {
            case TELL:
                // reveals beam then transitions to dash
                if (elapsed >= TELLTime) {
                    state = State.DASH;
                    elapsed = 0f;

                    // calculats dash start and end positions
                    float halfW = camera.viewportWidth * 0.5f * camera.zoom;
                    dashStartX = camera.position.x - halfW - tileSize * dashScale;
                    dashEndX = camera.position.x + halfW + tileSize * dashScale;

                    enemyX = dashStartX;
                    enemyY = beamWorldY;
                }
                break;

            case DASH:
                // lines up enemy and adds some vertical bobbing to the dash
                float t = MathUtils.clamp(elapsed / dashDuration, 0f, 1f);
                enemyX = MathUtils.lerp(dashStartX, dashEndX, t);
                enemyY = beamWorldY + bobAmplitude * MathUtils.sin(t * MathUtils.PI2);
                if (t >= 1f) state = State.DONE;
                break;

            case DONE:
                break;
        }
    }

    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);

        if (state == State.TELL) {
            // works out bounds of screen
            float halfW = camera.viewportWidth * 0.5f * camera.zoom;
            float left = camera.position.x - halfW;
            float beamWidth = camera.viewportWidth * camera.zoom;

            // beam 'progress'
            float progress = MathUtils.clamp(elapsed / TELLTime, 0f, 1f);
            float visibleLength = beamWidth * visibleFraction;
            float beamHead = progress * beamWidth;
            float beamTail = beamHead - visibleLength;

            if (beamTail < 0f) beamTail = 0f;
            if (beamHead > beamWidth) beamHead = beamWidth;
            float u1 = beamTail / beamWidth;
            float u2 = beamHead / beamWidth;

            // computes which part of the beam asset should be visible
            TextureRegion visible = new TextureRegion(beamTexture);
            visible.setU(u1);
            visible.setU2(u2);
            visible.flip(true, false); 

            float drawX = left + beamTail;
            float drawWidth = beamHead - beamTail;

            batch.draw(visible, drawX, beamWorldY - beamHeight / 2f, drawWidth, beamHeight);
        }

        if (state == State.DASH) {
            // computes enemy sprite bounds
            float size = tileSize * dashScale;
            float drawX = enemyX - size / 2f;
            float drawY = enemyY - size / 2f;

            // misc orientation changes for seemlessness, enemy tilt, beam orientation etc
            boolean flipX = true; 
            if (enemyRegion.isFlipX() != flipX) enemyRegion.flip(true, false);

            float tilt = -tiltDegrees; 

            batch.draw(enemyRegion, drawX, drawY, size / 2f, size / 2f, size, size, 1f, 1f, tilt);
        }
    }

    public boolean isFinished() {
        return state == State.DONE;
    }
    
    public boolean collidingWithPlayer(float playerX, float playerY) {
        if (state != State.DASH || isHit) {
            return false;
        }

        float enemySize = tileSize * dashScale;

        // bounds of the enemy's full sprite
        float enemyLeft   = enemyX - enemySize / 2f;
        float enemyRight  = enemyX + enemySize / 2f;
        float enemyBottom = enemyY - enemySize / 2f;
        float enemyTop    = enemyY + enemySize / 2f;

        // bounds of the player's full sprite
        float playerLeft   = playerX;
        float playerRight  = playerX + 21f;
        float playerBottom = playerY;
        float playerTop    = playerY + 128f;

        // compares enemy and player positions
        boolean overlapX = enemyRight > playerLeft && enemyLeft < playerRight;
        boolean overlapY = enemyTop > playerBottom && enemyBottom < playerTop;

        if (overlapX && overlapY) {
            isHit = true; // marked as hit
            return true;
        }

        return false;
    }

}

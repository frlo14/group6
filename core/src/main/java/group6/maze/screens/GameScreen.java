package group6.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import group6.maze.Main;

public class GameScreen implements Screen {
    private final MenuWrapper gameWrapper; 
    private final Main main;
    private boolean isPaused;

    public GameScreen(MenuWrapper gameWrapper) {
        this.gameWrapper = gameWrapper;
        this.main = gameWrapper.getMainGame(); 
        this.isPaused = false;
    }

    public void pauseGame() { 
        isPaused = true; 
    }

    public void resumeGame() { 
        isPaused = false; 
    }

    // extends render logic from menu 
    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseGame();
            gameWrapper.setScreen(new MainMenu(gameWrapper, this)); 
            return;
        }

        if (!isPaused) {
            main.render(); // runs the gameplay loop from main
        }
    }

    @Override
    public void resize(int width, int height) { 
        main.resize(width, height); 
    }

    @Override public void pause() { 
        pauseGame(); 
    }
    
    @Override public void resume() { 
        resumeGame(); 
    }

    @Override public void dispose() {}
    @Override public void show() {}
    @Override public void hide() {}
}

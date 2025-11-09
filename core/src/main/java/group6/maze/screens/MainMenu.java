package group6.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MainMenu implements Screen {
    private final MenuWrapper gameWrapper;
    private final Stage stage;
    private final Skin skin;
    private final Screen previousScreen; 

    // default cconstructor, before the game has been loaded
    public MainMenu(MenuWrapper gameWrapper) { 
        this(gameWrapper, null); 
    }

    // overriden constructor to allow the player to go back to the same game state after accessing the pause menu
    public MainMenu(MenuWrapper gameWrapper, Screen previousScreen) {
        this.gameWrapper = gameWrapper;
        this.previousScreen = previousScreen;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // UI formatting
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        Table table = new Table(); // table acts as container for buttons
        table.setFillParent(true);
        stage.addActor(table);


        TextButton playButton = new TextButton("Play", skin);
        TextButton settingsButton = new TextButton("Settings", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // first adds play button 
        table.add(playButton).pad(10).row();
        
        // checks if in game, if so replaces play with resume
        if (previousScreen instanceof GameScreen) {
            table.removeActor(playButton);
            TextButton resumeButton = new TextButton("Resume", skin);
            table.add(resumeButton).pad(10).row();
            resumeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    ((GameScreen) previousScreen).resumeGame();
                    gameWrapper.setScreen(previousScreen);
                }
            });
        }

        // adds other buttons afterwards to preserve order
        table.add(settingsButton).pad(10).row();
        table.add(exitButton).pad(10).row();

        // listens for click on play button and switches screen
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameWrapper.setScreen(new GameScreen(gameWrapper));
            }
        });

        // listens for click on settings button and switches screen
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameWrapper.setScreen(new SettingsScreen(gameWrapper));
            }
        });

        // listens for click on exit button and closes game
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {}

    // ran once per frame
    @Override
    public void render(float delta) {
        if (previousScreen instanceof GameScreen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            ((GameScreen) previousScreen).resumeGame(); // pauses game on esc
            gameWrapper.setScreen(previousScreen);
            return;
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) { 
        stage.getViewport().update(width, height, true); 
    }
    
    @Override public void dispose() { 
        stage.dispose(); skin.dispose(); 
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    
}


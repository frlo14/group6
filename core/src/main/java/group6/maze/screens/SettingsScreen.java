package group6.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import group6.maze.game.AssetData;

public class SettingsScreen implements Screen {
    private final MenuWrapper game;
    private final Stage stage;
    private final Skin skin;
    private final Slider volumeSlider;
    private final Music music;

    public SettingsScreen(MenuWrapper game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        music = AssetData.soundtrack; 

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // constructs slider with 100 increments of volume
        Label volumeLabel = new Label("Volume", skin);
        volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(music.getVolume()); 

        // sets volume based on slider location
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float volume = volumeSlider.getValue();
                music.setVolume(volume); 
            }
        });

        // back button to return to main menu
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new MainMenu(game));
            }
        });

        table.add(volumeLabel).pad(10);
        table.add(volumeSlider).width(200).pad(10);
        table.row();
        table.add(backButton).colspan(2).padTop(20);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void dispose() { 
        stage.dispose(); skin.dispose(); 
    }

    @Override public void resize(int width, int height) { 
        stage.getViewport().update(width, height, true); 
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void show() {}
}

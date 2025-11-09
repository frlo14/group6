package group6.maze.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import group6.maze.Main;

public class MenuWrapper extends ApplicationAdapter {
    private Screen currentScreen;
    private Main mainGame; // shared game logic so pause doesnt reset the game

    // wrapper class as game must boot from an applicationAdapter class, but the menu screens all extend screen
    @Override
    public void create() {
        mainGame = new Main();
        mainGame.create();
        setScreen(new MainMenu(this)); // boots on main menu
    }

    @Override
    public void render() {
        if (currentScreen != null) {
            currentScreen.render(Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void resize(int width, int height) {
        if (currentScreen != null) currentScreen.resize(width, height);
    }

    @Override
    public void dispose() {
        if (currentScreen != null) currentScreen.dispose();
        if (mainGame != null) mainGame.dispose();
    }

    // changes which screen is displayed
    public void setScreen(Screen newScreen) {
        if (currentScreen != null) currentScreen.hide();
        currentScreen = newScreen;
        if (currentScreen != null) {
            currentScreen.show();
            currentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    public Main getMainGame() {
        return mainGame;
    }
}

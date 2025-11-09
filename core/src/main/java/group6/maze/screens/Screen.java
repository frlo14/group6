package group6.maze.screens;

public interface Screen {
    void update(float delta);
    void render(float delta);
    void resize(int width, int height);
    void dispose();
}

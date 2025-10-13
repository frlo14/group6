package group6.maze.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Array;
import group6.maze.AnimatedIsoSprite;
import group6.maze.Main;
import group6.maze.LocalMap;
import group6.maze.util.HasPosition3D;
import group6.maze.util.MiniNoise;

/**
 * A creature, hero, or hazard that can move around of its own accord.
 * A Mover can be an {@link #npc} or not; NPCs move on their own in somewhat-random paths, while the player character
 * should be the only Mover with {@code npc = false}, and should be moved by the player's input.
 */
public class Mover {

}
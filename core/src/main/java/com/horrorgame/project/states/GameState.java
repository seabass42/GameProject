package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.sprites.Player;

public class GameState extends State{
    private Player player;
    private SpriteBatch batch;
    private OrthographicCamera camera;


    public GameState(GameStateManager gsm){
        super(gsm);

        player = new Player(0,0);
        Gdx.input.setInputProcessor(player);
        camera = new OrthographicCamera();
        camera.viewportWidth = Gdx.graphics.getWidth()/2;
        camera.viewportHeight = Gdx.graphics.getHeight()/2;

    }
    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {
        player.update(dt);
        camera.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera to follow player (optional)
        //camera.position.set(player.getPositionX(), player.getPositionY(), 0);
        camera.update();

        // --- Draw the tiled map ---

        // --- Draw player and other sprites ---
        sb.setProjectionMatrix(camera.combined);
        sb.begin();
        player.render(sb);
        sb.end();
    }


    @Override
    public void dispose() {

    }
}

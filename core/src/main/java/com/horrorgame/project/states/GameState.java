package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

        camera.update();

        sb.setProjectionMatrix(camera.combined);
        sb.begin();
        player.render(sb);
        sb.end();
    }


    @Override
    public void dispose() {

    }
}

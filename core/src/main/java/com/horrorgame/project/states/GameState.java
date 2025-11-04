package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.horrorgame.project.HorrorMain;

public class GameState extends State{
    private Texture background;

    public GameState(GameStateManager gsm){
        super(gsm);
        background = new Texture("testBackground.png");
    }
    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        //sb.draw(background,0,0, HorrorMain.WIDTH,HorrorMain.HEIGHT/2);
        sb.end();
    }

    @Override
    public void dispose() {

    }
}

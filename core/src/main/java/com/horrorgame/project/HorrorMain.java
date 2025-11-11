package com.horrorgame.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.horrorgame.project.states.GameStateManager;
import com.horrorgame.project.states.MenuState;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class HorrorMain extends ApplicationAdapter {
    private SpriteBatch batch;
    private GameStateManager gsm;

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final String TITLE = "";

    @Override
    public void create() {
        batch = new SpriteBatch();
        gsm = new GameStateManager();
        //ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClearColor(1, 0, 0, 1);
        gsm.push(new MenuState(gsm));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        gsm.update(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gsm.render(batch);
    }

    @Override
    public void dispose() {
        batch.dispose();

    }
}

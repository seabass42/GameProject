package com.horrorgame.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.horrorgame.project.states.GameStateManager;
import com.horrorgame.project.states.MenuState;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class HorrorMain extends ApplicationAdapter {
    private SpriteBatch batch;
    private GameStateManager gsm;
    private AssetManager manager;

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final String TITLE = "";

    @Override
    public void create() {
        batch = new SpriteBatch();
        gsm = new GameStateManager();
        manager = new AssetManager();

        // Preload Menu assets only
        manager.load("onlytheocean-silent-hill-sm.jpeg", Texture.class);
        manager.load("vhsui/vhs-ui.json", Skin.class);
        manager.load("sounds/gui/vhsPlay.ogg", Sound.class);
        manager.load("sounds/gui/hoverSelect.wav", Sound.class);
        manager.finishLoading(); // Menu assets load instantly

        gsm.push(new MenuState(gsm, manager));
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

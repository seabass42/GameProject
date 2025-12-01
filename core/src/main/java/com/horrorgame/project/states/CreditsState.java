package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;
import jdk.javadoc.internal.doclets.formats.html.Table;

import java.awt.*;

public class CreditsState extends State {
    private Stage stage;
    private Label devSebastian, devRichard;
    private Table credits;
    private OrthographicCamera camera;


    public CreditsState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH;
        camera.viewportHeight = HorrorMain.HEIGHT;
        stage = new Stage(new ScreenViewport());
    }
    @Override
    protected void setDebugMode() {

    }

    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render(SpriteBatch sb) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void resize(int width, int height) {

    }
}

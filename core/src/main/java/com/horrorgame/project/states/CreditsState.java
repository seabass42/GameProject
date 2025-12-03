package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;

import java.awt.*;

public class CreditsState extends State {
    private Stage stage;
    private Label devSebastian, devRichard, developers;
    private Table credits;
    private OrthographicCamera camera;
    private Skin skin;
    private AssetManager manager;

    public CreditsState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;
        manager.load("vhsui/vhs-ui.json/", Skin.class);
        manager.finishLoading();
        skin = manager.get("vhsui/vhs-ui.json/", Skin.class);
        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH;
        camera.viewportHeight = HorrorMain.HEIGHT;
        stage = new Stage(new ScreenViewport());
        credits = new Table();
        credits.setPosition(HorrorMain.WIDTH /4f-140 ,HorrorMain.HEIGHT - 100);

        stage.addActor(credits);
       // stage.setDebugAll(true);

        developers = new Label("Developers", skin);
        devSebastian = new Label("Sebastian E Stewart", skin);
        devRichard = new Label("Richard Smith", skin);
        credits.add(developers).space(60);
        credits.row();
        credits.add(devRichard).space(30);
        credits.row();
        credits.add(devSebastian).space(30).padLeft(110);
        Gdx.input.setInputProcessor(Gdx.input.getInputProcessor());
    }
    @Override
    protected void setDebugMode() {

    }

    @Override
    protected void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            gsm.pop();
        }
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render(SpriteBatch sb) {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void resize(int width, int height) {

    }
}

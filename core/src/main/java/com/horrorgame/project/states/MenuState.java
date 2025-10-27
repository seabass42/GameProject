package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;

public class MenuState extends State {
    private Texture background;
    private Stage stage;
    private Skin skin;
    private Table table;
    TextButton playButton;

    public MenuState(GameStateManager gsm){
        super(gsm);
        background = new Texture("onlytheocean-silent-hill-sm.jpeg");
        skin = new Skin(Gdx.files.internal("vhsui/vhs-ui.json"));

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        playButton = new TextButton("Play",skin);
        stage.addActor(playButton);

    }

    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {

        stage.getViewport().update(HorrorMain.WIDTH, HorrorMain.HEIGHT, true);
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(background,0,0, HorrorMain.WIDTH / 1.2f, HorrorMain.HEIGHT/ 1.2f);
        sb.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void dispose() {

    }
}

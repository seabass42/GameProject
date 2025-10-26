package com.horrorgame.project.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;

public class MenuState extends State {
    private Texture background;
    private Texture playBtn, exitBtn;
    private Stage stage;
    private Skin skin;

    public MenuState(GameStateManager gsm){
        super(gsm);
        background = new Texture("menu.jpeg");
        //playBtn = new Texture("enter image here");
        //exitBtn = new Texture("enter image here");
        stage = new Stage(new ScreenViewport());
    }

    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        sb.draw(background,0,0, HorrorMain.WIDTH / 1.2f, HorrorMain.HEIGHT/ 1.2f);
        sb.end();
    }

    @Override
    public void dispose() {

    }
}

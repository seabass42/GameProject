package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HouseState extends State{

    private AssetManager manager;
    private Texture room1, room2;
    private Stage stage;
    private Actor continueA;
    public HouseState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;
        room1 = manager.get("House/amnesia_room1.jpeg", Texture.class);
        room2 = manager.get("House/amnesia_room2.jpeg");
        stage = new Stage(new ScreenViewport());
        continueA = new Actor();

        Gdx.input.setInputProcessor(stage);

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
    public void resize(int width, int height){

    }

    @Override
    public void dispose() {

    }
}

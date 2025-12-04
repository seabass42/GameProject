package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LoadingState extends State {

    private AssetManager manager;
    private Stage stage;
    private Skin skin;
    private Sound vhsPlay;
    private Label label;
    private boolean vhsPlayed = false;
    private float timer = 0f;

    public LoadingState(GameStateManager gsm, AssetManager manager) {
        super(gsm);
        this.manager = manager;

        stage = new Stage(new ScreenViewport());
        skin = manager.get("vhsui/vhs-ui.json", Skin.class);
        vhsPlay = manager.get("sounds/gui/vhsPlay.ogg", Sound.class);

        label = new Label("Loading 0%", skin);
        label.setPosition(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, Align.center);
        stage.addActor(label);

        Gdx.input.setInputProcessor(null);
    }

    // Queue assets only once
    private void queueAssets() {
            // GameState assets
            if(!manager.isLoaded("onlytheocean-silent-hill-sm.jpeg"))
                manager.load("onlytheocean-silent-hill-sm.jpeg", Texture.class);
            // Add other assets (sounds, maps, etc.) here
        manager.load("sounds/objectInteractions/flashlight_click.wav", Sound.class);
        manager.load("House/House.png", Texture.class);
        manager.load("sounds/objectInteractions/light-hum.mp3", Sound.class);
        manager.load("House/House.png", Texture.class);
        manager.load("House/amnesia_room1.jpeg", Texture.class);
        manager.load("House/amnesia_room2.png", Texture.class);
        manager.load("sounds/gui/text_sound.mp3", Sound.class);
        manager.load("sounds/objectInteractions/blip3.wav", Sound.class);
        manager.load("sounds/gnid.ogg", Sound.class);
        manager.load("sounds/eyeScare.wav", Sound.class);
        manager.load("sounds/door_open.mp3", Sound.class);
        manager.load("Bunker/Bunker.png", Texture.class);
        manager.load("Bunker/BunkerMainArea.jpeg", Texture.class);
        manager.load("Bunker/BunkerRoom2.jpeg", Texture.class);
    }

    @Override
    protected void setDebugMode() {

    }

    @Override
    protected void handleInput() { }

    @Override
    public void update(float dt) {
        queueAssets();

        // Play sound once
        if (!vhsPlayed) {
            vhsPlay.play();
            vhsPlayed = true;
        }
        timer += dt;

        if(manager.update() && timer >= 1f) {
            manager.finishLoading();
           // gsm.set(new HouseState(gsm, manager, GameState.player)); // Straight to house for testing
            gsm.set(new BunkerState(gsm, manager, GameState.player));
           // gsm.set(new GameState(gsm,manager));
        }

        int percent = (int)(manager.getProgress() * 100);
        label.setText("Loading... " + percent + "%");
        stage.act(dt);
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }
    @Override
    public void resize(int width, int height){
        stage.getViewport().update(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}

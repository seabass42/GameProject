package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Text.RPGText;
import com.horrorgame.project.sprites.Player;

public class BunkerState extends State {
    private Stage stage;
    private FitViewport fitViewport;
    private RPGText bunkerIntroText, lockedText, keyFoundText;
    private Skin skin;
    private RoomState currentRoom;
    private Texture room1, room2;
    private OrthographicCamera camera;
    private Button.ButtonStyle invis;
    private Button room2Entrance, collectKey, lockedDoor1, lockedDoor2;
    private Music bunkerAmbience, waterDrip;
    private Sound keyCollected, doorOpenSound;
    private Player player;

    public BunkerState(GameStateManager gsm, AssetManager manager, Player player){
        super(gsm);
        this.player = player;
        fitViewport = new FitViewport(HorrorMain.WIDTH, HorrorMain.HEIGHT);
        stage = new Stage(fitViewport);
        skin = manager.get("vhsui/vhs-ui.json", Skin.class);
        Gdx.input.setInputProcessor(stage);

        bunkerIntroText = new RPGText("I should be able to find what I need here.", skin);
        lockedText = new RPGText("Locked.", skin);
        keyFoundText = new RPGText("Seems to be for that house.", skin);

        room1 = manager.get("Bunker/BunkerMainArea.jpeg", Texture.class);
        room2 = manager.get("Bunker/BunkerRoom2.jpeg", Texture.class);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, fitViewport.getScreenWidth(), fitViewport.getScreenHeight());

        invis = new Button.ButtonStyle();
        invis.up = null;
        invis.down = null;
        invis.over = null;

        room2Entrance = new Button(invis);
        room2Entrance.setBounds(850, 255, 40, 310);
        room2Entrance.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                doorOpenSound.play();
                currentRoom = RoomState.ROOM2;
            }
        });

        collectKey = new Button(invis);
        collectKey.setBounds(400, 400, 16,16);
        collectKey.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                keyCollected.play();
                player.addToInventory(3);
                stage.addActor(keyFoundText);
            }
        });
        collectKey.setVisible(false);
        stage.addActor(room2Entrance);
        stage.addActor(collectKey);

        stage.setDebugAll(true);

        bunkerAmbience = Gdx.audio.newMusic(Gdx.files.internal("Bunker/BunkerAmbience.mp3"));
        bunkerAmbience.setVolume(0.2f);
        bunkerAmbience.setLooping(true);
        waterDrip = Gdx.audio.newMusic(Gdx.files.internal("Bunker/WaterDrip.mp3"));
        waterDrip.setVolume(0.2f);
        waterDrip.setLooping(true);
        keyCollected = manager.get("Bunker/KeyCollected.mp3", Sound.class);
        doorOpenSound = manager.get("Bunker/BunkerDoor(sh2).mp3", Sound.class);

        currentRoom = RoomState.ROOM1;
    }
    private enum RoomState {
        ROOM1, ROOM2
    }
    @Override
    protected void setDebugMode() {

    }

    @Override
    protected void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            bunkerAmbience.stop();
            waterDrip.stop();
            gsm.pop();
        }
    }

    @Override
    public void update(float dt) {
        fitViewport.apply();
        camera.update();
        handleInput();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        sb.setProjectionMatrix(camera.combined);

        switch (currentRoom){
            case ROOM1:
                sb.draw(room1, 0, 0, fitViewport.getScreenWidth(), fitViewport.getScreenHeight());
                bunkerAmbience.play();
                waterDrip.play();

                break;
            case ROOM2:
                sb.draw(room2, 0, 0, fitViewport.getScreenWidth(), fitViewport.getScreenHeight());
                room2Entrance.setVisible(false);
                collectKey.setVisible(true);
                bunkerAmbience.setVolume(0.07f);
                waterDrip.setVolume(0.008f);
                break;
        }
        sb.end();
        stage.draw();
        stage.act(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        bunkerAmbience.dispose();
        waterDrip.dispose();
    }

    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height);
        camera.update();
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    }
}

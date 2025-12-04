package com.horrorgame.project.states;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Text.RPGText;
import com.horrorgame.project.sprites.Player;

public class BunkerState extends State {
    private Stage stage;
    private FitViewport fitViewport;
    private RPGText bunkerIntroText;
    private Skin skin;
    private RoomState currentRoom = RoomState.ROOM1;
    private Texture room1, room2;
    private OrthographicCamera camera;

    public BunkerState(GameStateManager gsm, AssetManager manager, Player player){
        super(gsm);
        fitViewport = new FitViewport(HorrorMain.WIDTH, HorrorMain.HEIGHT);
        stage = new Stage(fitViewport);
        skin = manager.get("vhsui/vhs-ui.json", Skin.class);
        bunkerIntroText = new RPGText("I should be able to find what I need here.", skin);
        room1 = manager.get("Bunker/BunkerMainArea.jpeg", Texture.class);
        room2 = manager.get("Bunker/BunkerRoom2.jpeg", Texture.class);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, fitViewport.getScreenWidth(), fitViewport.getScreenHeight());

    }
    private enum RoomState {
        ROOM1, ROOM2
    }
    @Override
    protected void setDebugMode() {

    }

    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {
        fitViewport.apply();
        camera.update();

    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        sb.setProjectionMatrix(camera.combined);
        if (currentRoom == RoomState.ROOM1) {
            sb.draw(room1, 0, 0, fitViewport.getScreenWidth(), fitViewport.getScreenHeight());
        }
        sb.end();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height);
        camera.update();
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    }
}

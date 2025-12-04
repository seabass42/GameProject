package com.horrorgame.project.states;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Text.RPGText;
import com.horrorgame.project.sprites.Player;

public class HouseState extends State{

    private AssetManager manager;
    private Texture room1, room2;
    private Stage stage;
    private OrthographicCamera camera;
    private Music room1Music, room2Music;

    private Button proceedToRoom2, collectItem;
    private Label doorHoverPrompt;

    private RoomState state;
    private Skin skin;

    private World world = new World(new Vector2(0,0), false);
    private RayHandler rayHandler = new RayHandler(world);
    private PointLight cursorView;
    private Vector3 mouse;
    private Player player;
    private boolean doorPromptHover = false;

    RPGText warningText, itemCollectedText, itemHint;

    public HouseState(GameStateManager gsm, AssetManager manager, Player player){
        super(gsm);

        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH;
        camera.viewportHeight = HorrorMain.HEIGHT;
        stage = new Stage(new ScreenViewport());
        skin = manager.get("vhsui/vhs-ui.json", Skin.class);
        camera.setToOrtho(false);
        this.player = player;

        Gdx.input.setInputProcessor(stage);
        this.manager = manager;
        room1 = manager.get("House/amnesia_room1.jpeg", Texture.class);
        room2 = manager.get("House/amnesia_room2.png", Texture.class);
        room1Music = Gdx.audio.newMusic(Gdx.files.internal("House/House1(Grand Hall).mp3"));
        room1Music.setVolume(0.5f);
        room2Music = Gdx.audio.newMusic(Gdx.files.internal("House/Room2(Basement Storage).mp3"));

        Button.ButtonStyle invis = new Button.ButtonStyle();
        invis.up = null;
        invis.down = null;
        invis.over = null;
        proceedToRoom2 = new Button(invis);
        proceedToRoom2.setBounds(520 + HorrorMain.WIDTH / 4f, 320, 75, 280);
        proceedToRoom2.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                state = RoomState.ROOM2;
            }
        });
        collectItem = new Button(invis);
        collectItem.setBounds(500, 300, 40, 250);
        collectItem.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                stage.addActor(itemCollectedText);
                itemHint.removeEarly();
                player.addToInventory(1);

            }
        });


        //stage.setDebugAll(true);
        stage.addActor(proceedToRoom2);
        stage.addActor(collectItem);
        doorHoverPrompt = new Label("Enter?", skin);
        collectItem.setVisible(false);


        state = RoomState.ROOM1;

        rayHandler.setCombinedMatrix(camera.combined);
        rayHandler.useDiffuseLight(true);
        rayHandler.setAmbientLight(new Color(0.8f,0.8f,0.8f,1f));
        rayHandler.setAmbientLight(0.2f);

        cursorView = new PointLight(rayHandler, 600, Color.GRAY, 500, Gdx.input.getX(), Gdx.input.getY());

        warningText = new RPGText("Something is wrong here. I mustn't stray", skin);
        itemCollectedText = new RPGText("Wire cutters found.", skin);
        itemHint = new RPGText("There seems to be an item behind this side of the painting.", skin);

    }

    @Override
    protected void setDebugMode() {

    }
    private enum RoomState {
        ROOM1, ROOM2
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            room1Music.stop();
            gsm.pop();
        }
        if (collectItem.isOver()){
            stage.addActor(itemHint);
        }
        doorHoverPrompt.setPosition(mouse.x, mouse.y);
        if (proceedToRoom2.isOver()){
            stage.addActor(doorHoverPrompt);
        } else {
            doorHoverPrompt.remove();
        }
    }

    @Override
    public void update(float dt) {
        camera.update();
        mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        cursorView.setPosition(mouse.x, mouse.y);
        room1Music.play();
        room1Music.setLooping(true);
        stage.addActor(warningText);
        handleInput();
    }

    @Override
    public void render(SpriteBatch sb) {
        // Clear for lighting
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        sb.setProjectionMatrix(camera.combined);
        sb.setShader(null);
        sb.begin();
        switch (state) {
            case ROOM1:
                sb.draw(room1, HorrorMain.WIDTH / 12f, 0);
                room2Music.pause();

                break;
            case ROOM2:
                sb.draw(room2, 0, 0, HorrorMain.WIDTH, HorrorMain.HEIGHT);
                room1Music.pause();
                room2Music.play();
                collectItem.setVisible(true);
                proceedToRoom2.setVisible(false);
                warningText.removeEarly();
                GameState.houseLocked = true;
                break;
        }

        sb.end();

        rayHandler.setCombinedMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        rayHandler.updateAndRender();

        stage.draw();
        stage.act();
        world.step(1/60f, 6, 2);
    }

    @Override
    public void resize(int width, int height){

    }
    public static void onChange(Actor actor, Runnable runnable){ // Method for button behavior
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                runnable.run();
            }
        });
    }

    @Override
    public void dispose() {
        room1Music.dispose();
        room2Music.dispose();
        rayHandler.dispose();
        world.dispose();
    }
}

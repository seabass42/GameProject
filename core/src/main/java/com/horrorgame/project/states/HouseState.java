package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;

public class HouseState extends State{

    private AssetManager manager;
    private Texture room1, room2;
    private Stage stage;
    private OrthographicCamera camera;
    private Music room1Music, room2Music;
    private boolean startedPlaying = false;
    private Button proceed1;

    public HouseState(GameStateManager gsm, AssetManager manager){
        super(gsm);

        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH;
        camera.viewportHeight = HorrorMain.HEIGHT;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        this.manager = manager;
        room1 = manager.get("House/amnesia_room1.jpeg", Texture.class);
        room2 = manager.get("House/amnesia_room2.png", Texture.class);
        room1Music = Gdx.audio.newMusic(Gdx.files.internal("House/House1(Grand Hall).mp3"));
        room1Music.setVolume(0.5f);
        Button.ButtonStyle invis = new Button.ButtonStyle();
        invis.up = null;
        invis.down = null;
        invis.over = null;
        proceed1 = new Button(invis);
        proceed1.setBounds(520 + HorrorMain.WIDTH / 4f, 320, 75, 280);
        proceed1.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                System.out.println("Clicked!");

            }
        });

        stage.setDebugAll(true);
        stage.addActor(proceed1);


    }
    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {
        camera.setToOrtho(false, HorrorMain.WIDTH, HorrorMain.HEIGHT);
        if (!startedPlaying){
            room1Music.play();
            room1Music.setLooping(true);
            startedPlaying = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            room1Music.stop();
            gsm.pop();
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        sb.setProjectionMatrix(camera.combined);
        sb.draw(room1,HorrorMain.WIDTH / 12f,0);
        if (proceed1.isPressed()){
            sb.draw(room2, HorrorMain.WIDTH / 12f,0);
        }
        sb.end();
        stage.draw();
        stage.act();

    }

    @Override
    public void resize(int width, int height){

    }

    @Override
    public void dispose() {
        room1Music.dispose();
    }
}

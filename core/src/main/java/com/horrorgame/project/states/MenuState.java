package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;


public class MenuState extends State {
    private AssetManager manager = new AssetManager();

    private Texture background;
    private Stage stage;
    private Skin skin;
    private Table table;
    private TextButton playButton, exitButton, creditsButton;
    private ShapeRenderer shapeRenderer;
    private Label title;
    private Boolean playOnce = false;
    private final Sound hoverSelect;
    private Music menuMusic;

    public MenuState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;

        background = manager.get("onlytheocean-silent-hill-sm.jpeg", Texture.class);
        hoverSelect = manager.get("sounds/gui/hoverSelect.wav", Sound.class);
        skin = manager.get("vhsui/vhs-ui.json", Skin.class);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setPosition(300,300);
        stage.addActor(table);
        title = new Label(HorrorMain.TITLE, skin);
        title.setPosition(HorrorMain.WIDTH / 8f, HorrorMain.HEIGHT - 55, Align.center);
        stage.addActor(title);
       // stage.setDebugAll(true);

        playButton = new TextButton("Play",skin);
        exitButton = new TextButton("Exit", skin);
        creditsButton = new TextButton("Credits", skin);
        playButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                menuMusic.stop();
                gsm.set(new LoadingState(gsm, manager));
            }
        });
        onChange(creditsButton, () -> gsm.push(new CreditsState(gsm, manager)));
        onChange(exitButton, () -> Gdx.app.exit());

        table.padTop(60);
        table.add(playButton).padBottom(20);
        table.row();
        table.add(creditsButton).padBottom(20);
        table.row();
        table.add(exitButton);

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Main Menu Claw Finger.mp3"));
        menuMusic.play();
        menuMusic.setVolume(0.4f);
        menuMusic.setLooping(true);
    }


    @Override
    protected void setDebugMode() {
        debugMode = !debugMode;
    }

    @Override
    protected void handleInput() {

        if(Gdx.input.isKeyPressed(Input.Keys.NUM_2) && Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)){
            setDebugMode();
        }

        if(buttonHover() && !playOnce){
            hoverSelect.play();
            playOnce = true;
        }else if(!buttonHover()){
            playOnce = false;
        }

        if(debugMode) {
            table.setDebug(true);
        }else  {
            table.setDebug(false);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(background,0,0, HorrorMain.WIDTH, HorrorMain.HEIGHT);
        sb.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

    }

    private Boolean buttonHover(){
        if (playButton.isOver() || exitButton.isOver() || creditsButton.isOver()){
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void dispose() {

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(HorrorMain.WIDTH, HorrorMain.HEIGHT, true);
    }

    public static void onChange(Actor actor, Runnable runnable){ // Method for button behavior
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                runnable.run();
            }
        });
    }
}

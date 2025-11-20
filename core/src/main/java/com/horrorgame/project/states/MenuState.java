package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;

public class MenuState extends State {
    private AssetManager manager;

    private Texture background;
    private Stage stage;
    private Skin skin;
    private Table table;
    private TextButton playButton, exitButton, creditsButton;
    private Label title;
    private boolean playOnce = false;
    private final Sound hoverSelect;

    public MenuState(GameStateManager gsm, AssetManager manager){
        super(gsm); //Initialize background and ui skin
        this.manager = manager;
        background = manager.get("onlytheocean-silent-hill-sm.jpeg", Texture.class);
        hoverSelect = manager.get("sounds/gui/hoverSelect.wav", Sound.class);
        skin = manager.get("vhsui/vhs-ui.json", Skin.class);


        stage = new Stage(new ScreenViewport()); //Set up stage
        Gdx.input.setInputProcessor(stage);

        table = new Table();    //Set up table onto stage
        table.setPosition(300,300);
        stage.addActor(table);
        title = new Label(HorrorMain.TITLE, skin, "title");
        title.setPosition(200, HorrorMain.HEIGHT - 48, Align.center);
        stage.addActor(title);

        playButton = new TextButton("Play",skin);   // Add menu buttons
        exitButton = new TextButton("Exit", skin);
        creditsButton = new TextButton("Credits", skin);
        table.padTop(60);
        table.add(playButton).padBottom(20);
        table.row();
        table.add(creditsButton).padBottom(20);
        table.row();
        table.add(exitButton);
        //table.setDebug(true);

        onChange(playButton, () -> gsm.set(new LoadingState(gsm, manager)));// Buttons made functional

        onChange(exitButton, () -> Gdx.app.exit());
    }



    @Override
    protected void handleInput() {

    }

    private void onPlayClicked() {

        // Check if GameState assets are already loaded
        if(manager.isLoaded("onlytheocean-silent-hill-sm.jpeg")) {
            gsm.set(new GameState(gsm, manager));
        } else {
            gsm.set(new LoadingState(gsm, manager));
        }
    }

    @Override
    public void update(float dt) {
        if(buttonHover() && !playOnce){
            hoverSelect.play();
            playOnce = true;
        }else if(!buttonHover()){playOnce = false;}


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
        return playButton.isOver() ||
               exitButton.isOver() ||
               creditsButton.isOver();
    }
    @Override
    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();

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

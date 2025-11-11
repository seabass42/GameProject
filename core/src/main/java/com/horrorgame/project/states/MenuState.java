package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;

import java.util.concurrent.TimeUnit;


public class MenuState extends State {
    private Texture background;
    private Stage stage;
    private Skin skin;
    private Table table;
    private TextButton playButton, exitButton, creditsButton;
    private Rectangle textBox;
    private ShapeRenderer shapeRenderer;
    private Label title;

    public MenuState(GameStateManager gsm){
        super(gsm); //Initialize background and ui skin
        background = new Texture("onlytheocean-silent-hill-sm.jpeg");
        skin = new Skin(Gdx.files.internal("vhsui/vhs-ui.json")); // Use VHS ui folder

        stage = new Stage(new ScreenViewport()); //Set up stage
        Gdx.input.setInputProcessor(stage);

        table = new Table();    //Set up table onto stage
        table.setPosition(300,300);
        stage.addActor(table);
        title = new Label(HorrorMain.TITLE, skin);
        title.setPosition(HorrorMain.WIDTH, HorrorMain.HEIGHT, Align.center);
        stage.addActor(title);

        playButton = new TextButton("Play",skin);   //Add menu buttons
        exitButton = new TextButton("Exit", skin);
        creditsButton = new TextButton("Credits", skin);
        table.padTop(60);   //Improve spacing between buttons
        table.add(playButton).padBottom(20);
        table.row();
        table.add(creditsButton).padBottom(20);
        table.row();
        table.add(exitButton);
        //table.setDebug(true);
    }

    @Override
    protected void handleInput() {
        onChange(playButton, () -> gsm.set(new GameState(gsm))); // Buttons made functional
        onChange(exitButton, () -> Gdx.app.exit());
        dispose();
    }

    @Override
    public void update(float dt) {
        handleInput();
        stage.getViewport().update(HorrorMain.WIDTH, HorrorMain.HEIGHT, true);
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

    @Override
    public void dispose() {

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

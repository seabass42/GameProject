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



public class MenuState extends State {
    private Texture background;
    private Stage stage;
    private Skin skin;
    private Table table;
    private TextButton playButton, exitButton, creditsButton;
    private Rectangle textBox;
    private ShapeRenderer shapeRenderer;
    private Label exitWarning;

    public MenuState(GameStateManager gsm){
        super(gsm); //Initialize background and ui skin
        background = new Texture("onlytheocean-silent-hill-sm.jpeg");
        skin = new Skin(Gdx.files.internal("vhsui/vhs-ui.json")); // Use VHS ui folder

        stage = new Stage(new ScreenViewport()); //Set up stage
        Gdx.input.setInputProcessor(stage);

        table = new Table();    //Set up table onto stage
        table.setPosition(300,300);
        stage.addActor(table);

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

        playButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                System.out.println("Button Works!");
                gsm.set(new GameState(gsm));
            }
        });
        exitButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                table.reset();
                exitWarning = new Label("You can't hide forever.", skin);
                
                Gdx.app.exit();
            }
        });
    }

    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {

        stage.getViewport().update(HorrorMain.WIDTH, HorrorMain.HEIGHT, true);
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(background,0,0, HorrorMain.WIDTH / 1.2f, HorrorMain.HEIGHT/ 1.2f);
        sb.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void dispose() {

    }
}

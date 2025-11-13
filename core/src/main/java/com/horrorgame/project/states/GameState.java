package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Tiles.MapData;
import com.horrorgame.project.Tiles.MapDrawer;
import com.horrorgame.project.sprites.Player;

public class GameState extends State{
    private Texture background;
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int tileSize;
    private Player player;
    private SpriteBatch batch;
    private OrthographicCamera camera;


    public GameState(GameStateManager gsm){
        super(gsm);

        player = new Player(HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2);
        Gdx.input.setInputProcessor(player);
        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH/2f;
        camera.viewportHeight = HorrorMain.HEIGHT/2f;

        background = new Texture("testBackground.png");
        tileset = new Texture("TileAssets/Tileset.png");
        tileSize = 16;
        tiles = TextureRegion.split(tileset, tileSize, tileSize);
    }
    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {
        player.update(dt);
        camera.update();
        camera.position.set(player.getPositionX(),player.getPositionY(), 0);
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        camera.position.set(HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2,0);

        sb.setProjectionMatrix(camera.combined);
        sb.begin();

        //sb.draw(background,0,0, HorrorMain.WIDTH,HorrorMain.HEIGHT/2);
        MapDrawer mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer.render(sb);
        player.render(sb);


        sb.end();
    }


    @Override
    public void dispose() {

    }
}

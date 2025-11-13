package com.horrorgame.project.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Tiles.MapData;
import com.horrorgame.project.Tiles.MapDrawer;

public class GameState extends State{
    private Texture background;
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int tileSize;

    public GameState(GameStateManager gsm){
        super(gsm);
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

    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        //sb.draw(background,0,0, HorrorMain.WIDTH,HorrorMain.HEIGHT/2);
        MapDrawer mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer.render(sb);


        sb.end();
    }

    @Override
    public void dispose() {

    }
}

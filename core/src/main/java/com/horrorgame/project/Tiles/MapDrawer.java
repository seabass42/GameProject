package com.horrorgame.project.Tiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MapDrawer {
    private int[][] map;
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int tileSize, tilesPerRow;

    public MapDrawer(int[][] map){
        this.map = map;
        tileSize = 16;
        tileset = new Texture("TileAssets/Tileset.png");
        tiles = TextureRegion.split(tileset, tileSize, tileSize);
        tilesPerRow = 20;
    }
    public void render(SpriteBatch sb){
        for (int rows = 0; rows < map[0].length; rows++){
            for (int col = 0; col < map[0].length; col++){
                int tileIndex = map[rows][col];
                int mapRow = tileIndex / tilesPerRow;
                int mapCol = tileIndex % tilesPerRow;
               // sb.draw
            }
        }
    }
}

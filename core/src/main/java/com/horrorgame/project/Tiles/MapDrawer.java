/*
    MapDrawer is designed to fully render any given 2D array of integers that
    reference tiles in Tileset.png
 */
package com.horrorgame.project.Tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.horrorgame.project.HorrorMain;

import java.io.File;

public class MapDrawer {
    private int[][] map;
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int tileSize, tilesPerRow;

    private JsonReader reader;
    private JsonValue base, layer, data;

    public MapDrawer(int[][] map){
        this.map = map;     // map from state class
        tileSize = 16;
        tileset = new Texture("TileAssets/Tileset.png");
        tiles = TextureRegion.split(tileset, tileSize, tileSize);   // array of tiles used for maps
        tilesPerRow = 20; // tiles per row in Tileset.png
    }
    public void render(SpriteBatch sb){

        for (int rows = 0; rows < map.length; rows++){
            for (int col = 0; col < map[0].length; col++){
                int tileIndex = map[rows][col] - 1;     //  Get the key for which tile is to be rendered
                int mapRow = tileIndex / tilesPerRow;   //  Translate key into 2D array reference
                int mapCol = tileIndex % tilesPerRow;

                float x = tileSize * col;   // Tile rendered along the x-axis
                /*  2D arrays go from top left down, while LibGDX's draw() starts at bottom left,
                    so the y coordinate of the tile must be flipped to avoid an inverted map
                    */
                float y = (HorrorMain.HEIGHT - tileSize) - (rows * tileSize);
                sb.draw(tiles[mapRow][mapCol], x, y);
            }
        }
    }

}

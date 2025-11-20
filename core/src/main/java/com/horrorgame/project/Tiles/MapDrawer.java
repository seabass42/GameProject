/*
    MapDrawer is designed to fully render any given 2D array of integers that
    reference tiles in Tileset.png
 */
package com.horrorgame.project.Tiles;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.horrorgame.project.HorrorMain;

public class MapDrawer {
    private int[][] map;
    private Texture tileset, barbedWire;
    public static TextureRegion[][] tiles, barbed_wire;
    public static final int TILE_SIZE = 16;
    private static final int TILESET_FIRSTGRID = 1;
    private static final int BARBED_WIRE_FIRSTGRID = 401;

    public MapDrawer(int[][] map){
        this.map = map;     // map from state class
        tileset = new Texture("TileAssets/Tileset.png");
        barbedWire = new Texture("TileAssets/fence_barbed.png");

        tiles = TextureRegion.split(tileset, TILE_SIZE, TILE_SIZE);   // array of tiles used for map
        barbed_wire = TextureRegion.split(barbedWire, TILE_SIZE, TILE_SIZE);

    }
    public void render(SpriteBatch sb){

        for (int rows = 0; rows < map.length; rows++){

            for (int col = 0; col < map[0].length; col++){
                int gid = map[rows][col];
                if (gid == 0) continue;

                TextureRegion[][] regionUsed;
                int tileIndex;

                if (gid >= TILESET_FIRSTGRID && gid < BARBED_WIRE_FIRSTGRID){
                    regionUsed = tiles;
                    tileIndex = gid - TILESET_FIRSTGRID;
                }
                else if (gid >= BARBED_WIRE_FIRSTGRID){
                    regionUsed = barbed_wire;
                    tileIndex = gid - BARBED_WIRE_FIRSTGRID;
                } else{continue;}

                int mapRow = tileIndex / regionUsed[0].length;   //  Translate key into 2D array reference
                int mapCol = tileIndex % regionUsed[0].length;

                float x = TILE_SIZE * col;   // Tile rendered along the x-axis
                /*  2D arrays go from top left down, while LibGDX's draw() starts at bottom left,
                    so the y coordinate of the tile must be flipped to avoid an inverted map
                    */
                float y = (HorrorMain.HEIGHT - TILE_SIZE) - (rows * TILE_SIZE);
                sb.draw(regionUsed[mapRow][mapCol], x, y*0.99f);

            }
        }
    }

}

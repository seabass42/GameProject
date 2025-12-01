package com.horrorgame.project.Tiles;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.File;

public class MapData {
    private static JsonReader reader;
    private static JsonValue base, layer, data;


    private static int[][] prepareMap(File file, int layerIndex){ // Converts 1D array from json to 2D for use in MapDrawer
        reader = new JsonReader();
        base = reader.parse(Gdx.files.internal(file.getPath()));
        layer = base.get("layers").get(layerIndex);
        data = layer.get("data");

        int height = layer.getInt("height");
        int width = layer.getInt("width");
        int[][] map = new int[height][width];
        int dataIndex = 0;
        /*
        Iterate through 1D array (data) into map[][]
        put first "width" # of characters into map[0][0-79]
        go to map[1][0-79] and input next "width" # of characters
        repeat until map[n][0-79], in this case n = 44
         */
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                map[i][j] = data.getInt(dataIndex);
                dataIndex++;
            }
        }
        return map;
    }

   private static File gameStateMap = new File("Maps/MainMap.json");
   public static int[][] MainMap = prepareMap(gameStateMap, 0);
   public static int[][] MainMapLayer2 = prepareMap(gameStateMap, 1);



}

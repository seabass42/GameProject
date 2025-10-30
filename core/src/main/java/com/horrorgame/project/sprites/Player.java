package com.horrorgame.project.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

public class Player{
    private Vector3 position;
    private Vector3 walkSpeed;
    private Texture mainCharacter;


    public Player(int x, int y){ // Initializes the player to start at (x,y)
        position = new Vector3(x,y,0);
        mainCharacter = new Texture("idle.png");

    }

    public void update(float dt){

    }

}

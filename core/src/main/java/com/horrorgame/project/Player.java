package com.horrorgame.project;

import com.badlogic.gdx.math.Vector3;

public class Player {
    private Vector3 position;
    private Vector3 walkSpeed;

    public Player(int x, int y){ // Initializes the player to start at (x,y)
        position = new Vector3(x,y,0);

    }

}

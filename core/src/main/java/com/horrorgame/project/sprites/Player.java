package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Player extends Sprite implements InputProcessor {
    //private Vector3 position;
    private Vector2 walkSpeed = new Vector2();
    private float speed = 60*2;



    public Player(Sprite sprite){ // Initializes the player to start at (x,y)
        //position = new Vector3(x,y,0);
        super(sprite);

    }

    public void update(float dt){

        setX(getX() + walkSpeed.x * dt);
        setY(getY() + walkSpeed.y * dt);
    }

    public void draw(SpriteBatch batch){
        update(Gdx.graphics.getDeltaTime());
        super.draw(batch);

    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                walkSpeed.y = speed;
                break;
            case Input.Keys.S:
                walkSpeed.y = -speed;
                break;
            case Input.Keys.A:
                walkSpeed.x = -speed;
                break;
            case Input.Keys.D:
                walkSpeed.x = speed;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.S:
                walkSpeed.y = 0;
                break;
            case Input.Keys.A:
            case Input.Keys.D:
                walkSpeed.x = 0;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}

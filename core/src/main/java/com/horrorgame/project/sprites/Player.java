package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;

public class Player implements InputProcessor {

    private Vector2 position = new Vector2();
    private Vector2 walkSpeed = new Vector2();
    private float speed = 60 * 2;
    private boolean walkingLeft = false;

    private TextureAtlas atlas;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> idleAnimation;

    private float stateTime = 0; //animation time / frame number
    private boolean isWalking = false;

    public Player(float x, float y) {
        position.set(x, y);

        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/idleSprites.atlas"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/walkingSprites.atlas"));
        walkAnimation = new Animation<>(0.05f, atlas.findRegions("walking"));
    }


    public void update(float dt) {
        // Only advance animation time if walking
        if (isWalking) {
            stateTime += dt;
        }

        // Move player
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speed = 200;
        }
        position.x += walkSpeed.x * dt;
        position.y += walkSpeed.y * dt;
    }

    public float getPositionX() { return position.x; }
    public float getPositionY() { return position.y; }
    public Vector2 getVectorPos() {return position;}
    public float getPosXFace(){if(walkingLeft){return position.x;}else{return position.x +75;}}
    public float getAngleBetweenObj(Vector2 v1, Vector2 v2){
        Vector2 diff = v2.sub(v1);
        return diff.angle();
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime, true);

        // Only use walking animation if player is moving
        if (isWalking) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            // Use idle animation (or just the first frame if you only have one)
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }

        //checks the direction of the player in order to flip the images
        if (currentFrame.isFlipX() != walkingLeft) {
            currentFrame.flip(true, false);
        }
        batch.draw(currentFrame, position.x, position.y);
    }


    // --- Input handling ---
    @Override
    public boolean keyDown(int keycode) {
        //walkSpeed.set(0, 0); // (cancels last input) reset the walking velocity every time this is called (locks diagonal movement)
        switch (keycode) {
            case Input.Keys.W:
                walkSpeed.y = speed;
                break;
            case Input.Keys.S:
                walkSpeed.y = -speed;
                break;
            case Input.Keys.A:
                walkingLeft = true;
                walkSpeed.x = -speed;
                break;
            case Input.Keys.D:
                walkingLeft = false;
                walkSpeed.x = speed;
                break;
            default:
                return false;
        }

        isWalking = true;
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
                break;
            default:
                return false;
        }

        // Check if all movement keys are released
        if (walkSpeed.x == 0 && walkSpeed.y == 0) {
            isWalking = false;
            stateTime = 0; // optional: reset to first walking frame
        }

        return true;
    }

    // --- Unused Input methods ---
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}

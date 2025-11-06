package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;

public class Player implements InputProcessor {

    private Vector2 position = new Vector2();
    private Vector2 walkSpeed = new Vector2();
    private float speed = 60 * 2;

    private TextureAtlas atlas;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> idleAnimation;

    private float stateTime = 0;
    private boolean isWalking = false;

    public Player(float x, float y) {
        position.set(x, y);

        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/idleSprites.atlas"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    public float getPositionX() { return position.x; }
    public float getPositionY() { return position.y; }

    public void update(float dt) {
        // Only advance animation time if walking
        if (isWalking) {
            stateTime += dt;
            atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/walkingSprites.atlas"));
            walkAnimation = new Animation<>(0.05f, atlas.findRegions("walking"));
        }

        // Move player
        position.x += walkSpeed.x * dt;
        position.y += walkSpeed.y * dt;
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame;

        // Only use walking animation if player is moving
        if (isWalking) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            // Use idle animation (or just the first frame if you only have one)
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }

        batch.draw(currentFrame, position.x, position.y);
    }


    // --- Input handling ---
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

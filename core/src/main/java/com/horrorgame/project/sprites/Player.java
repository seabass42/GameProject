package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class Player implements InputProcessor {

    private Vector2 position = new Vector2();
    private Vector2 centerOfPlayer =  new Vector2();  //bc the sprites using position create an offset
    private Vector2 walkSpeed = new Vector2();
    private float speed = 120;
    private boolean facingLeft = false;

    //Inventory Maybe
    public boolean hasLight = true; //temp true until we make it so that flashlight is obtained

    private TextureAtlas atlas;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> idleAnimation;

    private float stateTime = 0; //animation time / frame number

    private ArrayList<Sound> footsteps = new ArrayList<Sound>();
    private float lastStepX, lastStepY;
    private final float STEP_DISTANCE = 50f;
    private final Random random = new Random();

    private boolean isWalking = false;
    private boolean allowDiagonals = false; // toggle diagonal movement


    public Player(float x, float y) {
        position.set(x, y);
        centerOfPlayer.set(x+40, y+50);

        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/idleSprites.atlas"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/walkingSprites.atlas"));
        walkAnimation = new Animation<>(0.05f, atlas.findRegions("walking"));
        footsteps = loadSounds("assets/sounds/player/LightDirt", 4);

    }


    public void update(float dt) {
        // Adjust speed based on Shift key
        speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 200 : 120;
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {walkAnimation.setFrameDuration(0.045f);
        }else {walkAnimation.setFrameDuration(0.06f);}

        // Reset movement
        walkSpeed.set(0, 0);

        // Determine movement direction
        if(allowDiagonals) {
            if(Gdx.input.isKeyPressed(Input.Keys.W)) walkSpeed.y = speed;
            if(Gdx.input.isKeyPressed(Input.Keys.S)) walkSpeed.y = -speed;
            if(Gdx.input.isKeyPressed(Input.Keys.A)) {
                walkSpeed.x = -speed;
                facingLeft = true;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.D)) {
                walkSpeed.x = speed;
                facingLeft = false;
            }
        } else {
            // Prevent diagonal: only move in one axis at a time (vertical first)
            if(Gdx.input.isKeyPressed(Input.Keys.W)) {
                walkSpeed.y = speed;
            }else if(Gdx.input.isKeyPressed(Input.Keys.S)) {
                walkSpeed.y = -speed;
            }else if(Gdx.input.isKeyPressed(Input.Keys.A)) {
                walkSpeed.x = -speed;
                facingLeft = true;
            } else if(Gdx.input.isKeyPressed(Input.Keys.D)) {
                walkSpeed.x = speed;
                facingLeft = false;
            }
        }

        // Determine if walking
        isWalking = walkSpeed.len() > 0;

        if (isWalking) {
            stateTime += dt; // Advance animation time only if walking

            //mark the new last step
            float dx = position.x - lastStepX;
            float dy = position.y - lastStepY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            //for every STEP_DISTANCE traveled, a random sound in ArrayList<Sound>footsteps will play
            if (distance >= STEP_DISTANCE) {
                //eventually, the sounds in footsteps will be changed to the running sounds
                footsteps.get(random.nextInt(footsteps.size())).play();
                lastStepX = position.x;  //mark last footstep
                lastStepY = position.y;
            }
        }

        // Move player
        position.x += walkSpeed.x * dt;
        position.y += walkSpeed.y * dt;
        centerOfPlayer.x = position.x + 20;
        centerOfPlayer.y = position.y + 20;
    }

    //new reusable method for loading sound files!!! for running versus walking, grass vs concrete etc
    private ArrayList<Sound> loadSounds(String basePath, int count) {
        ArrayList<Sound> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(Gdx.audio.newSound(Gdx.files.internal(basePath + i + ".wav")));
        }
        return list;
    }

    public float getPositionX() { return centerOfPlayer.x; }
    public float getPositionY() { return centerOfPlayer.y; }
    public Vector2 getVectorPos() {return centerOfPlayer;}
    public float getAngleBetweenObj(Vector2 v1, Vector2 v2){
        Vector2 diff = v2.sub(v1);
        return diff.angle();
    }
    public boolean getDirection(){ return facingLeft; }
    public void setDirection(boolean facingLeft){ this.facingLeft = facingLeft; }


    public void render(SpriteBatch batch) {
        TextureRegion currentFrame;

        // Only use walking animation if player is moving
        if (isWalking) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            // Use idle animation (or just the first frame if you only have one)
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }

        //checks the direction of the player in order to flip the images
        if (currentFrame.isFlipX() != facingLeft) {
            currentFrame.flip(true, false);
        }
        batch.draw(currentFrame, position.x, position.y);
    }


    // --- Input handling ---
    private void playerRun(){
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speed = 200;
        }else speed=120;
    }

    // --- Unused Input methods ---
    @Override public boolean keyDown(int keycode) {return false;}
    @Override public boolean keyUp(int keycode) {return false;}
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}

package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Random;

import static com.horrorgame.project.states.State.debugMode;

public class Player extends PhysicsSprite {

    private Vector2 velocity = new Vector2();
    private float speed = 30;
    private boolean facingLeft = false;
    public boolean hasLight = true;

    private TextureAtlas atlas;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> idleAnimation;
    private TextureRegion currentFrame;

    private float stateTime = 0f;

    private ArrayList<Sound> footsteps = new ArrayList<>();
    private Sound tired;
    private Sound out_of_breath;
    private float lastStepX, lastStepY;
    private float STEP_DISTANCE = 15f;
    private final Random random = new Random();

    private boolean isWalking = false;
    private boolean allowDiagonals = false;

    private float stamina = 1f;             // Stamina level (0 to 1)
    private final float RUN_DEPLETION_TIME = 5f;    // seconds to fully deplete
    private final float RECOVERY_TIME = 5f; // seconds to recover
    public boolean isTired = false;
    private boolean canRun = true;

    private Rectangle hitboxUp;
    private Rectangle hitboxLeft;
    private Rectangle hitboxRight;
    private Rectangle hitboxDown;

    private int[] inventory;


    public Player(String name, Texture texture, float x, float y, float width, float height) {
        super(name, texture, x, y, width, height, false);

        setSize(width, height);
        setOriginCenter(); // makes rotation around the sprite center

        // Create matching Box2D body
        setBoxFixture(width, height);
        body.setType(BodyDef.BodyType.DynamicBody); // ignore collisions but still movable

        // Load animations
        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/idleSprites.atlas"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/walkingSprites.atlas"));
        walkAnimation = new Animation<>(0.05f, atlas.findRegions("walking"));

        // Load footstep sounds
        footsteps = loadSounds("assets/sounds/player/LightDirt", 4);
        // Load tired sound
        tired = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/player/heartbeat.wav"));
        out_of_breath = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/player/out-of-breath2.ogg"));

        hitboxLeft = new Rectangle(x-width/2,y+5,width/2,height-10);
        hitboxRight = new Rectangle(x+width/2,y+5,width/2,height-10);
        hitboxUp = new Rectangle(x+5,y+height/2,width-10,height/2);
        hitboxDown = new Rectangle(x+5,y-height/2,width-10,height/2);

        inventory = new int[4];
        inventory[0] = 0;

        // Inventory key: 0 = flashlight, 1 = item1, 2 = item2, 3 = item3
    }
    //  Check if player is colliding with something
    public boolean collidesLeft(Rectangle rect){return hitboxLeft.overlaps(rect);}
    public boolean collidesRight(Rectangle rect) { return hitboxRight.overlaps(rect); }
    public boolean collidesUp(Rectangle rect){ return hitboxUp.overlaps(rect); }
    public boolean collidesDown(Rectangle rect){ return hitboxDown.overlaps(rect); }
    public int checkInventory(int index){
        return inventory[index];
    }
    public void setItem(int index, int item){
        inventory[index] = item;
    }

    public void setVelocity(float x, float y){
        velocity.x = x;
        velocity.y = y;
    }

    public Rectangle getHitboxLeft(){
        return hitboxLeft;
    }
    public Rectangle getHitboxRight(){
        return hitboxRight;
    }
    public Rectangle getHitboxUp(){
        return hitboxUp;
    }
    public Rectangle getHitboxDown(){
        return hitboxDown;
    }


    private ArrayList<Sound> loadSounds(String basePath, int count) {
        ArrayList<Sound> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(Gdx.audio.newSound(Gdx.files.internal(basePath + i + ".wav")));
        }
        return list;
    }

    public float getVelX(){
        return velocity.x;
    }
    public float getVelY(){
        return velocity.y;
    }

    public void setDirection(boolean facingLeft) { this.facingLeft = facingLeft; }

    public float getAngleBetweenObj(Vector2 v1) {
        return v1.cpy().sub(getPosition()).angle();
    }

    public void update(float dt) {

        boolean shiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        canRun = shiftPressed && !isTired;
        // Update stamina
        if (shiftPressed && stamina > 0f && isWalking && !isTired) {
            // Running depletes stamina
            stamina -= dt / RUN_DEPLETION_TIME;

            if (stamina <= 0f) {
                stamina = 0f;
                if (!isTired && !debugMode) {
                    tired.play(0.07f);
                    out_of_breath.play(0.1f);
                    isTired = true;
                }
            }
        } else if (stamina < 1f) {
            // Recover stamina when not running
            stamina += dt / RECOVERY_TIME;
            if (stamina >= 1f) {
                stamina = 1f;
                isTired = false;
            }
        }

        // Can run only if stamina > 0
        // Adjust speed
        speed = canRun ? 50 : 30;
        STEP_DISTANCE = canRun ? 20 : 15;
        footsteps = canRun ? loadSounds("assets/sounds/player/LightDirtRun", 4)
            : loadSounds("assets/sounds/player/LightDirt", 4);
        walkAnimation.setFrameDuration(canRun ? 0.045f : 0.06f);
        //END OF STAMINA
        // Reset movement
        velocity.set(0, 0);

        // Determine movement direction
        if (allowDiagonals) {
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) velocity.y = speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) velocity.y = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) { velocity.x = -speed; facingLeft = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { velocity.x = speed; facingLeft = false; }
        } else {
            // No diagonals
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {velocity.y = speed;}
            else if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {velocity.y = -speed;}
            else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) { velocity.x = -speed; facingLeft = true;}
            else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { velocity.x = speed; facingLeft = false;}
        }

        //Walking
        isWalking = velocity.len() > 0;

        //Footstep sounds
        if (isWalking) {
            stateTime += dt;
            float dx = body.getPosition().x - lastStepX;
            float dy = body.getPosition().y - lastStepY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance >= STEP_DISTANCE) {
                footsteps.get(random.nextInt(footsteps.size())).play();
                lastStepX = body.getPosition().x;
                lastStepY = body.getPosition().y;
            }
        }

        //Move body and hitbox
        hitboxLeft.setPosition(body.getPosition().x-width/2,body.getPosition().y-height/4);
        hitboxRight.setPosition(body.getPosition().x,body.getPosition().y-height/4);
        hitboxUp.setPosition(body.getPosition().x-width/4,body.getPosition().y);
        hitboxDown.setPosition(body.getPosition().x-width/4, body.getPosition().y-height/2);
        // Apply collision pushback AFTER movement:
        body.setLinearVelocity(velocity.x, velocity.y);
        //super.update();
    }

    public void render(SpriteBatch batch) {
        // Choose animation frame
        currentFrame = isWalking ? walkAnimation.getKeyFrame(stateTime, true)
            : idleAnimation.getKeyFrame(stateTime, true);

        // Flip sprite if needed
        if (currentFrame.isFlipX() != facingLeft) {
            currentFrame.flip(true, false);
        }

        // Draw sprite at scaled size
        batch.draw(currentFrame,
            body.getPosition().x - getWidth() / 2f,
            body.getPosition().y - getHeight() / 2f,
            getWidth(),
            getHeight());
    }
}

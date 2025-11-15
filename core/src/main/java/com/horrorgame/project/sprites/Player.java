package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.horrorgame.project.states.GameState;
import com.horrorgame.project.states.State;

import java.util.ArrayList;
import java.util.Random;

import static com.horrorgame.project.states.GameState.world;
import static com.horrorgame.project.states.State.debugMode;

public class Player extends PhysicsSprite{

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private Vector2 centerOfPlayer =  new Vector2();  //bc the sprites using position create an offset

    private Vector2 velocity = new Vector2();
    private float speed = 120;
    private boolean facingLeft = false;

    //Inventory Maybe
    public boolean hasLight = true; //temp true until we make it so that flashlight is obtained

    private TextureAtlas atlas;
    TextureRegion currentFrame;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> idleAnimation;

    private float stateTime = 0; //animation time / frame number

    private ArrayList<Sound> footsteps = new ArrayList<Sound>();
    private float lastStepX, lastStepY;
    private final float STEP_DISTANCE = 50f;
    private final Random random = new Random();

    private boolean isWalking = false;
    private boolean allowDiagonals = false; // toggle diagonal movement


    public Player(String name, Texture texture, float x, float y, float width, float height) {
        super(name, texture, x, y, width, height, false);

        centerOfPlayer.set(x+40, y+50);
        setBoxFixture(width,height);
        body.setType(BodyDef.BodyType.KinematicBody); //make player ignore collisions, but able to create them

        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/idleSprites.atlas"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/walkingSprites.atlas"));
        walkAnimation = new Animation<>(0.05f, atlas.findRegions("walking"));
        footsteps = loadSounds("assets/sounds/player/LightDirt", 4);

    }


    public void update(float dt) {
        // Adjust speed based on Shift key
        speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 200 : 80;
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            walkAnimation.setFrameDuration(0.045f); footsteps = loadSounds("assets/sounds/player/LightDirtRun", 4);
        }else {walkAnimation.setFrameDuration(0.06f); footsteps = loadSounds("assets/sounds/player/LightDirt", 4);}

        //Reset movement
        velocity.set(0, 0);

        // Determine movement direction
        if(allowDiagonals) {
            if(Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y = speed;
            if(Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y = -speed;
            if(Gdx.input.isKeyPressed(Input.Keys.A)) {velocity.x = -speed; facingLeft = true;}
            if(Gdx.input.isKeyPressed(Input.Keys.D)) {velocity.x = speed;facingLeft = false;}
        } else {
            // Prevent diagonal: only move in one axis at a time (vertical first)
            if(Gdx.input.isKeyPressed(Input.Keys.W)) {velocity.y = speed;
            }else if(Gdx.input.isKeyPressed(Input.Keys.S)) {velocity.y = -speed;
            }else if(Gdx.input.isKeyPressed(Input.Keys.A)) {velocity.x = -speed; facingLeft = true;
            } else if(Gdx.input.isKeyPressed(Input.Keys.D)) { velocity.x = speed; facingLeft = false;}
        }

        //Determine if walking
        isWalking = velocity.len() > 0;

        if (isWalking) {
            stateTime += dt; //Advance animation time only if walking

            //mark the new last step
            float dx = super.body.getPosition().x - lastStepX;
            float dy = super.body.getPosition().y - lastStepY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            //for every STEP_DISTANCE traveled, a random sound in ArrayList<Sound>footsteps will play
            if (distance >= STEP_DISTANCE) {
                //eventually, the sounds in footsteps will be changed to the running sounds
                footsteps.get(random.nextInt(footsteps.size())).play();
                lastStepX = super.body.getPosition().x;  //mark last footstep
                lastStepY = super.body.getPosition().y;
            }
        }

        // Move player
        super.body.setLinearVelocity(velocity.x , velocity.y);
        super.update();
        centerOfPlayer.x = super.body.getPosition().x + 40;
        centerOfPlayer.y = super.body.getPosition().y + 50;

        if(State.debugMode) {
            System.out.println(super.body.getPosition() + "      "
                + super.body.getLinearVelocity().x + "      " + super.body.getLinearVelocity());
        }
    }

    //new reusable method for loading sound files!!! for running versus walking, grass vs concrete etc
    private ArrayList<Sound> loadSounds(String basePath, int count) {
        ArrayList<Sound> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(Gdx.audio.newSound(Gdx.files.internal(basePath + i + ".wav")));
        }
        return list;
    }

    public void setDirection(boolean facingLeft){ this.facingLeft = facingLeft; }

    public float getAngleBetweenObj(Vector2 v1, Vector2 v2){
        Vector2 diff = v2.sub(v1);
        return diff.angle();
    }


    public void render(SpriteBatch batch) {
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
        batch.draw(currentFrame, super.body.getPosition().x-width/2, super.body.getPosition().y-height/2);



        if(debugMode) {
            // Draw hitboxes for debugging
            shapeRenderer.setProjectionMatrix(GameState.camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            //Draw player hitbox (rectangle)
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(getPosition().x - getBodyWidth() / 2,
                getPosition().y - getBodyHeight() / 2,
                getBodyWidth(),
                getBodyHeight());
            shapeRenderer.end();
        }

    }

}

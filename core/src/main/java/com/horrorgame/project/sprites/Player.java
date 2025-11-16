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
import java.util.ArrayList;
import java.util.Random;
import static com.horrorgame.project.states.State.debugMode;

public class Player extends PhysicsSprite {

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Vector2 velocity = new Vector2();
    private float speed = 20;
    private boolean facingLeft = false;
    public boolean hasLight = true;

    private TextureAtlas atlas;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> idleAnimation;
    private TextureRegion currentFrame;

    private float stateTime = 0f;

    private ArrayList<Sound> footsteps = new ArrayList<>();
    private float lastStepX, lastStepY;
    private float STEP_DISTANCE = 15f;
    private final Random random = new Random();

    private boolean isWalking = false;
    private boolean allowDiagonals = false;
    private int runStepCount = 0;
    private int cooldownStepCount = 0;
    public boolean isTired = false;


    public Player(String name, Texture texture, float x, float y, float width, float height) {
        super(name, texture, x, y, width, height, false);

        setSize(width, height);
        setOriginCenter(); // makes rotation around the sprite center

        // Create matching Box2D body
        setBoxFixture(width, height);
        body.setType(BodyDef.BodyType.KinematicBody); // ignore collisions but still movable

        // Load animations
        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/idleSprites.atlas"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        atlas = new TextureAtlas(Gdx.files.internal("assets/sprites/walkingSprites.atlas"));
        walkAnimation = new Animation<>(0.05f, atlas.findRegions("walking"));

        // Load footstep sounds
        footsteps = loadSounds("assets/sounds/player/LightDirt", 4);
    }

    public void update(float dt) {
        // Adjust speed based on Shift key
        boolean shiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        boolean isTryingToRun = shiftPressed && !isTired;

        speed = isTryingToRun ? 40 : 20;
        STEP_DISTANCE = isTryingToRun ? 20 : 15;
        footsteps = isTryingToRun ? loadSounds("assets/sounds/player/LightDirtRun", 4) :
            loadSounds("assets/sounds/player/LightDirt", 4);
        if (isTryingToRun) {
            walkAnimation.setFrameDuration(0.045f);
        } else {
            walkAnimation.setFrameDuration(0.06f);
        }

        // Reset movement
        velocity.set(0, 0);

        // Determine movement direction
        if (allowDiagonals) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y = speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { velocity.x = -speed; facingLeft = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { velocity.x = speed; facingLeft = false; }
        } else {
            // No diagonals
            if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y = speed;
            else if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y = -speed;
            else if (Gdx.input.isKeyPressed(Input.Keys.A)) { velocity.x = -speed; facingLeft = true; }
            else if (Gdx.input.isKeyPressed(Input.Keys.D)) { velocity.x = speed; facingLeft = false; }
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

                // --- RUNNING STEP COUNTER ---
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !isTired) {
                    runStepCount++;

                    if (runStepCount >= 12) {
                        isTired = true;       // disable running
                        runStepCount = 0;     // reset run counter
                        cooldownStepCount = 0; // start cooldown
                    }
                }

                // --- COOLDOWN STEPS ---
                if (isTired) {
                    cooldownStepCount++;

                    if (cooldownStepCount >= 20) {
                        isTired = false;        // running allowed again
                        cooldownStepCount = 0;
                    }
                }
            }
        }
        //Move body
        body.setLinearVelocity(velocity.x, velocity.y);
        super.update();
    }

    public boolean isTryingToRunWithoutStamina() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && isTired;
    }


    private ArrayList<Sound> loadSounds(String basePath, int count) {
        ArrayList<Sound> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(Gdx.audio.newSound(Gdx.files.internal(basePath + i + ".wav")));
        }
        return list;
    }

    public void setDirection(boolean facingLeft) { this.facingLeft = facingLeft; }

    public float getAngleBetweenObj(Vector2 v1, Vector2 v2) {
        return v2.cpy().sub(v1).angle();
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

        //Debug hitbox
        if (debugMode) {
            shapeRenderer.setProjectionMatrix(GameState.camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(body.getPosition().x - getWidth() / 2f,
                body.getPosition().y - getHeight() / 2f,
                getWidth(),
                getHeight());
            shapeRenderer.end();
        }
    }
}

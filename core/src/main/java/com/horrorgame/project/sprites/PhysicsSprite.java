package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.math.Vector2;
import box2dLight.Light;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.states.GameState;

import static com.horrorgame.project.states.GameState.camera;
import static com.horrorgame.project.states.State.debugMode;

public abstract class PhysicsSprite extends Sprite {

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Label label;
    private Skin skin = new Skin(Gdx.files.internal("vhsui/vhs-ui.json"));

    private String name;
    protected World world;
    protected Body body;
    protected boolean isStatic;
    protected Light light;
    protected float x, y, width, height;



    public PhysicsSprite(String name, Texture texture, float x, float y, float width, float height, boolean isStatic) {
        super(texture);
        this.name = name;
        this.world = GameState.world;
        this.isStatic = isStatic;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        label = new Label(name, skin);

        setSize(width, height);
        setPosition(x, y);

    }

    /** getters */
    public Vector2 getPosition() { return body.getPosition(); }
    public float getBodyWidth(){ return width;}
    public float getBodyHeight(){ return height;}
    public Label getLabel() { return label; }
    public String getName() { return name; }

    /** Sets a box-shaped fixture */
    protected void setBoxFixture(float width, float height) {
        if (body != null) world.destroyBody(body);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x + width / 2f, y + height / 2f);
        //bodyDef.position.set(x, y);
        bodyDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width/2, height/2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.3f;

        body.createFixture(fixtureDef);
        body.setLinearDamping(7f);
        body.setFixedRotation(true);
        shape.dispose();
    }

    /** Sets a circle-shaped fixture */
    protected void setCircleFixture(float radius) {
        if (body != null) world.destroyBody(body);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX() + radius, getY() + radius);
        bodyDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.9f; // Circles can be bouncier

        body.createFixture(fixtureDef);
        body.setLinearDamping(3f);
        body.setFixedRotation(true);
        shape.dispose();
    }

    public Body getBody() {return body;}

    /** Attach a light to this sprite */
    public void attachLight(Light light) {
        this.light = light;
    }

    /** Update sprite position/rotation to match body */
    public void update() {
        if (body == null) return;

        setPosition(body.getPosition().x - getWidth() / 2f, body.getPosition().y - getHeight() / 2f);
        setRotation((float) Math.toDegrees(body.getAngle()));

        if (light != null) {
            light.setPosition(body.getPosition().x, body.getPosition().y);
        }
    }

    public void render(SpriteBatch batch) {
        super.draw(batch);

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

    public void dispose() {
        if (body != null) world.destroyBody(body);
    }
}

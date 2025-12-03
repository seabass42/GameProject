package com.horrorgame.project.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.horrorgame.project.states.GameState;

import static com.horrorgame.project.states.GameState.player;

public class Log extends PhysicsSprite {

    private RevoluteJoint grabJoint;
    private Sound grabSound;
    private static final Vector2[] CHEST_ANCHORS = new Vector2[] {
        new Vector2( 40, 0 ),// right
        new Vector2( 20, 20 ),
        new Vector2(-40, 0 ),   // left
        new Vector2(-20, 20 ),
        new Vector2( 0, 20 ),   // top
        new Vector2( 20, -20 ),
        new Vector2( 0, -20 ),  // bottom
        new Vector2(-20,-20)
    };

    public Log(String name, Texture texture, float x, float y, float width, float height, boolean isStatic) {
        super(name, texture, x, y,width, height, isStatic);
        setSize(width, height*3.4f);
        setBoxFixture(width, height); // Automatically use box shape
        Player player = GameState.player;
        grabSound = Gdx.audio.newSound(Gdx.files.internal("sounds/objectInteractions/blip3.wav"));
    }

    @Override
    public void update() {
        super.update();
        // Add any chest-specific behavior here
        setPosition(body.getPosition().x - getWidth() / 2f, body.getPosition().y - getHeight() / 2f);
        setRotation((float) Math.toDegrees(body.getAngle())-29f);
        setOriginCenter();

        boolean isCloseEnough = player.getPosition().dst(getPosition()) < 45f;

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            // Try to grab the chest only if joint doesn't exist
            if (grabJoint == null && isCloseEnough) {
                Vector2 bestAnchor = getClosestAnchor();
                RevoluteJointDef def = new RevoluteJointDef();
                def.bodyA = player.getBody();
                def.bodyB = body;
                def.collideConnected = false;
                def.localAnchorA.set(0, 0);
                def.localAnchorB.set(bestAnchor);
                grabSound.play();
                grabJoint = (RevoluteJoint) world.createJoint(def);
            }
        } else {
            // Release joint when Space is released
            if (grabJoint != null) {
                world.destroyJoint(grabJoint);
                grabJoint = null;
            }
        }
    }

    private Vector2 getClosestAnchor() {

        Vector2 closest = null;
        float closestDist = Float.MAX_VALUE;

        for (Vector2 local : CHEST_ANCHORS) {

            //basically a loop to find minAnchor (smallest distance between the Vector2[] of anchors and player's getPosition() Vector)
            Vector2 worldAnchor = body.getWorldPoint(local);

            float dist = player.getPosition().dst(worldAnchor);

            if (dist < closestDist) {
                closestDist = dist;
                closest = local;
            }
        }

        return closest;
    }


}

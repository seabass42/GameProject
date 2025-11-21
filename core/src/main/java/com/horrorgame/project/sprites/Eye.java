package com.horrorgame.project.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static com.horrorgame.project.states.GameState.player;

public class Eye extends PhysicsSprite {
    Sprite pupil = new Sprite(new Texture("assets/sprites/pupil.png"));
    public Eye(float x, float y) {
        super("eye", new Texture("assets/sprites/eye.png"), x, y, 216/6, 125/6, true);
        setCircleFixture(10);
        body.setFixedRotation(true);
        setOriginCenter();
        pupil.setSize(216/6, 125/6);
        pupil.setOriginCenter();
        pupil.setPosition(x, y);
    }

    public void update() {

        // Eye center (world coords)
        float ex = getPosition().x+7;
        float ey = getPosition().y;

        // Direction to player
        float dx = player.getPosition().x - ex;
        float dy = player.getPosition().y - ey;

        // Normalize direction
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len == 0) len = 0.0001f;
        dx /= len;
        dy /= len;

        // Move pupil within radius
        float px = ex + dx * 5;
        float py = ey + dy * 2;

        // Center the sprite
        pupil.setPosition(px - pupil.getWidth() / 2, py - pupil.getHeight() / 2);
        setPosition(body.getPosition().x-10, body.getPosition().y-10);
    }

    public void render(SpriteBatch batch) {
        super.render(batch);
        pupil.draw(batch);
    }
}

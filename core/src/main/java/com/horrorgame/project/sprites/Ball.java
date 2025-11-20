package com.horrorgame.project.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class Ball extends PhysicsSprite {

    public Ball(String name, Texture texture, float x, float y, float radius, boolean isStatic) {
        super(name, texture, x, y,radius * 2, radius * 2, isStatic);
        setCircleFixture(radius); // Automatically use circle shape
    }

    @Override
    public void update() {
        super.update();
        // Add any ball-specific behavior here
    }
}

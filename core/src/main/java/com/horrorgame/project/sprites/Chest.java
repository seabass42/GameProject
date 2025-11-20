package com.horrorgame.project.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class Chest extends PhysicsSprite {

    public Chest(String name, Texture texture, float x, float y, float width, float height, boolean isStatic) {
        super(name, texture, x, y,width, height, isStatic);
        setBoxFixture(width, height); // Automatically use circle shape
    }

    @Override
    public void update() {
        super.update();
        // Add any ball-specific behavior here
    }
}

package com.horrorgame.project.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Random;


public class Tree extends PhysicsSprite{

    Sprite leaves;
    private final Random random = new Random();

    public Tree(String name, float x, float y) {
        super(name, new Texture("assets/sprites/Trees/tree.png"), x, y, 161/8, 501/8, true);
        setOriginCenter();
        setCircleFixture(width/3);
        body.setFixedRotation(true);
        body.setTransform(x+width/2, y+width/2-2, 0);
        leaves = new Sprite(new Texture("assets/sprites/Trees/tree" + (int)(random.nextInt(4)+1) + ".png"));
        leaves.setOriginCenter();
        leaves.setScale(0.3f);
    }

    public float getBodyHeight(){
        return super.getBodyWidth();
    }

    public void render(SpriteBatch sb) {
        super.render(sb);
        leaves.setPosition(body.getPosition().x-200, body.getPosition().y-130);
        System.out.println(body.getPosition().x + " " + body.getPosition().y+ "        " +leaves.getX() + " " + leaves.getY());
        System.out.println(leaves.getHeight());
        leaves.draw(sb);
    }
}

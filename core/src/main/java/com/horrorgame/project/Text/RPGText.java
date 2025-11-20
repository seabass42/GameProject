package com.horrorgame.project.Text;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class RPGText extends Group {
    private float typeTimer;
    private Label label;
    private int charsVisible;
    private String fullText;
    private Skin skin;


    public RPGText(String text, Skin skin){
        fullText = text;
        this.skin = skin;
        label = new Label("", skin);
        label.setWrap(true);
        label.setWidth(450);
        label.setPosition(-150,-300);
        addActor(label);

    }

    @Override
    public void act(float dt){
        super.act(dt);
        //System.out.println("Act accessed");
        typeTimer += dt;
        if (typeTimer > 0.01f && charsVisible < fullText.length()){
            charsVisible++;
            label.setText(fullText.substring(0, charsVisible));
            typeTimer = 0;
        }
        if (typeTimer > 4f){
            label.remove();
        }

    }
}

package com.horrorgame.project.Text;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.horrorgame.project.HorrorMain;

public class RPGText extends Group {
    private float typeTimer;
    private Label label;
    private int charsVisible;
    private String fullText;
    private Skin skin;
    private Sound textSound;
    private boolean notSkipped = true;

    public RPGText(String text, Skin skin){
        fullText = text;
        this.skin = skin;
        label = new Label("", skin);
        label.setWrap(true);
        label.setWidth(800);
        label.setPosition(-150,-300);
        addActor(label);
        setPosition(HorrorMain.WIDTH/4,HorrorMain.HEIGHT/2);
        textSound = Gdx.audio.newSound(Gdx.files.internal("sounds/gui/text_sound.mp3"));

    }

    @Override
    public void act(float dt){
        super.act(dt);
        //System.out.println("Act accessed");
        typeTimer += dt;
        if (typeTimer > 0.03f && charsVisible < fullText.length() && notSkipped){
            charsVisible++;
            label.setText(fullText.substring(0, charsVisible));
            typeTimer = 0;
            textSound.play();
        }
        if (typeTimer > 3f){
            label.remove();
        }

    }
    public void removeEarly(){
        label.remove();
        notSkipped = false;
    }
    public float getTypeTimer(){
        return typeTimer;
    }
}

package com.horrorgame.project.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Stack;

public class GameStateManager {

    private Stack<State> states;

    public GameStateManager(){
        states = new Stack<State>();
    }

    public void push(State state){  //Push state onto screen
        states.push(state);
    }

    public void pop(){
        states.pop();
    }   //Remove state

    public void update(float dt){
        states.peek().update(dt);
    }   //Use the update method of the state

    public void render(SpriteBatch sb){ // Use the render method of the state
        states.peek().render(sb);
    }
    public void set(State state){
        states.pop().dispose();
        states.push(state);
    }
}

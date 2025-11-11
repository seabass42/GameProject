package com.horrorgame.project.states;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.sprites.Player;

public class GameState extends State{
    private AssetManager manager = new AssetManager();

    //Cursor Position as Vector2
    private Vector2 cursorPosition = new Vector2();
    private static Player player;
    public final int tileSize = 16;
    private SpriteBatch batch;
    private static OrthographicCamera camera = new OrthographicCamera();

    //Lighting
    private static World world = new World(new Vector2(0,0), false);
    private RayHandler rayHandler = new RayHandler(world);
    private ConeLight flashlight;
    private Boolean flashOn = false;
    private Color flashRays = Color.CLEAR;
    private PointLight ambientLight;

    //Test background
    private Texture background;

    public GameState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;

        background = manager.get("onlytheocean-silent-hill-sm.jpeg", Texture.class);
        player = new Player(0,0);

        Gdx.input.setInputProcessor(player);
        camera.viewportWidth = Gdx.graphics.getWidth()/2;
        camera.viewportHeight = Gdx.graphics.getHeight()/2;

        //Flashlight Testing
        rayHandler.setCombinedMatrix(camera.combined);
        rayHandler.useDiffuseLight(true); //Stops "draining the life from the colors"/desaturation of textures
        Color lightColor = new Color(0.45f, 0.35f, 0.65f, 0f); //ambient light color
        rayHandler.setAmbientLight(Color.BLACK);
        //Actual Flashlight (testing)
        flashlight = new ConeLight(rayHandler, 250, Color.WHITE, 350, player.getPositionX(), player.getPositionY(), 0, 45);
        flashlight.setActive(false);
        //Player ambient light
        ambientLight = new PointLight(rayHandler, 10, Color.GRAY, 500, player.getPositionX(),player.getPositionY());
    }
    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched()) { // Check if the screen was just touched
            clickFlashlight();
        }
    }

    @Override
    public void update(float dt) { //Logic
        player.update(dt);
        camera.update();

        handleInput();
        //For getting cursor X and Y NOT according to camera
        // (otherwise it gets left behind when the player walks)
        Vector3 tempVec = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(tempVec);
        cursorPosition.set(tempVec.x, tempVec.y);
        //Light Updates
        ambientLight.setPosition(player.getPositionX()+20, player.getPositionY()+50);
        flashlightUpdate();
    }

    //METHODS FOR FLASHLIGHT
    //acts as a boolean switch
    public void clickFlashlight(){flashOn = !flashOn; flashlight.setActive(flashOn);}
    //updates flashlight
    private void flashlightUpdate(){
        flashlight.setPosition(player.getPosXFace(), player.getPositionY()+50);/*
        if(player.getPosXFace() > player.getPositionX()){ flashlight.setDirection(0);}
        else if(player.getPosXFace() == player.getPositionX()){ flashlight.setDirection(180);} */
        flashlight.setDirection(player.getAngleBetweenObj(player.getVectorPos(), cursorPosition));
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera to follow player (optional)
        camera.position.set(player.getPositionX()+20, player.getPositionY()+40, 0);


        // --- Draw player and other sprites ---
        sb.setProjectionMatrix(camera.combined);
        sb.begin();
        sb.draw(background, 0, 0, HorrorMain.WIDTH, HorrorMain.HEIGHT);
        player.render(sb);
        sb.end();

        //Light
        rayHandler.setCombinedMatrix(camera.combined);
        rayHandler.updateAndRender();

    }


    @Override
    public void dispose() {

    }
}

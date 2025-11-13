package com.horrorgame.project.states;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.horrorgame.project.HorrorMain;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Tiles.MapData;
import com.horrorgame.project.Tiles.MapDrawer;
import com.horrorgame.project.sprites.Player;
import jdk.internal.org.jline.terminal.TerminalBuilder;

public class GameState extends State{
    private AssetManager manager = new AssetManager();

    //Cursor Position as Vector2
    private Vector2 cursorPosition = new Vector2();
    private Vector3 cursorToWorldVec = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    private Vector2 cursorToPlayer = new Vector2();
    private static Player player;



    private SpriteBatch batch;
    private static OrthographicCamera camera = new OrthographicCamera();

    // --LIGHTING--
    private static World world = new World(new Vector2(0,0), false);
    private RayHandler rayHandler = new RayHandler(world);
    private PointLight ambientLight;

    //Flashlight Initiations
    private ConeLight flashlight;
    private Boolean flashOn = false;
    private Sound flashlight_click;



    public GameState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;

        player = new Player(HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2);
        flashlight_click = manager.get("sounds/objectInteractions/flashlight_click.wav", Sound.class);
        player = new Player(0,0);

        player = new Player(HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2);
        Gdx.input.setInputProcessor(player);
        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH/2f;
        camera.viewportHeight = HorrorMain.HEIGHT/2f;
        camera.viewportWidth = Gdx.graphics.getWidth()/2;
        camera.viewportHeight = Gdx.graphics.getHeight()/2;
        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH/2f;
        camera.viewportHeight = HorrorMain.HEIGHT/2f;

        //Lighting
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
        if (Gdx.input.justTouched() && player.hasLight) { // Check if the screen was just touched
            clickFlashlight();
            flashlight_click.play();
        }
    }

    @Override
    public void update(float dt) { //Logic
        camera.update();
        camera.position.set(player.getPositionX(),player.getPositionY(), 0);

        handleInput();
        //For getting cursor X and Y NOT according to camera
        // (otherwise it gets left behind when the player walks)
        cursorToWorldVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(cursorToWorldVec);
        cursorPosition.set(cursorToWorldVec.x, cursorToWorldVec.y);
        cursorToPlayer.set(Gdx.input.getX(), Gdx.input.getY());
        //Light Updates
        ambientLight.setPosition(player.getPositionX(), player.getPositionY());



        player.update(dt);

        flashlightUpdate();
    }

    //METHODS FOR FLASHLIGHT
    //acts as a boolean switch
    public void clickFlashlight(){flashOn = !flashOn; flashlight.setActive(flashOn);}
    //updates flashlight
    private void flashlightUpdate(){
        if(flashOn) {
            if (cursorPosition.x > player.getPositionX()) {
                player.setDirection(false);
            } else {
                player.setDirection(true);
            }
            flashlight.setPosition(player.getPositionX(), player.getPositionY());
            flashlight.setDirection(player.getAngleBetweenObj(player.getVectorPos(), cursorPosition));

            //System.out.println(player.getPositionX() + "      " + cursorToPlayer.x);
        }
        camera.position.set(player.getPositionX(),player.getPositionY(), 0);
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera to follow player (optional)
        camera.position.set(player.getPositionX()+20, player.getPositionY()+40, 0);
        camera.update();
        camera.position.set(HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2,0);


        // --- Draw player and other sprites ---
        sb.setProjectionMatrix(camera.combined);
        sb.begin();

        MapDrawer mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer.render(sb);
        MapDrawer second = new MapDrawer(MapData.MainMapLayer2);
        second.render(sb);
        player.render(sb);


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

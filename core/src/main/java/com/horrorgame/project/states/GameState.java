package com.horrorgame.project.states;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.sprites.Ball;
import com.horrorgame.project.sprites.Chest;
import com.horrorgame.project.sprites.PhysicsSprite;
import com.horrorgame.project.sprites.Player;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.awt.*;
import java.util.ArrayList;
import com.horrorgame.project.Tiles.MapData;
import com.horrorgame.project.Tiles.MapDrawer;

public class GameState extends State{
    private AssetManager manager = new AssetManager();

    // Body category bits
    public static final short CATEGORY_PLAYER = 0x0001; // 1
    public static final short CATEGORY_BALL   = 0x0002; // 2
    public static final short CATEGORY_WORLD  = 0x0004; // 4 (other objects)



    //Cursor Position as Vector2
    private Vector2 cursorPosition = new Vector2();
    private Vector3 cursorToWorldVec = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    private Vector2 cursorToPlayer = new Vector2();

    private int prevMouseX;
    private int prevMouseY;
    private int currentMouseX;
    private int currentMouseY;
    private int deltaX;
    private int deltaY;
    private float distance;
    private float deltaTime;
    private float mouseSpeed;


    private ArrayList<PhysicsSprite> physicsSprites = new ArrayList<>();
    private static Ball ball;
    private static Chest chest;

    private static Player player;

    public final int tileSize = 16;
    private SpriteBatch batch;
    public static OrthographicCamera camera = new OrthographicCamera();

    // --LIGHTING--
    public static World world = new World(new Vector2(0,0), false);
    private RayHandler rayHandler = new RayHandler(world);
    private PointLight ambientLight;

    //Flashlight Initiations
    private ConeLight flashlight;
    private Boolean flashOn = false;
    private Sound flashlight_click;
    private Sound light_hum;

    //Test background and objects
    private Texture background;
    private PointLight tempLight;
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int tileSize;


    public GameState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;


        background = manager.get("onlytheocean-silent-hill-sm.jpeg", Texture.class);
        flashlight_click = manager.get("sounds/objectInteractions/flashlight_click.wav", Sound.class);
        light_hum = manager.get("sounds/objectInteractions/light-hum.mp3", Sound.class);

        short COLLIDE_WITH_ALL = (short)(CATEGORY_PLAYER | CATEGORY_BALL | CATEGORY_WORLD);

        player = new Player("player", new Texture("assets/sprites/idleSprites.png"),
            HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2,80,105);
        assignCategory(player.getBody(), CATEGORY_PLAYER, COLLIDE_WITH_ALL);
        physicsSprites.add(player);

        ball = new Ball("ball", new Texture("assets/sprites/ball.png"),
            HorrorMain.WIDTH/2, (HorrorMain.HEIGHT/2-100),20, false);
        assignCategory(ball.getBody(),   CATEGORY_BALL,   COLLIDE_WITH_ALL);
        physicsSprites.add(ball);

        chest = new Chest("chest", new Texture("assets/sprites/chest.png"),
            HorrorMain.WIDTH/3, HorrorMain.HEIGHT/3, 60, 60, false);
        assignCategory(chest.getBody(),   CATEGORY_BALL,   COLLIDE_WITH_ALL);
        physicsSprites.add(chest);

        camera.viewportWidth = HorrorMain.WIDTH;
        camera.viewportHeight = HorrorMain.HEIGHT;

        //Mouse
        prevMouseX = Gdx.input.getX();
        prevMouseY = Gdx.input.getY();


        /** -----------------LIGHTING-----------------------------------*/
        rayHandler.setCombinedMatrix(camera.combined);
        rayHandler.useDiffuseLight(true); //Stops "draining the life from the colors"/desaturation of textures
        //ambient light color
        //rayHandler.setAmbientLight(new Color(0.1125f, 0.075f, 0.160f, 10f));

        //Actual Flashlight (testing)
        flashlight = new ConeLight(rayHandler, 1000, Color.WHITE, 700, player.getPosition().x, player.getPosition().y, 0, 15);
        flashlight.setActive(false);
        //Player ambient light
        ambientLight = new PointLight(rayHandler, 500, Color.GRAY, 500, player.getPosition().x, player.getPosition().y);
        // Only affect world objects
        Filter lightFilter = new Filter();
        lightFilter.categoryBits = CATEGORY_WORLD; // Light belongs to world
        lightFilter.maskBits     = CATEGORY_WORLD; // Only casts shadows on world
        ambientLight.setContactFilter(lightFilter);

        tempLight = new PointLight(rayHandler, 500, Color.WHITE, 10, ball.getPosition().x, ball.getPosition().y);




        background = new Texture("testBackground.png");
        tileset = new Texture("TileAssets/Tileset.png");
        tileSize = 16;
        tiles = TextureRegion.split(tileset, tileSize, tileSize);
    }

    private void assignCategory(Body body, short category, short mask) {
        for (Fixture fixture : body.getFixtureList()) {
            Filter f = fixture.getFilterData();
            f.categoryBits = category;
            f.maskBits = mask;
            fixture.setFilterData(f);
        }
    }


    @Override
    protected void setDebugMode() {
        debugMode = !debugMode;
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched() && player.hasLight) { // Check if the screen was just touched
            clickFlashlight();
            flashlight_click.play();
        }

        if(Gdx.input.isKeyPressed(Input.Keys.NUM_2) && Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)){
            setDebugMode();
        }
    }

    @Override
    public void update(float dt) { //Logic
        // Update camera to follow player (optional)
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);

        handleInput();
        //For getting cursor X and Y NOT according to camera
        // (otherwise it gets left behind when the player walks)
        cursorToWorldVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(cursorToWorldVec);
        cursorPosition.set(cursorToWorldVec.x, cursorToWorldVec.y);
        cursorToPlayer.set(Gdx.input.getX(), Gdx.input.getY());
        //detecting cursor speed
        currentMouseX = Gdx.input.getX();
        currentMouseY = Gdx.input.getY();
        deltaX = Gdx.input.getX() - prevMouseX;
        deltaY = Gdx.input.getY() - prevMouseY;
        //calculation change in distance of cursor/mouse
        distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        deltaTime = Gdx.graphics.getDeltaTime(); // Time in seconds since last frame

        mouseSpeed = distance / deltaTime;

        prevMouseX = currentMouseX;
        prevMouseY = currentMouseY;

        //Light Updates
        ambientLight.setPosition(player.getPosition().x, player.getPosition().y);
        //ambientLight.setPosition(cursorPosition.x, cursorPosition.y);
        tempLight.setPosition(ball.getPosition().x, ball.getPosition().y);


        player.update(dt);
        ball.update();
        chest.update();
        flashlightUpdate();
        camera.update();
    }

    //METHODS FOR FLASHLIGHT
    //acts as a boolean switch
    public void clickFlashlight(){
        flashOn = !flashOn;
        flashlight.setActive(flashOn);
        light_hum.loop(0.025f);
    }
    //updates flashlight
    private void flashlightUpdate(){
        if(flashOn) {
            player.setDirection(cursorPosition.x <= player.getPosition().x);

                flashlight.setPosition(player.getPosition().x, player.getPosition().y);
                flashlight.setDirection(player.getAngleBetweenObj(player.getPosition(), cursorPosition));

            } else {
                light_hum.stop();
            }
    }

    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(1/60f, 6, 2);

        // --- Draw player and other sprites ---
        sb.setProjectionMatrix(camera.combined);
        sb.begin();
        sb.draw(background, 0, 0, HorrorMain.WIDTH, HorrorMain.HEIGHT);
        player.render(sb);
        ball.render(sb);
        chest.render(sb);
        //sb.draw(background,0,0, HorrorMain.WIDTH,HorrorMain.HEIGHT/2);
        MapDrawer mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer.render(sb);


        sb.end();

        //Light
        if(debugMode) {
            sb.begin();
            for (PhysicsSprite s : physicsSprites) {   // ← every sprite stored here
                Label label = s.getLabel();
                label.setPosition(
                    s.getBody().getPosition().x - s.getBodyWidth() / 2f,
                    s.getBody().getPosition().y - s.getBodyHeight() / 2f - 20
                );
                label.draw(sb, 1f);
            }
            sb.end();
        }else {
            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();
        }

    }


    @Override
    public void dispose() {
        world.dispose();
    }
}

package com.horrorgame.project.states;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
    private boolean cameraDrag = false;
    private FrameBuffer fbo;
    private ShaderProgram shaderProgram;
    private float time;


    //Body category bits
    private Skin skin;

    //Cursor Position as Vector2
    private Vector2 cursorPosition = new Vector2();
    private Vector3 cursorToWorldVec = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    private Vector2 cursorToPlayer = new Vector2();


    private ArrayList<PhysicsSprite> physicsSprites = new ArrayList<>();
    private static Ball ball;
    private static Chest chest;

    private static Player player;
    private final Vector2 cameraTarget = new Vector2();
    private float shakeTimer = 0f;
    private float shakeMagnitude = 1.2f;  // stronger shake
    private float shakeSpeed = 90f;



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
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int tileSize;


    public GameState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        String vertexShader = Gdx.files.internal("shaders/vertex.glsl").readString();
        String fragmentShader = Gdx.files.internal("shaders/crt.glsl").readString();
        shaderProgram = new ShaderProgram(vertexShader,fragmentShader);
        shaderProgram.pedantic = false;

        skin = manager.get("vhsui/vhs-ui.json", Skin.class);
        flashlight_click = manager.get("sounds/objectInteractions/flashlight_click.wav", Sound.class);
        light_hum = manager.get("sounds/objectInteractions/light-hum.mp3", Sound.class);


        player = new Player("player", new Texture("assets/sprites/idleSprites.png"),
            HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2,20,26.25f);
        physicsSprites.add(player);

        ball = new Ball("ball", new Texture("assets/sprites/ball.png"),
            HorrorMain.WIDTH/2, (HorrorMain.HEIGHT/2-100),10, false);
        physicsSprites.add(ball);

        chest = new Chest("chest", new Texture("assets/sprites/chest.png"),
            HorrorMain.WIDTH/3, HorrorMain.HEIGHT/3, 20, 20, false);
        physicsSprites.add(chest);

        camera.viewportWidth = HorrorMain.WIDTH/4;
        camera.viewportHeight = HorrorMain.HEIGHT/4;


        /** -----------------LIGHTING-----------------------------------*/
        rayHandler.setCombinedMatrix(camera.combined);
        rayHandler.useDiffuseLight(true); //Stops "draining the life from the colors"/desaturation of textures
        //ambient light color
        //rayHandler.setAmbientLight(new Color(0.1125f, 0.075f, 0.160f, 10f));

        //Actual Flashlight (testing)
        flashlight = new ConeLight(rayHandler, 1000, Color.WHITE, 200, player.getPosition().x, player.getPosition().y, 0, 15);
        flashlight.setSoftnessLength(80f);
        flashlight.setActive(false);
        //Player ambient light
        ambientLight = new PointLight(rayHandler, 500, Color.GRAY, 130, player.getPosition().x, player.getPosition().y);
        ambientLight.setXray(true);

        tileset = new Texture("TileAssets/Tileset.png");
        tileSize = 16;
        tiles = TextureRegion.split(tileset, tileSize, tileSize);
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
        handleInput();

        //For getting cursor X and Y NOT according to camera
        // (otherwise it gets left behind when the player walks)
        cursorToWorldVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(cursorToWorldVec);
        cursorPosition.set(cursorToWorldVec.x, cursorToWorldVec.y);
        cursorToPlayer.set(Gdx.input.getX(), Gdx.input.getY());

        //Light Updates
        ambientLight.setPosition(player.getPosition().x, player.getPosition().y);
        //ambientLight.setPosition(cursorPosition.x, cursorPosition.y);

        // Follow player but NOT instantly — this creates softness that allows shake to work
        float lerp = 6f;  // increase for tighter following
        cameraTarget.x += (player.getPosition().x - cameraTarget.x) * lerp * dt;
        cameraTarget.y += (player.getPosition().y - cameraTarget.y) * lerp * dt;



        player.update(dt);
        ball.update();
        chest.update();
        flashlightUpdate();
        float shakeOffsetX = 0;
        float shakeOffsetY = 0;

        if (shakeTimer > 0) {
            shakeTimer -= dt;

            float shake = shakeMagnitude * (shakeTimer / 0.15f);
            shakeOffsetX = (float)Math.sin(shakeTimer * shakeSpeed) * shake;
            shakeOffsetY = (float)Math.cos(shakeTimer * shakeSpeed) * shake;
        }

        //Final camera placement
        if(cameraDrag) {camera.position.set(
            cameraTarget.x + shakeOffsetX,
            cameraTarget.y + shakeOffsetY,
            0
        );}else {camera.position.set(player.getPosition().x, player.getPosition().y, 0);}

        camera.update();
        if (player.isTryingToRunWithoutStamina()) {
            shakeTimer = 0.15f;
        }
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
        fbo.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        time+=Gdx.graphics.getDeltaTime();
        sb.setShader(null);

        // pass in the following to the fragment glsl scripts
        Vector2 v = new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        v.x = v.x / Gdx.graphics.getWidth();
        v.y = v.y / Gdx.graphics.getHeight();
        shaderProgram.setUniformf("center", v);
        shaderProgram.setUniformf("u_time", time);
        shaderProgram.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        sb.setProjectionMatrix(camera.combined);
        sb.begin();
        // your existing world & sprites
        world.step(1/60f, 6, 2);

        // --- Draw player and other sprites ---
        MapDrawer mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer.render(sb);
        player.render(sb);
        ball.render(sb);
        chest.render(sb);

        sb.end();
        fbo.end();

        if(player.isTired) {
            sb.setShader(shaderProgram);
        }else sb.setShader(null);
            sb.setProjectionMatrix(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()).combined);

            sb.begin();
            Texture fboTexture = fbo.getColorBufferTexture();
            sb.draw(
                fboTexture,
                -HorrorMain.WIDTH / 2, -HorrorMain.HEIGHT / 2,                                  // x, y
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),  // width, height
                0, 0,                                  // srcX, srcY
                fboTexture.getWidth(), fboTexture.getHeight(),     // srcWidth, srcHeight
                false, true                             // flipX, flipY
            );

            sb.end();

        if(debugMode) {
            sb.begin();
            for (PhysicsSprite s : physicsSprites) {   // ← every sprite stored here
                Label label = s.getLabel();
                label.setPosition(
                    s.getBody().getPosition().x-s.getBodyWidth()/2, s.getBody().getPosition().y);
                label.setFontScale(0.3f);
                label.draw(sb, 1f);
            }
            Label debugInfo = new Label("Press 2 and 7 to exit debugMode", skin);
            debugInfo.setFontScale(0.2f); debugInfo.setPosition(camera.position.x - 157, camera.position.y + 70);
            debugInfo.draw(sb, 1f);
            sb.end();
        }else {//Light disabled during debugMode
            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();
        }

    }


    @Override
    public void dispose() {
        world.dispose();
        shaderProgram.dispose();
    }
}

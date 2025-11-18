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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
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
    private FrameBuffer fbo;
    private ShaderProgram shaderProgram;
    private float time;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    //camera "dragging behind" (lerping) player and screen effects (CRT Monitor curvature when stamina's empty)
    //could later be placed in settings (to be placed somewhere else later as global variables)
    private boolean cameraDrag = false;
    private boolean doScreenEffects = true;
    private float tiredShaderIntensity = 0f;


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

    //  Collision
    private Array<Rectangle> bounds = new Array<>();


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

        createBounds(bounds);

    }

    private void createBounds(Array<Rectangle> bounds) {
        //Map hitboxes
        bounds.add(new com.badlogic.gdx.math.Rectangle(0,0, HorrorMain.WIDTH, 112)); // bottom
        bounds.add(new com.badlogic.gdx.math.Rectangle(0,0, 96, HorrorMain.HEIGHT)); // left
        bounds.add(new com.badlogic.gdx.math.Rectangle(80, (HorrorMain.HEIGHT - 64), 448, 64)); // top left
        bounds.add(new com.badlogic.gdx.math.Rectangle(656, (HorrorMain.HEIGHT - 64) , 368, 64)); // top right
        bounds.add(new com.badlogic.gdx.math.Rectangle(848, 80, 128, 80)); // lower right corner
        bounds.add(new com.badlogic.gdx.math.Rectangle(992, 160, 192, 224));  // under bridge
        bounds.add(new com.badlogic.gdx.math.Rectangle(992, 464, 192, 208)); // above bridge

        bounds.add(new Rectangle(544, HorrorMain.HEIGHT - 64, 80, 32)); // EXIT (Must be last)
    }


    @Override
    protected void setDebugMode() {
        debugMode = !debugMode;
    }

    @Override
    protected void handleInput() {
        //Debug
        if(Gdx.input.isKeyPressed(Input.Keys.NUM_2) && Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)){
            setDebugMode();
        }

        //Flashlight
        if (Gdx.input.justTouched() && player.checkInventory(0) == 1) { // Check if the screen was just touched
            clickFlashlight();
            flashlight_click.play();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.F)){   // F to equip flashlight
            player.setItem(0, 1);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){player.getBody().applyLinearImpulse(-1000,1000, player.getX(),player.getY(),true);}
    }

    @Override
    public void update(float dt) { //Logic
        handleInput();

        // Smoothly approach target intensity
        float target = player.isTired ? 1f : 0f;
        float speed = 2f; // how fast the shader ramps up/down
        tiredShaderIntensity += (target - tiredShaderIntensity) * dt * speed;


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

        //Hitbox collisions
        player.update(dt);

        float lastVelx = 0f;

        for (Rectangle bound : new Array.ArrayIterator<>(bounds)) {

            if (player.collides(bound)) {

                if (player.getVelX() > 0) {           // hit right wall
                    player.movementLocked = true;
                    player.getBody().applyLinearImpulse(-10000f, 0f,
                        player.getPosition().x, player.getPosition().y, true);
                }
                else if (player.getVelX() < 0) {      // hit left wall
                    player.movementLocked = true;
                    player.getBody().applyLinearImpulse(10000f, 0f,
                        player.getPosition().x, player.getPosition().y, true);
                }
                else if (player.getVelY() > 0) {      // hit top wall
                    player.movementLocked = true;
                    player.getBody().applyLinearImpulse(0f, -10000f,
                        player.getPosition().x, player.getPosition().y, true);
                }
                else if (player.getVelY() < 0) {      // hit bottom wall
                    player.movementLocked = true;
                    player.getBody().applyLinearImpulse(0f, 10000f,
                        player.getPosition().x, player.getPosition().y, true);
                }
            }

        }

        ball.update();
        chest.update();
        flashlightUpdate();


        //Final camera placement
        if(cameraDrag) {camera.position.set(
            cameraTarget.x,
            cameraTarget.y,
            0);
        }else {camera.position.set(player.getPosition().x, player.getPosition().y, 0);}

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

        // -------------------------------------------------------
        // 1. DRAW WORLD INTO FBO (the framebufferer)
        // -------------------------------------------------------
        fbo.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        time += Gdx.graphics.getDeltaTime();

        sb.setShader(null); // Always render world normally inside FBO
        sb.setProjectionMatrix(camera.combined);

        sb.begin();

        world.step(1/60f, 6, 2);

        // Draw world
        MapDrawer mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer.render(sb);
        MapDrawer second = new MapDrawer(MapData.MainMapLayer2);
        second.render(sb);
        if(!debugMode) {
            player.render(sb);
            ball.render(sb);
            chest.render(sb);
        }
        sb.end();
        fbo.end();


        // -------------------------------------------------------
        // 2. APPLY SCREEN SHADER OR NOT (based on tired status)
        // -------------------------------------------------------

        if (doScreenEffects) {

            //Configure shader uniforms
            Vector2 center = new Vector2(
                (float)Gdx.graphics.getWidth() / 2f / Gdx.graphics.getWidth(),
                (float)Gdx.graphics.getHeight() / 2f / Gdx.graphics.getHeight()
            );

            shaderProgram.bind();
            shaderProgram.setUniformf("u_tiredIntensity", tiredShaderIntensity/1.7f);
            shaderProgram.setUniformf("center", center);
            shaderProgram.setUniformf("u_time", time);
            shaderProgram.setUniformf("u_resolution",
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            sb.setShader(shaderProgram);

        } else {
            sb.setShader(null);
        }


        // -------------------------------------------------------
        // 3. DRAW FBO TO SCREEN (fullscreen pass)
        // -------------------------------------------------------
        OrthographicCamera screenCam =
            new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenCam.position.set(0, 0, 0);
        screenCam.update();

        sb.setProjectionMatrix(screenCam.combined);
        sb.begin();

        Texture tex = fbo.getColorBufferTexture();

        sb.draw(
            tex,
            -Gdx.graphics.getWidth() / 2f,
            -Gdx.graphics.getHeight() / 2f,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            0, 0,
            tex.getWidth(), tex.getHeight(),
            false, true
        );

        sb.end();


        // -------------------------------------------------------
        // 4. DEBUG OR LIGHTING
        // -------------------------------------------------------
        if (debugMode) {
            sb.setShader(null);
            sb.setProjectionMatrix(camera.combined);
            sb.begin();

            for (PhysicsSprite s : physicsSprites) {
                Label label = s.getLabel();
                label.setPosition(
                    s.getBody().getPosition().x - s.getBodyWidth() / 2f,
                    s.getBody().getPosition().y
                );
                label.setFontScale(0.3f);
                label.draw(sb, 1f);
            }

            Label debugInfo = new Label("Press 2 and 7 to exit debugMode", skin);
            debugInfo.setFontScale(0.2f);
            debugInfo.setPosition(camera.position.x - 157, camera.position.y + 70);
            debugInfo.draw(sb, 1f);

            player.render(sb);
            ball.render(sb);
            chest.render(sb);

            sb.end();

            // ---- DRAW PHYSICS OUTLINES ----
            Gdx.gl.glLineWidth(2); // Optional thickness

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            shapeRenderer.setColor(Color.RED); // Outline color

            for (PhysicsSprite s : physicsSprites) {
                float x = s.getBody().getPosition().x - s.getBodyWidth() / 2f;
                float y = s.getBody().getPosition().y - s.getBodyHeight() / 2f;
                float w = s.getBodyWidth();
                float h = s.getBodyHeight();

                shapeRenderer.rect(x, y, w, h);
            }
            Gdx.gl.glLineWidth(6);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(player.getHitbox().x, player.getHitbox().y, player.getHitbox().width, player.getHitbox().height);
            shapeRenderer.end();

        } else {
            //Lights activated when outside debugMOde
            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();
        }
    }



    @Override
    public void dispose() {
        world.dispose();
        shaderProgram.dispose();
    }

    @Override
    public void resize(int width, int height) {

    }
}

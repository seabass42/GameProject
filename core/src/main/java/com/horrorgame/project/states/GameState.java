package com.horrorgame.project.states;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Text.RPGText;
import com.horrorgame.project.sprites.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.ArrayList;
import java.util.Random;

import com.horrorgame.project.Tiles.MapData;
import com.horrorgame.project.Tiles.MapDrawer;

public class GameState extends State{
    private AssetManager manager = new AssetManager();
    private FrameBuffer fbo;

    private ShaderProgram crtShaderProgram = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl").readString(),
            Gdx.files.internal("shaders/crt.glsl").readString());
    private ShaderProgram monochromeShaderProgram = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl").readString(),
        Gdx.files.internal("shaders/blackandwhite.glsl").readString());
    private float time;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    //camera "dragging behind" (lerping) player and screen effects (CRT Monitor curvature when stamina's empty)
    //could later be placed in settings (to be placed somewhere else later as global variables)
    private boolean cameraDrag = false;
    private boolean doScreenEffects = true;
    private float tiredShaderIntensity = 0f;
    private boolean doJumpScares = true;
    private boolean jumpScarePlayed = false;

    //Body category bits
    private MapDrawer mapDrawer, mapDrawer2;
    private Stage stage;
    private Skin textSkin;

    //Cursor Position as Vector2
    private Vector2 cursorPosition = new Vector2();
    private Vector3 cursorToWorldVec = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    private Vector2 cursorToPlayer = new Vector2();

    private ArrayList<PhysicsSprite> physicsSprites = new ArrayList<>();
    private static Ball ball;
    private static Log log;
    private static Log log2;
    public static Player player;
    private static Eye leftEye;
    private static Eye rightEye;
    private static Texture eyesTexture;
    private Sound eyeSound, jumpScare, openHouseDoor;
    private final Vector2 cameraTarget = new Vector2();

    //Trees
    private Random random = new Random();
    private static Tree[] tree = new Tree[15];

    //private SpriteBatch batch;
    public static OrthographicCamera camera = new OrthographicCamera();

    public static World world = new World(new Vector2(0,0), false);
    private RayHandler rayHandler = new RayHandler(world);
    private PointLight ambientLight;

    private ConeLight flashlight;
    private Boolean flashOn = false;
    private Sound flashlight_click, light_hum;

    private Music ambience, lakeAmbience;
    private Rectangle lakeRange1 = new Rectangle(816, 0, 96, HorrorMain.HEIGHT);
    private Rectangle lakeRange2 = new Rectangle(912, 0, 368, HorrorMain.HEIGHT);

    //Test background and objects
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int tileSize;

    //  Collision
    private Array<Rectangle> bounds = new Array<>();
    private Texture house, bunker;
    private final int HOUSE_HEIGHT = 105;
    private final int HOUSE_WIDTH = 116;
    private final int BUNKER_WIDTH = 160;
    private final int BUNKER_HEIGHT = 120;
    public static boolean houseLocked = false;

    private RPGText introText, houseLockedText;
    //Player capabilities
    //Log Bridge
    private boolean logBridge = false;


    public GameState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;
        mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer2 = new MapDrawer(MapData.MainMapLayer2);
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        Texture tex = fbo.getColorBufferTexture();
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);


        //Shaders
        crtShaderProgram.pedantic = false;
        monochromeShaderProgram.pedantic = false;


        textSkin = manager.get("vhsui/vhs-ui.json", Skin.class);
        flashlight_click = manager.get("sounds/objectInteractions/flashlight_click.wav", Sound.class);
        light_hum = manager.get("sounds/objectInteractions/light-hum.mp3", Sound.class);

        house = manager.get("House/House.png", Texture.class);
        bunker = manager.get("Bunker/Bunker.png", Texture.class);
        player = new Player("player", new Texture("assets/sprites/idleSprites.png"),
            HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2,15,30);
        physicsSprites.add(player);

        ball = new Ball("ball", new Texture("assets/sprites/ball.png"),
            HorrorMain.WIDTH/2, (HorrorMain.HEIGHT/2-100),10, false);
        physicsSprites.add(ball);

        log = new Log("log", new Texture("assets/sprites/log.png"),
            HorrorMain.WIDTH/3, HorrorMain.HEIGHT/3, 60, 20, false);
        physicsSprites.add(log);

        log2 = new Log("log2", new Texture("assets/sprites/log.png"),
            HorrorMain.WIDTH/3+50, HorrorMain.HEIGHT/3-100, 60, 20, false);
        physicsSprites.add(log2);

        leftEye = new Eye(HorrorMain.WIDTH / 2 - 50, HorrorMain.HEIGHT / 2);
        physicsSprites.add(leftEye);
        rightEye = new Eye(HorrorMain.WIDTH / 2, HorrorMain.HEIGHT / 2);
        physicsSprites.add(rightEye);

        //TREES
        for(int i = 0; i < tree.length; i++) {
            tree[i] = new Tree("tree", HorrorMain.WIDTH / 2 - random.nextInt(600), HorrorMain.HEIGHT / 2 - random.nextInt(300));
            physicsSprites.add(tree[i]);
        }

        eyeSound = manager.get("sounds/gnid.ogg", Sound.class);
        eyesTexture = new Texture("assets/eyes.jpg");
        jumpScare = manager.get("sounds/eyeScare.wav", Sound.class);
        openHouseDoor = manager.get("sounds/door_open.mp3", Sound.class);

        camera.viewportWidth = HorrorMain.WIDTH/4;
        camera.viewportHeight = HorrorMain.HEIGHT/4;
        stage = new Stage(new ScreenViewport());

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

        introText = new RPGText("There must be a way out.", textSkin);
        houseLockedText = new RPGText("It is unreasonable to return.", textSkin);

        stage.addActor(introText);
        ambience = Gdx.audio.newMusic(Gdx.files.internal("GameStateMusic/outsideambience.mp3"));
        lakeAmbience = Gdx.audio.newMusic(Gdx.files.internal("GameStateMusic/lakeambience.mp3"));
        lakeAmbience.setVolume(0.3f);


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
        if(Gdx.input.isKeyJustPressed(Input.Keys.F)){   // F to equip flashlight
            player.setItem(0, 1);
        }

    }

    @Override
    public void update(float dt) { //Logic
        handleInput();

        ambience.play();
        ambience.setLooping(true);

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
        float lerp = 0.3f;  // increase for tighter following
        cameraTarget.x += (player.getPosition().x - cameraTarget.x) * lerp * dt;
        cameraTarget.y += (player.getPosition().y - cameraTarget.y) * lerp * dt;

        /*-------------------------HITBOX COLLISIONS--------------------*/
        player.update(dt);


        for (Rectangle bound : bounds){     // Check X collision
                if (player.collidesLeft(bound)) {  //Walking left into boundary
                    player.getBody().setTransform(bound.x + bound.width + player.getWidth()/2, player.getPosition().y, 0);
                    player.setVelocity(0, player.getVelY());
                } else if (player.collidesRight(bound)) { //Walking right into boundary
                    player.getBody().setTransform(bound.x - player.getWidth() / 2, player.getPosition().y, 0);
                    player.setVelocity(0, player.getVelY());
                }
                if (player.collidesUp(bound)) {    // Check Y collision   //walking Up into boundary
                    player.getBody().setTransform(player.getPosition().x, bound.y - player.getHeight() / 2, 0);
                    player.setVelocity(player.getVelX(), 0);
                } else if (player.collidesDown(bound)) {    //Walking down into boundary
                    player.getBody().setTransform(player.getPosition().x, bound.y + bound.height + player.getHeight()/2, 0);
                    player.setVelocity(player.getVelX(), 0);
            }

        }

        ball.update();
        log.update();
        log2.update();
        if(leftEye != null && rightEye != null) {
            leftEye.update();
            rightEye.update();
        }
        flashlightUpdate();


        //Final camera placement
        if(cameraDrag) {camera.position.set(
            cameraTarget.x,
            cameraTarget.y,
            0);
        }else {camera.position.set(player.getPosition().x, player.getPosition().y, 0);}

        camera.update();

        //THE HAUNTING EYES MIRAGE (still working on them)
        eyesMirageUpdate();
        //HOUSE
        if (!houseLocked) {
            if (entry(new HouseState(gsm, manager, player), 134 + (HOUSE_WIDTH / 2), 560, 8, 16)){
                openHouseDoor.play();
            }
        }
        entry(new BunkerState(gsm, manager, player),600, 228, 48, 32);


        if (player.collidesRight(lakeRange1)){
            lakeAmbience.setVolume(0.15f);
            lakeAmbience.play();
        }
        else if (player.collidesRight(lakeRange2)){
            lakeAmbience.setVolume(0.4f);
        }
        else{
            lakeAmbience.pause();
        }

        if(player.getPosition().x > 1190 && !logBridge) {
            player.getBody().setTransform(1190, player.getPosition().y, 0);
        }
        if(log.getPosition().x > 1220 && log.getPosition().y > 400){
            System.out.println("Hello");
            logBridge = true;
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
            //player.setDirection(cursorPosition.x <= player.getPosition().x);

            if (player.getAngleBetweenObj(cursorPosition) <= 45 || player.getAngleBetweenObj(cursorPosition) >= 315){
                player.setDirection(Player.Direction.RIGHT);
            }else if (player.getAngleBetweenObj(cursorPosition) <= 135){
                player.setDirection(Player.Direction.UP);
            }else if (player.getAngleBetweenObj(cursorPosition) <= 225){
                player.setDirection(Player.Direction.LEFT);
            }else if (player.getAngleBetweenObj(cursorPosition) <= 315){
                player.setDirection(Player.Direction.DOWN);
            }

                flashlight.setPosition(player.getPosition().x, player.getPosition().y);
                flashlight.setDirection(player.getAngleBetweenObj(cursorPosition));

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
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        time += Gdx.graphics.getDeltaTime();

        sb.setShader(null); // Always render world normally inside FBO
        sb.setProjectionMatrix(camera.combined);

        sb.begin();

        world.step(1/60f, 6, 2);

        // Draw world
        mapDrawer.render(sb);
        sb.draw(house, 144, 544, HOUSE_WIDTH, HOUSE_HEIGHT);
        sb.draw(bunker, 544, 192, BUNKER_WIDTH, BUNKER_HEIGHT);
        if(!debugMode) {
            for(PhysicsSprite sprite : physicsSprites) {
                sprite.render(sb);
            }
        }
        mapDrawer2.render(sb);
        eyesMirageRender(sb);
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

            crtShaderProgram.bind();
            crtShaderProgram.setUniformf("u_tiredIntensity", tiredShaderIntensity/1.7f);
            crtShaderProgram.setUniformf("center", center);
            crtShaderProgram.setUniformf("u_time", time);
            crtShaderProgram.setUniformf("u_resolution",
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            sb.setShader(crtShaderProgram);

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
                s.render(sb);
                label.draw(sb, 1f);
            }

            Label debugInfo = new Label("Press '2' and '=' to exit debugMode", textSkin);
            debugInfo.setFontScale(0.2f);
            debugInfo.setPosition(camera.position.x - 157, camera.position.y + 70);
            debugInfo.draw(sb, 1f);

            sb.end();

            // ---- DRAW PHYSICS OUTLINES ----
            Gdx.gl.glLineWidth(2); // Optional thickness

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.rect(600, 228, 48, 32);

            shapeRenderer.setColor(Color.RED); // Outline color

            for (PhysicsSprite s : physicsSprites) {
                float x = s.getBody().getPosition().x - s.getBodyWidth() / 2f;
                float y = s.getBody().getPosition().y - s.getBodyHeight() / 2f;
                float w = s.getBodyWidth();
                float h = s.getBodyHeight();
                shapeRenderer.rect(x, y, w, h);
            }
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(player.getHitboxLeft().x, player.getHitboxLeft().y, player.getHitboxLeft().width, player.getHitboxLeft().height);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(player.getHitboxRight().x, player.getHitboxRight().y, player.getHitboxRight().width, player.getHitboxRight().height);
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(player.getHitboxUp().x, player.getHitboxUp().y, player.getHitboxUp().width, player.getHitboxUp().height);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(player.getHitboxDown().x, player.getHitboxDown().y, player.getHitboxDown().width, player.getHitboxDown().height);
            shapeRenderer.end();

        } else {
            //Lights activated when outside debugMOde
            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();

        }
        // Show text
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void createBounds(Array<Rectangle> bounds){
        bounds.add(new Rectangle(0,0, HorrorMain.WIDTH, 96)); // bottom
        bounds.add(new Rectangle(0,0, 96, HorrorMain.HEIGHT)); // left
        bounds.add(new Rectangle(80, (HorrorMain.HEIGHT - 80), 448, 64)); // top left
        bounds.add(new Rectangle(656, (HorrorMain.HEIGHT - 80) , 368, 64)); // top right
        bounds.add(new Rectangle(848, 80, 128, 80)); // lower right corner
        bounds.add(new Rectangle(992, 160, 192, 224));  // under bridge
        bounds.add(new Rectangle(992, 464, 192, 208)); // above bridge
        bounds.add(new Rectangle(150, 560, HOUSE_WIDTH - 16, HOUSE_HEIGHT)); // House
        bounds.add(new Rectangle(130 + (HOUSE_WIDTH / 2), 560, 8,16)); // House door
        bounds.add(new Rectangle(1200, 0, 16, HorrorMain.HEIGHT)); // End bridge
        bounds.add(new Rectangle(568, 228, BUNKER_WIDTH - 48, BUNKER_HEIGHT/2f)); // Bunker

        bounds.add(new Rectangle(544, HorrorMain.HEIGHT - 80, 96, 32)); // EXIT (MUST be last)
    }

    public void eyesMirageUpdate(){
        if(doJumpScares && player.tiredCount == 5 && player.isTired) {
            if(flashOn && player.getAngleBetweenObj(cursorPosition) < player.getAngleBetweenObj(leftEye.getPosition())+45
                && player.getAngleBetweenObj(cursorPosition) > player.getAngleBetweenObj(rightEye.getPosition())-45) {
                flashlight.setColor(Color.WHITE);
                leftEye.setOpacity(leftEye.getPosition().dst(player.getPosition()) / 100);
                rightEye.setOpacity(leftEye.getPosition().dst(player.getPosition()) / 100);
            }else {
                flashlight.setColor(1, 1, 1, leftEye.getPosition().dst(player.getPosition()) / 1000);
                leftEye.getBody().setTransform(cameraTarget.x, cameraTarget.y, 0);
            }
            eyeSound.play(0.1f);
        }else {
            flashlight.setColor(Color.WHITE);
            eyeSound.stop();
            leftEye.getBody().setTransform(player.getPosition().x - 1000, player.getPosition().y - 1000, 0);
        }
        rightEye.getBody().setTransform(leftEye.getPosition().x-40, leftEye.getPosition().y,0);

    }
    public void eyesMirageRender(SpriteBatch sb){
        if(doJumpScares&& leftEye.getPosition().dst(player.getPosition()) < 30) {
            sb.setShader(monochromeShaderProgram);
            sb.setColor(1,1,1,0.5f);
            sb.draw(eyesTexture, player.getPosition().x-HorrorMain.WIDTH/8, player.getPosition().y-HorrorMain.HEIGHT/8,
                eyesTexture.getWidth()/2, eyesTexture.getHeight()/2);
            sb.setColor(1,1,1,1);
            if (!jumpScarePlayed) {
                jumpScarePlayed = true;
                jumpScare.play(1f);   // play ONCE
            }

        } else {
            jumpScarePlayed = false;
            //eyesTexture.dispose();// Reset when player leaves scare
        }
    }

    @Override
    public void resize(int width, int height){
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    }
    private boolean entry(State state, int x, int y, int width, int height){ // If rectangle is an entry point (16w x 16h), push requested state
        if (player.collidesUp(new Rectangle(x,y, width, height))){
            ambience.pause();
            gsm.push(state);
            return true;
        }
        return false;
    }
    @Override
    public void dispose() { // Prevent memory leaks!
        rayHandler.dispose();
        world.dispose();
        flashlight.dispose();
        ambientLight.dispose();
        manager.dispose();
        stage.dispose();
        ambience.dispose();
        lakeAmbience.dispose();
    }
}

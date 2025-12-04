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
import com.badlogic.gdx.graphics.g2d.Sprite;
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
    private MapDrawer mapDrawer, mapDrawer2, finalMapDrawer;
    private Stage stage;
    private Skin textSkin;

    //Cursor Position as Vector2
    private Vector2 cursorPosition = new Vector2();
    private Vector3 cursorToWorldVec = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    private Vector2 cursorToPlayer = new Vector2();

    private ArrayList<PhysicsSprite> physicsSprites = new ArrayList<>();
    private static Log log, sign;
    public static Player player;
    private static Eye leftEye, rightEye;
    private static Texture eyesTexture;
    private static Sprite pupil = new Sprite(new Texture("assets/sprites/pupil.png"));;
    private Sound eyeSound, jumpScare, openHouseDoor;
    private final Vector2 cameraTarget = new Vector2();

    //Trees
    private Random random = new Random(45);
    private static ArrayList<Tree> trees = new ArrayList<>();
    private static ArrayList<Tree> trees2 = new ArrayList<>();

    //private SpriteBatch batch;
    public static OrthographicCamera camera = new OrthographicCamera();

    public static World world = new World(new Vector2(0,0), false);
    private RayHandler rayHandler = new RayHandler(world);
    private PointLight ambientLight;


    //Flashlight
    private Texture flashlightObj = new Texture(Gdx.files.internal("assets/sprites/flashlight.png"));
    private ConeLight flashlight;
    private Boolean flashOn = false;
    private Sound flashlight_click, light_hum;

    private Texture crowbar = new Texture(Gdx.files.internal("assets/sprites/crowbar.png"));
    private Texture streetLight = new Texture(Gdx.files.internal("assets/sprites/streetLight.png"));
    private ConeLight streetlight;

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
    public static boolean houseLocked = true;
    public static boolean bunkerLocked = true;
    private boolean exitOpen = false;

    private RPGText introText, houseLockedText, exitFenceText, houseNeedsKeyText;
    private Rectangle fenceExit, houseDoor, initiateEnding;
    private Texture demoEndingScene;
    Label hint;
    private boolean gaveHint = false;
    private boolean gaveHintSign = false;
    private int hintOn = 0;

    //Player capabilities
    //Log Bridge
    private boolean logBridge = false;
    private Texture waterlog;
    int seed = 0;
    private ArrayList<Eye> eyes = new ArrayList<>();
    boolean endingTime = false;

    public GameState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;
        mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer2 = new MapDrawer(MapData.MainMapLayer2);
        finalMapDrawer = new MapDrawer(MapData.ExitOpenMap);


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

        sign = new Log("sign", new Texture("assets/sprites/funSign.png"),
        HorrorMain.WIDTH/3-200, HorrorMain.HEIGHT/3-150, 40,14, true);
        physicsSprites.add(sign);

        log = new Log("log", new Texture("assets/sprites/log.png"),
            HorrorMain.WIDTH/3-120, HorrorMain.HEIGHT/3, 60, 20, false);
        physicsSprites.add(log);

        leftEye = new Eye(HorrorMain.WIDTH / 2 - 50, HorrorMain.HEIGHT / 2);
        physicsSprites.add(leftEye);
        rightEye = new Eye(HorrorMain.WIDTH / 2, HorrorMain.HEIGHT / 2);
        physicsSprites.add(rightEye);

        //TREES
        for(int i = 0; i < 30; i++) {   //Bottom Trees
            trees.add(new Tree("tree" + i, HorrorMain.WIDTH/2+200 - random.nextInt(700), HorrorMain.HEIGHT / 2 - random.nextInt(300)));
        }
        trees.sort((a, b) -> Float.compare(b.getPosition().y, a.getPosition().y));

        for(int i = 0; i < 9; i++) {   //Top Trees
            trees2.add(new Tree("tree" + i, HorrorMain.WIDTH/2+250 - random.nextInt(605), HorrorMain.HEIGHT / 2+250 - random.nextInt(70)));
        }
        trees2.sort((a, b) -> Float.compare(b.getPosition().y, a.getPosition().y));

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

        streetlight = new ConeLight(rayHandler, 1000, Color.GOLD, 130, 1180, 515, 260, 15);

        tileset = new Texture("TileAssets/Tileset.png");
        tileSize = 16;
        tiles = TextureRegion.split(tileset, tileSize, tileSize);

        fenceExit = new Rectangle(544, HorrorMain.HEIGHT - 80, 118, 32);
        houseDoor = new Rectangle(136 + (HOUSE_WIDTH / 2), 560, 16,16);
        initiateEnding = new Rectangle(560, HorrorMain.HEIGHT - 32, 112, 48);
        createBounds(bounds);

        introText = new RPGText("There must be a way out.", textSkin);
        houseLockedText = new RPGText("It is unreasonable to return.", textSkin);
        exitFenceText = new RPGText("Maybe with wire cutters, escape is possible", textSkin);
        houseNeedsKeyText = new RPGText("Need a key to get in.", textSkin);

        stage.addActor(introText);
        ambience = Gdx.audio.newMusic(Gdx.files.internal("GameStateMusic/outsideambience.mp3"));
        lakeAmbience = Gdx.audio.newMusic(Gdx.files.internal("GameStateMusic/lakeambience.mp3"));
        lakeAmbience.setVolume(0.3f);

        hint = new Label("", textSkin);
        hint.setFontScale(0.2f);

        demoEndingScene = manager.get("EndingJumpscare.png", Texture.class);

        //Test Ending
        //exitOpen = true;
        //bounds.removeIndex(bounds.size - 1);
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
        if(debugMode && Gdx.input.isKeyJustPressed(Input.Keys.K)){
            seed++;
            random = new Random(seed);
            for(Tree tree : trees) {
                tree.getBody().setTransform(HorrorMain.WIDTH /2 + 200 - random.nextInt(700), HorrorMain.HEIGHT / 2 - random.nextInt(300),0);
            }
            trees.sort((a, b) -> Float.compare(b.getPosition().y, a.getPosition().y));
            System.out.println(seed);
        }

        //Flashlight
        if (Gdx.input.justTouched() && player.checkInventory(0) == 1) { // Check if the screen was just touched
            clickFlashlight();
            flashlight_click.play();
        }
        if (player.canCutFence() && player.collidesUp(fenceExit) && Gdx.input.isKeyJustPressed(Input.Keys.F)){ // End the game!!
            exitFenceText.removeEarly();
            exitOpen = true;
            bounds.removeIndex(bounds.size - 1);
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
        if(log!=null) log.update();
        for(Tree tree : trees){
            tree.update();
        }
        for(Tree tree : trees2){
            tree.update();
        }
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

        for(Eye eye : eyes){
            eye.update();
        }
        if(player.checkInventory(3)==1){
            if(endingTime) {
                for(int i = 0; i < 10; i++){
                    eyes.add(new Eye(player.getPosition().x - random.nextInt(50), player.getPosition().y+600 - random.nextInt(50)));
                    physicsSprites.add(eyes.get(i));
                    endingTime = false;
                }
            }
        }

        //HOUSE
        if (!bunkerLocked) {
            entry(BunkerState.class, 600, 228, 48, 32);

        }
        if (!houseLocked) {
            if (entry(HouseState.class, 134 + (HOUSE_WIDTH / 2), 560, 8, 16)){
                openHouseDoor.play();
            }
        } else if (player.collidesUp(houseDoor)){
            stage.addActor(houseNeedsKeyText);
        }


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

        //LOG BRIDGE
        if(player.getPosition().x > 1050 && !logBridge) {
            player.getBody().setTransform(1050, player.getPosition().y, 0);
            if(!gaveHint) {
                stage.addActor(new RPGText("Need something to get across...", textSkin));
                gaveHint = true;
            }
        }
        if(log!=null&&log.getPosition().x > 1070 && log.getPosition().y > 350){
            log.getBody().setTransform(-1000, -1000, 0);
            physicsSprites.remove(log);
            log = null;
            System.out.println("Hello");
            logBridge = true;
            waterlog = new Texture(Gdx.files.internal("assets/sprites/logBridge.png"));
        }
        if(logBridge && player.getPosition().x > 1150 && player.checkInventory(1) != 1){
            hint.setPosition(1150, 390);
            hint.setText("'F' to equip");
            if(Gdx.input.isKeyJustPressed(Input.Keys.F)){   // F to equip flashlight
                player.setItem(1, 1);
                hint.setText("");
                crowbar = null;
                bunkerLocked = false;
            }
        }

        if (!exitOpen && player.collidesUp(fenceExit)){
            stage.addActor(exitFenceText);
        }
        if (player.checkInventory(3) == 1){
            houseLocked = false;
        }
        if (exitOpen && player.collidesUp(initiateEnding)){
            Gdx.app.exit();
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
        if(player.getPosition().x < HorrorMain.WIDTH/2-30
            && player.getPosition().x > HorrorMain.WIDTH/2-58
            && player.getPosition().y < HorrorMain.HEIGHT/2-170
            && player.getPosition().y > HorrorMain.HEIGHT/2-200
        && player.checkInventory(0) != 1)
        {
            hint.setPosition(HorrorMain.WIDTH/2-58, HorrorMain.HEIGHT/2-180);
            hint.setText("'F' to equip");
            if(Gdx.input.isKeyJustPressed(Input.Keys.F)){   // F to equip flashlight
                player.setItem(0, 1);
                hint.setText("");
                flashlightObj = null;
            }
        }
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
        if(waterlog!=null) sb.draw(waterlog, 1055, 390, 50, 20);
        sb.draw(streetLight, 1170, 440,streetLight.getWidth()/1.2f,streetLight.getHeight()/1.2f);
        if(!debugMode) {
            for(PhysicsSprite sprite : physicsSprites) {
                sprite.render(sb);
            }

            for(Tree tree : trees) {
                tree.render(sb);
            }
        }
        if (!exitOpen) {
            mapDrawer2.render(sb);
        } else {
            finalMapDrawer.render(sb);
        }
        mapDrawer2.render(sb);
        for(Tree tree : trees2) {
            tree.render(sb);
        }
        eyesMirageRender(sb);
        if(flashlightObj!= null) sb.draw(flashlightObj,HorrorMain.WIDTH/2-50, HorrorMain.HEIGHT/2-190, 7, 21);
        if(crowbar!=null) sb.draw(crowbar, 1150, 400, crowbar.getWidth()/12, crowbar.getHeight()/12);
        hint.draw(sb, 1f);
        pupil.draw(sb);
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
            for(Tree tree : trees) {
                Label label = tree.getLabel();
                label.setPosition(
                    tree.getBody().getPosition().x - tree.getBodyWidth() / 2f,
                    tree.getBody().getPosition().y
                );
                label.setFontScale(0.3f);
                tree.render(sb);
                label.draw(sb, 1f);
            }
            for(Tree tree : trees2) {
                Label label = tree.getLabel();
                label.setPosition(
                    tree.getBody().getPosition().x - tree.getBodyWidth() / 2f,
                    tree.getBody().getPosition().y
                );
                label.setFontScale(0.3f);
                tree.render(sb);
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
            for(Tree tree : trees) {
                float x = tree.getBody().getPosition().x - tree.getBodyWidth() / 2f;
                float y = tree.getBody().getPosition().y - tree.getBodyHeight() / 2f;
                float w = tree.getBodyWidth();
                float h = tree.getBodyHeight();
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
        bounds.add(new Rectangle(992, 160, 200, 224));  // under bridge
        bounds.add(new Rectangle(992, 464, 200, 208)); // above bridge
        bounds.add(new Rectangle(1055, 435, 50, 192)); // above log bridge
        bounds.add(new Rectangle(1055, 390, 50, 10)); //below log bridge
        bounds.add(new Rectangle(150, 560, HOUSE_WIDTH - 16, HOUSE_HEIGHT)); // House
        bounds.add(new Rectangle(houseDoor));
        bounds.add(new Rectangle(1200, 0, 16, HorrorMain.HEIGHT)); // End bridge
        bounds.add(new Rectangle(568, 228, BUNKER_WIDTH - 48, BUNKER_HEIGHT/2f)); // Bunker

        bounds.add(fenceExit); // EXIT (MUST be last)
    }

    public void eyesMirageUpdate(){
        if(player.getPosition().dst(sign.getPosition()) < 45f){
            pupil.setSize(100,100);
            tiredShaderIntensity = player.getPosition().dst(sign.getPosition());
            pupil.setPosition(cursorPosition.x-40, cursorPosition.y-50);
            if(!gaveHintSign){
                stage.addActor(new RPGText("Deep breaths...", textSkin));
                gaveHintSign = true;
            }
        }else if(gaveHintSign){
            gaveHintSign = false;
            pupil.setPosition(-1000,-1000);
        }
        if(doJumpScares && player.tiredCount == 3 && player.isTired) {
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
    private boolean entry(Class<? extends State> stateClass, int x, int y, int width, int height){ // If rectangle is an entry point (16w x 16h), push requested state
        if (player.collidesUp(new Rectangle(x,y, width, height))){
            ambience.pause();
            try {
                State newState = stateClass
                    .getConstructor(GameStateManager.class, AssetManager.class, Player.class)
                    .newInstance(gsm, manager, player);
                gsm.push(newState);
            } catch (Exception e){
                e.printStackTrace();
            }
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

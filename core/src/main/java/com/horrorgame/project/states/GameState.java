package com.horrorgame.project.states;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.horrorgame.project.HorrorMain;
import com.horrorgame.project.Tiles.MapData;
import com.horrorgame.project.Tiles.MapDrawer;
import com.horrorgame.project.sprites.Player;

public class GameState extends State{
    private AssetManager manager;

    //Cursor Position as Vector2
    private Vector2 cursorPosition = new Vector2();
    private Vector3 cursorToWorldVec = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    private Vector2 cursorToPlayer = new Vector2();
    private static Player player;

    //  Collision
    private Array<Rectangle> bounds;


    private SpriteBatch batch;
    private static OrthographicCamera camera = new OrthographicCamera();

    // --LIGHTING--
    private World world = new World(new Vector2(0,0), false);
    private RayHandler rayHandler = new RayHandler(world);
    private PointLight ambientLight;

    //Flashlight
    private ConeLight flashlight;
    private Boolean flashOn = false;
    private Sound flashlight_click;



    public GameState(GameStateManager gsm, AssetManager manager){
        super(gsm);
        this.manager = manager;
        bounds = new Array<>();

        player = new Player(HorrorMain.WIDTH/2,HorrorMain.HEIGHT/2);
        Gdx.input.setInputProcessor(player);

        camera = new OrthographicCamera();
        camera.viewportWidth = HorrorMain.WIDTH/3.5f;
        camera.viewportHeight = HorrorMain.HEIGHT/3.5f;

        //Lighting
        rayHandler.setCombinedMatrix(camera.combined);
        rayHandler.useDiffuseLight(true); //Stops "draining the life from the colors"/desaturation of textures
        Color lightColor = new Color(0.45f, 0.35f, 0.65f, 0f); //ambient light color
        rayHandler.setAmbientLight(Color.BLACK);

        //Actual Flashlight (testing)
        flashlight_click = manager.get("sounds/objectInteractions/flashlight_click.wav", Sound.class);
        flashlight = new ConeLight(rayHandler, 250, Color.WHITE, 350, player.getPositionX(), player.getPositionY(), 0, 25);
        flashlight.setActive(false);

        //Player ambient light
        ambientLight = new PointLight(rayHandler, 10, Color.GRAY, 100, player.getPositionX(),player.getPositionY());


        bounds.add(new Rectangle(0,0, HorrorMain.WIDTH, 112)); // bottom
        bounds.add(new Rectangle(0,0, 96, HorrorMain.HEIGHT)); // left
        bounds.add(new Rectangle(80, (HorrorMain.HEIGHT - 64), 448, 64)); // top left
        bounds.add(new Rectangle(656, (HorrorMain.HEIGHT - 64) , 368, 64)); // top right
        bounds.add(new Rectangle(848, 80, 128, 80)); // lower right corner
        bounds.add(new Rectangle(992, 160, 192, 224));  // under bridge
        bounds.add(new Rectangle(992, 464, 192, 208)); // above bridge

        bounds.add(new Rectangle(544, HorrorMain.HEIGHT - 64, 80, 32)); // EXIT (Must be last)



    }
    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched() && player.checkInventory(0) == 1) { // Check if the screen was just touched
            clickFlashlight();
            flashlight_click.play();

        }
        if(Gdx.input.isKeyPressed(Input.Keys.F)){   // F to equip flashlight
            player.setItem(0, 1);
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
        ambientLight.setPosition(player.getPositionX()-10, player.getPositionY());


        for (Rectangle bound : new Array.ArrayIterator<>(bounds)){     // Check X collision
            if (player.collides(bound)){
                if (player.getVelX() < 0){
                    player.position.x = bound.x + bound.width;
                    player.setVelocity(0, player.getVelY());
                } else if (player.getVelX() > 0) {
                    player.setPosition(bound.x - 20, player.position.y);
                    player.setVelocity(0, player.getVelY());
                }
                else if (player.getVelY() > 0){    // Check Y collision
                    player.setPosition(player.position.x, bound.y - 32);
                    player.setVelocity(player.getVelX(), 0);
                }
                else if (player.getVelY() < 0){
                    player.setPosition(player.position.x, bound.y + bound.height);
                    player.setVelocity(player.getVelX(), 0);
                }

            }


        }

        player.update(dt);

        flashlightUpdate();

    }

    //METHODS FOR FLASHLIGHT
    //acts as a boolean switch
    public void clickFlashlight(){flashOn = !flashOn; flashlight.setActive(flashOn);}

    //updates flashlight
    private void flashlightUpdate(){
        if(flashOn) {
            if (cursorPosition.x > player.getPositionX()) {     // Player faces direction of cursor, with setDirection() determining if the player is facing left
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
        camera.position.set(player.getPositionX() - 10, player.getPositionY() + 0, 0);
        camera.update();



        // --- Draw player and other sprites ---
        sb.setProjectionMatrix(camera.combined);
        sb.begin();

        MapDrawer mapDrawer = new MapDrawer(MapData.MainMap);
        mapDrawer.render(sb);
        MapDrawer second = new MapDrawer(MapData.MainMapLayer2);
        second.render(sb);

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

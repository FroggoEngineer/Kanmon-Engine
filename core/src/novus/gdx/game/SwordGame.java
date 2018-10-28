package novus.gdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import novus.gdx.assets.Assets;
import novus.gdx.assets.PlaceObject;
import novus.gdx.graphics.Animation;
import novus.gdx.graphics.Camera;
import novus.gdx.world.Level;

public class SwordGame extends ApplicationAdapter {
	SpriteBatch batch;
	private BitmapFont font;
	private novus.gdx.characters.Character dude;
	private Texture backImage;
	private Level world;
	Texture img;
	
	private boolean showDebug = false;
	private final float WORLD_TO_RENDER = 96f;
	private final float RENDER_TO_WORLD = 1/96f;
	private final float STEP = 1/60f;
	
	//------------------------------------
	private World box2DWorld;
	private ContactListener cl;
	private Contact contact;
	
	private RayHandler rayhandler;
	private PointLight pl;
	
	private Texture[] blockTex;
	
	private Matrix4 cameraBox2D;
	private Box2DDebugRenderer debugRender;
	private OrthographicCamera worldCamera, lightCamera;
	private Camera cam;
	
	private Sound testSound;
	private Music testMusic;
	
	float camPosX = 1;
	float camPosY = 1;
	
	private ParticleEffect snow;
	
	private Animation testAni;
	private novus.gdx.characters.Character testChar;
	
	PlaceObject po1, po2;
	
	@Override
	public void create () {
		//Körs vid startup, skapa de saker som behövs vid startup enbart, resterande kan ske på andra ställen.
		Box2D.init();
		box2DWorld = new World(new Vector2(0, -10f), true);
		
		rayhandler = new RayHandler(box2DWorld);
		rayhandler.setShadows(true);
		rayhandler.setAmbientLight(0,0,0, 0.0f);
		rayhandler.setBlurNum(2);
//		rayhandler.useDiffuseLight(true);
		
		//create level
		world = new Level("../core/assets/MansionTest", box2DWorld, RENDER_TO_WORLD);
		
		po1 = new PlaceObject("../core/assets/interact/lantern.txt", rayhandler, 9, 12);
		po2 = new PlaceObject("../core/assets/interact/lantern.txt", rayhandler, 12, 12);
		
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		worldCamera = new OrthographicCamera(width, height);
		lightCamera = new OrthographicCamera(width*RENDER_TO_WORLD, height*RENDER_TO_WORLD);
		debugRender = new Box2DDebugRenderer();
		Assets.loadTextures();
		blockTex = new Texture[4];
        blockTex[0] =  new Texture(Gdx.files.internal("../core/assets/block1.png"));
        blockTex[1] =  new Texture(Gdx.files.internal("../core/assets/block2.png"));
        blockTex[2] =  new Texture(Gdx.files.internal("../core/assets/tree.png"));
        blockTex[3] =  new Texture(Gdx.files.internal("../core/assets/grass.png"));
		
        backImage = new Texture(Gdx.files.internal("../core/assets/background.png"));
        
        snow = new ParticleEffect();
        snow.load(Gdx.files.internal("../core/assets/snow"), Gdx.files.internal("../core/assets"));
        snow.start();
        
		batch = new SpriteBatch();
		
		//testAni = new Animation("../core/assets/gothloli.txt");
		testChar = new novus.gdx.characters.Character("../core/assets/characters/gothloli.txt", world.getSpawnX()*64, -1*world.getSpawnY()*64, box2DWorld, rayhandler, world);
		
		cam = new Camera(testChar, width, height, world);
	}

	@Override
	public void render () {
		
		//update function
		/*
		 * The update function should use a multithread approach
		 * Each part that can be split onto it's own should be on its own thread
		 * Examples:
		 * 	-Physics updates (Box2D)
		 * 	-Light updates (Box2D Lights), might have to be on physics thread
		 * 	-Input
		 * 	-Sound
		 * 	-Animation updates
		 * 	-A.I updates
		 * 	-World updates (Simulations etc) 
		 */
		
		update();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		
		batch.setProjectionMatrix(cam.getWorld().combined);
		batch.begin();
		float camPosX = cam.getWorld().position.x;
		float camPosY = cam.getWorld().position.y;
		
		batch.draw(backImage,camPosX - cam.getWorld().viewportWidth/2 ,camPosY - cam.getWorld().viewportHeight/2);
    	
    	//left
    	//batch.draw(charTex, loli.getBoxX()*WORLD_TO_RENDER, loli.getBoxY()*WORLD_TO_RENDER);
    	//dude.draw(batch, WORLD_TO_RENDER);
    	
		//Render the map from map object
    	for(int y = world.getMapHeight() - 1; y >= 0 ; y--) {
			for(int x = 0; x < world.getMapWidth(); x++) {
				if(world.getBackValue(y, x) != 0) {
					int h = Assets.BACK_OBJECT.get(world.getBackValue(y, x)-1).getHeight();
					int drawY = -(y*64) - (h-64);
		    		batch.draw(Assets.BACK_OBJECT.get(world.getBackValue(y, x)-1), x*64, drawY);
		    	}	
			}
		}
    	
    	batch.draw(po1.getTex(), po1.getX()*64, -po1.getY()*64);
    	batch.draw(po2.getTex(), po2.getX()*64, -po2.getY()*64);
    	
		
    	//Render the map from map object
    	for(int y = world.getMapHeight() - 1; y >= 0 ; y--) {
			for(int x = 0; x < world.getMapWidth(); x++) {
				if(world.getValue(y, x) != 0) {
		    		batch.draw(Assets.BLOCKS.get(world.getValue(y, x)), x*64, -y*64);
		    	}	
			}
		}
    	
    	//batch.draw(testAni.getTex(),camPosX*WORLD_TO_RENDER, camPosY*WORLD_TO_RENDER - worldCamera.viewportHeight/2  + 32);
    	batch.draw(testChar.getTex(), (testChar.getBoxX()-testChar.getWidth()/2)*WORLD_TO_RENDER, testChar.getBoxY()*WORLD_TO_RENDER);
    	snow.draw(batch, STEP);
    	
		batch.end();
		
		rayhandler.setCombinedMatrix(cam.getLight());
        rayhandler.updateAndRender();
        
        if(showDebug) {
        	cameraBox2D = cam.getWorld().combined.cpy();
        	cameraBox2D.scl(WORLD_TO_RENDER);
        	debugRender.render(box2DWorld, cameraBox2D);
//        	debugRender.render(box2DWorld, cam.getDebug());
        }
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		
		//Dispose all assets
		Assets.dispose();
		rayhandler.dispose();
		box2DWorld.dispose();
	}
	
	private void input() {
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			//Move left
//			testAni.changeAnimation(1);
			testChar.moveX(-2);
			camPosX -= 0.1f;
		} else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			//Move right
//			testAni.changeAnimation(0);
			testChar.moveX(2);
			camPosX += 0.1f;
		} else {
			//Stop moving in X
			testChar.moveX(0);
		}
		
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			//Jump
//			testAni.changeAnimation(3);
			testChar.moveY(1f);
			camPosY += 0.1f;
		} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//			testAni.changeAnimation(2);
			camPosY -= 0.1f;
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			showDebug = !showDebug;
			System.out.println("CamX: " + camPosX);
			System.out.println("CamY: " + camPosY);
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.P)) {
			testSound.play();
		}
	}

	private void update() {
		input();
		//update characters from input
		
		box2DWorld.step(STEP, 3, 3);
//		camPosX = 19.5f;
//		camPosY = -7.5f;
		camPosX = cam.getWorld().position.x;
		camPosY = cam.getWorld().position.y;
		float camX = camPosX*WORLD_TO_RENDER + 0;
		float camY = camPosY*WORLD_TO_RENDER + 0;

//		worldCamera.position.set(camX, camY, 0);
//		worldCamera.update();
		snow.setPosition(camPosX-cam.getWorld().viewportWidth/2, camPosY+cam.getWorld().viewportHeight/2);
		
		//lights
		//----------------------
		po1.update();
		po2.update();
		//----------------------
		
		camX = 0 + 0;
		camY = 0 + 0;
		lightCamera.position.set(camX, camY, 0);
		lightCamera.update();
		
		cam.update();
		
		//testAni.update();
		testChar.update();
	}
	
}

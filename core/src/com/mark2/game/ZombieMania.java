package com.mark2.game;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import sun.awt.SunHints;


public class ZombieMania extends ApplicationAdapter implements InputProcessor,ContactListener{
	
	TextureAtlas textureAtlas;
	SpriteBatch batch;
	OrthographicCamera camera;
    ExtendViewport viewport;
    
    
    static final float STEP_TIME = 1f / 60f;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    public static final short xZOMBIE       = 0x0001;
    public static final short xBULLET       = 0x0002;
    public static final short xWALL         = 0x0004;
    public static final short xPLAYER       = 0x0008; 
    
    public static final short PLAYER_MASK = xZOMBIE;
    public static final short ZOMBIE_MASK = xPLAYER | xBULLET;
    public static final short BULLET_MASK = xZOMBIE;
    
	final HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();

	int shotgunCounter =0;

	int itemSelect = 1;

	Player player;
	Zombie zombie;
	BulletManager magazine;

	World world;
	BodyEditorLoader loader;
	Box2DDebugRenderer debugRenderer;

	private float accumulator = 0;

	Pool<Bullet> bulletPool;
	
	ArrayList<Bullet> activeBullets = new ArrayList<Bullet>();
	
	ParticleEffect effect;
	@Override
	public void create () {
		
		Box2D.init();
		
		loader = new BodyEditorLoader(Gdx.files.internal("BodyColliders.json"));
		
		world = new World(new Vector2(0,0),true);
		
		world.setContactListener(this);
		
		effect = new ParticleEffect();
		
		effect.load(Gdx.files.internal("Particles/bleed.p"), Gdx.files.internal("Particles"));

		effect.flipY();

		debugRenderer = new Box2DDebugRenderer();
		
		textureAtlas = new TextureAtlas("Sprites.txt");

		batch = new SpriteBatch();
		
		camera = new OrthographicCamera();

		camera.setToOrtho(true);

        viewport = new ExtendViewport(800, 600, camera);

        Gdx.input.setInputProcessor(this);
        
        addSprites();
        
        player = new Player(sprites,world,loader);

        zombie = new Zombie(sprites,world,loader);
        
        
        bulletPool  = new Pool<Bullet>()
    	{

    		@Override
    		protected Bullet newObject() {
    			
    			return new Bullet(sprites, world, player, loader);
    		}
    		


    	};
        
        magazine = new BulletManager(sprites,player,world);
        
        
        
	}


	public void shotgunBaby(int bulletNumb)
	{
		//Grab my two end vectors
		Vector2 v1 = player.shotGunVectors.get(0);
		Vector2 v2 = player.shotGunVectors.get(1);

		//find the length of those vectors
		float unitV1 =  (float) Math.sqrt(Math.pow(v1.x,2)+ Math.pow(v1.y,2));
		float unitV2 =  (float) Math.sqrt(Math.pow(v2.x,2)+ Math.pow(v2.y,2));

		//Make two unit vectors
		Vector2 newV1 = new Vector2(v1.x /unitV1, v1.y / unitV1);
		Vector2 newV2 = new Vector2(v2.x / unitV2, v2.y / unitV2);

		//Calculate dot product

		float DP = (newV1.x * newV2.x) + (newV1.y * newV2.y);

		//Determine the angles
		float availableAngle = (float) Math.acos(DP);

		float rotationAngle = availableAngle / (bulletNumb - 1);

			if (bulletNumb > 2)
			{
				for (int i = 0; i < bulletNumb; i++) {
					//since i know my starting vector is, I can just rotate that

					float bulletRotation = (rotationAngle * i);

					float newX = v1.x * (float) Math.cos(bulletRotation) - (v1.y * (float) Math.sin(bulletRotation));
					float newY = v1.x * (float) Math.sin(bulletRotation) + (v1.y * (float) Math.cos(bulletRotation));

					Vector2 bulletDir = new Vector2(newX, newY);

					Bullet item = bulletPool.obtain();
					item.resetBulletVals(player.mam.mBody.getPosition(), player.mam.bulletVel, player.mam.mBody.getAngle());
					item.setDir(bulletDir);
					activeBullets.add(item);

				}

			}else {
				for (int i=0; i<2; i++) {
					Bullet item = bulletPool.obtain();

					item.alive = true;

					item.resetBulletVals(player.mam.mBody.getPosition(), player.mam.bulletVel, player.mam.mBody.getAngle());

					item.setDir(player.shotGunVectors.get(i));

					activeBullets.add(item);
				}
			}
		}



	public void spawnBullets()
	{

		Bullet item = bulletPool.obtain();

		item.alive = true;

		item.resetBulletVals(player.mam.mBody.getPosition(),player.mam.bulletVel, player.mam.mBody.getAngle());

		//System.out.println("("+player.x+","+player.y+")"+"/"+"("+item.x+","+item.y+")");

		activeBullets.add(item);

		
	}

	@Override
	public void render () {
 
	     
	     //cameraUpdate();
		 
		
		 Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
	     Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	     
	     checkInput(); 
	  
	     batch.begin();
	     
	     zombie.updateZombie(batch);
//	     
	     player.updatePlayer(batch);
	     
	     for(Bullet bullet : activeBullets)
	     {
	    	 
	    	 
	    	 bullet.updateBullet(batch);

	    	 
	     }
	     
	     effect.update(Gdx.graphics.getDeltaTime());
	     
	     effect.draw(batch);
	     
	     batch.end();
	     
	     debugRenderer.render(world,camera.combined);
	     
	     world.step(1/120f, 6, 2);

	}
	
	
	public void cameraUpdate()
	{
		Vector3 position = camera.position;
		position.x = player.sprite.getX();
		position.y = player.sprite.getY();
		
		camera.position.set(position);

		camera.update();

	}
	
	public void checkInput()
	{

		if(Gdx.input.isKeyPressed(Keys.W))
		{

			player.setDir(0,-5);
			player.mam.updateMam();
		}
		if(Gdx.input.isKeyPressed(Keys.S))
		{
			player.setDir(0,5);
			player.mam.updateMam();
		}
		if(Gdx.input.isKeyPressed(Keys.A))
		{

			player.setDir(-5,0);
			player.mam.updateMam();
			
		}
		if(Gdx.input.isKeyPressed(Keys.D))
		{

			player.setDir(5,0);
			player.mam.updateMam();
		}
		if (Gdx.input.isKeyPressed(Keys.C))
		{

			shotgunCounter++;


		}

	
		
	}
	public void drawSprite(String name, float x, float y)
	{
		Sprite sprite = sprites.get(name);
		
		sprite.setPosition(x, y);
		sprite.draw(batch);
		
	}
	
	public void drawSprite(Sprite sprite)
	{
		
		sprite.draw(batch);
	}
	
	
	public void addSprites()
	{
		Array<AtlasRegion> regions = textureAtlas.getRegions();
		
		for(AtlasRegion region : regions)
		{
			region.flip(false, true);

			Sprite sprite = textureAtlas.createSprite(region.name);

            sprites.put(region.name, sprite);
			
		}
		
	}
	
	@Override
	public void resize(int width, int height) {
		
	    viewport.update(width, height, true);

	    batch.setProjectionMatrix(camera.combined);
	}
	
	@Override
	public void dispose () {
		
		textureAtlas.dispose();
		
		sprites.clear();

		magazine.ll.clear();
		magazine.sprites.clear();
		debugRenderer.dispose();
		world.dispose();
		
	}

	
	//KeyBoard Input
	@Override
	public boolean keyDown(int keycode) {
		
		if(keycode == Keys.UP)
		{}
		
		if(keycode == Keys.DOWN)
		{}
		
		if(keycode == Keys.LEFT)
		{}
		
		if(keycode == Keys.RIGHT)
		{}
		if (keycode == Keys.SPACE)
		{

			player.isDashing = true;

		}
		if (keycode == Keys.SHIFT_LEFT)
		{
			itemSelect++;

			if (itemSelect > 2)
			{
				itemSelect =1;
			}

		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.W)
		{
			player.setDir(0,0);

			
		}
		if(keycode == Keys.S)
		{
			player.setDir(0,0);


		}
		if(keycode == Keys.A)
		{

			player.setDir(0,0);

			
		}
		if(keycode == Keys.D)
		{

			player.setDir(0,0);

			
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character){return false;}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){

		if (itemSelect == 1) {
			spawnBullets();
		}
		else
		{

			shotgunBaby(20);
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){return false;}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}

	@Override
	//TODO: It looks like the problem is the player's sprite position, gotta fix that...
	public boolean mouseMoved(int screenX, int screenY) {

		Vector2 mousePos = new Vector2(screenX,screenY);

		Vector2 relativeMousePos = new Vector2( screenX - player.sprite.getX(), screenY - player.sprite.getY());

		float mouseLength = (float) Math.sqrt(Math.pow(relativeMousePos.x,2) + Math.pow(relativeMousePos.y,2));

		Vector2 mouseVec = new Vector2(relativeMousePos.x/mouseLength,relativeMousePos.y/mouseLength);

		Vector2 playerVec = new Vector2(player.mam.initialVector.x/player.mam.length,player.mam.initialVector.y/player.mam.length);

		float DP = (playerVec.x * mouseVec.x) + (playerVec.y * mouseVec.y);

		float angle = (float) Math.acos(DP);

		player.sprite.setRotation(MathUtils.radiansToDegrees * angle);
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {return false;}

	
	
	//ContactListener Stuff
	@Override
	public void beginContact(Contact contact) {

		
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		
		switch(cDef)
		{
		case xZOMBIE | xBULLET:
			
			if(fixA.getBody().getUserData() instanceof Zombie)
			{
	
				
				
				Zombie zombie = (Zombie) fixA.getBody().getUserData();
				
				Vector2 pushDir = player.mam.bulletVel;
				
				zombie.x += pushDir.x;
				zombie.y += pushDir.y;
				
				Bullet bullet =  (Bullet) fixB.getBody().getUserData();
				
				effect.setPosition(bullet.x+(Constants.PPM/2), bullet.y+(Constants.PPM/2));
				
				effect.setDuration(1);
				
				for(int i =0; i<effect.getEmitters().size; i++)
				{
					
					effect.getEmitters().get(i).getWind().setHigh((-1 * pushDir.x)*200);
//					effect.getEmitters().get(i).getGravity().setHigh(value);
//					effect.getEmitters().get(i).getGravity().setLow(value);
				}
				
		        effect.start();
				
				bullet.alive = false;
			
		    	activeBullets.remove(bullet);
		    	bulletPool.free(bullet);
				
			
		    }

		    	    
		}
		
		
		

		
	}

	@Override
	public void endContact(Contact contact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}
}

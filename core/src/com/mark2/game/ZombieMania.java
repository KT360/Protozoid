package com.mark2.game;

import java.util.HashMap;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;


public class ZombieMania extends ApplicationAdapter implements InputProcessor{
	TextureAtlas textureAtlas;
	SpriteBatch batch;
	OrthographicCamera camera;
    ExtendViewport viewport;
    
    
    static final float STEP_TIME = 1f / 60f;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    
    
	final HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	
	Player player;
	Zombie zombie;
	BulletManager magazine;
	
	int bulletCounter = 0;
	
	World world;
	BodyEditorLoader loader;
	Box2DDebugRenderer debugRenderer;

	private float accumulator = 0;

	Pool<Bullet> bulletPool;
	
	Array<Bullet> activeBullets = new Array<Bullet>();
	 
	@Override
	public void create () {
		
		Box2D.init();
		
		loader = new BodyEditorLoader(Gdx.files.internal("BodyColliders.json"));
		
		world = new World(new Vector2(0,0),true);

		debugRenderer = new Box2DDebugRenderer();
		
		textureAtlas = new TextureAtlas("Sprites.txt");
		
		batch = new SpriteBatch();
		
		camera = new OrthographicCamera();

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
	
	public void spawnBullets()
	{
		
		Bullet item = bulletPool.obtain();
		
		item.body.setActive(true);
		
		activeBullets.add(item);
		
		
	}
	
	private void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;
            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
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

	     
	     System.out.println(player.body.getAngle());
		// magazine.tick(batch);
	    	 
	  
//
//	     stepWorld();
//
//	     
	     batch.end();
	     
	     //debugRenderer.render(world,camera.combined);
	     
	     world.step(1/60f, 6, 2);

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
		int linearForce =0;
		
		if(Gdx.input.isKeyPressed(Keys.UP))
		{
			//player.yDir = 5;
			
			player.body.setTransform(player.body.getWorldCenter(), MathUtils.degreesToRadians * 180);
			
			System.out.println(MathUtils.radiansToDegrees *player.body.getAngle());
			
			player.body.setLinearVelocity(0, 200);
			
//			magazine.moveBulletPos(player);
//			
//			player.move(player.xDir, player.yDir);
			
			
		}
		if(Gdx.input.isKeyPressed(Keys.DOWN))
		{
			
			player.body.setTransform(player.body.getWorldCenter(), MathUtils.degreesToRadians * 360);
			
			System.out.println(MathUtils.radiansToDegrees *player.body.getAngle());
			
			player.body.setLinearVelocity(0, -200);
			
			//player.yDir = -5;
//			magazine.moveBulletPos(player);
//			player.move(player.xDir, player.yDir);
			
			
		}
		if(Gdx.input.isKeyPressed(Keys.LEFT))
		{
			
			player.body.setTransform(player.body.getWorldCenter(), MathUtils.degreesToRadians * -90);
			
			System.out.println(MathUtils.radiansToDegrees *player.body.getAngle());
			
			player.body.setLinearVelocity(-200, 0);
			
			//player.xDir = -5;
//			magazine.moveBulletPos(player);
//			player.move(player.xDir, player.yDir);
			
			
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT))
		{
			
			player.body.setTransform(player.body.getWorldCenter(), MathUtils.degreesToRadians * 90);
			
			System.out.println(MathUtils.radiansToDegrees *player.body.getAngle());
			
			player.body.setLinearVelocity(200,0);
			
			//player.xDir = 5;
//			magazine.moveBulletPos(player);
//			player.move(player.xDir, player.yDir);
			
		
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
	
		if(keycode == Keys.C)
		{
			spawnBullets();
		}
		
		if(keycode == Keys.UP)
		{
			
			
		}
		
		if(keycode == Keys.DOWN)
		{
			
			
		}
		
		if(keycode == Keys.LEFT)
		{
			
			
		}
		
		if(keycode == Keys.RIGHT)
		{
			
			
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.UP)
		{
			
			float xVel = player.body.getLinearVelocity().x;
			
			player.body.setLinearVelocity(xVel, 0);
			
			
			
		}
		if(keycode == Keys.DOWN)
		{
			
			float xVel = player.body.getLinearVelocity().x;
			
			player.body.setLinearVelocity(xVel, 0);
			
			
		}
		if(keycode == Keys.LEFT)
		{
			
			float yVel = player.body.getLinearVelocity().y;
			
			player.body.setLinearVelocity(0, yVel);

			
		}
		if(keycode == Keys.RIGHT)
		{
			float yVel = player.body.getLinearVelocity().y;
			
			player.body.setLinearVelocity(0, yVel);
			
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		// TODO Auto-generated method stub
		return false;
	}
}

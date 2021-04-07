package com.mark2.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Zombie implements Entity{

	Random r = new Random();
	
	float x = r.nextInt(230);
	float y = r.nextInt(230);
	
	//Added directions
	float xDir = 0;
	float yDir = 0;
	
	public boolean alive = true;
	public int Health = 100;
	int bloodSpillTimer = 50;
	boolean alert = false;
	boolean spotPlayer = false;
	boolean isInjured = false;
	boolean idle = true;
	int animationDelay = 20;
	int collisionTimer = 50;
	boolean collided = false;

	Bullet bullet;

	Sprite sprite;
	BodyDef bodyDef;
	Body body;
	FixtureDef fixtureDef;
	Vector2[] originalShapeVertices;
	Vector2[] scaledShapeVertices;
	Vector2 dashDir;
	Animator animation;
	Animator explosion;
	Animator bulletHit;
	Texture frames;
	Texture explosionFrames;
	Texture splatterFrames;

	public Zombie(HashMap<String, Sprite> sprites, World world, BodyEditorLoader loader, Player player) {

		frames = new Texture(Gdx.files.internal("Animations/Enemy_Slime_Walk.png"));
		explosionFrames = new Texture(Gdx.files.internal("Animations/Explosion.png"));
		splatterFrames = new Texture(Gdx.files.internal("Animations/Bullet_Hit.png"));

		sprite = sprites.get("Enemy");
		sprite.setOriginBasedPosition(x,y);
		sprite.setOriginCenter();
		sprite.setScale(Constants.SPRITE_SCALING);
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		body = world.createBody(bodyDef);
		body.setUserData(this);
		
		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.1f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 1f;
		//fixtureDef.filter.maskBits = ZombieMania.ZOMBIE_MASK;
		
		
		loader.attachFixture(body, "Player",fixtureDef, Constants.BODY_SCALING,this);
		originalShapeVertices = loader.shapeVertices;
		scaledShapeVertices = new Vector2[originalShapeVertices.length];
		updatePolygonVertices();
		dashDir = new Vector2(0, 0);

		animation = new Animator(frames,3,4,1/8f,true);
		explosion = new Animator(explosionFrames,2,3,1/24f,true);
		bulletHit = new Animator(splatterFrames,3,3,1/24f,true);
	}

	public void updateZombie(SpriteBatch batch, Player player) {
		if (spotPlayer && player.alive && !collided)
		{
			chase(player);
			idle = false;
		}else if (collided){
			Vector2 impulse = new Vector2(dashDir.x*-50,dashDir.y*-50);
			body.applyLinearImpulse(impulse,body.getPosition(), true);
			collisionTimer--;
			if (collisionTimer <= 0)
			{
				collided = false;
				collisionTimer = 50;
			}
		}
		else {
			idle = true;
		}
		Vector2 position = body.getPosition();
		this.x = position.x;
		this.y = position.y;
		if (idle) {
			sprite.setOriginBasedPosition(x, y);
			updatePolygonVertices();
			sprite.draw(batch);
		}else{
			animation.renderAnimation(batch,this,18,18);
		}
		if (isInjured)
		{
			explosion.renderAnimation(batch,this,15,15);
			if (bullet != null) {
				float rotation = bullet.getAngle();
				bulletHit.renderAnimation(batch,bullet,40,40,0.8f,0.8f,rotation);
			}
			if (explosion.animation.isAnimationFinished(explosion.getStateTime()))
			{
				animationDelay--;
				if (animationDelay <=0) {
					animationDelay = 20;
					isInjured = false;
					if (bullet!= null) {
						bullet.alive = false;
					}
				}
			}
		}
	}
	public void updatePolygonVertices()
	{
		//Update Polygon
		for (int i =0; i<scaledShapeVertices.length; i++)
		{
			Vector2 originalVertex = originalShapeVertices[i];
			Vector2 updatedVertex = new Vector2(originalVertex.x + body.getPosition().x, originalVertex.y + body.getPosition().y);
			scaledShapeVertices[i] = updatedVertex;
		}
	}

	public float[] getPolygonVectors(Vector2 [] vertices)
	{
		float[] copiedVertices = new float[vertices.length * 2];
		for (int i=0, j=0; i<copiedVertices.length; i+=2,j++)
		{
			copiedVertices[i] = vertices[j].x;
			copiedVertices[i+1] = vertices[j].y;

		}

		return copiedVertices;

	}
	
//If zombie spots player, get the vector from zombie to player. If within range, lunge at the player
	//else, keep chasing using the vector
	public void chase(Player player)
	{
		Vector2 direction = new Vector2(player.x - x, player.y - y);
		float length = (float) Math.sqrt(Math.pow(direction.x,2)+Math.pow(direction.y,2));
		Vector2 chaseDir = new Vector2(direction.x/length,direction.y/length);

		checkRange(player);

		if (alert) {
			xDir = dashDir.x;//*10;
			yDir = dashDir.y;//*10;
		}else {

			dashDir = chaseDir;
			xDir = chaseDir.x;//*5;
			yDir = chaseDir.y;//*5;
		}
		move();

	}

	public void checkRange(Player player)
	{
		float attackRange = 55;
		float currentRange = (float) Math.sqrt(Math.pow((player.getPos().x - x),2)+Math.pow((player.getPos().y - y),2));

		if (currentRange < attackRange)
		{
			alert = true;

		}
		else
		{
			alert = false;
		}
	}

	public void reset()
	{
		x = r.nextInt(200);
		y = r.nextInt(200);
		body.setTransform(x,y,body.getAngle());
		alive = true;
	}
	public void move()
	{
		body.setLinearVelocity(xDir*50,yDir*50);
	}
	public void setDir(float x, float y)
	{
		this.xDir = x;
		this.yDir = y;

	}

	public float getDirX()
	{
		return xDir;
	}

	public float getDirY()
	{
		return yDir;

	}


	
	
	
	public void attack ()
	{
		
		xDir = 0;
		yDir = 0;
		
		
	}
	public void setX(int value) {
		x = value;

	}

	public void setY(int value) {

		y = value;

	}
	
	public void setPos(int x, int y)
	{
		this.x = x;
		this.y = y;
		
		
	}

	@Override
	public void checkCollision(Entity otherEntity) {
		if(otherEntity != null){
			switch (otherEntity.getType())
			{
				case Constants.BULLET_TYPE:
					if (Health > 0)
					{
						Health-=17;
						bullet = (Bullet) otherEntity;
						isInjured = true;
					}else{
						alive = false;
						Player.comboCounter++;
						Player.comboTimer  = Constants.COMBO_TIMER;
					}
					break;
				case Constants.PLAYER_TYPE:
					Player player = (Player) otherEntity;
					if (player.health > 0) {
						player.health -= 10;
					}else{
						player.alive = false;
					}
					System.out.println("!!!");
					break;
				case Constants.ZOMBIE_TYPE:
					collided = true;
					break;
			}
		}
	}
	@Override
	public int getType()
	{
		return Constants.ZOMBIE_TYPE;
	}

	@Override
	public Body getBody() {
		return this.body;
	}

	@Override
	public Vector2 getPosition() {
		return new Vector2(x,y);
	}

}


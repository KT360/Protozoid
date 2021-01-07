package com.mark2.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Zombie {

	Random r = new Random();
	
	float x = r.nextInt(400);
	float y = r.nextInt(200);
	
	//Added directions
	float xDir = 0;
	float yDir = 0;
	
	public boolean alive = true;
	
	public int Health = 100;

	boolean attacking = false;

	Sprite sprite;
	BodyDef bodyDef;
	Body body;
	FixtureDef fixtureDef;

	Vector2 dashDir = new Vector2(0,0);
	
	public Zombie(HashMap<String, Sprite> sprites, World world, BodyEditorLoader loader) {

		sprite = sprites.get("Zombie");
		sprite.setPosition(x,y);
		sprite.setScale(0.1f);
		sprite.setCenter(sprite.getWidth() /2f, sprite.getHeight() /2f);
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		body = world.createBody(bodyDef);
		body.setUserData(this);
		
		
		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.8f; 
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.7f;
		fixtureDef.filter.categoryBits = ZombieMania.xZOMBIE;
		//fixtureDef.filter.maskBits = ZombieMania.ZOMBIE_MASK;
		
		
		loader.attachFixture(body, "Player",fixtureDef, sprite.getScaleX() * Constants.PPM,this);
		System.out.println(x+","+y);
	}

	public void updateZombie(SpriteBatch batch, Player player) {
		if (alive) {

			chase(player);

			sprite.setPosition(x, y);
			
			Vector2 position = new Vector2(sprite.getX(),sprite.getY());
			
			body.setTransform(position, MathUtils.degreesToRadians *sprite.getRotation());
			
			sprite.draw(batch);
			
		}
//		else{
//			body.setActive(false);
//		}
	}

	
//TODO: NEW CHASE METHOD FOR THE ZOMBIE
//	public void chase(Player player) {
	public void chase(Player player)
	{

		Vector2 direction = new Vector2(player.x - x, player.y -y);

		float length = (float) Math.sqrt(Math.pow(direction.x,2)+Math.pow(direction.y,2));

		Vector2 chaseDir = new Vector2(direction.x/length,direction.y/length);

		checkRange(player);

		if (attacking) {
			xDir = dashDir.x*10;
			yDir = dashDir.y*10;
		}else {

			dashDir = chaseDir;
			xDir = chaseDir.x;
			yDir = chaseDir.y;

		}
		move();

	}

	public void checkRange(Player player)
	{
		float attackRange = 200;

		float currentRange = (float) Math.sqrt(Math.pow((player.getPos().x - x),2)+Math.pow((player.getPos().y - y),2));

		if (currentRange < attackRange)
		{
			attacking = true;
		}
		else
		{
			attacking = false;
		}
	}

	public void move()
	{
		x+=xDir;
		y+=yDir;
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
		this.y = x;
		
		
	}

}


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
	
	float x = 400;
	float y = 300;
	
	int width = 50;
	int height = 50;
	
	//Added directions
	int xDir;
	int yDir;
	
	boolean knockedBack = false;
	
	public Rectangle zombie;
	
	public boolean alive = true;
	
	int knockBackForce = 3;
	
	boolean shouldChase = true;
	
	int knockBackDir;
	
	public int Health = 100;

	float xCenter = x + 25;
	float yCenter = y + 25;
	
	Sprite sprite;
	BodyDef bodyDef;
	Body body;
	FixtureDef fixtureDef;
	
	
	public Zombie(HashMap<String, Sprite> sprites, World world, BodyEditorLoader loader) {

		sprite = sprites.get("Zombie");
		sprite.setPosition(x,y);
		sprite.setScale(sprite.getScaleX()/4, sprite.getScaleY()/4);
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
	
	}

	public void updateZombie(SpriteBatch batch) {
		if (alive) {
			
			sprite.setPosition(x, y);
			
			Vector2 position = new Vector2(sprite.getX(),sprite.getY());
			
			body.setTransform(position, MathUtils.degreesToRadians *sprite.getRotation());
			
			sprite.draw(batch);
			
		}
	}

	
//TODO: NEW CHASE METHOD FOR THE ZOMBIE
//	public void chase(Player player) {
//
//		if (player.x > zombie.x) {
//			
//			xDir =1;
//			
//			x += xDir;
//			
//			xCenter += xDir;
//
//		}
//		if (player.y > zombie.y) {
//			
//			yDir = 1;
//			
//			y += yDir;
//			
//			yCenter += yDir;
//		}
//		if (player.x < zombie.x ) {
//			
//			xDir = -1;
//			
//			x += xDir;
//			
//			xCenter += xDir;
//		}
//		if (player.y < zombie.y ) {
//			
//			yDir = -1;
//			
//			y += yDir;
//			
//			yCenter += yDir;
//		}
//
//		
//	}


	
	
	
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


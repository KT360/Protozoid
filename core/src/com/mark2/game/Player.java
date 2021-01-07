package com.mark2.game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;

public class Player {
	
	Sprite sprite;
	BodyDef bodyDef;
	Body body;
	FixtureDef fixtureDef;

	float x  = 200f;
	float y = 400f;
	float xDir = 0;
	float yDir = 0;
	int energy = 100;

	boolean isDashing = false;

	int shotgunCounter =0;

	BulletPosManager mam;

	Vector2 vector1;
	Vector2 vector2;

	ArrayList<Vector2> shotGunVectors = new ArrayList<>();

	public Player(HashMap<String, Sprite> sprites, World world, BodyEditorLoader loader)
	{
		
		sprite = sprites.get("Player");
		
		sprite.setPosition(x, y);
		
		sprite.setScale(0.1f);

		sprite.setOriginCenter();
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		body = world.createBody(bodyDef);
		body.setUserData(this);

		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.5f; 
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.4f;
		fixtureDef.filter.categoryBits = ZombieMania.xPLAYER;
		//fixtureDef.filter.maskBits = ZombieMania.PLAYER_MASK;
		
		
		loader.attachFixture(body, "Player",fixtureDef, sprite.getScaleX() * Constants.PPM,this);
		
		mam = new BulletPosManager(world);

		initVectors();
		
	}

	public void initVectors()
	{
		vector1 = new Vector2(1,1);
		vector2 = new Vector2(-1,1);

		shotGunVectors.add(vector1);
		shotGunVectors.add(vector2);

	}

	public Vector2 getPos()
	{
		return new Vector2(x,y);

	}

	public void setDir(float newDirX, float newDirY)
	{

		this.xDir = newDirX;
		this.yDir = newDirY;

	}


	
	public void updatePlayer(SpriteBatch batch)
	{

		if (isDashing)
		{
			Dash();

		}
		sprite.translate(xDir,yDir);
		Vector2 spritePos = new Vector2(sprite.getX(),sprite.getY());
		sprite.setPosition(spritePos.x, spritePos.y);
		this.x = spritePos.x;
		this.y = spritePos.y;
		sprite.setRotation(sprite.getRotation());
		body.setTransform(spritePos,MathUtils.degreesToRadians * sprite.getRotation());
		sprite.draw(batch);
		
	}

	public void Dash()
	{
		if(energy > 0)
		{
			Vector2 dashDir = new Vector2(mam.currentDir.x *20,mam.currentDir.y *20);
			System.out.println(dashDir);
			setDir(mam.currentDir.x *20, mam.currentDir.y *20);
			energy-=10;
			System.out.println(mam.currentDir);
			if (energy <= 0)
			{
				setDir(0,0);
				energy = 100;
				isDashing = false;

			}
		}


	}
	//A manager class that contains all the information that could be possibly need
	//to be retrieved from the player

	class BulletPosManager
	{

		BodyDef bDef;
		Body mBody;
		FixtureDef fDef;
		Vector2 bulletVel;
		Vector2 currentDir;
		float initialAngle;
		Vector2 initialVector;
		float offset = 50;
		float length;

		public BulletPosManager(World world)
		{
			initialAngle = -90;

			Vector2 bodyPos = body.getPosition();

			float vecX = x - bodyPos.x;
			float vecY = (y+offset) - bodyPos.y;

			initialVector = new Vector2(vecX,vecY);

			//Give the bullet an initial direction at the player's initialization
			length = (float) Math.sqrt(Math.pow(initialVector.x,2)+ Math.pow(initialVector.y,2));

			Vector2 initialBulletVector = new Vector2(initialVector.x/length,initialVector.y/length);

			bulletVel = initialBulletVector;

			currentDir = bulletVel;

			System.out.println(initialVector);

			float actualX = initialVector.x + bodyPos.x;
			float actualY = initialVector.y + bodyPos.y;

			bDef = new BodyDef();
			bDef.type = BodyDef.BodyType.KinematicBody;
			bDef.position.set(actualX,actualY);
			mBody = world.createBody(bDef);

			PolygonShape manager = new PolygonShape();
			manager.setAsBox(6f, 6f);
			
			fDef = new FixtureDef();
			fDef.shape = manager;
			fDef.isSensor = true;
			fDef.density = 0; 
			fDef.friction = 0;
			fDef.restitution = 0;
			
			Fixture fixture = mBody.createFixture(fDef);

			manager.dispose();

		}


		public void updateMam() {

			float bulletFactor = 5;

			Vector2 bodyPosition = body.getPosition();

			float iniX = initialVector.x * (float) Math.cos(body.getAngle()) - initialVector.y * (float) Math.sin(body.getAngle());
			float iniY = initialVector.x * (float) Math.sin(body.getAngle()) + initialVector.y * (float) Math.cos(body.getAngle());

			float newX = iniX + bodyPosition.x;
			float newY = iniY + bodyPosition.y;

			//Make it into a unit vector

			float scale = (float)Math.sqrt(Math.pow(iniX,2)+Math.pow(iniY,2));

			float velX = iniX / scale;
			float velY = iniY / scale;

			bulletVel = new Vector2(velX,velY);

			currentDir = new Vector2(velX,velY);

			Vector2 newVec = new Vector2(newX,newY);

			mBody.setTransform(newVec,body.getAngle());


			//Rotate shotgun vectors
			//TODO: MAKE THIS BETTER
			Vector2 v1 = vector1;
			Vector2 v2 = vector2;

			float vX = v1.x * (float) Math.cos(body.getAngle()) - v1.y * (float) Math.sin(body.getAngle());
			float vY = v1.x * (float) Math.sin(body.getAngle()) + v1.y * (float) Math.cos(body.getAngle());

			Vector2 newV1 = new Vector2(vX,vY);

			shotGunVectors.remove(0);
			shotGunVectors.add(0,newV1);

			float v2X = v2.x * (float) Math.cos(body.getAngle()) - v2.y * (float) Math.sin(body.getAngle());
			float v2Y = v2.x * (float) Math.sin(body.getAngle()) + v2.y * (float) Math.cos(body.getAngle());

			Vector2 newV2 = new Vector2(v2X,v2Y);

			shotGunVectors.remove(shotGunVectors.size() - 1);
			shotGunVectors.add(newV2);

		}
		
	}


}
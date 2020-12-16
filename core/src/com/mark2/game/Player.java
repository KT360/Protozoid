package com.mark2.game;

import java.awt.Color;
import java.awt.Graphics;
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
	float y = 200f;
	int xDir = 0;
	int yDir = 5;
	int dashDirX =0;
	int dashDirY=0;
	int energy = 100;
	
	BulletPosManager mam;

	public Player(HashMap<String, Sprite> sprites, World world, BodyEditorLoader loader)
	{
		
		sprite = sprites.get("Player");
		
		sprite.setPosition(x, y);
		
		sprite.setScale(sprite.getScaleX()/4, sprite.getScaleY()/4);

		//sprite.setOriginCenter();
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		body = world.createBody(bodyDef);
		body.setUserData(this);

		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.5f; 
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.4f;
		
		
		loader.attachFixture(body, "Player",fixtureDef, sprite.getScaleX() * Constants.PPM);
		
		mam = new BulletPosManager(world);
		
	}
	

	

	
	public void updatePlayer(SpriteBatch batch)
	{
		Vector2 position = body.getPosition();
		x= position.x;
		y=position.y;
		sprite.setPosition(position.x, position.y);
		//sprite.setOriginCenter();
		mam.updateManager();
		sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
		sprite.draw(batch);
		
	}
	
	
	class BulletPosManager
	{
		
		Vector2 spawnPos;
		float initialOffset;
		BodyDef bDef;
		Body mBody;
		Shape shape;
		FixtureDef fDef;
		float offsetLength;
		Vector2 bulletVel;
		
		public BulletPosManager(World world)
		{
			
			
			offsetLength = sprite.getWidth()/4;
			
			initialOffset =  y - offsetLength;
			
			spawnPos = new Vector2(x,initialOffset);

			bDef = new BodyDef();
			bDef.type = BodyDef.BodyType.KinematicBody;
			bDef.position.set(spawnPos);
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
		
		public void updateManager()
		{
			int playerAngle = (int) (MathUtils.radiansToDegrees * body.getAngle());
			
			Vector2 newPos;
			
			switch(playerAngle)
			{
			case 0:
				
				bulletVel = new Vector2(0,-100*Constants.PPM);
				newPos = new Vector2(x,y  - offsetLength);
				mBody.setTransform(newPos, body.getAngle());
				
				break;
			case 90:
				
				bulletVel = new Vector2(100*Constants.PPM,0);
				newPos = new Vector2(x + offsetLength,y);
				mBody.setTransform(newPos, body.getAngle());
				
				break;
			case -90:
				
				bulletVel = new Vector2(-100*Constants.PPM,0);
				newPos = new Vector2(x - offsetLength,y);
				mBody.setTransform(newPos, body.getAngle());
				
				break;
			case 180:
				
				bulletVel = new Vector2(0,100*Constants.PPM);
				newPos = new Vector2(x ,y + offsetLength);
				mBody.setTransform(newPos, body.getAngle());
				
				break;
			case 360:
				
				bulletVel = new Vector2(0,-100*Constants.PPM);
				newPos = new Vector2(x,y  - offsetLength);
				mBody.setTransform(newPos, body.getAngle());
				
				break;
				
			}	
			
		}
		
	}


}
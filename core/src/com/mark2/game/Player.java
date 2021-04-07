package com.mark2.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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

public class Player implements Entity {
	
	Sprite facing_right;
	Sprite facing_left;
	BodyDef bodyDef;
	Body body;
	FixtureDef fixtureDef;
	Random r;

	float x = 100f;
	float y = 100f;
	float xDir = 0;
	float yDir = 0;
	float playerScale = Constants.BODY_SCALING;
	float spriteScale = Constants.SPRITE_SCALING;
	int energy = 80;
	int health = 100;
	boolean alive = true;
	boolean idle = true;
	int stepEffectCounter = 30;
	int animationDelay = 10;
	public static int comboCounter= 0;
	public static int comboTimer = Constants.COMBO_TIMER;
	boolean isDashing = false;
	BulletPosManager mam;

	Vector2 vector1;
	Vector2 vector2;
	Vector2 dashDir;
	Vector2[] originalShapeVertices;
	Vector2[] scaledShapeVertices;

	ArrayList<Vector2> shotGunVectors = new ArrayList<>();
	Animator player_facing_right;
	Animator player_facing_left;
	Animator stepEffect;
	Texture stepEffectFrames;
	Texture frames;
	Texture left_frames;

	float stepX;
	float stepY;

	Animator dashAnimation;
	Texture dashFrames;
	float dashAnimationRotation;
	float dashAnimationX;
	float dashAnimationY;

	public Player(HashMap<String, Sprite> sprites, World world, BodyEditorLoader loader,ArrayList<Zombie> horde)
	{
		reset(horde);

		frames = new Texture(Gdx.files.internal("Animations/Walk cycle.png"));
		stepEffectFrames = new Texture(Gdx.files.internal("Animations/Dash.png"));
		left_frames = new Texture(Gdx.files.internal(("Animations/Walk_Cycle_Left.png")));
		dashFrames = new Texture(Gdx.files.internal("Animations/Dash_Route.png"));

		stepX = x;
		stepY = y;

		facing_right = sprites.get("Player");;
		facing_right.setScale(spriteScale);
		facing_right.setOriginCenter();
		facing_right.setOriginBasedPosition(x,y);

		facing_left = sprites.get("Player_Left");
		facing_left.setScale(spriteScale);
		facing_left.setOriginCenter();
		facing_left.setOriginBasedPosition(x,y);

		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x,y);
		body = world.createBody(bodyDef);
		body.setUserData(this);

		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.5f; 
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.4f;

		//fixtureDef.filter.maskBits = ZombieMania.PLAYER_MASK;
		
		loader.attachFixture(body, "Player",fixtureDef, playerScale,this);

		System.out.println("Player scale: "+playerScale * Constants.PPM);

		originalShapeVertices = loader.shapeVertices;
		scaledShapeVertices = new Vector2[originalShapeVertices.length];

		mam = new BulletPosManager(world);

		initVectors();

		player_facing_right = new Animator(frames,3,4,1/40f,true);
		player_facing_left = new Animator(left_frames,3,4,1/40f,true);
		stepEffect = new Animator(stepEffectFrames,2,3,1/8f,true);
		dashAnimation = new Animator(dashFrames,2,2,1/12f,true);
		dashDir = mam.currentDir;
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

	public void setPosition(Vector2 position)
	{
		body.setTransform(position,body.getAngle());
	}

	public void updatePlayer(SpriteBatch batch)
	{
		if (comboCounter > 0) {
			comboTimer--;
			if (comboTimer <= 0) {
				comboCounter = 0;
				comboTimer = Constants.COMBO_TIMER;
			}
		}
		if (isDashing)
		{
			Dash(batch);
		}
		else{
			dashDir = mam.currentDir;
		}
		Vector2 position = body.getPosition();
		body.setTransform(position,0);
		this.x = position.x;
		this.y = position.y;

		if (idle) {
			if (mam.mBody.getAngle() < 1) {
				facing_right.setOriginBasedPosition(x, y);
				facing_right.setRotation(MathUtils.radiansToDegrees * body.getAngle());
				facing_right.draw(batch);
			}else{
				facing_left.setOriginBasedPosition(x, y);
				facing_left.setRotation(MathUtils.radiansToDegrees * body.getAngle());
				facing_left.draw(batch);
			}

		}else {
			if (mam.mBody.getAngle() < 1) {
				player_facing_right.renderAnimation(batch, this, playerScale, playerScale);
			}else{
				player_facing_left.renderAnimation(batch,this,playerScale,playerScale);
			}
			stepEffectCounter--;
			if (stepEffectCounter <= 0)
			{
				stepEffect.renderAnimation(batch,stepX,stepY+playerScale/3,playerScale-1,playerScale-1);
				animationDelay--;
				if (animationDelay <= 0) {
					animationDelay = 10;
					stepEffectCounter = 30;
				}
			}else {
				stepX = x;
				stepY = y;
			}
		}
//		for (int i =0; i<scaledShapeVertices.length; i++)
//		{
//			Vector2 originalVertex = originalShapeVertices[i];
//			Vector2 updatedVertex = new Vector2(originalVertex.x + body.getPosition().x, originalVertex.y + body.getPosition().y);
//			scaledShapeVertices[i] = updatedVertex;
//		}

		
	}

	public float[] getPolygonVectors(Vector2 [] vertices) {
		float[] copiedVertices = new float[vertices.length * 2];

		for (int i = 0, j = 0; i < copiedVertices.length; i += 2, j++) {
			copiedVertices[i] = vertices[j].x;
			copiedVertices[i + 1] = vertices[j].y;
		}
		return copiedVertices;
	}

	public Line2D.Float[] getBodySegments()
	{
		Vector2[] vertices = scaledShapeVertices;
		Line2D.Float[] segments = new Line2D.Float[vertices.length];

		for (int i =0; i<segments.length; i++)
		{
			int nextPoint = i+1;
			if (nextPoint >= segments.length)
			{
				nextPoint = segments.length-1;
				segments[i] = new Line2D.Float(vertices[nextPoint].x,vertices[nextPoint].y,vertices[0].x,vertices[0].y);
			}
			else {
				segments[i] = new Line2D.Float(vertices[i].x, vertices[i].y, vertices[nextPoint].x, vertices[nextPoint].y);
			}
		}

		return segments;

	}

	public void Dash(SpriteBatch batch)
	{
		if(energy > 0)
		{
			body.setLinearVelocity(dashDir.x *300, dashDir.y *300);
			dashAnimation.renderAnimation(batch,dashAnimationX,dashAnimationY,40,70,1,1,dashAnimationRotation);
			energy-=2;
			if (energy <= 0)
			{
				body.setLinearVelocity(0,0);
				body.setAngularVelocity(0);
				energy = 80;
				isDashing = false;
			}
		}


	}

	public void reset(ArrayList<Zombie> horde)
	{
		r = new Random();
		if(horde != null) {
			for (Zombie z : horde) {
				Vector2 v = new Vector2(z.x - x, z.y - y);
				float distance = getLengthOf(v.x, v.y);
				if (distance < 85) {
					x = r.nextInt(200) + 10;
					y = r.nextInt(200) + 10;
					if (body != null) {
						body.setTransform(x, y, body.getAngle());
					}
				}
			}
		}
	}

	@Override
	public void checkCollision(Entity otherEntity) {
		if (otherEntity != null) {
			int type = otherEntity.getType();
			switch (type) {
				case Constants.STRUCTURE_TYPE:

					body.setLinearVelocity(0,0);
					body.setAngularVelocity(0);
					break;

			}
		}
	}
	@Override
	public int getType()
	{
		return Constants.PLAYER_TYPE;
	}

	@Override
	public Body getBody() {
		return this.body;
	}

	@Override
	public Vector2 getPosition() {
		return getPos();
	}

	public float getLengthOf(float vectorX, float vectorY)
	{
		float length = (float) Math.sqrt(Math.pow(vectorX,2)+Math.pow(vectorY,2));
		return length;
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
		Vector2 initialVector;
		Vector2 spriteRelativeVector;
		float offset = 15;
		float length;

		public BulletPosManager(World world)
		{

			Vector2 bodyPos = body.getPosition();

			float vecX = x - bodyPos.x;
			float vecY = (y+offset) - bodyPos.y;

			initialVector = new Vector2(vecX,vecY);

			//Give the bullet an initial direction at the player's initialization
			length = (float) Math.sqrt(Math.pow(initialVector.x,2)+ Math.pow(initialVector.y,2));
			Vector2 initialBulletVector = new Vector2(initialVector.x/length,initialVector.y/length);
			bulletVel = initialBulletVector;
			currentDir = initialBulletVector;
			System.out.println(initialVector);

			//Set the position
			float actualX = initialVector.x + bodyPos.x;
			float actualY = initialVector.y + bodyPos.y;

			bDef = new BodyDef();
			bDef.type = BodyDef.BodyType.KinematicBody;
			bDef.position.set(actualX,actualY);
			mBody = world.createBody(bDef);

			PolygonShape manager = new PolygonShape();
			manager.setAsBox(2f, 2f);


			fDef = new FixtureDef();
			fDef.shape = manager;
			fDef.isSensor = true;
			fDef.density = 0; 
			fDef.friction = 0;
			fDef.restitution = 0;
			
			Fixture fixture = mBody.createFixture(fDef);

			manager.dispose();

		}


		public void updateMam(float angle) {

			Vector2 bodyPosition = body.getPosition();

			float iniX = initialVector.x * (float) Math.cos(angle) - initialVector.y * (float) Math.sin(angle);
			float iniY = initialVector.x * (float) Math.sin(angle) + initialVector.y * (float) Math.cos(angle);

			//Vector for the bullet sprites
			spriteRelativeVector = new Vector2(iniX+x,iniY+y);

			//Calculate the unit vector that will be used for my direction and bullets
			float scale = (float)Math.sqrt(Math.pow(iniX,2)+Math.pow(iniY,2));
			float velX = iniX / scale;
			float velY = iniY / scale;
			bulletVel = new Vector2(velX,velY);
			currentDir = new Vector2(velX,velY);

			//calculate coords relative to player body and set the body's position
			float newX = iniX + bodyPosition.x;
			float newY = iniY + bodyPosition.y;
			Vector2 newVec = new Vector2(newX,newY);
			mBody.setTransform(newVec,angle);




			//Rotate shotgun vectors
			//TODO: MAKE THIS BETTER
			Vector2 v1 = vector1;
			Vector2 v2 = vector2;

			float vX = v1.x * (float) Math.cos(angle) - v1.y * (float) Math.sin(angle);
			float vY = v1.x * (float) Math.sin(angle) + v1.y * (float) Math.cos(angle);

			Vector2 newV1 = new Vector2(vX,vY);

			shotGunVectors.remove(0);
			shotGunVectors.add(0,newV1);

			float v2X = v2.x * (float) Math.cos(angle) - v2.y * (float) Math.sin(angle);
			float v2Y = v2.x * (float) Math.sin(angle) + v2.y * (float) Math.cos(angle);

			Vector2 newV2 = new Vector2(v2X,v2Y);

			shotGunVectors.remove(shotGunVectors.size() - 1);
			shotGunVectors.add(newV2);

		}
		
	}

}
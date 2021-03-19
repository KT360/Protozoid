package com.mark2.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.graphics.Camera;
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
	
	Sprite sprite;
	BodyDef bodyDef;
	Body body;
	FixtureDef fixtureDef;
	Random r;

	float x = 100f;
	float y = 100f;
	float xDir = 0;
	float yDir = 0;
	float playerScale = Constants.SPRITE_SCALING;
	int energy = 100;
	int health = 100;
	boolean alive = true;

	boolean isDashing = false;
	BulletPosManager mam;

	Vector2 vector1;
	Vector2 vector2;
	Vector2[] originalShapeVertices;
	Vector2[] scaledShapeVertices;

	ArrayList<Vector2> shotGunVectors = new ArrayList<>();

	public Player(HashMap<String, Sprite> sprites, World world, BodyEditorLoader loader,ArrayList<Zombie> horde)
	{
		r = new Random();
		if(horde != null) {
			for (Zombie z : horde) {
				Vector2 v = new Vector2(z.x - x, z.y - y);
				float distance = getLengthOf(v.x, v.y);
				if (distance < 85) {
					x = r.nextInt(190) + 10;
					y = r.nextInt(190) + 10;
				}
			}
		}
		sprite = sprites.get("Player");;
		sprite.setScale(playerScale);
		sprite.setOriginCenter();
		sprite.setOriginBasedPosition(x,y);

		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x,y);
		body = world.createBody(bodyDef);
		body.setUserData(this);

		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.5f; 
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.4f;
		fixtureDef.filter.categoryBits = ZombieMania.xPLAYER;
		//fixtureDef.filter.maskBits = ZombieMania.PLAYER_MASK;
		
		loader.attachFixture(body, "Player",fixtureDef, sprite.getScaleX()*Constants.PPM,this);

		originalShapeVertices = loader.shapeVertices;
		scaledShapeVertices = new Vector2[originalShapeVertices.length];

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

	public void setPosition(Vector2 position)
	{
		body.setTransform(position,body.getAngle());
	}

	public void updatePlayer(SpriteBatch batch)
	{

		if (isDashing)
		{
			Dash();
		}
		Vector2 position = body.getPosition();
		this.x = position.x;
		this.y = position.y;
		sprite.setOriginBasedPosition(x, y);
		sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
		for (int i =0; i<scaledShapeVertices.length; i++)
		{
			Vector2 originalVertex = originalShapeVertices[i];
			Vector2 updatedVertex = new Vector2(originalVertex.x + body.getPosition().x, originalVertex.y + body.getPosition().y);
			scaledShapeVertices[i] = updatedVertex;
		}
		sprite.draw(batch);
		
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

	public void Dash()
	{
		if(energy > 0)
		{
			Vector2 dashDir = new Vector2(mam.currentDir.x *20,mam.currentDir.y *20);
			body.applyLinearImpulse(dashDir.x *155, dashDir.y *155,x,y,true);
			energy-=2;
			if (energy <= 0)
			{
				body.setLinearVelocity(0,0);
				body.setAngularVelocity(0);
				energy = 100;
				isDashing = false;
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
		float offset = 7;
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


		public void updateMam() {

			Vector2 bodyPosition = body.getPosition();

			float iniX = initialVector.x * (float) Math.cos(body.getAngle()) - initialVector.y * (float) Math.sin(body.getAngle());
			float iniY = initialVector.x * (float) Math.sin(body.getAngle()) + initialVector.y * (float) Math.cos(body.getAngle());

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
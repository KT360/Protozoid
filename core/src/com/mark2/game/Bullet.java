package com.mark2.game;


import java.util.HashMap;


import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Bullet implements Poolable{
	
	float x;
	float y;
	float xDir = 0;
	float yDir = 0;
	boolean alive;
	
	BodyDef bodyDef;
	
	Body body;
	
	Sprite sprite;
	
	FixtureDef fixtureDef;
	
	float Angle;

	
	public Bullet(HashMap<String, Sprite> sprites, World world,Player player, BodyEditorLoader loader)
	{
		
		alive = false;
		
		x = player.mam.mBody.getPosition().x;
		y = player.mam.mBody.getPosition().y;


		xDir = player.mam.bulletVel.x;
		yDir = player.mam.bulletVel.y;
		
		Angle = player.mam.mBody.getAngle();
		
		sprite = sprites.get("Bullet");
		sprite.setPosition(x, y);
		sprite.setScale(player.sprite.getScaleX());



		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x,y);
		body = world.createBody(bodyDef);
		body.setUserData(this);
		body.setTransform(x, y, Angle);
		body.setSleepingAllowed(false);



		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.01f; 
		fixtureDef.friction = 0.01f;
		fixtureDef.restitution = 1.0f;
		fixtureDef.filter.categoryBits = ZombieMania.xBULLET;
		//fixtureDef.filter.maskBits = ZombieMania.BULLET_MASK;
		fixtureDef.isSensor = true;
	
		loader.attachFixture(body, "Bullet", fixtureDef, sprite.getScaleX() * Constants.PPM,this);
		
	}

	
	public void updateBullet(SpriteBatch batch)
	{
		sprite.setRotation( MathUtils.radiansToDegrees * Angle);
		
		sprite.setPosition(x,y);
			
		translateSprite(xDir,yDir);

		sprite.draw(batch);
		
	}

	public Vector2 getDir()
	{
		return new Vector2(xDir,yDir);
	}
	
	
	public void resetBulletVals(Vector2 spawnPos, Vector2 direction, float angle)
	{
		
		this.x = spawnPos.x;
		this.y = spawnPos.y;
		
		this.xDir =  direction.x;
		this.yDir =  direction.y;
		
		this.Angle = angle;
		
	}


	
	public void translateSprite(float xAmount, float yAmount)
	{
		
		sprite.translate(xAmount, yAmount);
		
		Vector2 spritePos = new Vector2(sprite.getX(),sprite.getY());
		
		this.x = spritePos.x;
		this.y = spritePos.y;
		
		body.setTransform(x, y, Angle);
	}
	
	public float getDirX()
	{
		return xDir;

	}

	public float getDirY()
	{
		return yDir;

	}

	public void setDir(int x, int y)
	{
		this.xDir = x;
		this.yDir = y;
	}

	public void setDir(Vector2 direction)
	{
		this.xDir =  direction.x;
		this.yDir =  direction.y;

	}

	public float getAngle()
	{
		return MathUtils.radiansToDegrees * Angle;

	}

	public void setAngle(float newAngle)
	{

		this.Angle = newAngle;

	}
	
	public float getX()
	{
		
		return x;
		
	}
	
	public float getY()
	{
		
		return y;
		
	}


	public Vector2 getSpritePos()
	{
		return new Vector2(sprite.getX(),sprite.getY());

	}
	
	
	
	public Sprite getSprite()
	{
		
		return this.sprite;
		
	}

	@Override
	public void reset() {

		
		alive = false;
	}


}

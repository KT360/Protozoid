package com.mark2.game;


import java.util.HashMap;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Bullet implements Poolable, Entity {
	
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
	float aliveTime = 120;

	
	public Bullet(HashMap<String, Sprite> sprites, World world,Player player,Vector2 direction, BodyEditorLoader loader)
	{
		alive = false;
		
		x = player.mam.mBody.getPosition().x;
		y = player.mam.mBody.getPosition().y;

		Angle = player.mam.mBody.getAngle();

		sprite = sprites.get("Bullet");
		sprite.setOriginBasedPosition(x,y);
		sprite.setOriginCenter();
		sprite.setScale(player.sprite.getScaleX());


		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x,y);
		body = world.createBody(bodyDef);
		body.setUserData(this);
		body.setTransform(x, y, Angle);

		fixtureDef = new FixtureDef();
		fixtureDef.density = 1.01f;
		fixtureDef.friction = 0.01f;
		fixtureDef.restitution = 0.5f;
		fixtureDef.filter.categoryBits = ZombieMania.xBULLET;
		//fixtureDef.filter.maskBits = ZombieMania.BULLET_MASK;

		loader.attachFixture(body, "Bullet", fixtureDef, sprite.getScaleX() * Constants.PPM,this);

	}

	
	public void updateBullet(SpriteBatch batch)
	{
		Vector2 position = body.getPosition();
		this.x = position.x;
		this.y = position.y;
		sprite.setOriginBasedPosition(x,y);
		sprite.draw(batch);
		aliveTime--;
		if (aliveTime < 0)
		{
			alive = false;
		}
	}

	public Vector2 getDir()
	{
		return new Vector2(xDir,yDir);
	}


	public void resetBulletVals(Vector2 spawnPos, Vector2 direction, float angle,float aliveTime)
	{
		body.setTransform(spawnPos,angle);
		setDir(direction.x,direction.y);
		this.Angle = angle;
		this.aliveTime = aliveTime;
		body.setActive(true);
	}
	
	public float getDirX()
	{
		return xDir;

	}

	public float getDirY()
	{
		return yDir;

	}

	public void setDir(float x, float y)
	{
		this.xDir = x;
		this.yDir = y;
		body.setLinearVelocity(xDir*200,yDir*200);
	}

	//For vectors
	public void setDir(Vector2 direction)
	{
		this.xDir =  direction.x;
		this.yDir =  direction.y;
		body.setLinearVelocity(xDir*200,yDir*200);
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
		//this.body.getWorld().destroyBody(body);
	}


	@Override
	public void checkCollision(Entity otherEntity) {
		if (otherEntity != null)
		{

		}
	}

	@Override
	public int getType() {
		return Constants.BULLET_TYPE;
	}

	@Override
	public Body getBody() {
		return this.body;
	}

}

package com.mark2.game;


import java.util.HashMap;


import com.badlogic.ashley.core.Entity;
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
	int xDir = 0;
	int yDir = 0;
	boolean alive;
	
	BodyDef bodyDef;
	
	Body body;
	
	Sprite sprite;
	
	FixtureDef fixtureDef;
	
	float Angle;
	
	
	
	public Bullet(HashMap<String, Sprite> sprites, World world,Player player, BodyEditorLoader loader)
	{
		
		alive = false;
		
		this.x = player.mam.mBody.getPosition().x;
		this.y = player.mam.mBody.getPosition().y;
		
		Angle = player.mam.mBody.getAngle();
		
		sprite = sprites.get("Bullet");
		sprite.setPosition(x, y);
		sprite.setScale(player.sprite.getScaleX() /2);
		
		float scale = (player.sprite.getScaleX()  / 2) * Constants.PPM;
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x,y);
		body = world.createBody(bodyDef);
		
		body.setTransform(x, y, Angle);
		body.applyLinearImpulse(player.mam.bulletVel, body.getWorldCenter(), true);
		body.setUserData(this);
		body.isBullet();
		body.setActive(false);
		
		fixtureDef = new FixtureDef();
		fixtureDef.density = 0.5f; 
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.0f;
	
		loader.attachFixture(body, "Bullet", fixtureDef, sprite.getScaleX() * Constants.PPM);
		
	}

	
	public void updateBullet(SpriteBatch batch)
	{
		
			Vector2 position = body.getPosition();
			x= position.x;
			y=position.y;
			sprite.setPosition(position.x, position.y);
			sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
			sprite.draw(batch);
			
	}
	
	
	
	public void setXDir(int value)
	{
		
		this.xDir = value;
		
	}
	
	public void setYDir(int value)
	{
		this.yDir = value;
		
		
	}
	
	public float getX()
	{
		
		return x;
		
	}
	
	public float getY()
	{
		
		return y;
		
	}
	
	public Sprite getSprite()
	{
		
		return this.sprite;
		
	}

	@Override
	public void reset() {
		
		x=0;
		y=0;
		
		alive = false;
	}


}

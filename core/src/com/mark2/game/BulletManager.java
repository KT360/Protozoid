package com.mark2.game;

import java.awt.Graphics2D;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;

public class BulletManager{

	ArrayList <Bullet> ll;
	
	HashMap<String,Sprite> sprites;
	
	Player player;
	
	BodyEditorLoader loader;
	
	Pool<Bullet> bulletPool;
	
	public BulletManager(HashMap<String,Sprite> sprites,Player player,World world)
	{
		
		this.sprites = sprites;
		this.player = player;
		
		loader = new BodyEditorLoader(Gdx.files.internal("BodyColliders.json"));
		
		
		ll = new ArrayList<Bullet>();
		
//		for(int i =0; i<100; i++)
//		{
//			
//			ll.add(new Bullet(sprites,world,player,loader));
//			
//		}
		
		
		
	
	}
	
	public void tick(SpriteBatch batch)
	{
		
		for(Bullet bullet : ll)
		{
				bullet.body.setActive(true);
				bullet.updateBullet(batch);
	
		}
		
	}


	
	public void addBullet(Player player,World world)
	{
		
		ll.add(new Bullet(sprites,world,player,loader));
		
	}
}


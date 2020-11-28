package Mechanics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

public class Zombie {

	Random r = new Random();
	int x = r.nextInt(800);
	int y = r.nextInt(200);
	int width = 50;
	int height= 50;
	public Rectangle zombie;
	public boolean alive = true;
	public int Health = 100;
	public Zombie()
	{
		
		zombie = new Rectangle(x,y, width, height);
		
	}
	public void drawZombie(Graphics g)
	{
		if (alive)
		{
		g.setColor(Color.gray);
		g.fillRect(zombie.x, zombie.y,zombie.width, zombie.height);
		}
		}
	
	public void chase(Player player)
	{
		
		if (player.x > zombie.x)
		{
			zombie.x+=1;
			
		}
		 if (player.y>zombie.y)
		{
			zombie.y+=1;
			
		}
		 if (player.x<zombie.x)
		{
			
			zombie.x-= 1;
		}
		 if (player.y<zombie.y)
		{
			
			zombie.y-=1;
			
		}
	}
	
	public void setX(int value)
	{
		x = value;
		
	}
	
	public void setY (int value)
	{
		
		
		y = value;
		
	}
	
}

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
	int height = 50;
	
	//Added directions
	int xDir;
	int yDir;
	
	public Rectangle zombie;
	
	public boolean alive = true;
	
	boolean shouldChase = true;
	
	public int Health = 100;

	int xCenter = x + 25;
	int yCenter = y + 25;
	
	
	public Zombie() {

		zombie = new Rectangle(x, y, width, height);

	}

	public void drawZombie(Graphics g) {
		if (alive) {
			g.setColor(Color.gray);
			g.fillRect(zombie.x, zombie.y, zombie.width, zombie.height);
		}
	}

	public void chase(Player player) {

		if (player.x > zombie.x) {
			
			xDir =1;
			
			zombie.x += xDir;
			
			xCenter += xDir;

		}
		if (player.y > zombie.y) {
			
			yDir = 1;
			
			zombie.y += yDir;
			
			yCenter += yDir;
		}
		if (player.x < zombie.x ) {
			
			xDir = -1;
			
			zombie.x += xDir;
			
			xCenter += xDir;
		}
		if (player.y < zombie.y ) {
			
			yDir = -1;
			
			zombie.y += yDir;
			
			yCenter += yDir;
		}

		
	}

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

}

package Mechanics;

import java.awt.Color;
import java.awt.Graphics;

public class Player {

	int x = 400;
	int y = 300;
	int xCenter = 400 + 20; // the center is at xCoordinates + half the side
	int yCenter = 300 + 20; // same
	int xDir = 0;
	int yDir = 0;
	int width = 40;
	int height = 40;
	int dashDirX =0;
	int dashDirY=0;
	int energy = 100;
	

	public void drawPlayer(Graphics g) {

		g.setColor(Color.black);
		g.fillRect(x, y, width, height);
	}

	public void move(int xDir, int yDir) {
		if (x + xDir > -10 && x + xDir < 750) {
			x += xDir;
			xCenter += xDir;
		}
		if (y + yDir > -10 && y + yDir < 530) {
			y += yDir;
			yCenter += yDir;
		}
	}
	
	public void dash()
	{
		
		
		for(int i =0; i<1; i++)
		{
			
			move(dashDirX,dashDirY);
			
		}
		
	}
	
	


}

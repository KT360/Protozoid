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


	public void drawPlayer(Graphics g) {

		g.setColor(Color.black);
		g.fillRect(x, y, width, height);
	}

	public void move() {
		if (x + xDir > -10 && x + xDir < 750) {
			x += xDir;
			xCenter += xDir;
		}
		if (y + yDir > -10 && y + yDir < 530) {
			y += yDir;
			yCenter += yDir;
		}
	}
	
	


}

package Mechanics;

import java.awt.Color;
import java.awt.Graphics;

public class Player {

	
	int x = 400;
	int y = 300;
	int xDir =0;
	int yDir =0;
	
	
	
	public void drawPlayer(Graphics g)
	{
		
		g.setColor(Color.black);
		g.fillRect(x, y, 40, 40);
		
	}
	
	public void move()
	{
		
		x+= xDir;
		y+= yDir;
		
	}
}
